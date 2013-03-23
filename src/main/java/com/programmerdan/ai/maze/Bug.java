/*
	Programmer: Daniel J. Boston
	Date: May 7, 2007
	Class: CS 370

	Basic Bug -- with location, direction, success, failure, etc.
	This is built on a Chromosome sequence, and contains a NeuralNetwork which is built based on the Chromosome sequence.
*/
public class Bug extends Position
{
	//private double x; // location
	//private double y;

	private double direction; // rotation
	private double velocity; // motion

	private double rotateMult; // rotation multiplication
	private double speedMult; // velocity multiplication

	private int success;
	private int failure;

	private double fitness;

	// inputs
	public static int DISTANCE = 0;
	public static int POSITION = 1;
	public static int TYPE = 2;

	private int INPUTS = 4; // lets make this variable now.
	private int[] inputClasses;
	private int HIDDENS = 5;
	private int HIDDENW = 7;

	public int INPUTS() { return INPUTS; }
	public int HIDDENS() { return HIDDENS; }
	public int HIDDENW() { return HIDDENW; }

	private double[] inputStore;

	// outputs
	public static int VEL = 0;
	public static int DELTA = 1;

	public static int OUTPUTS = 2;

	private double[] outputStore;
	private int[] outputClasses;

	private Chromosome dna;
	private NeuralNetwork brain;

	public static int estimateChromosome(int inputs, int hiddenWidth, int hiddens)
	{
		int genes = 2; // for the basic two genes defining learning and forgetting.

		// two genes for each input. (weight, and activation threshold)
		genes += 2 * inputs;

		// first layer of hidden interfaces with inputs:
		genes += hiddenWidth * (inputs + 1); // the plus one is the activation threshold of that neuron.

		// subsequent layers of hiddens ( hiddens - 1 ) are homogenous.
		genes += (hiddens - 1) * hiddenWidth * (hiddenWidth + 1); // each hidden layer neuron has a weight for every previous layer neuron; there is additionally the activation threshold.

		// finally, output layer. Similar, each output neuron has a weight for every previous layer neuron; additionally, there is an activation threshold.
		genes += Bug.OUTPUTS * (hiddenWidth + 1);

		return genes;
	}

	/** The bug's brain size is controlled by hiddenwidth and hidden size. Choose widths and size appropriate to the number of inputs. **/
	public Bug(double _x, double _y, double _dir, double _vel, double _rotate, double _speed, int[] _inputs, int _hiddenwidth, int _hiddensize, int[] _outputs, Chromosome _DNA)
	{
		super(_x, _y);

		direction = _dir;
		velocity = _vel;

		rotateMult = _rotate;
		speedMult = _speed;

		success = 0;
		failure = 0;
		fitness = 0.0;

		INPUTS = _inputs.length;
		inputClasses = _inputs;
		HIDDENS = _hiddensize;
		HIDDENW = _hiddenwidth;

		inputStore = new double[INPUTS];

		outputStore = new double[OUTPUTS];

		outputClasses = _outputs;

		// Build Chromosome
		dna = _DNA;

		if (outputClasses.length != OUTPUTS)
		{
			brain = null;
			System.out.println("Lobotomy");
		}
		else
		{
			build();
		}
	}

	private double fitGene(int i)
	{
		return fit(dna.getGene(i).toDouble());
	}

	private double midGene(int i)
	{
		return mid(dna.getGene(i).toDouble());
	}

	private double tinGene(int i)
	{
		return tin(dna.getGene(i).toDouble());
	}

	// Builds a brain based on certain parameters.
	private void build()
	{
		if (dna != null) // now compute size of needed chromosome.
		{
			if (dna.numGenes() >= Bug.estimateChromosome(INPUTS, HIDDENW, HIDDENS) ) // ok, now we can build this brain.
			{
				//System.out.println("Input: " + INPUTS + " HIDDENS: " + HIDDENS + " HIDDENW: " + HIDDENW + " OUTPUTS: " + OUTPUTS);

				int i = 0;
				brain = new NeuralNetwork(INPUTS, HIDDENS, HIDDENW, OUTPUTS, midGene(i++), midGene(i++));

				for (int in = 0; in < INPUTS; in++)
				{
					brain.addInput(fitGene(i++), ( (inputClasses[in] == 0) ? midGene(i++) : ( (inputClasses[in] == 1) ? fitGene(i++) : tinGene(i++) ) ), AF_Tanh.Default);
				}

				double[] hidden; int c = 0;

				for (int a = 0 ; a < HIDDENS; a++)
				{
					if (a == 0)
					{
						c = INPUTS;
					}
					else
					{
						c = HIDDENW;
					}

					for (int d = 0 ; d < HIDDENW; d++)
					{
						hidden = new double[c];

						for (int b = 0; b < c; b++)
						{
							hidden[b] = fitGene(i++);
						}

						brain.addHidden( hidden, fitGene(i++), AF_Tanh.Default );
					}
				}

				for (int ou = 0; ou < OUTPUTS; ou++)
				{
					hidden = new double[HIDDENW];

					for (int b = 0; b < HIDDENW; b++)
					{
						hidden[b] = fitGene(i++);
					}

					brain.addOutput( hidden, fitGene(i++), ( outputClasses[ou] == 0 ? AF_Tanh.Default : AF_Sigmoid.Default ) );
				}

				if (i > dna.numGenes()) // invalid! oh no!
				{
					brain = null;
					System.out.println("Lobotomy");
				}
				else
				{
					//System.out.println("Genes: " + dna.numGenes() + " used: " + i);
				}
			}
		}
	}

