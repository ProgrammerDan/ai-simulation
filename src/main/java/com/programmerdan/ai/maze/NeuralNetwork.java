package com.programmerdan.ai.maze;

import java.io.*;

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
 * @version 1.0 May 7, 2007
 * @see {@link Neuron}
 */
public class NeuralNetwork
{
	/**
	 * Simple executable test of the Neural Network. The parameters are well know, making
	 * verification simple.
	 * TODO: Move this into a JUnit test.
	 *
	 * @param	args	Ignored.
	 */
	public static void main(String[] args)
	{
		// test some simple networks and see that they connect correctly.

		NeuralNetwork brain = new NeuralNetwork(3, 5, 7, 3, 0.1, 0.2);

		brain.addInput(.5, .1, AF_Tanh.Default); // distance input 0 - inf
		brain.addInput(.5, .2, AF_Tanh.Default); // position input -1 - 1
		brain.addInput(.5, .3, AF_Tanh.Default); // type: -1,0, 1

		brain.addHidden(new double[] {0.1,0.2,0.3}, 0.4, AF_Tanh.Default);
		brain.addHidden(new double[] {0.2,0.3,0.4}, 0.5, AF_Tanh.Default);
		brain.addHidden(new double[] {0.3,0.4,0.5}, 0.6, AF_Tanh.Default);
		brain.addHidden(new double[] {0.4,0.5,0.6}, 0.7, AF_Tanh.Default);
		brain.addHidden(new double[] {0.5,0.6,0.7}, 0.8, AF_Tanh.Default);
		brain.addHidden(new double[] {0.6,0.7,0.8}, 0.9, AF_Tanh.Default);
		brain.addHidden(new double[] {0.7,0.8,0.9}, 1.0, AF_Tanh.Default);

		brain.addHidden(new double[] {0.1,0.2,0.3,0.4,0.5,0.6,0.7}, 0.8, AF_Tanh.Default);
		brain.addHidden(new double[] {0.2,0.3,0.4,0.5,0.6,0.7,0.8}, 0.9, AF_Tanh.Default);
		brain.addHidden(new double[] {0.3,0.4,0.5,0.6,0.7,0.8,0.9}, 1.0, AF_Tanh.Default);
		brain.addHidden(new double[] {0.4,0.5,0.6,0.7,0.8,0.9,1.0}, 0.1, AF_Tanh.Default);
		brain.addHidden(new double[] {0.5,0.6,0.7,0.8,0.9,1.0,0.1}, 0.2, AF_Tanh.Default);
		brain.addHidden(new double[] {0.6,0.7,0.8,0.9,1.0,0.1,0.2}, 0.3, AF_Tanh.Default);
		brain.addHidden(new double[] {0.7,0.8,0.9,1.0,0.1,0.2,0.3}, 0.4, AF_Tanh.Default);

		brain.addHidden(new double[] {0.2,0.3,0.4,0.5,0.6,0.7,0.8}, 0.9, AF_Tanh.Default);
		brain.addHidden(new double[] {0.3,0.4,0.5,0.6,0.7,0.8,0.9}, 1.0, AF_Tanh.Default);
		brain.addHidden(new double[] {0.4,0.5,0.6,0.7,0.8,0.9,1.0}, 0.1, AF_Tanh.Default);
		brain.addHidden(new double[] {0.5,0.6,0.7,0.8,0.9,1.0,0.1}, 0.2, AF_Tanh.Default);
		brain.addHidden(new double[] {0.6,0.7,0.8,0.9,1.0,0.1,0.2}, 0.3, AF_Tanh.Default);
		brain.addHidden(new double[] {0.7,0.8,0.9,1.0,0.1,0.2,0.3}, 0.4, AF_Tanh.Default);
		brain.addHidden(new double[] {0.8,0.9,1.0,0.1,0.2,0.3,0.4}, 0.5, AF_Tanh.Default);

		brain.addHidden(new double[] {0.3,0.4,0.5,0.6,0.7,0.8,0.9}, 1.0, AF_Tanh.Default);
		brain.addHidden(new double[] {0.4,0.5,0.6,0.7,0.8,0.9,1.0}, 0.1, AF_Tanh.Default);
		brain.addHidden(new double[] {0.5,0.6,0.7,0.8,0.9,1.0,0.1}, 0.2, AF_Tanh.Default);
		brain.addHidden(new double[] {0.6,0.7,0.8,0.9,1.0,0.1,0.2}, 0.3, AF_Tanh.Default);
		brain.addHidden(new double[] {0.7,0.8,0.9,1.0,0.1,0.2,0.3}, 0.4, AF_Tanh.Default);
		brain.addHidden(new double[] {0.8,0.9,1.0,0.1,0.2,0.3,0.4}, 0.5, AF_Tanh.Default);
		brain.addHidden(new double[] {0.9,1.0,0.1,0.2,0.3,0.4,0.5}, 0.6, AF_Tanh.Default);

		brain.addHidden(new double[] {0.4,0.5,0.6,0.7,0.8,0.9,1.0}, 0.1, AF_Tanh.Default);
		brain.addHidden(new double[] {0.5,0.6,0.7,0.8,0.9,1.0,0.1}, 0.2, AF_Tanh.Default);
		brain.addHidden(new double[] {0.6,0.7,0.8,0.9,1.0,0.1,0.2}, 0.3, AF_Tanh.Default);
		brain.addHidden(new double[] {0.7,0.8,0.9,1.0,0.1,0.2,0.3}, 0.4, AF_Tanh.Default);
		brain.addHidden(new double[] {0.8,0.9,1.0,0.1,0.2,0.3,0.4}, 0.5, AF_Tanh.Default);
		brain.addHidden(new double[] {0.9,1.0,0.1,0.2,0.3,0.4,0.5}, 0.6, AF_Tanh.Default);
		brain.addHidden(new double[] {1.0,0.1,0.2,0.3,0.4,0.5,0.6}, 0.7, AF_Tanh.Default);

		// Velocity Modifier
		brain.addOutput(new double[] {0.1,0.2,0.3,0.4,0.5,0.6,0.7}, 0.8, AF_Sigmoid.Default);
		// Left Turn Rate
		brain.addOutput(new double[] {0.2,0.3,0.4,0.5,0.6,0.7,0.8}, 0.9, AF_Tanh.Default);
		// Right Turn Rate
		brain.addOutput(new double[] {0.3,0.4,0.5,0.6,0.7,0.8,0.9}, 1.0, AF_Tanh.Default);

		System.out.println(brain.printConstruct());

		brain.setInputs(new double[] {0.1, 0.2, 0.3} );
		brain.step();

		System.out.println(brain.printMatrix());
	}

