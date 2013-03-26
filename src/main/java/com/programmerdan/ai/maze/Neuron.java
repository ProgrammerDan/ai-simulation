import java.io.*;

/**
 * This Neuron class is a bit messy still, but is the result of lots of careful thought involving the construction
 *   and functioning of a neural network. The Neuron includes the "dendrites" and "axons" and "somas" (parts of your
 *   brain) all in one object, as other models made traversal (and activation propogation) WAY too difficult.
 * Inputs must have attached weights.
 * Outputs all get the same valued applied to them, based on the ActivationFunction. Basically, the neuron "activates"
 *   at a particular value, and that value is sent to all Neurons attached to the output. It's up to each connected
 *   neuron to weight that value based on their input weights.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 *
 * @see {@link NeuralNetwork} for how these Neurons link together
 * @see {@link ActivationFunction} for a listing of activation functions usable by these Neurons.
 */
public class Neuron
{
	private Neuron[] inList;
	private double[]  inListWeight;
	/**
	 * Introspection function, allows a monitor to see what the current input weights of this Neuron are.
	 *
	 * @return	the array of doubles representing the current weights applied to each input.
	 */
	public double[] getInWeights() {
		return inListWeights;
	}
	private Neuron[] outList;

	private int inputs;
	private int outputs;

	private double[] inputValue;
	private double outputValue;
	/**
	 * Introspective function, allows a monitor to see what the current output value is.
	 *
	 * @return	the double value representing this neuron's current output strength.
	 */
	public double getOutputValue() {
		return outputValue;
	};

	private int inCount;
	private int outCount;

	/** Learning rate */
	private double alpha;
	/** Forgetting factor */
	private double phi;

	/** Activation Threshold/Level */
	private double theta;
	/**
	 * Introspective function, allows a monitor to see what the current activation threshold is.
	 *
	 * @return	the double value representing this neuron's current activation threshold.
	 */
	public double getTheta() {
		return theta;
	}

	/** Old-style debug mode flag */
	private boolean debug;
	/** Old-style debug mode output target */
	private PrintWriter debugOut;

	// TODO: add Logger

	/**
	 * The Neuron's activation function.
	 * @see {@link ActivationFunction}
	 */
	private ActivationFunction activator;

	/**
	 * To prevent Neuron weights from growing without bound, it's typical to set a maximum weight.
	 * This static parameter is just such a maximum.
	 */
	public static double MAXWEIGHT = 10.0;

	/**
	 * Sets up a Neuron with the specified number of input "slots" (filled later), output "slots", along with other
	 *   important characteristics such as learning factor, forgetting factor, and activation threshold, along with
	 *   the {@link ActivationFunction} that will determine output weight based on inputs.
	 *
	 * @param	_inputs		the number of inputs this Neuron will accept.
	 * @param	_outputs	the number of outputs this Neuron will accept.
	 * @param	_alpha		the learning factor
	 * @param	_phi		the forgetting factor
	 * @param	_theta		the activation threshold
	 * @param	_act		the {@link ActivationFunction}
	 *
	 * TODO: remove C style params, evaluate if statically sized inputs/outputs makes sense still.
	 */
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

	/**
	 * Duplicate? Returns the current output value.
	 *
	 * @return	the output value.
	 */
	public double getOutput()
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].getOutput() = " + String.valueOf(outputValue));
		return outputValue;
	}

	/**
	 * Sets the output value of this neuron. Questionable to call from outside.
	 *
	 * @param	_output	the new output value. Violates activation function contract.
	 */
	void setOutput(double _output)
	{
		outputValue = _output;
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].setOutput(" + String.valueOf(_output) + ")");
	}

	/**
	 * Add an input to this neuron. You can add inputs up until the maximum inputs you specified in constructing the neuron.
	 *  Each input must have a weight.
	 *
	 * @param	_in		the Neuron to add as an input
	 * @param	_weight	the weight of this input
	 * @return			True if the Neuron was added, False if the input set is full.
	 */
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

	/**
	 * Adds an output to the Neuron. Again, can add only until the maximum number of outputs you specified in constructing the neuron.
	 *
	 * @param	_out	the Neuron to add as an output
	 * @return			True if the Neuron was added, False if the output set is full.
	 */
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

	/**
	 * Step this Neuron by running the neuron's values against the activation function.
	 *   Uses the input values, input list of Neurons, input list of weights and activation
	 *   threshold. It sends those values to the activation function.
	 * After stepping, calls {@link learn()}.
	 *
	 * @see {@link ActivationFunction.activate}
	 */
	public void step()
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].step()");

		outputValue = activator.activate(inputValue, inList, inListWeight, theta);

		if (debug) debugOut.println("  outputValue: " + String.valueOf(outputValue));

		learn();
	}

	/**
	 * Learns, based on the Hebbian model of learning. The weight update is based on the output
	 *   values of the input list against the output of this neuron. Note that Hebbian learning
	 *   is pretty complex, so I won't give a detailed breakdown here. Basically, for each
	 *   input and using the current output, the input weights are adjusted.
	 * Learning is applied first, then forgetting, for each input weight in order.
	 */
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

	/**
	 * Turn on the debug output for this neuron.
	 *
	 * @param	_debout	The output {@link PrintWriter} for debug statements.
	 * TODO: replace with logger and logging level throughout.
	 */
	public void activateDebug(PrintWriter _debout)
	{
		debugOut = _debout;
		debug = true;
	}

	/**
	 * Turn off debugging for this neuron.
	 */
	public void deactivateDebug()
	{
		debugOut = null;
		debug = false;
	}

	/**
	 * Output the various values of this neuron as a list of weights followed by the activation threshold.
	 *
	 * @return	The input weights and threshold.
	 */
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

/**
 * This is a dummy class to handle straight passthrough and exposure of input to attached neurons.
 *   Allows external players to set inputs on Neurons, such as based on environment interaction or
 *   training patterns.
 */
class NetworkInput extends Neuron
{
	/**
	 * Construct a new input, with a linear activation function and default of 0.0 output.
	 */
	public NetworkInput()
	{
		super(0,1,0,0, 0.0, AF_Linear.Default);

		setOutput(0.0);
	}

	/**
	 * Sets the output value of this Neuron, or, sets the "input" of this passthrough.
	 *
	 * @param	_output	The new "input" value.
	 */
	public void setValue(double _output)
	{
		setOutput(_output);
	}

}