package com.programmerdan.ai.maze;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Neural Network builder/holder class.
 * This is meant to encompass a fairly classic neural network, which consists of an input layer, zero or more hidden layers, and
 *   one output layer. The output layer is either the input layer (a single layer network), the last hidden layer (no specific
 *   output layer), or the specified output layer.
 *
 * Note that this is a very simple neural network, where all the outputs of each layer feed every neuron in the subsequent layer.
 *   Inputs propogate, then, from the "top" at the input layer down through to the output layer, passing through each successive
 *   layer in turn.
 * More complex neural networks would have a more diversified structure, where propogation was based on a more tree-like organizational
 *   structure, although with this simplicity there is opportunity. Given the regularity of the structure, more complex organizations
 *   can be simulated by zeroing selective inputs to specific neurons in subsequent layers.
 *
 * An additional simplification is that learning and forgetting are captured as network-wide parameters, so the entire network
 *   will learn (and forget) at a consistent rate. More advanced implementations of Neural Networks may prefer to blend the
 *   learning and forgetting factors, allowing portions of the network to learn and forget slowly, while other portions learn and
 *   forget more quickly, or whatever blend is preferred for the problem at hand. This remains a topic for future implementation.
 *
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 *
 * @version 1.0 May 7, 2007
 *   Initial version.
 * @version 1.01 December 14, 2013
 *   Revised version, improved comments and the like.
 *
 * @see {@link Neuron}
 */
public class NeuralNetwork
{
	/**
	 * Logger for this class.
	 */
	private final Logger log = LoggerFactory.getLogger(NeuralNetwork.class);

	/**
	 * The input layer of Neurons.
	 */
	private Neuron[] inputLayer;

	/**
	 * Each input Neuron is connected to a pass-through Neuron of type NetworkInput, which allows external values to
	 * pass into the network without violating the Neuron construction expectations.
	 * It also allows a particular input to map to several (or all) input layer Neurons with distinct weights.
	 */
	private NetworkInput[] inputHandlers;

	/**
	 * A "classic" NeuralNetwork like this class exemplifies has zero or more interal hidden layers of Neurons. They are
	 * "stored" here.
	 */
	private Neuron[][] hiddenLayers;

	/**
	 * The output layer of Neurons. The "end of the line" for the Network.
	 */
	private Neuron[] outputLayer;

	/**
	 * Configuration of network -- number of inputs.
	 */
	private int nInputs;

	/**
	 * Configuration of network -- number of hidden layers.
	 */
	private int nHidden;

	/**
	 * Configuration of network -- size of each hidden layer.
	 */
	private int sizeHidden;

	/**
	 * Configuration of network -- indicator if hidden layers exist.
	 */
	private boolean hasHidden;

	/**
	 * Configuration of network -- number of outputs.
	 */
	private int nOutputs;

	/**
	 * Network setup -- current input Neuron to set.
	 */
	private int cInput;
	/**
	 * Network setup -- current layer of hidden Neurons.
	 */
	private int cLayer;
	/**
	 * Network setup -- current Neuron in hidden layer.
	 */
	private int cHidden;
	/**
	 * Network setup -- current output Neuron to set.
	 */
	private int cOutput;

	/**
	 * Configuration of network -- Learning factor (global)
	 */
	private double alpha;
	/**
	 * Configuration of network -- Forgetting factor (global)
	 */
	private double phi;

	/**
	 * Network setup -- number of discrete components to the network, configured on creation.
	 */
	private int factorSize;

	/**
	 * Diagnostic setup -- number of discrete propagating outputs in the network, configured during creation.
	 */
	private int diagnosticSize;

	/**
	 * Debug param revealing the internals of the Neural Network (including diagnostics)
	 */
	private double[] networkFactors;

