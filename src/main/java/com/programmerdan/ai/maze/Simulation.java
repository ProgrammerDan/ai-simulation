package com.programmerdan.ai.maze;

import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 *	@author Daniel Boston <programmerdan@gmail.com>
 *	@version 1.0 November 22, 2010
 *
 *  Class: Artificial Intelligence
 *
 *	This is a simulation model that uses the map and "bug" class to generationally create a "better maze solver" at least for the training set.
 */
public class Simulation extends JPanel implements MouseListener
{
	// We need a SimulationMap for each maze we are training against.
	// We need a list of "bugs" that will be traversing our maze.
	// We will also have a list of brainless bugs that will use a random walk model to traverse the maze.
	// We can then give, generationally, some idea of how well our genetically driven bugs are doing
	// compared to their brainless cousins.

	// The interaction parameters are simple. The "bugs" have free motion but are prevented by the simulation from passing through walls
	// of the maze.
	// Each "bug" has two "eyes" that 'see' ahead (10 deg. left and 10 deg right). What they see is the nearest point on the nearest wall,
	// and a normalized distance to that point on the wall.

	// Each maze has a "path" associated with it that is used to determine fitness. The maze is "solved" if a bug
	// reaches the end of the path within some distance threshold (this represents maximum fitness). A bug that solves the maze stops
	// traversing the maze. The progress along the path is computed based on, first, which segment the bug is closest to, and then,
	// the inverse of the distance to that segment. So even if the bug is "closest" to the end of the path, but is far from that point,
	// the fitness may be low.

	//    Bug lists are independently sorted by fitness, and weighted by fitness.
	//    Bottom 50% of the list is discarded.
	//    Pick pairs to cross or random parent to clone from success list.

	private float[] redFit = new float[] {1.0f,0.0f,0.0f};
	private float[] adjustFit = new float[] {-1.0f,1.0f,0.0f};

	public int BUGS = 1000;

	public int MAZES = 8;

	private double sightDistance = 5.0;

	private int simLengthCap = 50000;

	private Bug[][] Actor;				// This holds the actual bugs
	private boolean[][] ActorActive;		// This determines if the bug is still searching the maze. If false, fitness is 1.0!
	private GeneralPath[][] ActorPath;
	private double[][] ActorPathLength;

	private double[] ActorEyes;

	private SimulationMap[] Maze;
	private File[] MazeStatistics;
	private double[] MazeXOffset;
	private double[] MazeYOffset;
	private double[] MazeScaleFactor;

	private double[] MazeMinFitness;
	private double[] MazeMaxFitness;
	private double[] MazeAvgFitness;
	private double[] MazeTotFitness;
	private long[] MazeSamFitness;

	private double[] MazeMinProgress;
	private double[] MazeMaxProgress;
	private double[] MazeAvgProgress;
	private double[] MazeTotProgress;
	private long[] MazeSamProgress;

	private long[] MazeMinStep;
	private long[] MazeMaxStep;
	private double[] MazeAvgStep;
	private long[] MazeTotStep;
	private long[] MazeSamStep;

	private double simSizeX;
	private double simSizeY;
	private double simBorder;

	private javax.swing.Timer tick;
	private int timeStep = 1; // in milliseconds

	private boolean active;
	private int numSteps;
	private int generation = 0;

	private double crossover;
	private int crosstime;
	private double mutation;
	private double preservation;

	private String filename;
	private BufferedReader fileIn;
	private BufferedWriter fileOut;

	public static void main(String[] args)
	{
		try
		{
			JFrame display = new JFrame("Daniel's GA/NN Maze Trainer Simulation");

			Keyboard kb = new Keyboard();

			System.out.println("Daniel's GA/NN Simulation");

			System.out.print("Sequence file rootname: ");

			String filenameroot = kb.getLine(); // get the root of the simulation runpath.

			System.out.print("Number of Actors (if new, or empty if not): ");

			String tlien = kb.getLine();

			Simulation draw;

			if (tlien.equalsIgnoreCase(""))
			{
				draw = new Simulation(filenameroot);
			}
			else
			{
				int tBugs = Integer.parseInt(tlien);

				System.out.print("Number of maps: ");

				int tMaps = Integer.parseInt(kb.getLine());

				String[] tMap = new String[tMaps];

				for (int i = 0; i < tMaps; i++)
				{
					System.out.print("Map to load? ");

					tMap[i] = kb.getLine();
				}

				draw = new Simulation(filenameroot, tBugs, tMap); // pass it along.

			}

			display.add(draw, BorderLayout.CENTER);

			display.setSize(draw.getSimSizeX() + 2 * draw.getSimBorder(), draw.getSimSizeY() + 2 * draw.getSimBorder());

			display.setVisible(true);

			draw.init();

			draw.start(); // run the simulation!
		} catch (IOException e) {
			System.out.println("File error" + e.toString() );
		}
	}

	public int getSimSizeX()
	{
		return (int) simSizeX;
	}

	public int getSimSizeY()
	{
		return (int) simSizeY;
	}

	public int getSimBorder()
	{
		return (int) simBorder;
	}

	/*
		This function finds the next saved generation for this filepath.
	*/
	private boolean findNextFile()
	{
		boolean genExists = false;

		int a = generation;
		File fn = new File(filename + String.valueOf(a) + ".gen");

		while (fn.exists())
		{
			generation = a;
			genExists = true;
			a++;
			fn = new File(filename + String.valueOf(a) + ".gen");
		}

		return genExists;
	}

