package com.programmerdan.ai.maze;

import static org.junit.Assert.assertEquals();

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class SimulationMapTest {

	/**
	 * Logger for this class.
	 */
	private final Logger log = LoggerFactory.getLogger(SimulationMapTest.class);

	/**
	 * Simple Map Test. Creates a simple map from statically defined
	 *   {@link SimpleLine} objects.
	 *
	 * TODO: Add validation.
	 */
	@Test
	public void SimpleMapTest() {
		SimpleLine[] walls = new SimpleLine[6];

		walls[0] = new SimpleLine(-20.0, -10.0, -20.0,  10.0 );
		walls[1] = new SimpleLine(-20.0,  10.0,  20.0,  10.0 );
		walls[2] = new SimpleLine( 20.0,  10.0,  20.0, -10.0 );
		walls[3] = new SimpleLine( 20.0, -10.0, -20.0, -10.0 );
		walls[4] = new SimpleLine( 10.0, -10.0,  15.0,   5.0 );
		walls[5] = new SimpleLine(-10.0,  10.0, -14.0,  -5.0 );

		SimpleLine[] paths = new SimpleLine[2];

		paths[0] = new SimpleLine(-15.0,  -5.0,  0.0,   0.0 );
		paths[1] = new SimpleLine(  0.0,   0.0,  5.0,   5.0 );

		SimulationMap map = new SimulationMap(walls, paths);
	}

	/**
	 * Load map test. Used to be a main member, now a test -- loads a map
	 *   from disk, and runs a simple random walk against it, to make
	 *   sure the map gets loaded from disk properly and various internal
	 *   functions fire correctly.
	 */
	@Test
	public void LoadMapTest() {
		// TODO: add pre-step that builds and saves a map to disk for this to load.
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

			log.info("Using smooth random walk model to construct probabilistic baseline");

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
					log.error("out of bounds");
				else
				{
					//System.out.println(" Max: " + maxFit + " Min: " +  minFit + " Avg: " + (totFit / (double) numSamp));
				}

				if (samp % (attempts/75) == 0)
					log.info(".");

			}

			avgFit = totFit / (double) numSamp;

			avgProg = avgProg / (double) numSamp;

			avgStep = avgStep / (double) numSamp;

			log.info("Fitness: Max = {} Min = {} Avg = {}", maxFit, minFit, avgFit );
			log.info("Path: Max = {} Min = {} Avg = {}", maxProg, minProg, avgProg);
			log.info("Steps: Max = {} Min = {} Avg = {}", maxStep, minStep, avgStep);
			log.info("Attempts: {}", attempts);
		}
		catch (Exception ioe)
		{
			log.error("Something failed", ioe);
		}
	}

	/**
	 * TODO: Placeholder for more tests.
	 */
	@Test
	@Ignore
	public void functionTest() {

	}
}

