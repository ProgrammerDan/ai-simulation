package com.programmerdan.ai.maze;

/**
 * Basic Bug, which extends {@link Position}. It has location, direction, success, failure, etc.
 *   A Bug is built around the idea of Inputs (senses), Outputs (direction and velocity), and a Brain to
 *   tie them together ({@link NeuralNetwork}). The brain is built using a Chromosome sequence.
 * This is a fascinating idea, lending itself to the idea that future generations will inherit the
 *   brain characteristics of their forbears. Other future ideas could include epigentics -- allowing
 *   the "final" state of a Bug to influence their children.
 * Another fascinating idea would be to build the whole bug -- all variables in entire -- from a Chromosome.
 * More ideas include graph-based neural networks instead of layer-based NN for a brain, cluster-graph based
 *   NNs, and internal feedback mechanics -- allowing outputs to feed into inputs. Additionally, external
 *   or system feedback mechanisms will be particularly useful -- allowing the present state of the Bug
 *   to feed back into the NN.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 *    Initial release, with a NN built from a Chromosome, expecting several inputs and ouputs.
 * @version 1.01 Christmas 19, 2013
 *    Fixing up comments, general class cleanup.
 */
public class Bug extends Position
{
	/**
	 * Rotation factor
	 */
	private double direction; // rotation
	/**
	 * Motion Factor // velocity
	 */
	private double velocity; // motion

	/**
	 * Rotation multiplier
	 */
	private double rotateMult; // rotation multiplication
	/**
	 * Veloity multiplier
	 */
	private double speedMult; // velocity multiplication

	/**
	 * Success counter for fitness computation
	 */
	private int success;
	/**
	 * Failure counter for fitness computation
	 */
	private int failure;

	/**
	 * Actually fitness computation
	 */
	private double fitness;

	/**
	 * TODO: Make this an enumeration!
	 *
	 * Distance input flag. The input which should be a normalized distance to the nearest object seen
	 */
	public static int DISTANCE = 0;
	/**
	 *  Position input flag. The input which should be a normalized angle to the nearest object seen
	 */
	public static int POSITION = 1;
	/**
	 * Type input flag. The input which should indicate what is seen; wall, other bug, etc.
	 */
	public static int TYPE = 2;

	/**
	 * Number of inputs.
	 */
	private int INPUTS = 4; // lets make this variable now.
	/**
	 * The type of each input
	 */
	private int[] inputClasses;
	/**
	 * Default number of hidden layers
	 */
	private int HIDDENS = 5;
	/**
	 * Default number of hidden nodes per layer.
	private int HIDDENW = 7;

	/**
	 * Returns the number of inputs for this bug.
	 *
	 * @return Number of inputs this bug expects.
	 */
	public int INPUTS() { return INPUTS; }
	/**
	 * Returns the number of hidden layers in the brain of this bug.
	 *
	 * @return Number of hidden layers.
	 */
	public int HIDDENS() { return HIDDENS; }
	/**
	 * Returns the number of neurons in each hidden layer of the brain of this bug.
	 *
	 * @return Number of neurons in each hidden layer.
	 */
	public int HIDDENW() { return HIDDENW; }

	/**
	 * Latest input value "registers"
	 */
	private double[] inputStore;

	/**
	 * The Velocity output parameter flag
	 */
	public static int VEL = 0;
	/**
	 * The Delta (direction) output parameter flag
	 */
	public static int DELTA = 1;

	/**
	 * The number of outputs for this bug
	 */
	public static int OUTPUTS = 2;

	/**
	 * Latest output value "registers"
	 */
	private double[] outputStore;
	/**
	 * Output "classes", type of output
	 */
	private int[] outputClasses;

	/**
	 * The makeup of this Bug -- its Chromosome.
	 */
	private Chromosome dna;
	/**
	 * The brain of this bug!
	 */
	private NeuralNetwork brain;

