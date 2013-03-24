import java.io.*;
import java.awt.geom.*;
import java.text.*;

/**
 * This is the simulation map object, it encapsulates a number of ideas including the bounding boxes of lines, and the concept of "proper path".
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 2010
 * @version 1.0-mvn March 23, 2013
 *
 * TODO: refactor main() as test
 * TODO: refactor to use logging framework
 */
public class SimulationMap
{
	/**
	 * Simple test validation of the map loader.
	 *
	 * @param 	args 	The map to load, excluding path and extension, followed by number of attempts,
	 *					and steps to take in each attempt.
	 */
	public static void main(String[] args)
	{
		/*SimpleLine[] walls = new SimpleLine[6];

		walls[0] = new SimpleLine(-20.0, -10.0, -20.0,  10.0 );
		walls[1] = new SimpleLine(-20.0,  10.0,  20.0,  10.0 );
		walls[2] = new SimpleLine( 20.0,  10.0,  20.0, -10.0 );
		walls[3] = new SimpleLine( 20.0, -10.0, -20.0, -10.0 );
		walls[4] = new SimpleLine( 10.0, -10.0,  15.0,   5.0 );
		walls[5] = new SimpleLine(-10.0,  10.0, -14.0,  -5.0 );

		SimpleLine[] paths = new SimpleLine[2];

		paths[0] = new SimpleLine(-15.0,  -5.0,  0.0,   0.0 );
		paths[1] = new SimpleLine(  0.0,   0.0,  5.0,   5.0 );

		SimulationMap map = new SimulationMap(walls, paths);*/

		SimulationMap map = new SimulationMap("./mazes/" + args[0] + ".maze");

		double x =  map.getStartX();
		double y =  map.getStartY();

		double mrho = 1.0; // will use random veloctiy of sorts based on max and min of sigmoid function
		double mtheta = Math.toRadians(1.716 * 4); // will use random motion of sorts based on max and min of tanh activation function

		double vr = 0.0;
		double vt = 0.0;

		double vx = 0.0;
		double vy = 0.0;

		SimpleLine[] rays = new SimpleLine[] {new SimpleLine(), new SimpleLine()}; double[] raytheta = new double[] { Math.toRadians( 15.0 ), - Math.toRadians( 15.0 ) }; double[] raylength = new double[] { 8.0, 8.0 };

		SimpleLine vector;

		try
		{

			System.out.println("Using smooth random walk model to construct probabilistic baseline");

			long step = 0;

			double[] mapex = map.mapExtent();

			double maxFit = Double.MIN_VALUE;
			double maxProg = Double.MIN_VALUE;
			double minFit = Double.MAX_VALUE;
			double minProg = Double.MAX_VALUE;
			double avgProg = 0.0;
			double avgFit = 0.0;
			double totFit = 0.0;

			long maxStep = Long.MIN_VALUE;
			long minStep = Long.MAX_VALUE;
			double avgStep = 0.0;

			long numSamp = 0l;
			double fitness = 0.0;
			double pathProg = 0.0;
			double maxPathProg = 0.0;

			double pathTravel = 0.0;

			int attempts = Integer.parseInt(args[1]);

			int maxsteps = Integer.parseInt(args[2]);

			for (int samp = 0; samp < attempts; samp++)
			{
				step = 0;
				pathTravel = 0.0;
				pathProg = 0.0;
				maxPathProg = Double.MIN_VALUE;

				x =  map.getStartX();
				y =  map.getStartY();
				vx = 0.0;
				vy = 0.0;
				vr = 0.0;
				vt = 0.0;

				while (x >= mapex[0] && x <= mapex[1] && y >= mapex[2] && y <= mapex[3] && step < maxsteps && pathProg < 1.0)
				{
					vr = (mrho * Math.random() );
					vt = vt + (mtheta * Math.random() - (mtheta / 2.0) );

					if (vt > 2.0 * Math.PI) vt -= 2.0 * Math.PI;
					if (vt < 0.0) vt += 2.0 * Math.PI;

					vx = vr * Math.cos(vt);
					vy = vr * Math.sin(vt);

					vector = map.fixMove(new SimpleLine(x, y, x + vx, y + vy));

					for (int i = 0; i < rays.length; i++)
					{
						rays[i].setX1(x);
						rays[i].setY1(y);
						rays[i].setX2( x + ( raylength[i] * Math.cos( vt + raytheta[i] ) ) );
						rays[i].setY2( y + ( raylength[i] * Math.sin( vt + raytheta[i] ) ) );
					}

					double[] nearPoints = map.nearestWalls(rays);
					double[] normPoints = SimulationMap.normalizeSight(nearPoints, raylength, 1.0, 0.0);
					int[] wallIdx = map.lastWallIdx();

					//System.out.println("opt: < " + (Math.round(vr * 1000.0) / 1000.0) + ", " + (Math.round( (vt * (180.0 / Math.PI) ) * 100.0) / 100.0) + " > near: < ");

					/*for (int j = 0; j < normPoints.length; j++)
					{
						System.out.print( (Math.round(normPoints[j] * 100.0) / 100.0 ) );
						System.out.print( "/" );
						System.out.print( wallIdx[j] );
						if (j < normPoints.length - 1)
							System.out.print(", ");
					}

					System.out.println(" > pdst: < " + (Math.round( map.relativePathProgress(x, y) * 1000.0 ) / 1000.0) + " >");

					System.out.println("pos: < " + (Math.round(x * 1000.0) / 1000.0) + ", " + (Math.round(y * 1000.0) / 1000.0) + " > next: < " +
													(Math.round(vector.x2() * 1000.0) / 1000.0) + ", " + (Math.round(vector.y2() * 1000.0) / 1000.0) + " >");
					System.out.println("vel: < " + (Math.round(vx * 1000.0) / 1000.0) + ", " + (Math.round(vy * 1000.0) / 1000.0) + " > adj.: < " +
													(Math.round((vector.x2() - vector.x1()) * 1000.0) / 1000.0) + ", " + (Math.round((vector.y2() - vector.y1()) * 1000.0) / 1000.0) + " >");
	*/

					x = vector.x2(); vx = vector.x2() - vector.x1();
					y = vector.y2(); vy = vector.y2() - vector.y1();

					pathTravel += Math.sqrt( Math.pow(vx, 2.0) + Math.pow(vy, 2.0) );

					pathProg = map.relativePathProgress(x,y);

					if (pathProg > maxPathProg) maxPathProg = pathProg;

					//totFit += fitness;
					//numSamp ++;

					step++;
				}

				if (step < minStep) minStep = step;
				if (step > maxStep) maxStep = step;

				avgStep += (double) step;

				if (pathTravel > 0.0)
					fitness = map.pathTravel(pathTravel);
				else
					fitness = 0.0;

				//if (fitness > 1.0)
				//	fitness = 1.0;

				//System.out.print("Prog: " + fitness);

				avgProg += pathProg;

				if (pathProg > maxProg) maxProg = pathProg;
				if (pathProg < minProg) minProg = pathProg;

				fitness *= ((pathProg + maxPathProg * 3.0) / 4.0);

				//fitness *= pathProg;

				//System.out.print(" Fit: " + fitness);

				if (step >= maxsteps && pathProg < 1.0)// step >= maxsteps && pathProg < 1.0)
					fitness *= computeStepFitness(maxPathProg, maxsteps ); // decay fitness! We didn't make it ... how close did we get, though?

				if (fitness > maxFit) maxFit = fitness;
				if (fitness < minFit) minFit = fitness;
				totFit += fitness;
				numSamp ++;

				if (!( x >= mapex[0] && x <= mapex[1] && y >= mapex[2] && y <= mapex[3] ))
					System.out.println("out of bounds");
				else
				{
					//System.out.println(" Max: " + maxFit + " Min: " +  minFit + " Avg: " + (totFit / (double) numSamp));
				}

				if (samp % (attempts/75) == 0)
					System.out.print(".");

			}

			System.out.println();
			System.out.println();

			avgFit = totFit / (double) numSamp;

			avgProg = avgProg / (double) numSamp;

			avgStep = avgStep / (double) numSamp;

			System.out.println("Fitness: Max = " + maxFit + " Min = " +  minFit + " Avg = " + avgFit);
			System.out.println("Path: Max = " + maxProg + " Min = " + minProg + " Avg = " + avgProg);
			System.out.println("Steps: Max = " + maxStep + " Min = " + minStep + " Avg = " + avgStep);
			System.out.println("Attempts: " + attempts);


		}
		catch (Exception ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * This functions is used to compute the "fitness" of motion along the map optimal path,
	 * after all steps complete.
	 *
	 * We only call when we've reached the limit. Otherwise no decay!
	 *
	 * @param	progressFitness	The current progress
	 * @param	simLengthCap	Simulation length cap
	 * @return					Double value of the current fitness
	 */
	private static double computeStepFitness(double progressFitness, double simLengthCap)
	{
		double b = 10.0 / simLengthCap;

		double x = progressFitness * simLengthCap;

		// now use a heavily modified sigmoid type function
		double sig = 2.0 / (1.0 + Math.exp( (-(x - simLengthCap)) * b) );

		if (sig > 1.0)
			return 1.0; // we are making good progress (at least in step with the simulation's progress.
		else
			return sig; // we are not making good enough progress.
	}

	/**
	 * Normalize the distances to objects based on sight ranges and min, max values of sight ability.
	 *
	 * @param	distances	distances to objects
	 * @param	sightranges	sight ranges
	 * @param	maxvalue	value indicating nothing seen
	 * @param	minvalue	value indicating starting point
	 * @return				normalized distances to seen (or if max, not seen) objects
	 */
	public static double[] normalizeSight(double[] distances, double[] sightranges, double maxvalue, double minvalue)
	{
		if (distances.length != sightranges.length)
		{
			return null;
		}

		double[] normalized = new double[distances.length];

		for (int i = 0; i < distances.length; i++)
		{
			if (distances[i] > sightranges[i]) // nothing within range.
				normalized[i] = maxvalue;
			else
				normalized[i] = minvalue + (maxvalue - minvalue) * (distances[i] / sightranges[i]); // simple linear interpolation!
		}

		return normalized;
	}

	/**
	 * Iterates over all wall lines and determines the coordinate extent of the map.
	 *
	 * @return	Array of map extents in the order min x, max x, min y, max y
	 */
	public double[] mapExtent()
	{
		double minx, miny, maxx, maxy;
		minx = Double.MAX_VALUE; miny = Double.MAX_VALUE;
		maxx = Double.MIN_VALUE; maxy = Double.MIN_VALUE;

		for (SimpleLine wall : wallLines)
		{
			if (wall.x1() < minx) minx = wall.x1();
			if (wall.x2() < minx) minx = wall.x2();
			if (wall.x1() > maxx) maxx = wall.x1();
			if (wall.x2() > maxx) maxx = wall.x2();
			if (wall.y1() < miny) miny = wall.y1();
			if (wall.y2() < miny) miny = wall.y2();
			if (wall.y1() > maxy) maxy = wall.y1();
			if (wall.y2() > maxy) maxy = wall.y2();
		}

		return new double[] { minx, maxx, miny, maxy };
	}

	/**
	 * A utility method used to draw maps as string (for debugging).
	 *
	 * @param	minx	Map's min X coord
	 * @param	maxx	Map's max X coord
	 * @param	miny	Map's min Y coord
	 * @param	maxy	Map's max Y coord
	 * @param	x		"Player" X coord location
	 * @param   y		"Player" Y coord location
	 * @param	vx		"Player" X velocity
	 * @param	vy		"Player" Y velocity
	 * @param	unit	Unit of distance
	 *
	 * @return			A string representation of the map
	 */
	public String drawMap(double minx, double maxx, double miny, double maxy, double x, double y, double vx, double vy, double unit)
	{
		StringBuffer mapout = new StringBuffer();

		// TODO: Why does this duplicate mapExtent? Instead of accepting extents, just calculate using mapExtent()
		// doublecheck that range fits extent.
		for (SimpleLine wall : wallLines)
		{
			if (wall.x1() < minx) minx = wall.x1();
			if (wall.x2() < minx) minx = wall.x2();
			if (wall.x1() > maxx) maxx = wall.x1();
			if (wall.x2() > maxx) maxx = wall.x2();
			if (wall.y1() < miny) miny = wall.y1();
			if (wall.y2() < miny) miny = wall.y2();
			if (wall.y1() > maxy) maxy = wall.y1();
			if (wall.y2() > maxy) maxy = wall.y2();
		}

		for (double j = maxy; j >= miny; j -= unit)
		{
			for (double i = minx; i <= maxx; i += unit)
			{
				// draw player
				if ( Math.sqrt( Math.pow(i - x, 2.0) + Math.pow(j - y, 2.0) ) <= unit / 1.414214) // within sqrt(2) unit distance
				{
					mapout.append("P");
				}
				else if (Line2D.ptSegDist( x, y, x-vx, y-vy, i, j) <= unit / 2.0) // trailing vector
				{
					mapout.append("o");
				}
				else
				{
					boolean taken = false;

					// loop through walls, see if a wall occupies this space.
					for (SimpleLine wall : wallLines)
					{
						if ( Line2D.ptSegDist(wall.x1(), wall.y1(), wall.x2(), wall.y2(), i, j) <= unit / 2.0 )
						{
							mapout.append("#");
							taken = true;
							break;
						}
					}

					if (!taken)
					{
						for (SimpleLine path : pathLines)
						{
							if ( Line2D.ptSegDist(path.x1(), path.y1(), path.x2(), path.y2(), i, j) <= unit / 2.0 )
							{
								mapout.append("+");
								taken = true;
								break;
							}
						}
					}

					if (!taken)
					{
						mapout.append(" ");
					}
				}
			}

			mapout.append("\n");
		}

		return mapout.toString();
	}

	/** The lines of the wall */
	private SimpleLine[] wallLines;
	/** Path lines, or, the solution to the map */
	private SimpleLine[] pathLines;
	/** The map title */
	private String title;

	/**
	 * Getter for the title
	 * TODO: Let's use standard getter/setter pairs, please.
	 *
	 * @return	The title as a {@link String}
	 */
	public String title()
	{
		return title;
	}

	/**
	 * For internal unit tests, instantiates the map with an arbitrary set of walls and paths
	 *
	 * @param	testWalls	The walls as {@link SimpleLine}s.
	 * @param	testPaths	The paths as {@link SimpleLine}s.
	 */
	private SimulationMap(SimpleLine[] testWalls, SimpleLine[] testPaths)
	{
		wallLines = testWalls;
		pathLines = testPaths;
	}

	/**
	 * Simulation Map's constructor; builds a map object from a file of map values.
	 *
	 * File format:
	 * Map Title
	 *  numWalls
	 *  wallx1,wally1,wallx2,wally2
	 *  ...
	 *  wallxn,wallyn,wallxn,wallyn
	 *  numPaths
	 *  pathx1,pathy1
	 *  ...
	 *  pathxn,pathyn
	 *  nearPath
	 *  farPath
	 *
	 * @param	String	mapFile	The map file to load. If file is invalid, fails.
	 */
	public SimulationMap(String mapFile)
	{
		// TODO: Split up this method a bit. Why is so much in the constructor?
		try
		{
			File map = new File(mapFile);

			BufferedReader br = new BufferedReader( new FileReader( map ) );

			String  line = br.readLine();

			title = line;

			line = br.readLine();

			int     numLines       = Integer.parseInt(line);

			wallLines = new SimpleLine[numLines];

			double X1, X2, Y1, Y2; String[] posplit;

			for (int i = 0; i < numLines; i++)
			{
				line = br.readLine();
				posplit = line.split(",");
				X1 = Double.parseDouble(posplit[0]);
				Y1 = Double.parseDouble(posplit[1]);
				X2 = Double.parseDouble(posplit[2]);
				Y2 = Double.parseDouble(posplit[3]);
				wallLines[i] = new SimpleLine(X1,Y1,X2,Y2);
			}

			line      = br.readLine();
			numLines  = Integer.parseInt(line);

			pathLines = new SimpleLine[numLines - 1];

			line = br.readLine();
			posplit = line.split(",");
			X1 = Double.parseDouble(posplit[0]);
			Y1 = Double.parseDouble(posplit[1]);

			for (int j = 0; j < numLines - 1; j++)
			{
				line = br.readLine();
				posplit = line.split(",");
				X2 = Double.parseDouble(posplit[0]);
				Y2 = Double.parseDouble(posplit[1]);
				pathLines[j] = new SimpleLine(X1,Y1,X2,Y2);

				X1 = X2;
				Y1 = Y2;
			}

			pathNearThreshold = Double.parseDouble(br.readLine());
			pathFarThreshold = Double.parseDouble(br.readLine());

			br.close();
		}
		catch(Exception e)
		{
			System.out.println("Error reading map " + mapFile);
			e.printStackTrace();
		}
	}

	/** The last distances to nearby walls detected by {@link nearestWalls(SimpleLine[])} */
	public double[] lastNear = null;
	/** The last wall IDs detected by {@link nearestWalls{SimpleLine[])} */
	public int[] lastWallIdx = null;

	/**
	 * Give a series of rays radiating from a central point, returns the distance to the nearest wall.
	 * If no wall is intersected within sight distance, returns Double.MAX_VALUE for that ray.
	 *
	 * @param	rays	The "rays" to find wall intersections on.
	 * @return			A double[] of the same size as rays, containing distances to the nearest walls.
	 */
	public double[] nearestWalls(SimpleLine[] rays)
	{
		double[] near = new double[rays.length];
		int[] wallidx = new int[rays.length];
		for (int k = 0; k < rays.length; k++)
		{
			near[k] = Double.MAX_VALUE;
			wallidx[k] = -1;
		}

		// Compute over all wall lines.
		for (int j = 0; j < wallLines.length; j++)
		{
			// Compute over all rays, finding closest intersection..
			for (int i = 0; i < rays.length; i++)
			{
				if ( Line2D.linesIntersect( rays[i].x1(), rays[i].y1(), rays[i].x2(), rays[i].y2(), wallLines[j].x1(), wallLines[j].y1(), wallLines[j].x2(), wallLines[j].y2() ) )
				{ // by convention, first point of all rays is the same (viewer)
					double dis = Line2D.ptSegDist(wallLines[j].x1(), wallLines[j].y1(), wallLines[j].x2(), wallLines[j].y2(),rays[i].x1(), rays[i].y1() );
					if ( near[i] > dis )
					{
						near[i] = dis;
						wallidx[i] = j;
					}
				}
			}
		}

		lastNear = near;
		lastWallIdx = wallidx;

		return near;
	}

	/**
	 * Simple test function for vector intersection against a wall.
	 *
	 * @param	x1	The start X coord of the vector
	 * @param	y1	The start Y coord of the vector
	 * @param	x2	The end X coord of the vector
	 * @param	y2	The end Y coord of the vector
	 * @return		True if the vector intersects any wall, false otherwise.
	 */
	public boolean intersectsWall(double x1, double y1, double x2, double y2)
	{
		for (int i = 0; i < wallLines.length; i++)
		{
			if ( Line2D.linesIntersect(x1, y1, x2, y2, wallLines[i].x1(), wallLines[i].y1(), wallLines[i].x2(), wallLines[i].y2() ) )
				return true;
		}

		return false;
	}

	/**
	 * Accessor of last wall IDs detected by {@link nearestWalls{SimpleLine[])}
	 *
	 * @return	The array of wall IDs
	 */
	public int[] lastWallIdx()
	{
		return lastWallIdx;
	}

	/**
	 * Tests if this proposed movement vector passes through a wall.
	 * TODO: This duplicates in inverse {@link intersectsWall}
	 *
	 * @param	SimpleLine	The vector to test against all walls.
	 * @return				True if vector doesn't intersect any walls, false if it does.
	 */
	public boolean canMove(SimpleLine vector)
	{
		for (int j = 0; j < wallLines.length; j++)
		{
			if ( Line2D.linesIntersect( vector.x1(), vector.y1(), vector.x2(), vector.y2(), wallLines[j].x1(), wallLines[j].y1(), wallLines[j].x2(), wallLines[j].y2() ) )
				return false;
		}

		return true;
	}

	/** Wall buffer -- "constant" parameter saying how close to the wall a movement vector is allowed. */
	private double wallBuffer = 0.70711;
	/** Adjustment factor to prevent accidental wall overtake */
	private double adjfactor = 0.1;

	/**
	 * Returns a "shortened" vector based on wall location, useful to foreshorten vectors that would otherwise pass through walls.
	 *
	 * TODO: some error conditions are ignored. This should throw exceptions where appropriate.
	 *
	 * @param	vector	The vector to shorten
	 * @return			The shortened vector (a new SimpleLine)
	 */
	public SimpleLine fixMove(SimpleLine vector)
	{
		double near = 1.0; // max extent of interest for our parametric is 1.0
		double dis = 0.0;
		int wallidx = -1;

		double x1, x2, x3, x4, x5, xt, xa, y1, y2, y3, y4, y5, yt, ya;
		double ds, de;

		x1 = vector.x1(); x2 = vector.x2(); xa = x2;
		y1 = vector.y1(); y2 = vector.y2(); ya = y2;

		// first, find closest intersecting wall.
		//
		// Instead of finding closest intersecting wall, find the smallest parametric out of all computed, and use that to shorten the line (only if parametric is < 1.0)
		for (int j = 0 ; j < wallLines.length; j++)
		{
			x3 = wallLines[j].x1(); x4 = wallLines[j].x2();
			y3 = wallLines[j].y1(); y4 = wallLines[j].y2();
			ds = Line2D.ptSegDist(x3, y3, x4, y4, x1, y1);
			de = Line2D.ptSegDist(x3, y3, x4, y4, x2, y2);

			// basically what we do is, see if these lines intersect. If they do intersect, and the wall is within the segment, adjust the vector parametrics.
			// If they do not intersect, we are still interested in seeing if this vector gets us "too close" to the wall.

			double denom = (y4 - y3)*(x2 - x1) - (x4 - x3)*(y2 - y1);
			double numT  = (x4 - x3)*(y1 - y3) - (y4 - y3)*(x1 - x3);
			double numS  = (x2 - x1)*(y1 - y3) - (y2 - y1)*(x1 - x3);

			if (denom == 0.0 && numT == 0.0 && numS == 0.0)
			{
				// coincident lines?! this is bad.
				//System.err.println("riding the wall");
				//return vector; // riding the wall.
			}
			else if (denom == 0.0)
			{
				//System.err.println("parallel lines");
				// parallel! -- no possibility of intersection.
			}
			else
			{
				double parT = numT / denom;
				double parS = numS / denom;

				if (parT >= 0.0 && parT <= 1.0 && parS >= 0.0 && parS <= 1.0) // intersection!
				{
					if (parT - adjfactor < near)
					{
						near = parT - adjfactor;
						wallidx = j;
					}

				}
				else
				{
					 // no intersection
				}
			}
		}

		if (wallidx == -1)
		{
			return vector; // no intersection, no adjustments.
		}

		// At the end of this loop, we have the shortest adjustment that satisfies all bounds.

		if (near < 0.0)
			near = 0.0;

		x5 = x1 + near*(x2 - x1);
		y5 = y1 + near*(y2 - y1);

		return new SimpleLine(x1, y1, x5, y5);
	}

	/**
	 * Fix a motion vector so that it doesn't pass through walls.
	 *
	 * @param	x		The origin of the vector in X coords
	 * @param	y		The origin of the vector in Y coords
	 * @param	vector	The vector delta coords {x, y}
	 * @return			The modified vector deltas {x, y}
	 * @see				{@link fixMove}
	 */
	public double[] fixMoveV(double x, double y, double[] vector)
	{
		SimpleLine fixed = fixMove(new SimpleLine(x, y, x + vector[0], y + vector[1]));

		return new double[] {fixed.x2(), fixed.y2()};
	}

	/**
	 * Returns how close to the path this point is (closest straightline distance).
	 *
	 * TODO: Evaluate efficiency and adjust
	 *
	 * @param	x1	The point in X coords to compare against
	 * @param	y1	The point in Y coords to compare against
	 *
	 * @return		The distance, or {@link Double.MAX_VALUE} if nothing nearby.
	 */
	public double distToPath(double x1, double y1)
	{
		double near = Double.MAX_VALUE;

		if (!intersectsWall(x1, y1, pathLines[0].x1(), pathLines[0].y1() ) )
			near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );

		for (int j = 0; j < pathLines.length; j++)
		{
			if (!intersectsWall(x1, y1, pathLines[j].x2(), pathLines[j].y2() ) )
			{
				double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );

				if ( dis < near )
					near = dis;
			}
		}

		return near;
	}

