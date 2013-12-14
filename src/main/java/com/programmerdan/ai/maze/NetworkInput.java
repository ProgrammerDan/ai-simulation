package com.programmerdan.ai.maze;

/**
 * This is a simple class to handle straight passthrough and exposure of input to attached neurons.
 *   Allows external players to set inputs on Neurons, such as based on environment interaction or
 *   training patterns.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 *
 * @version 1.00
 *   Initial version.
 */
class NetworkInput extends Neuron
{
	/**
	 * Construct a new input, with a linear activation function and default of 0.0 output.
	 */
	public NetworkInput()
	{
		super(0,1,0,0, 0.0, AF_Linear.Default);

		super.setOutput(0.0);
	}

	/**
	 * Sets the output value of this Neuron, or, sets the "input" of this passthrough.
	 *
	 * @param	output	The new "input" value.
	 */
	public void setValue(double output)
	{
		super.setOutput(output);
	}

}