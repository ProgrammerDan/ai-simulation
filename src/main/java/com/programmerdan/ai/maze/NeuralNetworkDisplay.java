package com.programmerdan.ai.maze;

import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A display panel for a NeuralNetwork.
 *
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 *
 * @version 1.0 Jan 25, 2015
 *   Initial Version
 *
 * @see {@link NeuralNetwork}
 */
public class NeuralNetworkDisplay extends JPanel implements MouseListener, Runnable {

	private boolean running;
	private boolean active;

	private NeuralNetwork network;

	public NeuralNetworkDisplay(NeuralNetwork network) {
		this.network = network;
		running = true;
		active = false;
	}

	public boolean toggleActive() {
		active = !active;

		return active;
	}

	public void finished() {
		running = false;
	}

	// Fulfill the contract of MouseListener
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

	// Was there a click? Is so, pause the simulation or unpause.
    public void mouseClicked(MouseEvent e) {
		System.out.println(active);
		if (active)
			active = false;
		else
			active = true;
    }

    public void run() {
		while (running) {
			if (active) {
				this.repaint();
			}
			try {
				Thread.sleep(0);
			} catch (InterruptedException ie) {
				// do nothing.
			}
		}
	}

    public void paint(Graphics g)
    {
		Graphics2D g2 = (Graphics2D) g;

        super.paint(g2); // clears the screen.

        g2.setFont(g2.getFont().deriveFont(10f)); // sets the font to size 8.

        int m = g2.getFontMetrics().getAscent(); // sets the multipler for font spacing.

        g2.setColor(Color.BLACK);

		double[] factors = network.getNetworkFactors();

		String LF = "Learning (\u03b1): " + factors[0];

		int LFwidth = g2.getFontMetrics().stringWidth(LF);

		g2.drawString(LF, 10, m);
		g2.drawString("Forgetting (\u03a6): " + factors[1], 20 + LFwidth, m);

		// Draw Input weights and Theta
		int n = m/3;
		int nf = 2;

		for (int i = 0; i < network.getNumInputs(); i++) {
			Color inputColor = new Color( factors[nf+1] < 0f ? 1f : 0f, 0f, factors[nf+1] < 0f ? 0f : (float) (factors[nf+1] / Neuron.MAXWEIGHT));
			g2.setColor(inputColor);
			g2.fillOval(m-n, (2+i)*m-n, m+n, (2+i)*m+n);
			Color weightColor = new Color( factors[nf] < 0f ? 1f : 0f, factors[nf] < 0f ? 0f : (float) factors[nf], 0f);
			g2.drawLine(0,(2+i)*m,m-n,(2+i)*m);
			nf += 2;
		}
	}

}