	private Neuron[] inputLayer;

	private NetworkInput[] inputHandlers; // handles are straightline input vs. weight appliers -- linear passthroughs that
											// are exposed to the outside world.
	private Neuron[][] hiddenLayers;
	private Neuron[] outputLayer;

	private int nInputs;
	private int nHidden;
	private int sizeHidden;
	private boolean hasHidden;
	private int nOutputs;

	private int cInput;
	private int cLayer;
	private int cHidden;
	private int cOutput;

	// learning/forgetting factors (global)
	private double alpha; // learning
	private double phi;   // forgetting

	private int factorSize;

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
		double[] nf = new double[factorSize];

		nf[0] = alpha;
		nf[1] = phi;

		// inputs.
		int nfi = 2;

		for (int k = 0; k < nInputs; k ++)
		{
			double[] inweights = inputLayer[k].getInWeights(); // weights.
			for (int j = 0; j < inweights.length; j ++)
				nf[ nfi++ ] = inweights[j];

			nf[ nfi++ ] = inputLayer[k].getTheta(); // activation.
		}

		for (int k = 0; k < nHidden; k ++)
		{
			for (int i = 0; i < hiddenLayers[k].length; i++)
			{
				double[] inweights = hiddenLayers[k][i].getInWeights();
				for (int j = 0; j < inweights.length; j++)
					nf[ nfi++ ] = inweights[j];

				nf[ nfi++ ] = hiddenLayers[k][i].getTheta();
			}
		}

