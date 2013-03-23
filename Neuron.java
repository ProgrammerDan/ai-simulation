import java.io.*;

/*
	Programmer: Daniel J. Boston
	Date: May 7, 2007
	Class: CS 370

	Building the Neuron model took a bit of thought. Basically, the Neuron includes the "dendrites" and "axons" and "somas" all on one
	 object, as other models made traversal WAY too difficult. Inputs must have attached weights. Outputs all get the same value applied
	 to them, based on the ActivationFunction.
*/
public class Neuron
{
	private Neuron[] inList;
	private double[]  inListWeight;
	private Neuron[] outList;

	private int inputs;
	private int outputs;

	private double[] inputValue;
	private double outputValue;

	private int inCount;
	private int outCount;

	private double alpha; // learning rate
	private double phi; // forgetting factor

	private double theta; // activation level

	private boolean debug; // debugging this node?
	private PrintWriter debugOut;

	private ActivationFunction activator;

	public static double MAXWEIGHT = 10;

	// Setup a Neuron with the specified number of inputs, outpus, and the specified learning, forgetting, and threshold, with the
	//  passed ActivationFunction.
	public Neuron(int _inputs, int _outputs, double _alpha, double _phi, double _theta, ActivationFunction _act)
	{
		inputs = _inputs;
		outputs = _outputs;

		if (inputs > 0)
		{
			inList = new Neuron[inputs];
			inListWeight = new double[inputs];
			inputValue = new double[inputs];
		}

		if (outputs > 0)
			outList = new Neuron[outputs];

		outputValue = 0.0;

		inCount = 0;
		outCount = 0;

		alpha = _alpha;
		phi = _phi;
		theta = _theta;

		activator = _act;
	}

	// Returns the base output value of this neuron.
	public double getOutput()
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].getOutput() = " + String.valueOf(outputValue));
		return outputValue;
	}

	// Sets the output value of this neuron. Questionable to call from outside.
	void setOutput(double _output)
	{
		outputValue = _output;
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].setOutput(" + String.valueOf(_output) + ")");
	}

	// Add an input to this neuron. You can add inputs up until the maximum inputs you specified in constructing the neuron.
	//  Each input as a weight.
	public boolean addInput(Neuron _in, double _weight)
	{
		if (debug) debugOut.print("[" + String.valueOf(this.hashCode()) + "].addInput([" + String.valueOf(_in.hashCode()) + "]," + String.valueOf(_weight) + ") = ");

		if (inCount < inputs)
		{
			inList[inCount] = _in;
			inListWeight[inCount] = _weight;
			inCount++;
			if (debug) debugOut.println("success");
			return true;
		}
		else
		{
			if (debug) debugOut.println("failure");
			return false;
		}
	}

	// Adds an output to the Neuron. Again, can add only until the maximum number of outputs you specified in constructing the neuron.
	public boolean addOutput(Neuron _out)
	{
		if (debug) debugOut.print("[" + String.valueOf(this.hashCode()) + "].addOutput([" + String.valueOf(_out.hashCode()) + "]) = ");
		if (outCount < outputs)
		{
			outList[outCount] = _out;
			outCount++;
			if (debug) debugOut.println("success");
			return true;
		}
		else
		{
			if (debug) debugOut.println("failure");
			return false;
		}
	}

	// Step this Neuron by running the neuron's values against the activation function.
	public void step()
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].step()");

		outputValue = activator.activate(inputValue, inList, inListWeight, theta);

		if (debug) debugOut.println("  outputValue: " + String.valueOf(outputValue));

		learn();
	}

	// Learns, based on the Hebbian model of learning. The weight update is based on the output values of the input list
	//  against the output of this neuron.
	private void learn()
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].learn()");

		double nextWeight = 0.0;

		for (int wC = 0; wC < inCount; wC++)
		{
			if (debug) debugOut.print("  " + String.valueOf(wC) + " - weight: " + String.valueOf(inListWeight[wC]));

			nextWeight = 0.0;
			if (inList[wC] != null)
			{
				nextWeight = alpha * inList[wC].getOutput() * outputValue; // alpha * xi * yj (learning)
				nextWeight -= phi * outputValue * inListWeight[wC]; // phi * yj * wij (forgetting)
			}

			if (debug) debugOut.print(" - delta: " + String.valueOf(nextWeight));

			// Accumulation is mexican hatted, to a degree.
			// if accumulation is the same sign as weight, slow it down as it nears maximum.
			//  if opposite sign, apply as given.

			if (Math.signum(inListWeight[wC]) == Math.signum(nextWeight))
				nextWeight = nextWeight * Math.pow( (1.0 + Math.cos( (inListWeight[wC] * Math.PI) / MAXWEIGHT) ) / 2.0 , 0.75);

			inListWeight[wC] += nextWeight; // accumulate.

			if (Math.abs(inListWeight[wC]) > MAXWEIGHT)
				System.out.println("Weight out of line -- nextWeight: " + nextWeight);

			if (debug) debugOut.println(" - final: " + String.valueOf(inListWeight[wC]));
		}
	}

	// Turn on the debug output for this neuron.
	public void activateDebug(PrintWriter _debout)
	{
		debugOut = _debout;
		debug = true;
	}

	// Turn off the debug for this neuron.
	public void deactivateDebug()
	{
		debugOut = null;
		debug = false;
	}

	// Output the various values of this neuron as a list of weights followed by the activation threshold.
	public String toString()
	{
		StringBuffer ret = new StringBuffer("<");

		for (int sC = 0; sC < inCount; sC++)
		{
			ret.append(inListWeight[sC]);
			if (sC < inCount - 1)
			{
				ret.append(",");
			}
		}

		ret.append(">[");
		ret.append(theta);
		ret.append("]");

		return ret.toString();
	}

}

// This is a dummy class to handle straight passthrough and exposure of input to attached neurons.
class NetworkInput extends Neuron
{
	// allows direct input into the network.

	public NetworkInput()
	{
		super(0,1,0,0, 0.0, AF_Linear.Default);

		setOutput(0.0);
	}

	public void setValue(double _output)
	{
		setOutput(_output);
	}

}