	/**
	 * Returns the relative path index this point is closest to.
	 *
	 * @param	x1	The point's X coord
	 * @param	y1	The point's Y coord
	 *
	 * @return		The ratio of nearest index over number of pathLines.
	 */
	public double distToPathIdx(double x1, double y1)
	{
		//double near = Double.MAX_VALUE;
		int idx = -1;
		double near = Double.MAX_VALUE;

		if (!intersectsWall(x1, y1, pathLines[0].x1(), pathLines[0].y1() ) )
		{
			near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );
			idx = 0;
		}

		for (int j = 0; j < pathLines.length; j++)
		{
			if (!intersectsWall(x1, y1, pathLines[j].x2(), pathLines[j].y2() ) )
			{
				double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );
				//double dis = Line2D.ptSegDist(pathLines[j].x1(), pathLines[j].y1(), pathLines[j].x2(), pathLines[j].y2(), x1, y1 );

				if ( dis < near )
				{
					near = dis;
					idx = j + 1;
				}
			}
		}

		if (idx < 0)
			near = 0.0;
		else
			near = (double) idx / pathLines.length; // simple relative progress. Could add some to indicate relative progress within the closest path segment.

		return near;
	}

	/**
	 * Returns how close to the path this point is.
	 */
/*	public double distToPath(double x1, double y1)
	{
		double near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );//Double.MAX_VALUE;

		for (int j = 0; j < pathLines.length; j++)
		{
			double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );
			//Line2D.ptSegDist(pathLines[j].x1(), pathLines[j].y1(), pathLines[j].x2(), pathLines[j].y2(), x1, y1 );

			if ( dis < near )
				near = dis;
		}

		return near;
	}
*/
	/**
	 * Returns the relative path index this point is closest to.
	 */