	/**
	 * Estimates the size of the Chromosome needed to set up this bug based on the number of
	 *   inputs, number of hidden layers, and size of each hidden layer.
	 * Factors in things like learning, forgetting, activation thresholds and intra-neuron weights.
	 *
	 * @param	inputs			The number of inputs this brain expects.
	 * @param	hiddenWidth		The number of neurons in each hidden layer.
	 * @param	hiddens			The number of hidden layers.
	 * @return					The recommended Chromosome size to support a bug with these brain characteristics.
	 */
	public static int estimateChromosome(int inputs, int hiddenWidth, int hiddens)
	{
		int genes = 2; // for the basic two genes defining learning and forgetting.

		// two genes for each input. (weight, and activation threshold)
		genes += 2 * inputs;

		// first layer of hidden interfaces with inputs:
		genes += hiddenWidth * (inputs + 1); // the plus one is the activation threshold of that neuron.

		// subsequent layers of hiddens ( hiddens - 1 ) are homogenous.
		genes += (hiddens - 1) * hiddenWidth * (hiddenWidth + 1); // each hidden layer neuron has a weight for every previous layer neuron; there is additionally the activation threshold.

		// finally, output layer. Similar, each output neuron has a weight for every previous layer neuron; additionally, there is an activation threshold.
		genes += Bug.OUTPUTS * (hiddenWidth + 1);

		return genes;
	}

	/**
	 * The Bug's brain size is controlled by hiddenwidth and hidden size. Choose widths and size appropriate to the number of inputs.
	 * There are quite a few other factors here, each described below.
	 *
	 * @param	x			The starting X coord of this Bug.
	 * @param	y			The starting Y coord of this Bug.
	 * @param	dir			The starting direction of this Bug (an angle, not in radians).
	 * @param	vel			The starting velocity of this Bug.
	 * @param	rotate		The rotation multiple of this Bug (e.g. this Bug can rotate faster or slower than other Bugs).
	 * @param	speed		The velocity multiple of this Bug (e.g. this Bug is quicker or slower than other Bugs).
	 * @param	inputs		The input classes -- determines the way to interpret the Gene used to weight this input will be constructed.
	 *						  Class 0 uses function {@link midGene()}, class 1 uses {@link fitGene()}, and any other class uses {@link tinGene()}.
	 * 						  Note that regardless of class, {@link AF_Tanh} is the {@link ActivationFunction} used.
	 * @param	hiddenwidth	The size of every hidden brain layer.
	 * @param	hiddensize	The number of hidden layers.
	 * @param	outputs		The output classes -- determines which {@link ActivationFunction} applies to the outputs.
	 * @param	DNA			The DNA to use when building the bug's brain.
	 **/
	public Bug(double x, double y, double dir, double vel, double rotate, double speed, int[] inputs, int hiddenwidth, int hiddensize, int[] outputs, Chromosome DNA)
	{
		super(x, y);

		direction = dir;
		velocity = vel;

		rotateMult = rotate;
		speedMult = speed;

		success = 0;
		failure = 0;
		fitness = 0.0;

		INPUTS = inputs.length;
		inputClasses = inputs;
		HIDDENS = hiddensize;
		HIDDENW = hiddenwidth;

		inputStore = new double[INPUTS];

		outputStore = new double[OUTPUTS];

		outputClasses = outputs;

		// Build Chromosme
		dna = DNA;

		if (outputClasses.length != OUTPUTS)
		{
			brain = null;
			System.out.println("Lobotomy");
		}
		else
		{
			build();
		}
	}

	/**
	 * Formats a Gene's Double value using the {@link fit(double)} function.
	 *
	 * @param	i	Index of gene to "fit".
	 * @return		The result of the "fit" operation on the Double value of the {@link Gene}.
	 */
	private double fitGene(int i)
	{
		return fit(dna.getGene(i).toDouble());
	}

	/**
	 * Formats a Gene's Double value using the {@link mid(double)} function.
	 *
	 * @param	i	Index of gene to "mid".
	 * @return		The result of the "mid" operation on the Double value of the {@link Gene}.
	 */
	private double midGene(int i)
	{
		return mid(dna.getGene(i).toDouble());
	}

