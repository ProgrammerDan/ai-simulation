package com.programmerdan.ai.maze;

/**
 * A simple point class to encapsulate position.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 2007
 */
public class Position
{
	//TODO: Evaluate why these are protected instead of private.
	double x;
	double y;

	/**
	 * Creates a new Position with the given X and Y coords.
	 *
	 * @param	_x	The X coord to use.
	 * @param	_y	The Y coord to use.
	 *
	 * TODO: cleanup C style params
	 */
	public Position(double _x, double _y)
	{
		x = _x;
		y = _y;
	}

	/**
	 * Accessor for the position's X coord.
	 *
	 * @return	the X coord of this position.
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * Accessor for the position's Y coord.
	 *
	 * @return	the Y coord of this position.
	 */
	public double getY()
	{
		return y;
	}
}