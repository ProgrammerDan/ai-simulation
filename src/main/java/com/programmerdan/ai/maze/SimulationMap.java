package com.programmerdan.ai.maze;

import java.io.*;
import java.awt.geom.*;
import java.text.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the simulation map object, it encapsulates a number of ideas including the bounding boxes of lines, and the concept of "proper path".
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 2007
 *   Initial version, for a class project
 * @version 1.0-mvn March 23, 2013
 *   Mavenized the project
 * @version 1.01 March 31, 2013
 *   Refactored out testing patterns, organized code, added comments, added logging framework.
 */
public class SimulationMap {
	/* Class Attributes */

	/**
	 * Logger for this class.
	 */
	private final Logger log = LoggerFactory.getLogger(SimulationMap.class);

	/**
	 * The lines of the wall
	 */
	private SimpleLine[] wallLines;
	/**
	 * Path lines, or, the solution to the map
	 */
	private SimpleLine[] pathLines;
	/**
	 * The map title
	 */
	private String title;

	/**
	 * The last distances to nearby walls detected by {@link nearestWalls(SimpleLine[])}
	 */
	private double[] lastNear = null;
	/**
	 The last wall IDs detected by {@link nearestWalls{SimpleLine[])}
	 */
	private int[] lastWallIdx = null;

	/**
	 * Wall buffer -- "constant" parameter saying how close to the wall a movement vector is allowed.
	 */
	private double wallBuffer = 0.70711;
	/**
	 * Adjustment factor to prevent accidental wall overtake
	 */
	private double adjfactor = 0.1;

	/**
	 * Nearness threshold for relative distance computation
	 */
	private double pathNearThreshold = 1.75;
	/**
	 * Far threshold for relative distance computation
	 */
	private double pathFarThreshold = 15.0;
	/**
	 * The length of the path
	 */
	private double pathLength = -1.0;
	/**
	 * The mean of the path
	 */
	private double pathMean = -1.0;
	/**
	 * The mode of the path
	 */
	private double pathMode = -1.0;
	/**
	 * the peak of the path
	 */
	private double pathPeak = -1.0;


	/* ACCESSORS/MUTATORS */


	/**
	 * Getter for the title
	 *
	 * @return	The title as a {@link String}
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the start X of the first pathline.
	 *
	 * @return	starting X coord.
	 */
	public double getStartX() {
		return pathLines[0].x1();
	}

	/**
	 * Get the start Y of the first pathline.
	 *
	 * @return	starting Y coord.
	 */
	public double getStartY() {
		return pathLines[0].y1();
	}

	/**
	 * Accessor for the wall {@link SimpleLine}s.
	 *
	 * @return the array of walls
	 */
	public SimpleLine[] getWalls() {
		return wallLines;
	}

	/**
	 * Accessor for the path {@link SimpleLine}s.
	 *
	 * @return the array of paths
	 */
	public SimpleLine[] getPaths() {
		return pathLines;
	}

	/**
	 * Accessor of last wall IDs detected by {@link nearestWalls{SimpleLine[])}
	 *
	 * @return	The array of wall IDs
	 */
	public int[] getLastWallIdx() {
		return lastWallIdx;
	}

	/**
	 * Accessor for {@link pathNearThreshold}
	 *
	 * @return	The pathNearThreshold
	 */
	public double getNearThreshold() {
		return pathNearThreshold;
	}

	/**
	 * Returns the length of the path. Computes the path on first request.
	 *
	 * @return	The length of the path.
	 */
	public double getPathLength() {
		if (pathLength < 0) {
			pathLength = 0.0;

			for (int j = 0; j < pathLines.length; j++) {
				pathLength += Math.sqrt( Math.pow(pathLines[j].x2() - pathLines[j].x1(), 2.0) + Math.pow(pathLines[j].y2() - pathLines[j].y1(), 2.0) );
			}
		}

		return pathLength;
	}

	/**
	 * Honestly not sure what this is doing. Takes the log of the {@link getPathLength()}.
	 *
	 * @return	the mean
	 */
	private double getPathMean() {
		if (pathMean < 0.0) {
			pathMean = Math.log(getPathLength()) + Math.pow(.5,2.0);
		}
		return pathMean;
	}

	/**
	 * Again, I can't remember what this does. Takes E to the power of the Mean.
	 *
	 * @return	the mode
	 */
	private double getPathMode() {
		if (pathMode < 0.0) {
			pathMode = Math.pow(Math.E, getPathMean() - Math.pow(.5, 2.0) );
		}
		return pathMode;
	}