	/**
	 * Formats a Gene's Double value using the {@link tin(double)} function.
	 *
	 * @param	i	Index of gene to "tin".
	 * @return		The result of the "tin" operation on the Double value of the {@link Gene}.
	 */
	private double tinGene(int i)
	{
		return tin(dna.getGene(i).toDouble());
	}

	/**
	 * Builds a brain based on certain parameters.
	 *
	 * @see {@link Bug}
 	 */
	private void build()
	{
		if (dna != null) // now compute size of needed chromosome.
		{
			if (dna.numGenes() >= Bug.estimateChromosome(INPUTS, HIDDENW, HIDDENS) ) // ok, now we can build this brain.
			{
				//System.out.println("Input: " + INPUTS + " HIDDENS: " + HIDDENS + " HIDDENW: " + HIDDENW + " OUTPUTS: " + OUTPUTS);

				int i = 0;
				brain = new NeuralNetwork(INPUTS, HIDDENS, HIDDENW, OUTPUTS, midGene(i++), midGene(i++));

				for (int in = 0; in < INPUTS; in++)
				{
					brain.addInput(fitGene(i++), ( (inputClasses[in] == 0) ? midGene(i++) : ( (inputClasses[in] == 1) ? fitGene(i++) : tinGene(i++) ) ), AF_Tanh.Default);
				}

				double[] hidden; int c = 0;

				for (int a = 0 ; a < HIDDENS; a++)
				{
					if (a == 0)
					{
						c = INPUTS;
					}
					else
					{
						c = HIDDENW;
					}

					for (int d = 0 ; d < HIDDENW; d++)
					{
						hidden = new double[c];

						for (int b = 0; b < c; b++)
						{
							hidden[b] = fitGene(i++);
						}

						brain.addHidden( hidden, fitGene(i++), AF_Tanh.Default );
					}
				}

				for (int ou = 0; ou < OUTPUTS; ou++)
				{
					hidden = new double[HIDDENW];

					for (int b = 0; b < HIDDENW; b++)
					{
						hidden[b] = fitGene(i++);
					}

					brain.addOutput( hidden, fitGene(i++), ( outputClasses[ou] == 0 ? AF_Tanh.Default : AF_Sigmoid.Default ) );
				}

				if (i > dna.numGenes()) // invalid! oh no!
				{
					brain = null;
					System.out.println("Lobotomy");
				}
				else
				{
					//System.out.println("Genes: " + dna.numGenes() + " used: " + i);
				}
			}
		}
	}

	/** This function basically takes an image of the active brain of this bug, to see how it has changed over use.
	 *  One thought was to capture the "brain" at various junctures and save it, to analyze at a later point.
	 *  Could do statistical analysis, such as % changed, etc.
	 *  Another thought is a heatmap visualization of the brain ... not sure how to go about it but have a few ideas.
	 *
	 **/
	/*public Chromosome capture()
	{
		Chromosome matureBrain = new Chromosome();

		double[] brainfactors = brain.getNetworkFactors();

		for (int a = 0; a < brainfactors.length; a++)
		{
			matureBrain.addGene(
		}
	}*/
	// Note: turns out that the weights inside a mature brain can get larger than is possible to encode in my current scheme -- the "max weight"
	//       is currently 10 inside the brains ... simplest would be to "expand" a chromosome on start to fit within this maximum value.

	/**
	 * Returns the {@link Chromosome} sequence used to create the bug's brain.
	 *
	 * @return	the DNA of this bug's brain (a {@link Chromosome} object).
	 */
	public Chromosome getDNA()
	{
		return dna;
	}

	/**
	 * Returns the (@link NeuralNetwork} "brain" that this bug contains.
	 *
	 * @return	the "brain" of this bug (a {@link NeuralNetwork} object).
	 */
	public NeuralNetwork getBrain()
	{
		return brain;
	}

	/**
	 * Sets the X coord of this bug.
	 *
	 * @param	_x	the new X coord of this Bug.
	 */
	public void setX(double _x)
	{
		x = _x;
	}

