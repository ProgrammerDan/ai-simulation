package com.programmerdan.ai.maze;

/**
 * Activation function plug-in interface for Neurons. Based on standard activation function
 *   types.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 * TODO: Move all the implementations into their own class files, clean up static instances
 *         and add logging framework.
 */
public interface ActivationFunction
{
	/**
	 * Interface contract method for activation.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate (double[] _in, Neuron[] _n, double[] _w, double _limit);
}

/**
 * Modified Sigmoid -- my own invention, expands sigmoid between -1 and 1.
 *   A sigmoid is not a hard activation function, meaning that it activates
 *   variably depending on the inputs and weight.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_ModifiedSigmoid implements ActivationFunction
{
	/**
	 * Static instance of default modified sigmoid.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_ModifiedSigmoid Default = new AF_ModifiedSigmoid();

	/**
	 * Modified sigmoid. All the weighted inputs are summed, then applied to a sigmoid function with 2 in the numerator.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
				//if (debug) debugOut.println("  " + String.valueOf(iC) + ": " + String.valueOf(_in[iC]));
			}
		}

		X -= _limit; // subtract the activation level.

		//if (debug) debugOut.println("  X: " + String.valueOf(X));

		double ret = (2.0 / (1.0 + Math.exp(-X) )) - 1.0; // modified sigmoid activation function (range of output: -1 to 1)

		return ret;
	}
}

/**
 * Hyperbolic Tangent -- the definition here appears to stray a bit from the notes I've found online;
 *   this implementation was based on some class notes, and is a configurable TanH -- the defaults should
 *   result in an activation function output between [-1.0, 1.0].
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_Tanh implements ActivationFunction
{
	/**
	 * Static instance of default Hyperbolic Tangent.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_Tanh Default = new AF_Tanh(1.716,0.667);

	/** TODO: Why are these protected? */
	double a;
	double b;

	/**
	 * Customizable constructor.
	 *
	 * @param	_a	Parameter A.
	 * @param	_b	Paramater B.
	 */
	public AF_Tanh(double _a, double _b)
	{
		a = _a;
		b = _b;
	}

	/**
	 * Default constructor, sets parameter A to 1.716, and B to 0.667.
	 */
	public AF_Tanh()
	{
		a = 1.716;
		b = 0.667;
	}

	/**
	 * Hyperbolic Tangent. All the weighted inputs are summed, then used in a hyperbolic tangent function, after
	 *   adjustment for activation threshold.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
			}
		}

		X -= _limit; // subtract the activation level.

		double ret = ((2.0 * a) / (1f + Math.exp( -X * b) ) ) - a ;

		return ret;
	}
}

/**
 * Sigmoid Activation Function -- squeezes output between 0 to 1, using
 *   a sigmoid function.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_Sigmoid implements ActivationFunction
{
	/**
	 * Static instance of default Sigmoid.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_Sigmoid Default = new AF_Sigmoid();

	/**
	 * Sigmoid. All the weighted inputs are summed, then applied to a sigmoid function with 1 in the numerator.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
			}
		}

		X -= _limit; // subtract the activation level.

		double ret = (1.0 / (1.0 + Math.exp( -X) ));

		return ret;
	}
}

/*
 * Either 1 or 0, a "step" activation function, this is either ON or OFF, depending
 * on if the activation threshold is exceeded.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_Step implements ActivationFunction
{
	/**
	 * Static instance of default Step.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_Step Default = new AF_Step();

	/**
	 * Step. All the weighted inputs are summed, then the activation threshold is tested. If above, output
	 *   is 1, else, 0.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
			}
		}

		X -= _limit; // subtract the activation level.

		double ret = (X >= 0.0) ? 1.0 : 0.0;

		return ret;
	}
}

/*
 * Either -1 to 1 on output, a "Sign" function.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_Sign implements ActivationFunction
{
	/**
	 * Static instance of default Sign.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_Sign Default = new AF_Sign();

	/**
	 * Sign. All the weighted inputs are summed, then the activation threshold is subtracted.
	 *   If result below 0, -1 is the output, otherwise 1.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
			}
		}

		X -= _limit; // subtract the activation level.

		double ret = (X >= 0.0) ? 1.0 : -1.0;

		return ret;
	}
}

/*
 * Linear -- straight map of input to output, adjusted for activation threshold.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 */
class AF_Linear implements ActivationFunction
{
	/**
	 * Static instance of default Linear.
	 * TODO: Get rid of this, or make activate thread safe.
	 */
	public static AF_Linear Default = new AF_Linear();

	/**
	 * Linear activation. The inputs are weight adjusted and summed, the activation
	 *   threshold is subtracted, and this result is returned.
	 *
	 * @param	_in		The array to store the weighted inputs, formed during activation calculation.
	 * @param	_n		The array of Neurons, whose outputs form the inputs for this activation calculation.
	 * @param	_w		The array of weights to apply to the outputs of the Neurons.
	 * @param	_limit	The activation threshold level.
	 * @return			The output level.
	 */
	public double activate(double[] _in, Neuron[] _n, double[] _w, double _limit)
	{
		double X = 0.0; // summation

		// calculate inputs to outputs, re-evaluate weights
		for (int iC = 0; iC < _n.length; iC++)
		{
			if (_n[iC] != null)
			{
				_in[iC] = _n[iC].getOutput() * _w[iC];
				X += _in[iC]; // summation.
			}
		}

		X -= _limit; // subtract the activation level.

		double ret = X;

		return ret;
	}
}