	/*
		This function loads the last found saved generation.
	*/
	private void loadGen()
	{
		try
		{
			File fn = new File(filename + String.valueOf(generation) + ".gen");
			if (fn.exists())
			{
				fileIn = new BufferedReader( new FileReader( fn ) );

				// File Format:
				// Generation
				// # Maze
				// Maze file
				// [Min fitness]
				// [Avg fitness]
				// [Max fitness]
				// [# Samples]
				// Maze file
				// [Min fitness]
				// [Avg fitness]
				// [Max fitness]
				// [# Samples]
				// ...
				// # Bugs
				// [Bug 1 chromosome]
				// [Bug 2 chromosome]
				// ...
				// [Bug n chromosome]

				generation = Integer.parseInt(fileIn.readLine());
				MAZES = Integer.parseInt(fileIn.readLine());
				Maze = new SimulationMap[MAZES];
				MazeMinFitness = new double[MAZES];
				MazeMaxFitness = new double[MAZES];
				MazeAvgFitness = new double[MAZES];
				MazeTotFitness = new double[MAZES];
				MazeSamFitness = new long[MAZES]; // number of samples.

				System.out.println("Mazes: " + MAZES );

				for ( int i = 0; i < MAZES; i ++)
				{
					Maze[i] = new SimulationMap("./mazes/" + fileIn.readLine() + ".maze");

					/*MazeMinFitness[i] = Double.parseDouble(fileIn.readLine());
					MazeAvgFitness[i] = Double.parseDouble(fileIn.readLine());
					MazeMinFitness[i] = Double.parseDouble(fileIn.readLine());
					MazeSamFitness[i] = Double.parseDouble(fileIn.readLine());

					MazeTotFitness[i] = MazeAvgFitness[i] * MazeSamFitness[i];*/
				}

				fitMazes();

				BUGS = Integer.parseInt(fileIn.readLine());

				Actor = new Bug[BUGS][MAZES];
				ActorActive = new boolean[BUGS][MAZES];
				ActorPath = new GeneralPath[BUGS][MAZES];
				ActorPathLength = new double[BUGS][MAZES];

				System.out.println("Gen: " + generation + " Bugs: " + BUGS );

				for ( int i = 0; i < BUGS; i ++ )
				{
					Chromosome chromo = newGenomeEncoded(fileIn.readLine());

					for (int j = 0; j < MAZES; j ++ )
					{
						ActorActive[i][j] = true;

						Actor[i][j] = newBug(chromo, j);
						ActorPath[i][j] = new GeneralPath();
						ActorPath[i][j].moveTo((float) (simBorder + MazeXOffset[j] + MazeScaleFactor[j] * Maze[j].getStartX()),
										 	   (float) (simBorder + MazeYOffset[j] + MazeScaleFactor[j] * Maze[j].getStartY()));
						ActorPathLength[i][j] = 0.0;
					}
				}

				numSteps = 0;
			}
		} catch (IOException e) { System.out.println("File error" + e.toString() ); }
	}