	/**
	 * This debug function returns an array of all the weights and factors in the network at the time of the call.
	 * It probably won't be useful except where in-depth knowledge of the network construction is available.
	 *
	 * @return	an array of doubles, holding all factors. Element 0 is learning, 1 is forgetting, followed
	 *			  successively by input weights and activation thresholds for each layer, starting with
	 *            input layer, then hidden layers, finally output layer.
	 **/
	public double[] getNetworkFactors()
	{
		if (networkFactors == null) {
			networkFactors = new double[factorSize + diagnosticSize];
		}
		double[] nf = networkFactors;

		nf[0] = alpha;
		nf[1] = phi;

		// inputs.
		int nfi = 2;

		for (int k = 0; k < nInputs; k ++)
		{
			nf[ nfi++ ] = inputHandlers[k].getOutput();

			double[] inweights = inputLayer[k].getInWeights(); // weights.
			for (int j = 0; j < inweights.length; j ++)
				nf[ nfi++ ] = inweights[j];

			nf[ nfi++ ] = inputLayer[k].getTheta(); // activation.

			nf[ nfi++ ] = inputLayer[k].getOutput();
		}

		for (int k = 0; k < nHidden; k ++)
		{
			for (int i = 0; i < hiddenLayers[k].length; i++)
			{
				double[] inweights = hiddenLayers[k][i].getInWeights();
				for (int j = 0; j < inweights.length; j++)
					nf[ nfi++ ] = inweights[j];

				nf[ nfi++ ] = hiddenLayers[k][i].getTheta();

				nf[ nfi++ ] = hiddenLayers[k][i].getOutput();
			}
		}

		for (int k = 0; k < nOutputs; k ++)
		{
			double[] inweights = outputLayer[k].getInWeights();
			for (int j = 0; j < inweights.length; j ++)
				nf[ nfi++ ] = inweights[j];

			nf[ nfi++ ] = outputLayer[k].getTheta();

			nf[ nfi++ ] = outputLayer[k].getOutput();
		}

		if (Math.random() < .001) {
			System.out.println("" + nf.length + ": " + nfi);
		}

		networkFactors = nf;

		return nf;
	}

	/**
	 * Returns the number of input neurons.
	 *
	 * @return Number of input neurons
	 */
	public int getNumInputs() {
		return nInputs;
	}

	/**
	 * Returns the number of hidden neuron layers.
	 *
	 * @return Number of hidden neuron layers
	 */
	public int getNumHidden() {
		return nHidden;
	}

	/**
	 * Return the number of neurons in each hidden layer.
	 *
	 * @return Number of neurons in each hidden layer
	 */
	public int getSizeHidden() {
		return sizeHidden;
	}

	/**
	 * Returns the number of output neurons.
	 *
	 * @return Number of output neurons.
	 */
	public int getNumOutputs() {
		return nOutputs;
	}

	/**
	 * Initialize a new neural network.
	 * Specifies the network characteristics, including number of input neurons, number of hidden neurons in each layer
	 *   and how many hidden layers, how many output neurons, and finally the learning and forgetting factors of the
	 *   network.
	 *
	 * @param	nInputs	Number of input neurons in the input layer. (if negative, absolute value used)
	 * @param	nHidden	Number of hidden neurons in each hidden layer. (if negative, absolute value used)
	 * @param	sizeHidden	Number of hidden layers. (if negative, absolute value used)
	 * @param	nOutputs	Number of output neurons in the output layer. (if negative, absolute value used)
	 * @param	alpha		The learning factor.
	 * @param	phi		The forgetting factor.
	 */
	public NeuralNetwork(int nInputs, int nHidden, int sizeHidden, int nOutputs, double alpha, double phi)
	{
		log.debug("Initializing a Neural Network with {} inputs, {} hidden layers of {} Neurons each, and {} outputs." +
				" Learning factor {} and forgetting factor {}.", new Object[] {nInputs, nHidden, sizeHidden, nOutputs,
				alpha, phi} );
		factorSize = 2; // alpha and phi.

		this.nInputs = (nInputs < 0) ? -nInputs: nInputs;
		this.nHidden = (nHidden < 0) ? -nHidden: nHidden;
		this.sizeHidden = (sizeHidden < 0) ? -sizeHidden: sizeHidden;
		this.nOutputs = (nOutputs < 0) ? -nOutputs: nOutputs;

		if (this.nInputs > 0)
		{
			inputLayer = new Neuron[this.nInputs];
			inputHandlers = new NetworkInput[this.nInputs];
		}

		if ((this.nHidden > 0) && (this.sizeHidden > 0))
		{
			hasHidden = true;
			hiddenLayers = new Neuron[this.nHidden][this.sizeHidden];
		}
		else
		{
			hasHidden = false;
		}

		if (this.nOutputs > 0)
		{
			outputLayer = new Neuron[this.nOutputs];
		}
		else if(hasHidden)
		{
			outputLayer = new Neuron[this.sizeHidden];
		}
		else // no hidden, and no discrete outputs
		{
			outputLayer = new Neuron[this.nInputs];
		}

		this.alpha = alpha;
		this.phi = phi;

		cInput = 0;
		cLayer = -1;
		cHidden = 0;
		cOutput = 0;
	}

