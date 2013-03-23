/*
	Programmer: Daniel J. Boston
	Date: May 7, 2007
	Class: CS 370

	Activation function plug-in for Neurons. Based on functions in the lecture notes.

*/
public interface ActivationFunction
{
	public double activate (double[] _in, Neuron[] _n, double[] _w, double _limit);
}

/*
	Modified Sigmoid -- my own invention, expands sigmoid between -1 and 1.
*/
class AF_ModifiedSigmoid implements ActivationFunction
{
	public static AF_ModifiedSigmoid Default = new AF_ModifiedSigmoid();

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

/*
	Hyperbolic Tangent -- as defined in the notes, with default from notes as well.
*/
class AF_Tanh implements ActivationFunction
{
	public static AF_Tanh Default = new AF_Tanh(1.716,0.667);

	double a;
	double b;

	public AF_Tanh(double _a, double _b)
	{
		a = _a;
		b = _b;
	}

	public AF_Tanh()
	{
		a = 1.716;
		b = 0.667;
	}

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

/*
	Sigmoid -- squeeze output into 0 to 1.
*/
class AF_Sigmoid implements ActivationFunction
{
	public static AF_Sigmoid Default = new AF_Sigmoid();

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
	Either 1 or 0
*/
class AF_Step implements ActivationFunction
{
	public static AF_Step Default = new AF_Step();

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
	Either -1 to 1
*/
class AF_Sign implements ActivationFunction
{
	public static AF_Sign Default = new AF_Sign();

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
	Linear -- straight map of input to output.
*/
class AF_Linear implements ActivationFunction
{
	public static AF_Linear Default = new AF_Linear();

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