	/**
	 * The peak of the path. Again, can't remember what this is doing.
	 *
	 * @return	the peak
	 */
	private double getPathPeak() {
		if (pathPeak < 0.0) {
			pathPeak = (1.0 / ( getPathMode() * .5 * Math.sqrt( 2.0 * Math.PI ) ) ) *
							Math.exp( - ( Math.pow( ( Math.log(getPathMode()) - getPathMean() ), 2.0 ) /
							( 2.0 * Math.pow( 0.5, 2.0 ) ) ) ) ;
		}

		return pathPeak;
	}

	/**
	 * The travel distance along the path.
	 *
	 * @param	travel	The travel along the path
	 * @return			The adjusted travel along the path
	 */
	public double pathTravel(double travel) {
		return (1.0 / getPathPeak() ) * (1.0 / ( travel * .5 * Math.sqrt( 2.0 * Math.PI ) )) *
				Math.exp( - ( Math.pow( ( Math.log(travel) - getPathMean() ), 2.0 ) / ( 2.0 * Math.pow( 0.5, 2.0 ) ) ) );
	}


	/* CONSTRUCTORS */


	/**
	 * For internal unit tests, instantiates the map with an arbitrary set of walls and paths
	 *
	 * @param	testWalls	The walls as {@link SimpleLine}s.
	 * @param	testPaths	The paths as {@link SimpleLine}s.
	 */
	protected SimulationMap(SimpleLine[] testWalls, SimpleLine[] testPaths) {
		initialize(testWalls, testPaths);
	}