	/**
	 * Adds an input to the input layer by creating a new Neuron and wiring it up to the rest of the network.
	 *
	 * @param	weight		weight between Input Handler and Input Layer (how sensitive am I to input?)
	 * @param	theta		activation weight of Input Layer (what is my threshold for input?)
	 * @param	active		activation function of Input Layer (how do I fire?)
	 * @return				True if input was successfully created and added, False otherwise.
	 * @see {@link ActivationFunction}
	 * @see {@link Neuron}
	 */
	public boolean addInput(double weight, double theta, ActivationFunction active)
	{
		log.debug("NeuralNetwork {} adding input Neuron with weight {}, activation {}, and function {}",
				new Object[] {this.hashCode(), weight, theta, active.getClass().getName() } );

		if (cLayer == -1) // defining input mode.
		{
			int idx = cInput;

			if (inputLayer[idx] == null) // TODO: If input layer is size 0, this will throw null pointer exception.
			{
				if (hasHidden) // is there at least some kind of hidden layer?
					inputLayer[idx] = new Neuron(1, sizeHidden, alpha, phi, theta, active);
				else
					inputLayer[idx] = new Neuron(1, nOutputs, alpha, phi, theta, active); // no hidden layer, simple network.

				inputHandlers[idx] = new NetworkInput();

				if ((nOutputs == 0) && (!hasHidden)) // special case, simplest NN
				{
					outputLayer[idx] = inputLayer[idx];
					log.debug("NeuralNetwork {} input {} bound to output layer (simple NN)", this.hashCode(), idx);
				}

				if (inputHandlers[idx].addOutput(inputLayer[idx]))
				{
					log.debug("NeuralNetwork {} handler {} bound to input", this.hashCode(), idx);
				}
				else
				{
					log.error("NeuralNetwork {} handler {} unbound!", this.hashCode(), idx);
					return false;
				}

				if (inputLayer[idx].addInput(inputHandlers[idx], weight))
				{
					log.debug("NeuralNetwork {} input {} bound to handler with weight {}", this.hashCode(), idx, weight);
				}
				else
				{
					log.error("NeuralNetwork {} input {} unbound!", this.hashCode(), idx);
					return false;
				}
			}
			else
			{
				log.error("NeuralNetwork {} input {} already bound?!", this.hashCode(), idx);
				return false; // fatal error?
			}

			cInput ++; // next call to setInput, work on next input.
			if (cInput >= nInputs)
			{
				cLayer = 0; // done inputs, move on to first hidden layer or output layer.
			}

			factorSize += 2; // weight, threshold.
			diagnosticSize += 2; // handler, and neuron.

			return true;
		}
		else
		{
			log.warn("NeuralNetwork {} is in wrong mode for more inputs.", this.hashCode() );
			return false;
		}
	}