	// Puts together the "brain" for this bug based on the gene's in the chromosome
/*	private void build()
	{
		if (dna != null)
		{
			int i = 0;

			//brain = new NeuralNetwork(4, 8, 14, 3, midGene(i++), midGene(i++));
			brain = new NeuralNetwork(4, 5, 7, 3, midGene(i++), midGene(i++));

			brain.addInput(fitGene(i++), midGene(i++), AF_Tanh.Default); // distance input 0, 1.0
			brain.addInput(fitGene(i++), fitGene(i++), AF_Tanh.Default); // type: -1, 1
			brain.addInput(fitGene(i++), midGene(i++), AF_Tanh.Default); // distance input 0, 1.0
			brain.addInput(fitGene(i++), fitGene(i++), AF_Tanh.Default); // type: -1,1

			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);


			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);


			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);


			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);


			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			brain.addHidden(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);


			// Velocity Modifier
			brain.addOutput(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Sigmoid.Default);
			// Left Turn Rate
			brain.addOutput(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);
			// Right Turn Rate
			brain.addOutput(new double[] {fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble()),fit(dna.getGene(i++).toDouble())}, fit(dna.getGene(i++).toDouble()), AF_Tanh.Default);

			//rotateMult = dna.getGene(i++).toDouble() * 4;

			if (i > dna.numGenes()) // invalid! oh no!
			{
				brain = null;
				System.out.println("Lobotomy");
			}
		}
	}
*/
	public Chromosome getDNA()
	{
		return dna;
	}

	public NeuralNetwork getBrain()
	{
		return brain;
	}

	/*public double getX()
	{
		return x;
	}*/

	public void setX(double _x)
	{
		x = _x;
	}

	/*public double getY()
	{
		return y;
	}*/

	public void setY(double _y)
	{
		y = _y;
	}

	public void setPosition(double _x, double _y)
	{
		setX(_x); setY(_y);
	}


	public double getDir()
	{
		return direction;
	}

	public void setDir(double _dir)
	{
		direction = _dir;
	}


	public double getVel()
	{
		return velocity;
	}

	public void setVel(double _vel)
	{
		velocity = _vel;
	}

	public double getTrueVel()
	{
		if (trueVector == null)
			return 0.0;
		else
			return Math.sqrt( Math.pow( trueVector[0], 2.0) + Math.pow( trueVector[1], 2.0 ) );
	}

	// Get vector -- multiplies the direction by the velocity, based on the multipliers (which are probably set to 1)
	public double[] getVector()
	{
		// directed velocity

		double _tx = velocity * speedMult * Math.cos(Math.toRadians(direction * rotateMult) );
		double _ty = velocity * speedMult * Math.sin(Math.toRadians(direction * rotateMult) );

		double[] ret = new double[] {_tx, _ty};

		return ret;
	}

	private double[] trueVector;

	public double[] getTrueVector()
	{
		return trueVector;
	}

	// Applies the input to the brain.
	public void setInput(int _idx, double _val)
	{
		if ( ( _idx >= 0 ) && ( _idx < INPUTS ) )
		{
			inputStore[_idx] = _val; // temporarily stored locally

			brain.setInputs(inputStore); // thus changes can be updated piecemeal.
		}
	}

	// Get inputs if you select the proper input.
	public double getInput(int _idx)
	{
		if ( ( _idx >= 0 ) && ( _idx < INPUTS ) )
		{
			return inputStore[_idx];
		}
		return  Float.NaN;
	}

	// Grab outputs from the brain.
	public double getOutput(int _idx)
	{
		if ( ( _idx >= 0 ) && ( _idx < INPUTS ) )
		{
			return outputStore[_idx];
		}
		return  Float.NaN;
	}

	public void setFitness(double fit)
	{
		fitness = fit;
		if (fit > maxfitness) maxfitness = fit;
	}

	private double maxfitness = Double.MIN_VALUE;

	public double getFitness()
	{
		return fitness;
	}

	public double getMaxFitness()
	{
		return maxfitness;
	}

	// update success.
	public void incSuccess()
	{
		success++;
	}

	public void clrSuccess()
	{
		success = 0;
	}

	public int getSuccess()
	{
		return success;
	}

	// update failure.
	public void incFailure()
	{
		failure++;
	}

	public void clrFailure()
	{
		failure = 0;
	}

	public int getFailure()
	{
		return failure;
	}

	// Manipulators for Gene values.
	private double fit(double _a)
	{
		return _a - .5;
	}

	private double tin(double _a)
	{
		return _a * .1;
	}

	private double mid(double _a)
	{
		return _a * .5;
	}

	// Step the "brain" and apply the outputs to the velocity and direction.
	public void step(SimulationMap sm)
	{
		brain.setInputs( inputStore );

		brain.step();

		outputStore = brain.getOutputs();

		velocity = outputStore[VEL];

		direction += 2.0 * outputStore[DELTA]; // 2 times output b/c the original formula called for a union of two such outputs, which could be max 2 times any one.
		direction = direction % 360;

		if (direction < 0)
			direction += 360; // normalize direction between 0 and 360.

		double[] temp = getVector();

		double[] temp2 = sm.fixMoveV(x, y, temp);

		trueVector = new double[] { temp2[0] - x, temp2[1] - y };

		x = temp2[0];
		y = temp2[1]; // update location based on "Vector" -- directed velocity.
	}
}