		for (int k = 0; k < nOutputs; k ++)
		{
			double[] inweights = outputLayer[k].getInWeights();
			for (int j = 0; j < inweights.length; j ++)
				nf[ nfi++ ] = inweights[j];

			nf[ nfi++ ] = outputLayer[k].getTheta();
		}


		return nf;
	}

	//TODO: Let's modernize this debug.
	private boolean debug; // debugging this network?
	private PrintWriter debugOut;

	/**
	 * Initialize a new neural network.
	 * Specifies the network characteristics, including number of input neurons, number of hidden neurons in each layer
	 *   and how many hidden layers, how many output neurons, and finally the learning and forgetting factors of the
	 *   network.
	 *
	 * TODO: Get rid of C style params.
	 *
	 * @param	_nInputs	Number of input neurons in the input layer. (if negative, absolute value used)
	 * @param	_nHidden	Number of hidden neurons in each hidden layer. (if negative, absolute value used)
	 * @param	_sizeHidden	Number of hidden layers. (if negative, absolute value used)
	 * @param	_nOutputs	Number of output neurons in the output layer. (if negative, absolute value used)
	 * @param	_alpha		The learning factor.
	 * @param	_phi		The forgetting factor.
	 */
	public NeuralNetwork(int _nInputs, int _nHidden, int _sizeHidden, int _nOutputs, double _alpha, double _phi)
	{
		factorSize = 2; // alpha and phi.

		nInputs = (_nInputs < 0) ? -_nInputs: _nInputs;
		nHidden = (_nHidden < 0) ? -_nHidden: _nHidden;
		sizeHidden = (_sizeHidden < 0) ? -_sizeHidden: _sizeHidden;
		nOutputs = (_nOutputs < 0) ? -_nOutputs: _nOutputs;

		if (nInputs > 0)
		{
			inputLayer = new Neuron[nInputs];
			inputHandlers = new NetworkInput[nInputs];
		}

		if ((nHidden > 0) && (sizeHidden > 0))
		{
			hasHidden = true;
			hiddenLayers = new Neuron[nHidden][sizeHidden];
		}
		else
		{
			hasHidden = false;
		}

		if (nOutputs > 0)
		{
			outputLayer = new Neuron[nOutputs];
		}
		else if(hasHidden)
		{
			outputLayer = new Neuron[sizeHidden];
		}
		else // no hidden, and no discrete outputs
		{
			outputLayer = new Neuron[nInputs];
		}

		alpha = _alpha;
		phi = _phi;

		cInput = 0;
		cLayer = -1;
		cHidden = 0;
		cOutput = 0;
	}

	/**
	 * Adds an input to the input layer by creating a new Neuron and wiring it up to the rest of the network.
	 *
	 * @param	_weight		weight between Input Handler and Input Layer (how sensitive am I to input?)
	 * @param	_theta		activation weight of Input Layer (what is my threshold for input?)
	 * @param	active		activation function of Input Layer (how do I fire?)
	 * @return				True if input was successfully created and added, False otherwise.
	 * @see {@link ActivationFunction}
	 * @see {@link Neuron}
	 */
	public boolean addInput(double _weight, double _theta, ActivationFunction active)
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].addInput(" + String.valueOf(cInput) + "," + String.valueOf(_weight) + "," + String.valueOf(_theta) + ")");

		if (cLayer == -1) // defining input mode.
		{
			int _idx = cInput;

			if (inputLayer[_idx] == null) // TODO: If input layer is size 0, this will throw null pointer exception.
			{
				if (hasHidden) // is there at least some kind of hidden layer?
					inputLayer[_idx] = new Neuron(1, sizeHidden, alpha, phi, _theta, active);
				else
					inputLayer[_idx] = new Neuron(1, nOutputs, alpha, phi, _theta, active); // no hidden layer, simple network.

				inputHandlers[_idx] = new NetworkInput();

				if ((nOutputs == 0) && (!hasHidden)) // special case, simplest NN
				{
					outputLayer[_idx] = inputLayer[_idx];
					debugOut.println("  Input Node " + String.valueOf(_idx) + " Bound to Output Layer (simple NN) ");
				}

				if (inputHandlers[_idx].addOutput(inputLayer[_idx]))
				{
					if (debug) debugOut.println("  Handler " + String.valueOf(_idx) + " Bound to Input ");
				}
				else
				{
					if (debug) debugOut.println("  Handler " + String.valueOf(_idx) + " unbound! \n= failure");
					return false;
				}

				if (inputLayer[_idx].addInput(inputHandlers[_idx], _weight))
				{
					if (debug) debugOut.println("  Input " + String.valueOf(_idx) + " Bound to Handler with weight " + String.valueOf(_weight));
				}
				else
				{
					if (debug) debugOut.println("  Input " + String.valueOf(_idx) + " unbound! \n= failure");
					return false;
				}
			}
			else
			{
				if (debug) debugOut.println("  Input " + String.valueOf(_idx) + " already bound! \n= failure");
				return false; // fatal error?
			}

			cInput ++; // next call to setInput, work on next input.
			if (cInput >= nInputs)
			{
				cLayer = 0; // done inputs, move on to first hidden layer or output layer.
			}

			if (debug) debugOut.println("= success");

			factorSize += 2; // weight, threshold.

			return true;
		}
		else
		{
			if (debug) debugOut.println("  (wrong mode) \n= failure");
			return false;
		}
	}

	/**
	 * Add a hidden layer, with the specified application function.
	 *
	 * @param	_weights	For each new layer, this weight array must be equal in size to the size of the previous layer.
	 * 						  For example, if the input layer is 5 Neurons "wide", every neuron in the first hidden layer
	 *						  should have 5 weights. If that hidden layer is 7 Neurons "wide", every neuron in the second
	 *						  hidden layer (or output layer) must have 7 weights, and so on.
	 * @param	_theta		The activation threshold for this Neuron.
	 * @param	active		The activation function.
	 * @return				True if hidden Neuron was successfully added, False otherwise.
	 * @see {@link Neuron}
	 * @see {@link ActivationFunction}
	 */
	public boolean addHidden(double[] _weights, double _theta, ActivationFunction active) // _weights.length MUST == previous layer size
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].addHidden(" + String.valueOf(cLayer) + "," + String.valueOf(cHidden) + "," + String.valueOf(_weights.length) + "," + String.valueOf(_theta) + ")"); // output weight list size.

		if ((cLayer < 0) || (cLayer >= nHidden) || (!hasHidden)) // defining this layer's input mode.
		{
			if (debug) debugOut.println("  (wrong mode) \n= failure");
			return false;
		}
		else // good to go.
		{
			int prevLayer = (cLayer == 0) ? nInputs : sizeHidden; // first hidden layer?
			int nextLayer = (cLayer == nHidden - 1) ? nOutputs : sizeHidden; // last hidden layer?

			if (_weights.length != prevLayer) // check
			{
				if (debug) debugOut.println("  (weight array size != previous layer size) \n= failure");
				return false;
			}
			else
			{
				int id1 = cLayer;
				int id2 = cHidden;

				if (hiddenLayers[id1][id2] != null)
				{
					if (debug) debugOut.println("  (hidden node already bound!) \n= failure");
					return false;
				}
				else
				{
					hiddenLayers[id1][id2] = new Neuron(prevLayer, nextLayer, alpha, phi, _theta, active);

					if ((cLayer == nHidden - 1) && (nOutputs == 0)) // special case, no discrete output layer.
					{
						outputLayer[id2] = hiddenLayers[id1][id2]; // pass directly to output.
						debugOut.println("  Hidden Node <" + String.valueOf(id1) + "," + String.valueOf(id2) + "> Bound to Output Layer (exposed NN) ");
					}

					Neuron connect;

					for (int wC = 0; wC < prevLayer; wC++)
					{
						if (cLayer == 0) // previous layer was input layer.
						{
							if (debug) debugOut.println("  Connect is Input Layer " + String.valueOf(wC));
							connect = inputLayer[wC];
						}
						else // previous layer was hidden layer.
						{
							if (debug) debugOut.println("  Connect is Hidden Layer <" + String.valueOf(id1 - 1) + "," + String.valueOf(wC) + ">");
							connect = hiddenLayers[id1 - 1][wC];
						}

						if (connect.addOutput(hiddenLayers[id1][id2]))
						{
							if (debug) debugOut.println("  Connect bound to Hidden Layer <" + String.valueOf(id1) + "," + String.valueOf(id2) + ">");
						}
						else
						{
							if (debug) debugOut.println("  Connect unbound! \n= failure");
							return false;
						}

						if (hiddenLayers[id1][id2].addInput(connect, _weights[wC]))
						{
							if (debug) debugOut.println("  Hidden Layer <" + String.valueOf(id1) + "," + String.valueOf(id2) + "> Bound to Connect with weight " + String.valueOf(_weights[wC]));
						}
						else
						{
							if (debug) debugOut.println("  Hidden Layer <" + String.valueOf(id1) + "," + String.valueOf(id2) + "> unbound! \n= failure");
							return false;
						}
					} // loop through entire previous layer.

					if (debug) debugOut.println("= success");

					cHidden ++; // next call to setHidden, work on next hidden.
					if (cHidden >= sizeHidden)
					{
						cLayer ++; // done this layer, move on to next hidden layer or output layer.
						cHidden = 0;
					}

					factorSize += _weights.length + 1; // num weights + threshold.

					return true;
				}
			}
		}
	}

	/**
	 * Add output layer. Note that both hidden and output layers will only be filled if the previous layers are full.
	 *   E.g. only start calling this function once all input neurons and hidden neuron layers are complete.
	 *
	 * @param	_weights	{@link addHidden()} for explanation, as the size of this array must be the same as the number
	 *						  of Neurons in the final hidden layer.
	 * @param	_theta		The activation threshold for this output.
	 * @param	active		The activation function for this output.
	 * @return				True if output neuron was created and bound successfully, False otherwise.
	 * @see {@link Neuron}
	 * @see {@link ActivationFunction}
	 */
	public boolean addOutput(double[] _weights, double _theta, ActivationFunction active) // _weights.length MUST == previous layer size
	{
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].addOutput(" + String.valueOf(cOutput) + "," + String.valueOf(_weights.length) + "," + String.valueOf(_theta) + ")"); // output weight list size.

		if ((cLayer < nHidden) || (cLayer > nHidden) || (cOutput > nOutputs)) // defining this layer's input mode.
		{
			if (debug) debugOut.println("  (wrong mode or complete network) \n= failure");
			return false;
		}
		else // good to go.
		{
			int prevLayer = (nHidden == 0) ? nInputs : sizeHidden; // has hidden?

			if (_weights.length != prevLayer) // check
			{
				if (debug) debugOut.println("  (weight array size != previous layer size) \n= failure");
				return false;
			}
			else
			{
				int idx = cOutput;

				if (outputLayer[idx] != null)
				{
					if (debug) debugOut.println("  (output node already bound!) \n= failure");
					return false;
				}
				else
				{
					outputLayer[idx] = new Neuron(prevLayer, 0, alpha, phi, _theta, active); // output layer has no outputs.

					Neuron connect;

					for (int wC = 0; wC < prevLayer; wC++)
					{
						if (cLayer == 0) // previous layer was input layer -- there are no hidden layers.
						{
							if (debug) debugOut.println("  Connect is Input Layer " + String.valueOf(wC));
							connect = inputLayer[wC];
						}
						else // previous layer was hidden layer.
						{
							if (debug) debugOut.println("  Connect is Hidden Layer <" + String.valueOf(cLayer - 1) + "," + String.valueOf(wC) + ">");
							connect = hiddenLayers[cLayer - 1][wC];
						}

						if (connect.addOutput(outputLayer[idx]))
						{
							if (debug) debugOut.println("  Connect bound to Output Layer " + String.valueOf(idx));
						}
						else
						{
							if (debug) debugOut.println("  Connect unbound! \n= failure");
							return false;
						}

						if (outputLayer[idx].addInput(connect, _weights[wC]))
						{
							if (debug) debugOut.println("  Output Layer " + String.valueOf(idx) + " Bound to Connect with weight " + String.valueOf(_weights[wC]));
						}
						else
						{
							if (debug) debugOut.println("  Output Layer " + String.valueOf(idx) + " unbound! \n= failure");
							return false;
						}
					} // loop through entire previous layer.

					if (debug) debugOut.println("= success");

					cOutput ++; // next call to setOutput, work on next output.
					if (cOutput >= nOutputs)
					{
						cLayer ++; // done this layer, done the network!
					}
					factorSize += _weights.length + 1; // num weights + threshold.

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
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].setInputs(" + String.valueOf(inValues.length) + ")");
		if (inValues.length == nInputs)
		{
			for (int cI = 0; cI < nInputs; cI ++)
			{
				inputHandlers[cI].setValue(inValues[cI]);
				if (debug) debugOut.println("  input Handler " + String.valueOf(cI) + " value set to " + String.valueOf(inputHandlers[cI].getOutput()));
			}
			if (debug) debugOut.println("= success");
		}
		else
		{
			if (debug) debugOut.println("  Too few inputs on call to setInputs. \n= failure");
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
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].getOutputs()");
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
		if (debug) debugOut.println("[" + String.valueOf(this.hashCode()) + "].step()");
		// steps the entire network, from left to right.

		for (int iC = 0; iC < nInputs; iC ++)
		{
			inputLayer[iC].step();
			if (debug) debugOut.println("  Input Layer " + String.valueOf(iC) + " Stepped");
		}

		if (hasHidden)
		{
			for (int iH = 0; iH < nHidden; iH ++)
			{
				for (int iS = 0; iS < sizeHidden; iS ++)
				{
					hiddenLayers[iH][iS].step();
					if (debug) debugOut.println("  Hidden Layer <" + String.valueOf(iH) + "," + String.valueOf(iS) + "> Stepped");
				}
			}
		}

		for (int iO = 0; iO < nOutputs; iO ++)
		{
			outputLayer[iO].step();
			if (debug) debugOut.println("  Output Layer " + String.valueOf(iO) + " Stepped");
		}

		if (debug) debugOut.println("= success");
		// done.
	}

	/**
	 * Turn on the debug for the Network.
	 * TODO: Modernize.
	 *
	 * @param	_debout		The PrintWriter to send debug messages to.
	 */
	public void activateDebug(PrintWriter _debout)
	{
		debugOut = _debout;
		debug = true;
	}

	/**
	 * Turn on debug for all Neurons in the Network.
	 * Must be called strictly AFTER {@link activateDebug(PrintWriter)} or will cause quite a few
	 *   {@link NullPointerException}s.
	 */
	public void activateChildDebug()
	{
		if (debug)
		{
			for (int iC = 0; iC < nInputs; iC ++)
			{
				inputLayer[iC].activateDebug(debugOut);
			}

			if (hasHidden)
			{
				for (int iH = 0; iH < nHidden; iH ++)
				{
					for (int iS = 0; iS < sizeHidden; iS ++)
					{
						hiddenLayers[iH][iS].activateDebug(debugOut);
					}
				}
			}

			for (int iO = 0; iO < nOutputs; iO ++)
			{
				outputLayer[iO].activateDebug(debugOut);
			}
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

	/**
	 * Turn OFF the debug.
	 */
	public void deactivateDebug()
	{
		debugOut = null;
		debug = false;
	}


}