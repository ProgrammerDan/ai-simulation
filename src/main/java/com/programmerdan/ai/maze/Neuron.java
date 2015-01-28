package com.programmerdan.ai.maze;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *   Original release
 * @version 1.01 December 14, 2013
 *   Refining comments, restructing file, and cleaning up.
 *
 * @see {@link NeuralNetwork} for how these Neurons link together
 * @see {@link ActivationFunction} for a listing of activation functions usable by these Neurons.
 */
public class Neuron {
	private final Logger log = LoggerFactory.getLogger(Neuron.class);

	/**
	 * List of references to input Neurons. Basically, each Neuron has a collection of Neurons that feed into
	 * it, and collectively form the activation input.
	 */
	private Neuron[] inList;
	/**
	 * Each input Neuron has an associated weight, determining how strongly the output of the Neuron impacts
	 * the activation function of this Neuron.
	 */
	private double[] inListWeight;

	/**
	 * This Neuron may be connected to many other Neurons. Note that with this construction of
	 * having references both forwards and backwards, a well connected set of Neurons functions like
	 * a doubly-linked list, and all associated traversal properties and reconstruction properties apply.
	 */
	private Neuron[] outList;

	/**
	 * This is the maximum allowed number of inputs.
	 */
	private int inputs;

	/**
	 * This is the maximum allowed number of outputs.
	 */
	private int outputs;

	/**
	 * The set input values of this Neuron as of the last update.
	 */
	private double[] inputValue;
	/**
	 * The computed output of this Neuron (post-activation) as of last update.
	 */
	private double outputValue;

	/**
	 * This is the current number of connected input Neurons.
	 */
	private int inCount;
	/**
	 * This is the current number of connected output Neurons.
	 */
	private int outCount;

	/**
	 * Learning rate -- controls how strongly activation reinforces the input weight of a Neuron.
	 */
	private double alpha;
	/**
	 * Forgetting factor -- controls how quickly input weights decay, relative to strenght of activation.
	 */
	private double phi;

	/**
	 * Activation Threshold/Level -- controls what is considered activation. Depending on the activation
	 * function, might not strictly determine activation, but will always be part of the computation.
	 */
	private double theta;

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
	 * @param	inputs		the number of inputs this Neuron will accept.
	 * @param	outputs	the number of outputs this Neuron will accept.
	 * @param	alpha		the learning factor
	 * @param	phi		the forgetting factor
	 * @param	theta		the activation threshold
	 * @param	act		the {@link ActivationFunction}
	 */
	public Neuron(int inputs, int outputs, double alpha, double phi, double theta, ActivationFunction act) {
		this.inputs = inputs;
		this.outputs = outputs;

		if (this.inputs > 0) {
			this.inList = new Neuron[this.inputs];
			this.inListWeight = new double[this.inputs];
			this.inputValue = new double[this.inputs];
		}

		if (this.outputs > 0) {
			this.outList = new Neuron[this.outputs];
		}

		this.outputValue = 0.0;

		this.inCount = 0;
		this.outCount = 0;

		this.alpha = alpha;
		this.phi = phi;
		this.theta = theta;

		this.activator = act;

		log.debug("Neuron created with {} inputs, {} outputs, learning {}, forgetting {}, and activation threshold {}",
				new Object[] {this.inputs, this.outputs, this.alpha, this.phi, this.theta} );
	}

	/**
	 * Introspection function, allows a monitor to see what the current input weights of this Neuron are.
	 *
	 * @return	the array of doubles representing the current weights applied to each input.
	 */
	public double[] getInWeights() {
		return inListWeight;
	}

	/**
	 * Introspection function, fills the given double array with the current outputs of this neuron's inputs.
	 */
	public double[] getInValues(double[] inValues) {
		for (int wC = 0; wC < inList.length; wC++) {
			inValues[wC] = (inList[wC] == null) ? 0.0d : inList[wC].getOutput();
		}
		return inValues;
	}

	/**
	 * Introspective function, allows a monitor to see what the current activation threshold is.
	 *
	 * @return	the double value representing this neuron's current activation threshold.
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Returns the current output value.
	 *
	 * @return the double value representing this neuron's current output strength.
	 */
	public double getOutput() {
		return outputValue;
	}