	/**
	 * Add a hidden layer, with the specified application function.
	 *
	 * @param	weights	For each new layer, this weight array must be equal in size to the size of the previous layer.
	 * 						  For example, if the input layer is 5 Neurons "wide", every neuron in the first hidden layer
	 *						  should have 5 weights. If that hidden layer is 7 Neurons "wide", every neuron in the second
	 *						  hidden layer (or output layer) must have 7 weights, and so on.
	 * @param	theta		The activation threshold for this Neuron.
	 * @param	active		The activation function.
	 * @return				True if hidden Neuron was successfully added, False otherwise.
	 * @see {@link Neuron}
	 * @see {@link ActivationFunction}
	 */
	public boolean addHidden(double[] weights, double theta, ActivationFunction active) // weights.length MUST == previous layer size
	{
		log.debug("NeuralNetwork {} adding hidden Neuron with {} weights, activation {}, and function {}",
				new Object[] {this.hashCode(), weights.length, theta, active.getClass().getName() } );

		if ((cLayer < 0) || (cLayer >= nHidden) || (!hasHidden)) // defining this layer's input mode.
		{
			log.warn("NeuralNetwork {} is in wrong mode for adding hidden Neuron.", this.hashCode() );
			return false;
		}
		else // good to go.
		{
			int prevLayer = (cLayer == 0) ? nInputs : sizeHidden; // first hidden layer?
			int nextLayer = (cLayer == nHidden - 1) ? nOutputs : sizeHidden; // last hidden layer?

			if (weights.length != prevLayer) // check
			{
				log.error("NeuralNetwork {} new hidden Neuron has wrong number of weights to map against prior layer!",
						this.hashCode() );
				return false;
			}
			else
			{
				int id1 = cLayer;
				int id2 = cHidden;

				if (hiddenLayers[id1][id2] != null)
				{
					log.error("NeuralNetwork {} current hidden node ({},{}) already has bound Neuron!",
							this.hashCode(), cLayer, cHidden);
					return false;
				}
				else
				{
					hiddenLayers[id1][id2] = new Neuron(prevLayer, nextLayer, alpha, phi, theta, active);

					if ((cLayer == nHidden - 1) && (nOutputs == 0)) // special case, no discrete output layer.
					{
						outputLayer[id2] = hiddenLayers[id1][id2]; // pass directly to output.
						log.debug("NeuralNetwork {} hidden ({},{}) bound to output layer (exposed NN) ",
								this.hashCode(), id1, id2);
					}

					Neuron connect;

					for (int wC = 0; wC < prevLayer; wC++)
					{
						if (cLayer == 0) // previous layer was input layer.
						{
							log.debug("NeuralNetwork {} hidden ({},{}) connect to input {}",
									new Object[] { this.hashCode(), id1, id2, wC } );
							connect = inputLayer[wC];
						}
						else // previous layer was hidden layer.
						{
							log.debug("NeuralNetwork {} hidden ({},{}) connect to hidden ({},{})",
									new Object[] { this.hashCode(), id1, id2, id1-1, wC } );
							connect = hiddenLayers[id1 - 1][wC];
						}

						if (connect.addOutput(hiddenLayers[id1][id2]))
						{
							log.debug("NeuralNetwork {} connect bound to hidden ({},{})",
									this.hashCode(), id1, id2);
						}
						else
						{
							log.error("NeuralNetwork {} counnt to hidden ({},{}) failed!",
									this.hashCode(), id1, id2);
							return false;
						}

						if (hiddenLayers[id1][id2].addInput(connect, weights[wC]))
						{
							log.debug("NeuralNetwork {} hidden ({},{}) bound to hidden ({},{}) with weight {}",
									new Object[] {this.hashCode(), id1, id2, id1 - 1, wC, weights[wC]} );
						}
						else
						{
							log.debug("NeuralNetwork {} hidden ({},{}) bind to hidden ({},{}) failed!",
									new Object[] {this.hashCode(), id1, id2, id1-1, wC} );
							return false;
						}
					} // loop through entire previous layer.

					cHidden ++; // next call to setHidden, work on next hidden.
					if (cHidden >= sizeHidden)
					{
						cLayer ++; // done this layer, move on to next hidden layer or output layer.
						cHidden = 0;
					}

					factorSize += weights.length + 1; // num weights + threshold.
					diagnosticSize ++; //neuron

					return true;
				}
			}
		}
	}

