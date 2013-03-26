package com.programmerdan.ai.maze;

/**
 * A simple line, just the four data points and accessors/mutators.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 2007
 *
 * TODO: cleanup getters
 */
public class SimpleLine
{
	private double x1 = 0.0;
	private double y1 = 0.0;
	private double x2 = 0.0;
	private double y2 = 0.0;

	/**
	 * Get the first X coord.
	 * @return 	the first X coord.
	 */
	public double x1() {
		return x1;
	}
	/**
	 * Set the first X coord.
	 * @param	x1	The first X coord.
	 */
	public void setX1( double x1 ) {
		this.x1 = x1;
	}
	/**
	 * Get the first Y coord.
	 * @return	the first Y coord.
	 */
	public double y1() {
		return y1;
	}
	/**
	 * Set the first Y coord.
	 * @param	y1	The first Y coord.
	 */
	public void setY1( double y1 ) {
		this.y1 = y1;
	}
	/**
	 * Get the second X coord.
	 * @return	the second X coord.
	 */
	public double x2() {
		return x2;
	}
	/**
	 * Set the second X coord.
	 * @param	x2	The second X coord.
	 */
	public void setX2( double x2 ) {
		this.x2 = x2;
	}
	/**
	 * Get the second Y coord.
	 * @return	the second Y coord.
	 */
	public double y2() {
		return y2;
	}
	/**
	 * Set the second Y coord.
	 * @param	y2	The second Y coord.
	 */
	public void setY2( double y2 ) {
		this.y2 = y2;
	}

	/**
	 * Creates a new simple line.
	 *
	 * @param	x1	The first X coord.
	 * @param	y1	The first Y coord.
	 * @param	x2	The second X coord.
	 * @param	y2	The second Y coord.
	 */
	public SimpleLine(double x1, double y1, double x2, double y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	/**
	 * Creates a zeroed line (all coords are initialized to 0.0.
	 */
	public SimpleLine()
	{
		x1 = 0.0;
		x2 = 0.0;
		y1 = 0.0;
		y2 = 0.0;
	}
}