	/**
	 * Sets the Y coord of this bug.
	 *
	 * @param	_y	the new Y coord of this Bug.
	 */
	public void setY(double _y)
	{
		y = _y;
	}

	/**
	 * Sets the X and Y coord of this bug.
	 *
	 * @param	_x	the new X coord of this Bug.
	 * @param	_y	the new Y coord of this Bug.
	 */
	public void setPosition(double _x, double _y)
	{
		setX(_x); setY(_y);
	}

	/**
	 * Gets the current direction of this bug.
	 *
	 * @return	the Direction of this bug (an angle, not in radians).
	 */
	public double getDir()
	{
		return direction;
	}

	/**
	 * Sets the current direction of this bug arbitarily.
	 *
	 * @param	_dir	the new direction of this bug (an angle, not in radians).
	 */
	public void setDir(double _dir)
	{
		direction = _dir;
	}

	/**
	 * Gets the current velocity of this bug.
	 *
	 * @return	the Velocity of this bug.
	 */
	public double getVel()
	{
		return velocity;
	}

	/**
	 * Sets the current velocity of this bug arbitrarily.
	 *
	 * @param	_vel	the new velocity of this bug.
	 */
	public void setVel(double _vel)
	{
		velocity = _vel;
	}

	/**
	 * Gets the "true" velocity of this bug. Gets set by the {@link step()} function,
	 *   based on the expected velocity of the bug adjusted for wall/obstacle interactions.
	 *
	 * @return	the True Velocity of the bug, adjusted for environment interactions.
	 */
	public double getTrueVel()
	{
		if (trueVector == null)
			return 0.0;
		else
			return Math.sqrt( Math.pow( trueVector[0], 2.0) + Math.pow( trueVector[1], 2.0 ) );
	}

	/**
	 * Get velocity vector -- multiplies the direction by the velocity, based on the multipliers set at bug creation.
	 *
	 * @return	a two-element array, with element 0 being X vector component, element 1 being Y vector component.
	 */
	public double[] getVector()
	{
		// directed velocity

		double _tx = velocity * speedMult * Math.cos(Math.toRadians(direction * rotateMult) );
		double _ty = velocity * speedMult * Math.sin(Math.toRadians(direction * rotateMult) );

		double[] ret = new double[] {_tx, _ty};

		return ret;
	}

	private double[] trueVector;

	/**
	 * Returns the True Vector, in component form.
	 *
	 * @return	a two-element array, with element 0 being X true vector component, element 1 being Y true vector component.
	 */
	public double[] getTrueVector()
	{
		return trueVector;
	}

	/**
	 * Applies an input value to a specific input in the brain.
	 *   Also stores it in a temporary input store.
	 *
	 * @param	_idx	The input at index to update
	 * @param	_val	The value to apply to that input Neuron.
	 */
	public void setInput(int _idx, double _val)
	{
		if ( ( _idx >= 0 ) && ( _idx < INPUTS ) )
		{
			inputStore[_idx] = _val; // temporarily stored locally

			brain.setInputs(inputStore); // thus changes can be updated piecemeal.
		}
	}

	/**
	 * Get the most recent input value of a specific input index.
	 *
	 * @param	_idx	The input index to query
	 * @return			The most recent value assigned to this input.
	 */
	public double getInput(int _idx)
	{
		if ( ( _idx >= 0 ) && ( _idx < INPUTS ) )
		{
			return inputStore[_idx];
		}
		return  Float.NaN;
	}

	/**
	 * Get the most recent ouptut value of a specific output index.
	 *
	 * @param	_idx	The output index to query
	 * @return			The most recent output value generated
	 */
	public double getOutput(int _idx)
	{
		if ( ( _idx >= 0 ) && ( _idx < OUTPUTS ) )
		{
			return outputStore[_idx];
		}
		return  Float.NaN;
	}

	/**
	 * Sets the fitness of this bug.
	 *
	 * @param	fit		This bug's new fitness value. Also increases the max observed fitness if necessary.
	 *
	 * @see {@link maxfitness}
	 */
	public void setFitness(double fit)
	{
		fitness = fit;
		if (fit > maxfitness) maxfitness = fit;
	}