	/**
	 * Add output layer. Note that both hidden and output layers will only be filled if the previous layers are full.
	 *   E.g. only start calling this function once all input neurons and hidden neuron layers are complete.
	 *
	 * @param	weights	{@link addHidden()} for explanation, as the size of this array must be the same as the number
	 *						  of Neurons in the final hidden layer.
	 * @param	theta		The activation threshold for this output.
	 * @param	active		The activation function for this output.
	 * @return				True if output neuron was created and bound successfully, False otherwise.
	 * @see {@link Neuron}
	 * @see {@link ActivationFunction}
	 */
	public boolean addOutput(double[] weights, double theta, ActivationFunction active) // weights.length MUST == previous layer size
	{
		log.debug("NeuralNetwork {} adding output Neuron with {} weights, activation {}, and function {}",
				new Object[] {this.hashCode(), weights.length, theta, active.getClass().getName() } );

		if ((cLayer < nHidden) || (cLayer > nHidden) || (cOutput > nOutputs)) // defining this layer's input mode.
		{
			log.error("NeuralNetwork {} is in wrong mode or network is already complete!");
			return false;
		}
		else // good to go.
		{
			int prevLayer = (nHidden == 0) ? nInputs : sizeHidden; // has hidden?

			if (weights.length != prevLayer) // check
			{
				log.error("NeuralNetwork {} new output Neuron has wrong number of weights to map against prior layer!",
						this.hashCode() );
				return false;
			}
			else
			{
				int idx = cOutput;

				if (outputLayer[idx] != null)
				{
					log.error("NeuralNetwork {} output {} already bound!", this.hashCode(), idx );

					return false;
				}
				else
				{
					outputLayer[idx] = new Neuron(prevLayer, 0, alpha, phi, theta, active); // output layer has no outputs.

					Neuron connect;

					for (int wC = 0; wC < prevLayer; wC++)
					{
						if (cLayer == 0) // previous layer was input layer -- there are no hidden layers.
						{
							log.debug("NeuralNetwork {} output {} connect to input {}", this.hashCode(), idx, wC );
							connect = inputLayer[wC];
						}
						else // previous layer was hidden layer.
						{
							log.debug("NeuralNetwork {} output {} connect to hidden ({},{})",
									new Object[] {this.hashCode(), idx, cLayer - 1, wC} );
							connect = hiddenLayers[cLayer - 1][wC];
						}

						if (connect.addOutput(outputLayer[idx]))
						{
							log.debug("NeuralNetwork {} connect bound to output {}", this.hashCode(), idx);
						}
						else
						{
							log.error("NeuralNetwork {} connect bind to output {} failed!");
							return false;
						}

						if (outputLayer[idx].addInput(connect, weights[wC]))
						{
							log.debug("NeuralNetwork {} output {} bound to connect with weight {}",
									this.hashCode(), idx, weights[wC]);
						}
						else
						{
							log.error("NeuralNetwork {} output {} bind to connect failed!");
							return false;
						}
					} // loop through entire previous layer.

					cOutput ++; // next call to setOutput, work on next output.
					if (cOutput >= nOutputs)
					{
						cLayer ++; // done this layer, done the network!
					}
					factorSize += weights.length + 1; // num weights + threshold.
					diagnosticSize ++; //neuron

					return true;
				}
			}
		}
	}

	/**
	 * Set the inputs of the neural network.
	 *
	 * @param	inValues	The input values to pass into the network for this "step". The size of this array
	 *						  must be equal to the number of input Neurons defined.
	 */
	public void setInputs(double[] inValues)
	{
		log.debug( "NeuralNetwork {} setting {} inputs", this.hashCode(), inValues.length );
		if (inValues.length == nInputs)
		{
			for (int cI = 0; cI < nInputs; cI ++)
			{
				inputHandlers[cI].setValue(inValues[cI]);
				log.debug("NeuralNetwork {} input {} set to {}", this.hashCode(), cI, inputHandlers[cI].getOutput() );
			}
		}
		else
		{
			log.error( "NeuralNetwork {} not the right amount of inputs!");
		}
	}