/*	public double distToPathIdx(double x1, double y1)
	{
		//double near = Double.MAX_VALUE;
		int idx = 0;
		double near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );//Double.MAX_VALUE;

		for (int j = 0; j < pathLines.length; j++)
		{
			double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );
			//double dis = Line2D.ptSegDist(pathLines[j].x1(), pathLines[j].y1(), pathLines[j].x2(), pathLines[j].y2(), x1, y1 );

			if ( dis < near )
			{
				near = dis;
				idx = j + 1;
			}
		}

		near = (double) idx / pathLines.length; // simple relative progress. Could add some to indicate relative progress within the closest path segment.

		return near;
	}
*/
	/** Nearness threshold for relative distance computation */
	private double pathNearThreshold = 1.75;
	/**
	 * Accessor for {@link pathNearThreshold}
	 *
	 * @return	The pathNearThreshold;
	 */
	public double getNearThreshold()
	{
		return pathNearThreshold;
	}

	/** Far threshold for relative distance computation */
	private double pathFarThreshold = 15.0;

	/**
	 * Returns the relative path progress ( a number in [0.0, 1.0] )
	 *
	 * @param	x1	The point's X coord
	 * @param	y1	The point's Y coord
	 * @return		The relative progress, where 0.0 is no progress, 1.0 is at end.
	 */
	public double relativePathProgress(double x1, double y1)
	{
		// Basically, if the closest segment is the last segment, we also need to be very close to that segment to qualify as 1.0
		// otherwise, we approach 0.0

		double dpi = distToPathIdx(x1, y1);

		double dtp = distToPath(x1, y1);

		if (dtp > pathFarThreshold) dtp = pathFarThreshold;
		if (dtp < pathNearThreshold) dtp = pathNearThreshold;

		double pet = 1.0 - (dtp - pathNearThreshold) / (pathFarThreshold - pathNearThreshold);

		return dpi * pet;
	}

	/** The length of the path */
	private double pathLength = -1.0;

	/**
	 * Returns the length of the path. Computes the path on first request./
	 *
	 * @return	The length of the path.
	 */
	public double pathLength()
	{
		if (pathLength < 0)
		{
			pathLength = 0.0;

			for (int j = 0; j < pathLines.length; j++)
			{
				pathLength += Math.sqrt( Math.pow(pathLines[j].x2() - pathLines[j].x1(), 2.0) + Math.pow(pathLines[j].y2() - pathLines[j].y1(), 2.0) );
			}
		}

		return pathLength;
	}

	/** The mean of the path */
	private double pathMean = -1.0;

	/**
	 * Honestly not sure what this is doing. Takes the log of the {@link pathLength()}.
	 *
	 * @return	the mean
	 */
	private double pathMean()
	{
		if (pathMean < 0.0)
		{
			pathMean = Math.log(pathLength()) + Math.pow(.5,2.0);
		}
		return pathMean;
	}

	/** The mode of the path */
	private double pathMode = -1.0;

	/**
	 * Again, I can't remember what this does. Takes E to the power of the Mean.
	 *
	 * @return	the mode
	 */
	private double pathMode()
	{
		if (pathMode < 0.0)
		{
			pathMode = Math.pow(Math.E, pathMean() - Math.pow(.5, 2.0) );
		}
		return pathMode;
	}

	/** the peak of the path */
	private double pathPeak = -1.0;

	/**
	 * The peak of the path. Again, can't remember what this is doing.
	 *
	 * @return	the peak
	 */
	private double pathPeak()
	{
		if (pathPeak < 0.0)
		{
			pathPeak = (1.0 / ( pathMode() * .5 * Math.sqrt( 2.0 * Math.PI ) ) ) * Math.exp( - ( Math.pow( ( Math.log(pathMode()) - pathMean() ), 2.0 ) / ( 2.0 * Math.pow( 0.5, 2.0 ) ) ) ) ;
		}

		return pathPeak;
	}

	/**
	 * The travel distance along the path.
	 *
	 * @param	travel	The travel along the path
	 * @return			The adjusted travel along the path
	 */
	public double pathTravel(double travel)
	{
		return (1.0 / pathPeak() ) * (1.0 / ( travel * .5 * Math.sqrt( 2.0 * Math.PI ) )) * Math.exp( - ( Math.pow( ( Math.log(travel) - pathMean() ), 2.0 ) / ( 2.0 * Math.pow( 0.5, 2.0 ) ) ) );
	}

	/**
	 * Get the start X of the first pathline.
	 *
	 * @return	starting X coord.
	 */
	public double getStartX()
	{
		return pathLines[0].x1();
	}

	/**
	 * Get the start Y of the first pathline.
	 *
	 * @return	starting Y coord.
	 */
	public double getStartY()
	{
		return pathLines[0].y1();
	}

	/**
	 * Accessor for the wall {@link SimpleLine}s.
	 *
	 * @return the array of walls
	 */
	public SimpleLine[] walls()
	{
		return wallLines;
	}

	/**
	 * Accessor for the path {@link SimpleLine}s.
	 *
	 * @return the array of paths
	 */
	public SimpleLine[] paths()
	{
		return pathLines;
	}
}