	/**
	 * A tracking variable to keep tabs on the highest fitness achieved by this bug.
	 */
	private double maxfitness = Double.MIN_VALUE;

	/**
	 * Gets the current fitness of this bug.
	 *
	 * @return	the current fitness.
	 */
	public double getFitness()
	{
		return fitness;
	}

	/**
	 * Gets the maximum observed fitness of this bug.
	 *
	 * @return	the maximum observed fitness.
	 */
	public double getMaxFitness()
	{
		return maxfitness;
	}

	/**
	 * Increment the number of successes observed by one.
	 */
	public void incSuccess()
	{
		success++;
	}

	/**
	 * Zeroes out the number of successes.
	 */
	public void clrSuccess()
	{
		success = 0;
	}

	/**
	 * Returns the current number of successes.
	 *
	 * @return	the number of successes observed
	 */
	public int getSuccess()
	{
		return success;
	}

	/**
	 * Increment the number of failures observed by one.
	 */
	public void incFailure()
	{
		failure++;
	}

	/**
	 * Zeroes out the number of failures.
	 */
	public void clrFailure()
	{
		failure = 0;
	}

	/**
	 * Returns the current number of failures.
	 *
	 * @return	the number of failures observed.
	 */
	public int getFailure()
	{
		return failure;
	}

	/**
	 * Manipulator for Gene values, which are always in the range [0.0, 1.0).
	 * This function fits a gene value into the range [-0.5, 0.5).
	 *
	 * @return	the input value adjusted by -0.5
	 */
	private double fit(double _a)
	{
		return _a - .5;
	}

	/**
	 * Reverse manipulator for Gene values.
	 *
	 * @return	the input value adjusted by +0.5.
	 * @see {@link fit(double)}
	 */
	private double unfit(double _a)
	{
		return _a + .5;
	}

	/**
	 * Manipulator for Gene values, which are always in the range [0.0, 1.0).
	 * This function fits a gene value into the range [0.0, 0.1).
	 *
	 * @return	the input value multiplied by 0.1
	 */
	private double tin(double _a)
	{
		return _a * .1;
	}

	/**
	 * Reverse manipulator for Gene values.
	 *
	 * @return	the input value divided by 0.1
	 * @see {@link tin(double)}
	 */
	private double untin(double _a)
	{
		return _a / .1;
	}

	/**
	 * Manipulator for Gene values, which are always in the range [0.0, 1.0).
	 * This function fits a gene value into the range [0.0, 0.5).
	 *
	 * @return	the input value multiplied by 0.5
	 */
	private double mid(double _a)
	{
		return _a * .5;
	}

	/**
	 * Reverse manipulator for Gene values.
	 *
	 * @return	the input value divided by 0.5
	 * @see {@link mid(double)}
	 */
	private double unmid(double _a)
	{
		return _a / .5;
	}

	/**
	 * Applies most recent input values to the brain inputs.
	 * Steps the "brain", and apply the resulting outputs to the velocity and direction.
	 *
	 * @param	sm	The map to use in constraining the motion of this bug.
	 * TODO: refactor so the bug is not tightly dependent on a Map. It should instead ask the simulation
	 *        environment if the motion is acceptable, and if not, what kind of motion is acceptable.
	 */
	public void step(SimulationMap sm)
	{
		brain.setInputs( inputStore );

		brain.step();

		outputStore = brain.getOutputs();

		velocity = outputStore[VEL];

		direction += 2.0 * outputStore[DELTA]; // 2 times output b/c the original formula called for a union of two such outputs, which could be max 2 times any one.
		direction = direction % 360;

		if (direction < 0)
			direction += 360; // normalize direction between 0 and 360.

		double[] temp = getVector();

		double[] temp2 = sm.fixMoveV(x, y, temp);

		trueVector = new double[] { temp2[0] - x, temp2[1] - y };

		x = temp2[0];
		y = temp2[1]; // update location based on "Vector" -- directed velocity.
	}
}