	/**
	 * Get the output values from the network -- either the input layer (no hidden, no output), the last hidden (no outputs) or the output
	 *   layer.
	 *
	 * @return	The Neural Network output values. This array will be the same size as the output layer (as defined above).
	 */
	public double[] getOutputs()
	{
		log.debug("NeuralNetwork {} getting Outputs", this.hashCode() );
		double[] outValues;

		int oSize;

		if (nOutputs > 0)
			oSize = nOutputs;
		else if (hasHidden)
			oSize = sizeHidden;
		else
			oSize = nInputs;

		outValues = new double[oSize];

		for (int cO = 0; cO < oSize; cO ++)
		{
			if (nOutputs > 0)
				outValues[cO] = outputLayer[cO].getOutput();
			else if (hasHidden)
				outValues[cO] = hiddenLayers[nHidden - 1][cO].getOutput();
			else
				outValues[cO] = inputLayer[cO].getOutput();
		}

		return outValues;
	}

	/**
	 * Step the network one layer at a time. Start at input layer, then progress by layers back to the output layer.
	 *  Learning is also applied progressively -- Hebb's learning principle is applied on a Neuron level.
	 */
	public void step()
	{
		log.debug("NeuralNetwork {} stepping started", this.hashCode() );
		// steps the entire network, from left to right.

		for (int iC = 0; iC < nInputs; iC ++)
		{
			inputLayer[iC].step();
			log.debug("NeuralNetwork {} input layer {} stepped", this.hashCode(), iC);
		}

		if (hasHidden)
		{
			for (int iH = 0; iH < nHidden; iH ++)
			{
				for (int iS = 0; iS < sizeHidden; iS ++)
				{
					hiddenLayers[iH][iS].step();
					log.debug("NeuralNetwork {} hidden layer ({},{}) stepped", this.hashCode(), iH, iS);
				}
			}
		}

		for (int iO = 0; iO < nOutputs; iO ++)
		{
			outputLayer[iO].step();
			log.debug("NeuralNetwork {} output layer {} stepped", this.hashCode(), iO);
		}
	}


	/**
	 * Print the construction of the input, hidden, and output layers in a matrix type style, based on the
	 *  toString of {@link Neuron.toString()}.
	 *
	 * @return	A multi-line String containing a representative "matrix" of the Neuron.
	 */
	public String printConstruct()
	{
		StringBuffer matrix = new StringBuffer("Input: ");

		for (int iC = 0; iC < nInputs; iC ++)
		{
			matrix.append("\n      ");
			matrix.append( inputLayer[iC].toString() );
		}

		if (hasHidden)
		{
			for (int iH = 0; iH < nHidden; iH ++)
			{
				matrix.append("\nHidden ");
				matrix.append(iH);
				matrix.append(": ");

				for (int iS = 0; iS < sizeHidden; iS ++)
				{
					matrix.append("\n      ");
					matrix.append( hiddenLayers[iH][iS].toString() );
				}
			}
		}

		matrix.append("\nOutput: ");

		for (int iO = 0; iO < nOutputs; iO ++)
		{
			matrix.append("\n      ");
			matrix.append( outputLayer[iO].toString() );
		}

		return matrix.toString();
	}

	/**
	 * Print the output of each layer in the network as an output matrix.
	 *
	 * @return	All the outputs of each layer as a String matrix.
	 */
	public String printMatrix()
	{
		StringBuffer matrix = new StringBuffer("Input: ");

		for (int iC = 0; iC < nInputs; iC ++)
		{
			matrix.append( inputLayer[iC].getOutput() );
			matrix.append("  ");
		}

		if (hasHidden)
		{
			for (int iH = 0; iH < nHidden; iH ++)
			{
				matrix.append("\nHidden ");
				matrix.append(iH);
				matrix.append(": ");

				for (int iS = 0; iS < sizeHidden; iS ++)
				{
					matrix.append( hiddenLayers[iH][iS].getOutput() );
					matrix.append("  ");
				}
			}
		}

		matrix.append("\nOutput: ");

		for (int iO = 0; iO < nOutputs; iO ++)
		{
			matrix.append( outputLayer[iO].getOutput() );
			matrix.append("  ");
		}

		return matrix.toString();
	}
}