	/**
	 * Sets the output value of this neuron. Used by subclasses.
	 *
	 * @param	output	the new output value.
	 */
	protected void setOutput(double output) {
		outputValue = output;
		log.debug("Neuron {} update output to {}", this.hashCode(), outputValue);
	}

	/**
	 * Add an input to this Neuron. You can add inputs up until the maximum inputs you specified in constructing the neuron.
	 *  Each input must have a weight.
	 *
	 * @param	in		the Neuron to add as an input
	 * @param	weight	the weight of this input
	 * @return			True if the Neuron was added, False if the input set is full.
	 */
	public boolean addInput(Neuron in, double weight) {
		if (inCount < inputs) {
			inList[inCount] = in;
			inListWeight[inCount] = weight;
			inCount++;
			log.debug("Neuron {} linked Neuron {} as input with weight {}", new Object[] {this.hashCode(), in.hashCode(), weight} );
			return true;
		} else {
			log.warn("Neuron {} FAILED to link Neuron {} as input with weight {}", new Object[] {this.hashCode(), in.hashCode(), weight} );
			return false;
		}
	}

	/**
	 * Adds an output to this Neuron. You can add Neurons only until the maximum number of outputs you specified in constructing the neuron.
	 *
	 * @param	out	the Neuron to add as an output
	 * @return			True if the Neuron was added, False if the output set is full.
	 */
	public boolean addOutput(Neuron out) {
		if (outCount < outputs) {
			outList[outCount] = out;
			outCount++;
			log.debug("Neuron {} linked Neuron {} as output", this.hashCode(), out.hashCode());
			return true;
		} else {
			log.debug("Neuron {} FAILED to link Neuron {} as output", this.hashCode(), out.hashCode());
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
	public void step() {
		log.debug("Neuron {} step function called", this.hashCode() );

		setOutput( activator.activate(inputValue, inList, inListWeight, theta) );

		learn();
	}

	/**
	 * Learns, based on the Hebbian model of learning. The weight update is based on the output
	 *   values of the input list against the output of this neuron. Note that Hebbian learning
	 *   is pretty complex, so I won't give a detailed breakdown here. Basically, for each
	 *   input and using the current output, the input weights are adjusted.
	 * Learning is applied first, then forgetting, for each input weight in order.
	 */
	private void learn() {
		log.debug("Neuron {} learning function called", this.hashCode() );

		double nextWeight = 0.0;

		for (int wC = 0; wC < inCount; wC++) {
			nextWeight = 0.0;
			if (inList[wC] != null) {
				nextWeight = alpha * inList[wC].getOutput() * outputValue; // alpha * xi * yj (learning)
				nextWeight -= phi * outputValue * inListWeight[wC]; // phi * yj * wij (forgetting)
			}

			log.debug("Neuron {} weight delta is {}", this.hashCode(), nextWeight);

			// Accumulation is mexican hatted, to a degree.
			// if accumulation is the same sign as weight, slow it down as it nears maximum.
			//  if opposite sign, apply as given.

			if (Math.signum(inListWeight[wC]) == Math.signum(nextWeight)) {
				nextWeight = nextWeight * Math.pow( (1.0 + Math.cos( (inListWeight[wC] * Math.PI) / MAXWEIGHT) ) / 2.0 , 0.75);
			}

			inListWeight[wC] += nextWeight; // accumulate.

			if (Math.abs(inListWeight[wC]) > MAXWEIGHT) {
				log.warn("Neuron {} input weight for input {} is too high at {}", new Object[] { this.hashCode(), wC, inListWeight[wC] } );
			} else {
				log.debug("Neuron {} input weight for input {} is adjusted to {}", new Object[] { this.hashCode(), wC, inListWeight[wC] } );
			}
		}
	}

	/**
	 * Output the various values of this neuron as a list of weights followed by the activation threshold.
	 *
	 * @return	The input weights and threshold.
	 */
	public String toString() {
		StringBuffer ret = new StringBuffer("[");
		ret.append(this.hashCode());
		ret.append("]<");

		for (int sC = 0; sC < inCount; sC++) {
			ret.append(inListWeight[sC]);
			if (sC < inCount - 1) {
				ret.append(",");
			}
		}

		ret.append(">[");
		ret.append(theta);
		ret.append("]");

		return ret.toString();
	}

}