	/*
		Save the current generation.
	*/
	private void saveGen()
	{
		try
		{
			File fn = new File(filename + String.valueOf(generation) + ".gen");
			if (!fn.exists())
			{
				fileOut = new BufferedWriter( new FileWriter( fn ) );

				// File Format:
				// Generation
				// # Maze
				// Maze file
				// [Min fitness]
				// [Avg fitness]
				// [Max fitness]
				// [# Samples]
				// Maze file
				// [Min fitness]
				// [Avg fitness]
				// [Max fitness]
				// [# Samples]
				// ...
				// # Bugs
				// [Bug 1 chromosome]
				// [Bug 2 chromosome]
				// ...
				// [Bug n chromosome]

				fileOut.write(String.valueOf(generation) + "\r\n");
				fileOut.write(String.valueOf(MAZES) + "\r\n");

				for ( int i = 0; i < MAZES; i++)
				{
					fileOut.write(Maze[i].title() + "\r\n");
					/*fileOut.write(MazeMinFitness[i]);
					fileOut.write("\r\n");
					fileOut.write(MazeAvgFitness[i]);
					fileOut.write("\r\n");
					fileOut.write(MazeMinFitness[i]);
					fileOut.write("\r\n");
					fileOut.write(MazeSamFitness[i]);
					fileOut.write("\r\n");*/

					if (MazeStatistics != null && MazeStatistics[i] != null)
					{
						PrintWriter dos = new PrintWriter( new BufferedWriter( new FileWriter( MazeStatistics[i], true ) ) );

						dos.print(String.valueOf(MazeMinFitness[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeMaxFitness[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeAvgFitness[i]));
						dos.print(",");

						dos.print(String.valueOf(MazeMinProgress[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeMaxProgress[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeAvgProgress[i]));
						dos.print(",");

						dos.print(String.valueOf(MazeMinStep[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeMaxStep[i]));
						dos.print(",");
						dos.print(String.valueOf(MazeAvgStep[i]));
						dos.print(",");

						dos.println(String.valueOf(BUGS));

						dos.flush();
						dos.close();
					}
				}

				fileOut.write(String.valueOf(BUGS) + "\r\n");

				for ( int i = 0; i < BUGS; i ++ )
				{
					fileOut.write(Actor[i][0].getDNA().toEncodedString() + "\r\n");
				}

				fileOut.flush();
				fileOut.close();
			}
		} catch (IOException e) { System.out.println("File error" + e.toString() ); }
	}

	/*
		Start the simulation based on the simulation filepath
	*/
	public Simulation(String loadname)
	{
		filename = loadname;

		MazeXOffset = new double[MAZES];
		MazeYOffset = new double[MAZES];
		MazeScaleFactor = new double[MAZES];
		MazeStatistics  = new File[MAZES];

		initSim();
	}

	public Simulation(String loadname, int nBugs, String[] mazes)
	{
		BUGS = nBugs;
		MAZES = mazes.length;

		Maze = new SimulationMap[MAZES];
		MazeXOffset = new double[MAZES];
		MazeYOffset = new double[MAZES];
		MazeScaleFactor = new double[MAZES];
		MazeStatistics  = new File[MAZES];

		for (int i = 0; i < MAZES; i++)
		{
			Maze[i] = new SimulationMap("./mazes/" + mazes[i] + ".maze");
		}

		filename = loadname;

		initSim();
	}
	/*
		Initialize the simulation.
	*/
	private void initSim()
	{
		crossover = .75;
		mutation = .025;

		crosstime = 4;

		preservation = .25; // in each generation, preserve the top 25%

		sightDistance = 8.0;
		simLengthCap = 5000;

		Actor = new Bug[BUGS][MAZES];
		ActorActive = new boolean[BUGS][MAZES];
		ActorPath = new GeneralPath[BUGS][MAZES];
		ActorPathLength = new double[BUGS][MAZES];

		ActorEyes = new double[] {15.0, 14.0,13.0,12.0,11.0,10.0,-10.0,-11.0,-12.0,-13.0,-14.0, -15.0}; // bug looks ahead.

		MazeMinFitness = new double[MAZES];
		MazeMaxFitness = new double[MAZES];
		MazeAvgFitness = new double[MAZES];
		MazeTotFitness = new double[MAZES];
		MazeSamFitness = new long[MAZES]; // number of samples.

		MazeMinProgress = new double[MAZES];
		MazeMaxProgress = new double[MAZES];
		MazeAvgProgress = new double[MAZES];
		MazeTotProgress = new double[MAZES];
		MazeSamProgress = new long[MAZES]; // number of samples.

		MazeMinStep = new long[MAZES];
		MazeMaxStep = new long[MAZES];
		MazeAvgStep = new double[MAZES];
		MazeTotStep = new long[MAZES];
		MazeSamStep = new long[MAZES]; // number of samples.

		for (int k = 0; k < MAZES; k++)
		{
			MazeMinFitness[k] = Double.MAX_VALUE;
			MazeMaxFitness[k] = Double.MIN_VALUE;
			MazeAvgFitness[k] = 0.0;
			MazeTotFitness[k] = 0.0;
			MazeSamFitness[k] = 0l;

			MazeMinProgress[k] = Double.MAX_VALUE;
			MazeMaxProgress[k] = Double.MIN_VALUE;
			MazeAvgProgress[k] = 0.0;
			MazeTotProgress[k] = 0.0;
			MazeSamProgress[k] = 0l;

			MazeMinStep[k] = Long.MAX_VALUE;
			MazeMaxStep[k] = Long.MIN_VALUE;
			MazeAvgStep[k] = 0.0;
			MazeTotStep[k] = 0l;
			MazeSamStep[k] = 0l;
		}

		this.setBackground(Color.WHITE);

		simSizeX = 700;
		simSizeY = 700;
		simBorder = 100;

		numSteps = 0;
		generation = 0;

		tick = new javax.swing.Timer(timeStep, new ActionListener(){	// this bugger, when turned on, keeps the simulation hopping.
					public void actionPerformed(ActionEvent e) {
						step();
					}
				});

		active = false;

		this.addMouseListener(this);
	}

	/*
		Load the next simulation generation if found, or setup a new simulation runpath.
	*/
	public void init()
	{
		if (findNextFile()) // load when found
		{
			loadGen();
		}
		else // or create a new runpath.
		{
			fitMazes();

			for ( int i = 0; i < BUGS; i ++ )
			{
				Chromosome genes = newGenome();

				for (int j = 0; j < MAZES; j++)
				{
					ActorActive[i][j] = true;

					Actor[i][j] = newBug(genes, j);
					ActorPath[i][j] = new GeneralPath();
					ActorPath[i][j].moveTo((float) (simBorder + MazeXOffset[j] + MazeScaleFactor[j] * Maze[j].getStartX()),
										   (float) (simBorder + MazeYOffset[j] + MazeScaleFactor[j] * Maze[j].getStartY()));
					ActorPathLength[i][j] = 0.0;
				}
			}

			numSteps = 0;
			generation = 0;

			saveGen(); // save origin pathing.
		}

		prepareMazeStatistics();
	}

	private void prepareMazeStatistics()
	{
		try
		{
			for (int i = 0; i < MAZES; i++)
			{
				MazeStatistics[i] = new File(filename + "_" + Maze[i].title() + ".csv");

				if (!MazeStatistics[i].exists())
				{
					PrintWriter dos = new PrintWriter( new BufferedWriter( new FileWriter( MazeStatistics[i] ) ) );

					dos.println(",Fitness,,,Path Progress,,,Steps,," + Maze[i].title() );
					dos.println("Min,Max,Avg,Min,Max,Avg,Min,Max,Avg,Actors");

					dos.flush();
					dos.close();
				}
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void start() // start this simulation running.
	{
		active = true;
		tick.start();
	}

	private double fitnessThreshold = 0.1; // if any results are below this line ... discard.
	private double randomPreserve = 0.1; // very small!

	public void gen()
	{
		TreeMap<Double, Vector<Bug>> fitness = new TreeMap<Double, Vector<Bug>>();

		Vector<Bug> curlist = null;

		double[] fitnessAvg = computeFitness();

		int c = 0; int d = 0;

		for ( int a = 0; a < BUGS; a++ )
		{
			if (!fitness.containsKey( fitnessAvg[a] ) )
			{
				curlist = new Vector<Bug>();
				fitness.put( fitnessAvg[a], curlist);
			}
			else
			{
				curlist = fitness.get( fitnessAvg[a] );
			}

			curlist.add( Actor[a][0] );

			//if (fitnessAvg[a] >= fitnessThreshold)
			//{
			//	c++; // only count it if it's good.
			//}

			if (fitnessAvg[a] > 0.0)
			{
				c++;
				d++; // count if it's not abject failure.
			}
		}

		// now sorted and limited.

		if (c == 0 && d == 0) // BAD, no good results. No even marginally acceptable results. Ugh.
		{
			System.out.println(generation + " -- No good results. Resetting! ");

			// construct a new random set.
			Actor = new Bug[BUGS][MAZES];
			ActorPath = new GeneralPath[BUGS][MAZES];

			for (int a = 0; a < BUGS; a++)
			{
				Actor[a][0] = newBug(newGenome(), 0);

				ActorPath[a][0] = new GeneralPath();
				ActorPath[a][0].moveTo((float) (simBorder + MazeXOffset[0] + MazeScaleFactor[0] * Maze[0].getStartX()),
									   (float) (simBorder + MazeYOffset[0] + MazeScaleFactor[0] * Maze[0].getStartY()));
				ActorPathLength[a][0] = 0.0;

				for (int b = 1; b < MAZES; b++)
				{
					Actor[a][b] = newBug(Actor[a][0].getDNA(), b);
					ActorPath[a][b] = new GeneralPath();
					ActorPath[a][b].moveTo((float) (simBorder + MazeXOffset[b] + MazeScaleFactor[b] * Maze[b].getStartX()),
										   (float) (simBorder + MazeYOffset[b] + MazeScaleFactor[b] * Maze[b].getStartY()));
					ActorPathLength[a][b] = 0.0;
				}
			}

		}
		/*else if (c == 0 && d != 0) // some results better than zero.
		{
			System.out.println(generation + " -- No really good results, discarding majority. ");

			int parentpool = Math.min( (int) ((double) BUGS * preservation), d);

			Actor = new Bug[BUGS][MAZES];
			ActorPath = new GeneralPath[BUGS][MAZES];

			Double key = fitness.lastKey();

			curlist = fitness.get(key);

			for ( int a = 0; a < parentpool; )
			{
				for ( Bug b : curlist )
				{
					Actor[a][0] = cloneBug(b, 0);
					Actor[a + parentpool][0] = mutateBug(b, 0); // shocking! Force a mutation? I am playing God!

					a++;

					if (a >= parentpool)
						break;
				}

				key = fitness.lowerKey(key);

				if (key != null)
					curlist = fitness.get(key);
			}


			for (int e = 0; e < BUGS; e++)
			{
				if (Actor[e][0] == null) // if not preserved, fill with random.
					Actor[e][0] = newBug( newGenome(), 0);

				ActorPath[e][0] = new GeneralPath();
				ActorPath[e][0].moveTo((float) (simBorder + MazeXOffset[0] + MazeScaleFactor[0] * Maze[0].getStartX()),
									   (float) (simBorder + MazeYOffset[0] + MazeScaleFactor[0] * Maze[0].getStartY()));

				ActorPathLength[e][0] = 0.0;

				for (int f = 1; f < MAZES; f++)
				{
					Actor[e][f] = newBug(Actor[e][0].getDNA(), f);
					ActorPath[e][f] = new GeneralPath();
					ActorPath[e][f].moveTo((float) (simBorder + MazeXOffset[f] + MazeScaleFactor[f] * Maze[f].getStartX()),
										   (float) (simBorder + MazeYOffset[f] + MazeScaleFactor[f] * Maze[f].getStartY()));
					ActorPathLength[e][f] = 0.0;
				}
			}

		}*/
		else
		{
			int parentpool = Math.min((int) ((double) BUGS * preservation), c);

			Bug[] Spawn = new Bug[parentpool];

			Double key = fitness.lastKey();

			curlist = fitness.get(key);

			for ( int a = 0; a < Spawn.length; )
			{
				for ( Bug b : curlist )
				{
					Spawn[a] = b;

					a++;

					if (a >= Spawn.length)
						break;
				}

				key = fitness.lowerKey(key);

				if (key != null)
					curlist = fitness.get(key);
			}

			Bug[][] oldActors = Actor;

			Actor = new Bug[BUGS][MAZES];
			ActorPath = new GeneralPath[BUGS][MAZES];

			double cCross = 0.0;

			for ( int a = 0; a < BUGS; a ++ )
			{
				cCross = (double) Math.random(); // Perform crossover?

				if (cCross < randomPreserve) // pick a random dude to save without respect to fitness
				{
					int x1 = (int) (Math.random() * BUGS);

					Actor[a][0] = cloneBug( oldActors[x1][0], 0 );
				}
				else if (cCross < crossover) // yes!
				{
					// perform crossover. Pick two at random.
					int x1 = (int) (Math.random() * parentpool);
					int x2 = (int) (Math.random() * parentpool);

					//System.out.println("Crossing: " + x1 + ", " + x2);

					Actor[a][0] = crossBug( Spawn[x1], Spawn[x2], 0 );
				}
				else // clone
				{
					int x1 = (int) (Math.random() * parentpool);

					//System.out.println("Cloning: " + x1);

					Actor[a][0] = cloneBug( Spawn[x1], 0 );

				}

				ActorPath[a][0] = new GeneralPath();
				ActorPath[a][0].moveTo((float) (simBorder + MazeXOffset[0] + MazeScaleFactor[0] * Maze[0].getStartX()),
									   (float) (simBorder + MazeYOffset[0] + MazeScaleFactor[0] * Maze[0].getStartY()));
				ActorPathLength[a][0] = 0.0;

				for (int b = 1; b < MAZES; b++)
				{
					Actor[a][b] = newBug(Actor[a][0].getDNA(), b);
					ActorPath[a][b] = new GeneralPath();
					ActorPath[a][b].moveTo((float) (simBorder + MazeXOffset[b] + MazeScaleFactor[b] * Maze[b].getStartX()),
										   (float) (simBorder + MazeYOffset[b] + MazeScaleFactor[b] * Maze[b].getStartY()));
					ActorPathLength[a][b] = 0.0;
				}
			}

			oldActors = null;
		}
	}

	// Step the simulation until step n, then evaluate for the new generation.
	public void step()
	{
		if (numSteps > simLengthCap && active)
		{
			// now time to crossit all.
			active = false;

			gen();

			for ( int i = 0; i < BUGS; i ++ )
			{
				for ( int j = 0; j < MAZES; j++)
				{
					ActorActive[i][j] = true;
				}
			}

			numSteps = 0;
			generation++;

			saveGen(); // save the new generation.

			resetStats();

			active = true;
			return;
		}
		if (active) // run if active.
		{

			Bug temp;
			// run through and Step all the bugs.
			for ( int i = 0; i < BUGS; i ++ )
			{
				for ( int j = 0; j < MAZES; j ++)
				{
					// need to update inputs ... eeak.

					// input definition:
					// input 0 - "left" eye distance
					// input 1 - "left" eye sees a wall? -1 no 1 yes
					// input 2 - "right" eye distance
					// input 3 - "right" eye sees a wall? -1 no 1 yes

					if (ActorActive[i][j])
					{
						temp = Actor[i][j];

						double x = temp.getX();
						double y = temp.getY();

						setInput(temp, i, j);

						temp.step(Maze[j]);

						x = temp.getX() - x;
						y = temp.getY() - y;

						ActorPathLength[i][j] += Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0));

						updateFitness(temp, i, j);

						if (solvedMaze(temp, i, j) )
						{
							ActorActive[i][j] = false; // solved the maze!

							// Immediately record these intermediate informations.

							if (numSteps < MazeMinStep[j]) MazeMinStep[j] = numSteps;
							if (numSteps > MazeMaxStep[j]) MazeMaxStep[j] = numSteps;
							MazeTotStep[j] += numSteps;
							MazeSamStep[j] ++;
							MazeAvgStep[j] = (double) MazeTotStep[j] / (double) MazeSamStep[j];

							if (Actor[i][j].getFitness() < MazeMinProgress[j]) MazeMinProgress[j] = Actor[i][j].getFitness();
							if (Actor[i][j].getFitness() > MazeMaxProgress[j]) MazeMaxProgress[j] = Actor[i][j].getFitness();
							MazeTotProgress[j] += Actor[i][j].getFitness();
							MazeSamProgress[j] ++;
							MazeAvgProgress[j] = (double) MazeTotProgress[j] / (double) MazeSamProgress[j];
						}

						ActorPath[i][j].lineTo((float) (simBorder + MazeXOffset[j] + MazeScaleFactor[j] * Actor[i][j].getX()),
											   (float) (simBorder + MazeYOffset[j] + MazeScaleFactor[j] * Actor[i][j].getY()));
					}
				}
			}

			this.repaint(); // draw it.

			numSteps ++; // increase the step
		}
	}

	// Recalculate the fitness of the passed bug.
	private void updateFitness(Bug cur, int idx, int midx)
	{
		// Compute fitness against the map midx.

		cur.setFitness( Maze[midx].relativePathProgress( cur.getX(), cur.getY() ) );
	}

	private double[] computeFitness()
	{
		// computes fitness as an average across all maps.
		// True fitness also incorporates how 'long' a path the particular actor travels, against the maze path (as a baseline)

		// here we also contribute samples to our overall fitness measurements.
		double ActorFitness[] = new double[BUGS];
		double curFitness = 0.0;

		for (int i = 0; i < BUGS; i++)
		{
			for (int j = 0; j < MAZES; j++)
			{
				double x = ActorPathLength[i][j];
				if (ActorPathLength[i][j] > 0.0)
					// use a log-normal function here to give us a nice, skewed answer. Basically, bugs that barely travel are bad bugs, and bugs that travel too far are also bad bugs.
					//curFitness = Maze[j].pathLength() / ActorPathLength[i][j];
					curFitness = Maze[j].pathTravel(x);
				else
					curFitness = 0.0;

				// Fitness is also adjusted by BEST progress and current progress through the maze, without regard to simulation steps.
				curFitness *= ((Actor[i][j].getFitness() + Actor[i][j].getMaxFitness() * 3.0) / 4.0);


				// Finally, we adjust based on expected progress given the number of steps through the simulation.
				if (ActorActive[i][j]) // but only if the actor is alive at compute time.
					curFitness *= computeStepFitness(Actor[i][j].getMaxFitness() ); // we base on BEST progress the actor has achieved.
				// else the factor would be 1.0, so no need to multiply.

				ActorFitness[i] += curFitness;
				if (MazeMinFitness[j] > curFitness) MazeMinFitness[j] = curFitness;
				if (MazeMaxFitness[j] < curFitness) MazeMaxFitness[j] = curFitness;

				MazeTotFitness[j] += curFitness;
				MazeSamFitness[j] ++;
				MazeAvgFitness[j] = MazeTotFitness[j] / (double) MazeSamFitness[j];

				if (ActorActive[i][j]) // otherwise we've already recorded.
				{
					if (simLengthCap < MazeMinStep[j]) MazeMinStep[j] = simLengthCap;
					if (simLengthCap > MazeMaxStep[j]) MazeMaxStep[j] = simLengthCap;
					MazeTotStep[j] += simLengthCap;
					MazeSamStep[j] ++;
					MazeAvgStep[j] = (double) MazeTotStep[j] / (double) MazeSamStep[j];

					if (Actor[i][j].getFitness() < MazeMinProgress[j]) MazeMinProgress[j] = Actor[i][j].getFitness();
					if (Actor[i][j].getFitness() > MazeMaxProgress[j]) MazeMaxProgress[j] = Actor[i][j].getFitness();
					MazeTotProgress[j] += Actor[i][j].getFitness();
					MazeSamProgress[j] ++;
					MazeAvgProgress[j] = (double) MazeTotProgress[j] / (double) MazeSamProgress[j];
				}
			}

			ActorFitness[i] /= (double) MAZES;
		}

		return ActorFitness;
	}

	/**
	 * This component of fitness measures the idea that an actor should continue to make progress as the simulation progresses. If the actor's progress does not AT LEAST keep in step with
	 * the simulation's progress, we introduce a decay to their fitness. Otherwise, this decay factor is 1.0 (no decay). E.g. after 75% of max steps in the simulation, a successful actor
	 * should have either solved the maze or be at least 75% of the way there (in terms of progress). This is to devalue "slow" actors that make it 75% of the way but no further; so yes they
	 * are most successful in a particular population, but they aren't necessarily the best genomes.
	 */
	private double computeStepFitness(double progressFitness)
	{
		double b = 10.0 / (double)simLengthCap;

		double x = progressFitness * (double) simLengthCap;

		// now use a heavily modified sigmoid type function
		double sig = 2.0 / (1.0 + Math.exp( (-(x - (double) numSteps)) * b) );

		if (sig > 1.0)
			return 1.0; // we are making good progress (at least in step with the simulation's progress.
		else
			return sig; // we are not making good enough progress.
	}

	private void resetStats()
	{
		MazeMinFitness = new double[MAZES];
		MazeMaxFitness = new double[MAZES];
		MazeTotFitness = new double[MAZES];
		MazeSamFitness = new long[MAZES];
		MazeAvgFitness = new double[MAZES];

		MazeMinStep = new long[MAZES];
		MazeMaxStep = new long[MAZES];
		MazeTotStep = new long[MAZES];
		MazeAvgStep = new double[MAZES];
		MazeSamStep = new long[MAZES];

		MazeMinProgress = new double[MAZES];
		MazeMaxProgress = new double[MAZES];
		MazeTotProgress = new double[MAZES];
		MazeSamProgress = new long[MAZES];
		MazeAvgProgress = new double[MAZES];

		for (int k = 0; k < MAZES; k++)
		{
			MazeMinFitness[k] = Double.MAX_VALUE;
			MazeMaxFitness[k] = Double.MIN_VALUE;
			MazeAvgFitness[k] = 0.0;
			MazeTotFitness[k] = 0.0;
			MazeSamFitness[k] = 0l;

			MazeMinProgress[k] = Double.MAX_VALUE;
			MazeMaxProgress[k] = Double.MIN_VALUE;
			MazeAvgProgress[k] = 0.0;
			MazeTotProgress[k] = 0.0;
			MazeSamProgress[k] = 0l;

			MazeMinStep[k] = Long.MAX_VALUE;
			MazeMaxStep[k] = Long.MIN_VALUE;
			MazeAvgStep[k] = 0.0;
			MazeTotStep[k] = 0l;
			MazeSamStep[k] = 0l;
		}
	}

	// Tests to see if this bug has solved his maze
	private boolean solvedMaze(Bug cur, int idx, int midx)
	{
		// see if we have "solved" this map.

		if (cur.getFitness() >= 1.0)
			return true;
		else
			return false;
	}

	private void setInput(Bug cur, int idx, int midx)
	{
		SimpleLine[] rays = new SimpleLine[ActorEyes.length];

		for (int i = 0; i < rays.length; i++)
		{
			rays[i] = new SimpleLine();
			rays[i].setX1( cur.getX() );
			rays[i].setY1( cur.getY() );
			rays[i].setX2( cur.getX() + ( sightDistance * Math.cos( Math.toRadians( cur.getDir() + ActorEyes[i] ) ) ) );
			rays[i].setY2( cur.getY() + ( sightDistance * Math.sin( Math.toRadians( cur.getDir() + ActorEyes[i] ) ) ) );
		}

		double[] nearPoints = Maze[midx].nearestWalls(rays);

		double[] normPoints = SimulationMap.normalizeSight(nearPoints, ActorEyes, 1.0, 0.0);

		for (int i = 0; i < rays.length; i++)
		{
			//cur.setInput(i, normPoints[i]);
			cur.setInput(i * 2, normPoints[i]);
			cur.setInput(i * 2 + 1, ((nearPoints[i] == Double.MAX_VALUE) ? -1.0 : 1.0) );
		}

		//cur.setInput(4, ActorPathLength[idx][midx] / (double) simLengthCap);

		//cur.setInput(3, cur.getTrueVel());

		//cur.setInput(4, cur.getDir());
	}

	public static Color lightRed = new Color(255,230,230);
	public static Color lightBlue = new Color(230,230,255);

	// Draw the buggers.
    public void paint(Graphics g)
    {
		Graphics2D g2 = (Graphics2D) g;

		double lm = 4; // vector multiplier.

        super.paint(g2); // clears the screen.

        g2.setFont(g2.getFont().deriveFont(10f)); // sets the font to size 8.

        int m = g2.getFontMetrics().getAscent(); // sets the multipler for font spacing.

        if (active || numSteps > 0) // if active or already run but paused.
        {
	        g2.setColor(Color.BLACK);
	        g2.drawString(Integer.toString(numSteps), 10,m);
	        g2.drawString(Integer.toString(generation), 10,2*m);

	        Bug temp;
	        double[] tVec;

	        int[] a = new int[MAZES];
	        int fo = 0;

			for (int b = 0; b < MAZES; b++)
			{
				g2.setColor(Color.BLACK);

				SimpleLine[] walls = Maze[b].walls();

				for (int c = 0; c < walls.length; c++)
				{
					g2.drawLine( (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * walls[c].x1()),
					             (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * walls[c].y1()),
					             (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * walls[c].x2()),
					             (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * walls[c].y2()) );
				}

				g2.setColor(lightBlue);

				SimpleLine[] paths = Maze[b].paths();

				for (int c = 0; c < paths.length; c++)
				{
					g2.drawLine( (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[c].x1()),
					             (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[c].y1()),
					             (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[c].x2()),
					             (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[c].y2()) );
				}

				// draw first and last "zone"

				g2.setColor(Color.GREEN);

				g2.drawOval( (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[0].x1() - Maze[b].getNearThreshold() * MazeScaleFactor[b]),
							 (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[0].y1() - Maze[b].getNearThreshold() * MazeScaleFactor[b]),
							 (int) (2.0 * Maze[b].getNearThreshold() * MazeScaleFactor[b]), (int) (2.0 * Maze[b].getNearThreshold() * MazeScaleFactor[b]) );

				g2.setColor(Color.BLUE);

				g2.drawOval( (int) MazeXOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[paths.length-1].x2() - Maze[b].getNearThreshold() * MazeScaleFactor[b]),
							 (int) MazeYOffset[b] + getSimBorder() + (int) (MazeScaleFactor[b] * paths[paths.length-1].y2() - Maze[b].getNearThreshold() * MazeScaleFactor[b]),
							 (int) (2.0 * Maze[b].getNearThreshold() * MazeScaleFactor[b]), (int) (2.0 * Maze[b].getNearThreshold() * MazeScaleFactor[b]) );
			}

			double avgFitness[] = new double[MAZES];
			double maxFitness[] = new double[MAZES];
			double avgPath[] = new double[MAZES];
			double maxPath[] = new double[MAZES];
			double avgProgress[] = new double[MAZES];
			double maxProgress[] = new double[MAZES];
			double curFitness = 0.0;

			for (int k = 0; k < MAZES; k++)
			{
				maxFitness[k] = Double.MIN_VALUE;
				maxPath[k] = Double.MIN_VALUE;
			}

			for (int f = 0; f < BUGS; f++)
			{
				for (int e = 0; e < MAZES; e++)
				{
					double x = ActorPathLength[f][e];
					if (x > maxPath[e]) maxPath[e] = x;
					avgPath[e] += x;
					if (ActorPathLength[f][e] > 0.0)
						curFitness = Maze[e].pathTravel(x);
					else
						curFitness = 0.0;

					curFitness *= ((Actor[f][e].getFitness() + Actor[f][e].getMaxFitness() * 3.0) / 4.0);

					if (ActorActive[f][e])
						curFitness *= computeStepFitness(Actor[f][e].getMaxFitness() );

					avgProgress[e] += Actor[f][e].getFitness();
					maxProgress[e] += Actor[f][e].getMaxFitness();

					if (curFitness > maxFitness[e]) maxFitness[e] = curFitness;

					avgFitness[e] += curFitness;

					if (ActorActive[f][e])
					{
						temp = Actor[f][e];
						tVec = temp.getVector();

						//g2.setColor(lightRed);
						//g2.draw(ActorPath[f][e]);

						/*if (curFitness > .3 && curFitness < .5)
							g2.setColor(Color.ORANGE);
						else if (curFitness >= .5)
							g2.setColor(Color.GREEN);
						else
							g2.setColor(Color.RED);*/

						Color acColor = new Color(redFit[0] + (float) curFitness * adjustFit[0],redFit[1] + (float) curFitness * adjustFit[1],redFit[2] + (float) curFitness * adjustFit[2]);

						g2.setColor(acColor);

						g2.fillOval( (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getX() - .5)),
									 (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getY() - .5)), (int) (MazeScaleFactor[e] * 1), (int) (MazeScaleFactor[e] * 1));

						g2.setColor(acColor.brighter());
						g2.drawLine( (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getX()),
									 (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getY()),
									 (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getX() + lm * tVec[0])),
									 (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getY() + lm * tVec[1])) );

						tVec = temp.getTrueVector();

						g2.setColor(acColor.darker());
						g2.drawLine( (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getX()),
									 (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getY()),
									 (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getX() + lm * tVec[0])),
									 (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * (temp.getY() + lm * tVec[1])) );

						if (!active)
							g2.drawString(Integer.toString(f), (int) MazeXOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getX()),
															   (int) MazeYOffset[e] + getSimBorder() + (int) (MazeScaleFactor[e] * temp.getY()) );

						a[e] ++;
					}
				}
			}

			// draw map
			g2.setColor(Color.BLACK);

			g2.drawString("Active:", getSimBorder(),m);

			g2.drawString("Maze:", getSimBorder() - 50,2*m);

			g2.drawString("Actors:", getSimBorder() - 50,3*m);
			g2.drawString("Path:", getSimBorder() - 50,4*m);
			g2.drawString("AvgPath:",  getSimBorder() - 50,5*m);
			g2.drawString("MaxPath:",  getSimBorder() - 50,6*m);
			g2.drawString("AvgProg:", getSimBorder() - 50,7*m);
			g2.drawString("MaxProg:", getSimBorder() - 50,8*m);
			g2.drawString("AvgFit:", getSimBorder() - 50,9*m);
			g2.drawString("MaxFit:", getSimBorder() - 50,10*m);

			int aa = 0;

			for (int e = 0 ; e < MAZES; e++)
			{
				avgFitness[e] /= (double) BUGS;
				avgPath[e] /= (double) BUGS;
				avgProgress[e] /= (double) BUGS;
				maxProgress[e] /= (double) BUGS;
				g2.drawString("M" + Integer.toString(e), getSimBorder() + e * 100,2*m);

				g2.drawString(Integer.toString(a[e]), getSimBorder() + e * 100,3*m);
				g2.drawString(Double.toString(Maze[e].pathLength()), getSimBorder() + e * 100,4*m);
				g2.drawString(Double.toString(Math.round(avgPath[e] * 100000.0) / 100000.0),  getSimBorder() + e * 100,5*m);
				g2.drawString(Double.toString(Math.round(maxPath[e] * 100000.0) / 100000.0),  getSimBorder() + e * 100,6*m);
				g2.drawString(Double.toString(Math.round(avgProgress[e] * 100000.0) / 1000.0), getSimBorder() + e * 100,7*m);
				g2.drawString(Double.toString(Math.round(maxProgress[e] * 100000.0) / 1000.0), getSimBorder() + e * 100,8*m);
				g2.drawString(Double.toString(Math.round(avgFitness[e] * 100000.0) / 1000.0), getSimBorder() + e * 100,9*m);
				g2.drawString(Double.toString(Math.round(maxFitness[e] * 100000.0) / 1000.0), getSimBorder() + e * 100,10*m);

				aa+=a[e];
			}

			if ( (aa == 0) && numSteps < simLengthCap)
				numSteps = simLengthCap;
		}
	}

	private void fitMazes()
	{
		// do some maths to fit the mazes nicely.

		int squaresX = (int) Math.ceil(Math.sqrt((double)MAZES));
		int squaresY = (int) Math.ceil((double) MAZES / (double) squaresX);

		// better to waste space than anything.

		double squareSizeX = (simSizeX - 2.0 * simBorder) / (double) squaresX;
		double squareSizeY = (simSizeY - 2.0 * simBorder) / (double) squaresY;

		int i = 0;

		for (int j = 0; j < squaresY; j ++)
		{
			for (int k = 0; k < squaresX; k ++)
			{
				double[] extent = Maze[i].mapExtent();

				MazeScaleFactor[i] = Math.min( (squareSizeX * .9) / (extent[1] - extent[0]), (squareSizeY * .9) / (extent[3] - extent[2]) );
				MazeXOffset[i]     = squareSizeX * (double) k + squareSizeX * .55 - MazeScaleFactor[i] * extent[0] - simBorder;
				MazeYOffset[i]     = squareSizeY * (double) j + squareSizeY * .55 - MazeScaleFactor[i] * extent[2] - simBorder;

				i++;
				if (i == MAZES)
				{
					k = squaresX; j = squaresY;
				}
			}
		}
	}

	// Helper function to measure the distance between two points.
	public static double getDistance(double _x1, double _y1, double _x2, double _y2)
	{
		double d = Math.sqrt(Math.pow( (_x2 - _x1), 2.0) + Math.pow( (_y2 - _y1), 2.0) );

		return d;
	}

	// Touched is defined as being within two units.
	public static boolean hasTouched(double _x1, double _y1, double _x2, double _y2)
	{
		return ( ( getDistance(_x1, _y1, _x2, _y2) <= 2.0 ) ? true : false );
	}

	public int bug_inputs = 24;
	public int bug_hiddenlayers = 10;//5;
	public int bug_hiddenlayersize = 30;//7;
	public int[] bug_inputclasses = new int[]{0,1,0,1 ,0,1,0,1,0,1,0,1,0,1 ,0,1,0,1,0,1,0,1,0,1};
	public int[] bug_output_classes = new int[]{1,0};
	public int bug_chromo_size = Bug.estimateChromosome(bug_inputs, bug_hiddenlayersize, bug_hiddenlayers);

	public Chromosome newGenome()
	{
		return Chromosome.randomChromosome(bug_chromo_size, 28);//295,20);//4, 20);
	}

	public Chromosome newGenome(String chromo)
	{
		Chromosome nC = new Chromosome();
		nC.setGenes(chromo);

		return nC;
	}

	public Chromosome newGenomeEncoded(String chromo)
	{
		Chromosome nC = new Chromosome();
		nC.setGenesEncoded(chromo);

		return nC;
	}

	// Create a new random bug.
	public Bug newBug(int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x
		//public Bug(double _x, double _y, double _dir, double _vel, double _rotate, double _speed, int[] _inputs, int _hiddenwidth, int _hiddensize, int[] _outputs, Chromosome _DNA)
		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, newGenome() );
	}

	// Create a new bug from a string representation of this chromosome.
	public Bug newBug(String chromo, int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x
		Chromosome nC = new Chromosome();
		nC.setGenes(chromo);
		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, nC );
	}

	// Create a new bug from a chromosome.
	public Bug newBug(Chromosome chromo, int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x
		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, chromo );
	}

	// Cross two bugs.
	public Bug crossBug(Bug x1, Bug x2, int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x

		Chromosome newDNA = x1.getDNA().crossover(x2.getDNA(), crosstime); // crossover.

		double mutate = Math.random();

		if (mutate < mutation)
			newDNA = newDNA.mutate(); // possibly apply mutation.

		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, newDNA );
	}

	// Clone a bug.
	public Bug cloneBug(Bug x1, int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x

		Chromosome newDNA = x1.getDNA().clone();

		double mutate = Math.random();

		if (mutate < mutation)
			newDNA = newDNA.mutate(); // possibly apply mutation.

		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, newDNA );
	}

	// Mutate a bug (this is not biologically correct, but does capture our intent.
	public Bug mutateBug(Bug x1, int mapidx)
	{
		double x = Maze[mapidx].getStartX();
		double y = Maze[mapidx].getStartY();
		double dir = 0.0;//( Math.random() * 360.0 );
		double vel = 0.0;
		double rot = 1.0; // multiplier of 2x
		double spe = 1.0; // multiplier of 1x

		Chromosome newDNA = x1.getDNA().clone();

		newDNA = newDNA.mutate(); // possibly apply mutation.

		return new Bug( x, y, dir, vel, rot, spe, bug_inputclasses, bug_hiddenlayersize, bug_hiddenlayers, bug_output_classes, newDNA );
	}

	// Fulfill the contract of MouseListener
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

	// Was there a click? Is so, pause the simulation or unpause.
    public void mouseClicked(MouseEvent e) {
		System.out.println(active);
		if (active)
			active = false;
		else
			active = true;
    }
}