	/**
	 * No-op private default constructor.
	 */
	private SimulationMap() {
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
	public SimulationMap(String mapFile) {
		try {
			File map = new File(mapFile);

			BufferedReader br = new BufferedReader( new FileReader( map ) );

			initialize(br);
		} catch (FileNotFoundException fnfe) {
			log.error("Unable to load given map file {} \n {}", mapFile, fnfe);
		} catch (InstantiationException ie) {
			log.error("Map instantiation failed.", ie);
		}
	}

	/* INITIALIZATION METHODS */

	/**
	 * Initialize a SimulationMap from a BufferedReader (which we expect to wrap a file,
	 *   but which doesn't have to).
	 *
	 * @param	br	The {@link BufferedReader} from which to build this map.
	 * @throws	{@link InstantiationException}	Thrown if unable to read from the file or file has corrupt contents.
	 */
	private void initialize(BufferedReader br) throws InstantiationException {
		String line;
		int numLines;

		title = getLineSafe(br, "Error reading title line of map file.");

		try {
			numLines = Integer.parseInt(getLineSafe(br, "Error reading wall count line of map file."));
		} catch (NumberFormatException nfe) {
			log.error("Format error, this map file does not have a wall count line.", nfe);
			throw new InstantiationException();
		}

		wallLines = new SimpleLine[numLines];

		double X1;
		double X2;
		double Y1;
		double Y2;
		String[] posplit;

		for (int i = 0; i < numLines; i++) {
			try {
				line = getLineSafe(br, "Failed to read a wall description line.");
				posplit = line.split(",");
				X1 = Double.parseDouble(posplit[0]);
				Y1 = Double.parseDouble(posplit[1]);
				X2 = Double.parseDouble(posplit[2]);
				Y2 = Double.parseDouble(posplit[3]);
			} catch (NumberFormatException nfe2) {
				log.error("Not every number in the wall description line was parseable.", nfe2);
				return;
			}

			wallLines[i] = new SimpleLine(X1,Y1,X2,Y2);
		}

		try {
			numLines  = Integer.parseInt(getLineSafe(br, "Error reading path count line of map file."));
		} catch (NumberFormatException nfe3) {
			log.error("Format error, this map file does not have a path count line.", nfe3);
			throw new InstantiationException();
		}

		pathLines = new SimpleLine[numLines - 1];

		try {
			line = getLineSafe(br, "Failed to read starting X,Y line.");
			posplit = line.split(",");
			X1 = Double.parseDouble(posplit[0]);
			Y1 = Double.parseDouble(posplit[1]);
		} catch (NumberFormatException nfe4) {
			log.error("Format error, this map file does not have a starting X,Y line.", nfe4);
			throw new InstantiationException();
		}

		for (int j = 0; j < numLines - 1; j++) {
			try {
				line = getLineSafe(br, "Failed to read a Path X,Y line.");
				posplit = line.split(",");
				X2 = Double.parseDouble(posplit[0]);
				Y2 = Double.parseDouble(posplit[1]);
			} catch (NumberFormatException nfe5) {
				log.error("Format error, this map file has an invalid X,Y line.", nfe5);
				throw new InstantiationException();
			}

			pathLines[j] = new SimpleLine(X1,Y1,X2,Y2);

			X1 = X2;
			Y1 = Y2;
		}

		try {
			pathNearThreshold = Double.parseDouble(getLineSafe(br, "Failed to read path nearness threshold."));
			pathFarThreshold = Double.parseDouble(getLineSafe(br, "Failed to read path far threshold."));
		} catch (NumberFormatException nfe6) {
			log.error("Format error on path near/far thresholds.", nfe6);
			throw new InstantiationException();
		}

		try {
			br.close();
		} catch(IOException e) {
			log.error("Error closing map file.", e);
		}
	}

	/**
	 * Simple function that finalizes the initialization of a SimulationMap.
	 *   Fills the walls and paths of the map.
	 *
	 * @param	wallLines	The {@link SimpleLine} array of lines in the wall.
	 * @param	pathLines	The {@link SimpleLine} array of path guide lines.
	 */
	private void initialize(SimpleLine[] wallLines, SimpleLine[] pathLines) {
		this.wallLines = wallLines;
		this.pathLines = pathLines;
	}

	/* UTILITY METHODS */

	/**
	 * Found myself wrapping too many single line reads; simple helper method that safely
	 *   reads a file line, and throws an InstantiationException if it fails. This method
	 *   is only used by the initialize() breed of internal methods.
	 *
	 * @param	br				The Reader to pull a line from.
	 * @param	failureMessage	The message text to output to logger on failure.
	 * @throws	{@link InstantiationException}	Indicates something horrible has happened while
	 * 			  creating an instance of this class.
	 * @return	A string drawn from the BufferedReader.
	 */
	private String getLineSafe(BufferedReader br, String failureMessage) throws InstantiationException{
		try {
			return br.readLine();
		} catch (IOException ioe1) {
			log.error(failureMessage, ioe1);
			throw new InstantiationException();
		}
	}

	/**
	 * Iterates over all wall lines and determines the coordinate extent of the map.
	 *
	 * @return	Array of map extents in the order min x, max x, min y, max y
	 */
	public double[] mapExtent() {
		double minx, miny, maxx, maxy;
		minx = Double.MAX_VALUE; miny = Double.MAX_VALUE;
		maxx = Double.MIN_VALUE; maxy = Double.MIN_VALUE;

		for (SimpleLine wall : wallLines) {
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
	 * Give a series of rays radiating from a central point, returns the distance to the nearest wall.
	 * If no wall is intersected within sight distance, returns Double.MAX_VALUE for that ray.
	 *
	 * @param	rays	The "rays" to find wall intersections on.
	 * @return			A double[] of the same size as rays, containing distances to the nearest walls.
	 */
	public double[] nearestWalls(SimpleLine[] rays) {
		double[] near = new double[rays.length];
		int[] wallidx = new int[rays.length];
		for (int k = 0; k < rays.length; k++) {
			near[k] = Double.MAX_VALUE;
			wallidx[k] = -1;
		}

		// Compute over all wall lines.
		for (int j = 0; j < wallLines.length; j++) {
			// Compute over all rays, finding closest intersection..
			for (int i = 0; i < rays.length; i++) {
				if ( Line2D.linesIntersect( rays[i].x1(), rays[i].y1(), rays[i].x2(), rays[i].y2(), wallLines[j].x1(), wallLines[j].y1(), wallLines[j].x2(), wallLines[j].y2() ) ) {
				    // by convention, first point of all rays is the same (viewer)
					double dis = Line2D.ptSegDist(wallLines[j].x1(), wallLines[j].y1(), wallLines[j].x2(), wallLines[j].y2(),rays[i].x1(), rays[i].y1() );
					if ( near[i] > dis ) {
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
	public boolean intersectsWall(double x1, double y1, double x2, double y2) {
		for (int i = 0; i < wallLines.length; i++) {
			if ( Line2D.linesIntersect(x1, y1, x2, y2, wallLines[i].x1(), wallLines[i].y1(), wallLines[i].x2(), wallLines[i].y2() ) )
				return true;
		}

		return false;
	}

	/**
	 * Tests if this proposed movement vector does not pass through a wall.
	 *
	 * @param	SimpleLine	The vector to test against all walls.
	 * @return				True if vector doesn't intersect any walls, false if it does.
	 * @see	{@link intersectsWall()}
	 */
	public boolean canMove(SimpleLine vector) {
		return !intersectsWall(vector.x1(), vector.y1(), vector.x2(), vector.y2());
	}

	/**
	 * Returns a "shortened" vector based on wall location, useful to foreshorten vectors that would otherwise pass through walls.
	 *
	 * @param	vector	The vector to shorten
	 * @return			The shortened vector (a new SimpleLine)
	 */
	public SimpleLine fixMove(SimpleLine vector) {
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
		for (int j = 0 ; j < wallLines.length; j++) {
			x3 = wallLines[j].x1(); x4 = wallLines[j].x2();
			y3 = wallLines[j].y1(); y4 = wallLines[j].y2();
			ds = Line2D.ptSegDist(x3, y3, x4, y4, x1, y1);
			de = Line2D.ptSegDist(x3, y3, x4, y4, x2, y2);

			// basically what we do is, see if these lines intersect. If they do intersect, and the wall is within the segment, adjust the vector parametrics.
			// If they do not intersect, we are still interested in seeing if this vector gets us "too close" to the wall.

			double denom = (y4 - y3)*(x2 - x1) - (x4 - x3)*(y2 - y1);
			double numT  = (x4 - x3)*(y1 - y3) - (y4 - y3)*(x1 - x3);
			double numS  = (x2 - x1)*(y1 - y3) - (y2 - y1)*(x1 - x3);

			if (denom == 0.0 && numT == 0.0 && numS == 0.0) {
				// coincident lines?! this is bad.
				log.error("Vector coincident with wall line!");
			} else if (denom == 0.0) {
				// parallel! -- no possibility of intersection.
			} else {
				double parT = numT / denom;
				double parS = numS / denom;

				if (parT >= 0.0 && parT <= 1.0 && parS >= 0.0 && parS <= 1.0) {// intersection!
					if (parT - adjfactor < near) {
						near = parT - adjfactor;
						wallidx = j;
					}
				}
				//else no intersection
			}
		}

		if (wallidx == -1) {
			return vector; // no intersection, no adjustments.
		}

		// At the end of this loop, we have the shortest adjustment that satisfies all bounds.

		if (near < 0.0) {
			near = 0.0;
		}

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
	public double[] fixMoveV(double x, double y, double[] vector) {
		SimpleLine fixed = fixMove(new SimpleLine(x, y, x + vector[0], y + vector[1]));

		return new double[] {fixed.x2(), fixed.y2()};
	}

	/**
	 * Returns how close to closest path point this point is (closest straightline distance).
	 *
	 * @param	x1	The point in X coords to compare against
	 * @param	y1	The point in Y coords to compare against
	 *
	 * @return		The distance, or {@link Double.MAX_VALUE} if nothing nearby.
	 */
	public double distToPath(double x1, double y1) {
		double near = Double.MAX_VALUE;

		if (!intersectsWall(x1, y1, pathLines[0].x1(), pathLines[0].y1() ) ) {
			near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );
		}

		for (int j = 0; j < pathLines.length; j++) {
			if (!intersectsWall(x1, y1, pathLines[j].x2(), pathLines[j].y2() ) ) {
				double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );

				if ( dis < near ) {
					near = dis;
				}
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
	public double distToPathIdx(double x1, double y1) {
		int idx = -1;
		double near = Double.MAX_VALUE;

		if (!intersectsWall(x1, y1, pathLines[0].x1(), pathLines[0].y1() ) ) {
			near = Math.sqrt( Math.pow( pathLines[0].x1() - x1, 2.0) + Math.pow( pathLines[0].y1() - y1, 2.0 ) );
			idx = 0;
		}

		for (int j = 0; j < pathLines.length; j++) {
			if (!intersectsWall(x1, y1, pathLines[j].x2(), pathLines[j].y2() ) ) {
				double dis = Math.sqrt( Math.pow( pathLines[j].x2() - x1, 2.0) + Math.pow( pathLines[j].y2() - y1, 2.0 ) );

				if ( dis < near ) {
					near = dis;
					idx = j + 1;
				}
			}
		}

		if (idx < 0) {
			near = 0.0;
		} else {
			near = (double) idx / pathLines.length; // simple relative progress. Could add some to indicate relative progress within the closest path segment.
		}

		return near;
	}

	/**
	 * Returns the relative path progress ( a number in [0.0, 1.0] )
	 *
	 * @param	x1	The point's X coord
	 * @param	y1	The point's Y coord
	 * @return		The relative progress, where 0.0 is no progress, 1.0 is at end.
	 */
	public double relativePathProgress(double x1, double y1) {
		// Basically, if the closest segment is the last segment, we also need to be very close to that segment to qualify as 1.0
		// otherwise, we approach 0.0

		double dpi = distToPathIdx(x1, y1);

		double dtp = distToPath(x1, y1);

		if (dtp > pathFarThreshold) dtp = pathFarThreshold;
		if (dtp < pathNearThreshold) dtp = pathNearThreshold;

		double pet = 1.0 - (dtp - pathNearThreshold) / (pathFarThreshold - pathNearThreshold);

		return dpi * pet;
	}

	/**
	 * A utility method used to draw maps as string (for debugging).
	 *
	 * @param	x		"Player" X coord location
	 * @param   y		"Player" Y coord location
	 * @param	vx		"Player" X velocity
	 * @param	vy		"Player" Y velocity
	 * @param	unit	Unit of distance
	 *
	 * @return			A string representation of the map
	 */
	public String drawMap(double x, double y, double vx, double vy, double unit) {
		StringBuffer mapout = new StringBuffer();

		double[] exts = this.mapExtent();

		for (double j = exts[3]; j >= exts[2]; j -= unit) {
			for (double i = exts[0]; i <= exts[1]; i += unit) {
				// draw player
				if ( Math.sqrt( Math.pow(i - x, 2.0) + Math.pow(j - y, 2.0) ) <= unit / 1.414214) { // within sqrt(2) unit distance
					mapout.append("P");
				} else if (Line2D.ptSegDist( x, y, x-vx, y-vy, i, j) <= unit / 2.0) { // trailing vector
					mapout.append("o");
				} else {
					boolean taken = false;

					// loop through walls, see if a wall occupies this space.
					for (SimpleLine wall : wallLines) {
						if ( Line2D.ptSegDist(wall.x1(), wall.y1(), wall.x2(), wall.y2(), i, j) <= unit / 2.0 ) {
							mapout.append("#");
							taken = true;
							break;
						}
					}

					if (!taken) {
						for (SimpleLine path : pathLines) {
							if ( Line2D.ptSegDist(path.x1(), path.y1(), path.x2(), path.y2(), i, j) <= unit / 2.0 ) {
								mapout.append("+");
								taken = true;
								break;
							}
						}
					}

					if (!taken) {
						mapout.append(" ");
					}
				}
			}

			mapout.append("\n");
		}

		return mapout.toString();
	}

	/* STATIC UTILITY METHODS */

	/**
	 * This function is used to compute the "fitness" of motion along the map optimal path,
	 * after all steps complete.
	 *
	 * We only call when we've reached the limit. Otherwise no decay!
	 *
	 * @param	progressFitness	The current progress
	 * @param	simLengthCap	Simulation length cap
	 * @return					Double value of the current fitness
	 */
	private static double computeStepFitness(double progressFitness, double simLengthCap) {
		double b = 10.0 / simLengthCap;

		double x = progressFitness * simLengthCap;

		// now use a heavily modified sigmoid type function
		double sig = 2.0 / (1.0 + Math.exp( (-(x - simLengthCap)) * b) );

		if (sig > 1.0) {
			return 1.0; // we are making good progress (at least in step with the simulation's progress.
		} else {
			return sig; // we are not making good enough progress.
		}
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
	public static double[] normalizeSight(double[] distances, double[] sightranges, double maxvalue, double minvalue) {
		if (distances.length != sightranges.length) {
			return null;
		}

		double[] normalized = new double[distances.length];

		for (int i = 0; i < distances.length; i++) {
			if (distances[i] > sightranges[i]) {// nothing within range.
				normalized[i] = maxvalue;
			} else {
				normalized[i] = minvalue + (maxvalue - minvalue) * (distances[i] / sightranges[i]); // simple linear interpolation!
			}
		}

		return normalized;
	}
}
