package com.programmerdan.ai.maze;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.programmerdan.ai.maze.NeuralNetwork;

/**
 * Test class for the Neural Network.
 * TODO: Make this more useful, empirically verifiable, etc.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 *
 * @version 1.0 December 14, 2013
 *   Refactored from NeuralNetwork class
 */
@RunWith(JUnit4.class)
public class NeuralNetworkTest {

	/**
	 * Simple executable test of the Neural Network. The parameters are well know, making
	 * verification simple.
	 */
	@Test
	public void simpleNetworkTest (
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

}