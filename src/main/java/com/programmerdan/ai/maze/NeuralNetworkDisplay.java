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

		this.setBackground(Color.WHITE);
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
				Thread.sleep(1000/30);
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

		String LF = "Learning (\u03b1): " + Double.toString(Math.round(factors[0] * 1000000.0) / 1000000.0);

		int LFwidth = g2.getFontMetrics().stringWidth(LF);

		g2.drawString(LF, 10, m);
		g2.drawString("Forgetting (\u03a6): " + Double.toString(Math.round(factors[1] * 1000000.0) / 1000000.0), 20 + LFwidth, m);

		int nf = 2;
		int n = (int) (m*1.5);
		int mm = 6;
		int fieldWidth = 50;
		int bO = 3*mm+fieldWidth; // base offset
		float factor = 0f; // factor for computation.
		double MW = Neuron.MAXWEIGHT;
		Color clr;

		for (int i = 0; i < network.getNumInputs(); i++) {
			// raw input
			factor = (float) ( factors[nf++] / 2.0);
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);

			String IV = Double.toString(Math.round(factors[nf-1] * 1000000.0) / 1000000.0);
			int IVwidth = g2.getFontMetrics().stringWidth(IV);
			g2.drawString(IV, 10, n*(1+i)+m);
			if (IVwidth > fieldWidth) {
				g2.clearRect(10+fieldWidth, n*(1+i), IVwidth - fieldWidth, m);
			}
			g2.fillOval(bO - m/2, n*(1+i), m, m);

			// weight
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.drawLine(bO + m/2, n*(1+i)+m/2, bO + 4*m, n*(1+i)+m/2);

			// activation (theta)
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + 4*m, n*(1+i), m, m, 90, 180);

			// adjusted input (output into the brain
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + 4*m, n*(1+i), m, m, 90, -180);
		}

		for (int j=0; j < network.getSizeHidden(); j++) {
			for (int i=0; i < network.getNumInputs(); i++) {
				// weight
				factor = (float) ( factors[nf++] / (2.0 * MW));
				clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
				g2.setColor(clr);
				g2.drawLine(bO + 5*m, n*(1+i)+m/2, bO + (5+mm)*m, n*(1+j)+m/2);
			}
			// activation (theta)
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + (5+mm)*m, n*(1+j), m, m, 90, 180);

			// inner output
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + (5+mm)*m, n*(1+j), m, m, 90, -180);
		}

		for (int k=1; k < network.getNumHidden(); k++) {
			for (int j=0; j < network.getSizeHidden(); j++) {
				for (int i=0; i < network.getSizeHidden(); i++) {
					// weight
					factor = (float) ( factors[nf++] / (2.0 * MW));
					clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
					g2.setColor(clr);
					g2.drawLine(bO + (5+((1+mm)*k))*m, n*(1+i)+m/2, bO + ((5+mm)+((1+mm)*k))*m, n*(1+j)+m/2);
				}
				// activation (theta)
				factor = (float) ( factors[nf++] / (2.0 * MW));
				clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
				g2.setColor(clr);
				g2.fillArc(bO + ((5+mm)+((1+mm)*k))*m, n*(1+j), m, m, 90, 180);

				// inner output
				factor = (float) ( factors[nf++] / (2.0 * MW));
				clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
				g2.setColor(clr);
				g2.fillArc(bO + ((5+mm)+((1+mm)*k))*m, n*(1+j), m, m, 90, -180);
			}
		}

		int sM = ((6+mm)+((1+mm)*(network.getNumHidden()-1)))*m;

		for (int j=0; j < network.getNumOutputs(); j++) {
			for (int i=0; i < network.getSizeHidden(); i++) {
				// weight
				factor = (float) ( factors[nf++] / (2.0 * MW));
				clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
				g2.setColor(clr);
				g2.drawLine(bO + sM, n*(1+i)+m/2, bO + sM+mm*m, n*(1+j)+m/2);
			}
			// activation (theta)
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + sM+mm*m, n*(1+j), m, m, 90, 180);

			// final output
			factor = (float) ( factors[nf++] / (2.0 * MW));
			clr = new Color( factor<0f?1f:0f,factor>0f?1f:0f,factor==0f?1f:0f,1f-Math.abs(factor*2f));
			g2.setColor(clr);
			g2.fillArc(bO + sM+mm*m, n*(1+j), m, m, 90, -180);

			//g2.setColor(Color.BLACK);
			String OV = Double.toString(Math.round(factors[nf-1] * 1000000.0) / 1000000.0);
			int OVwidth = g2.getFontMetrics().stringWidth(OV);
			g2.drawString(OV, bO + sM+(mm+2)*m, n*(1+j)+m);
			if (OVwidth > fieldWidth) {
				g2.clearRect(bO + sM+(mm+2)*m + fieldWidth, n*(1+j), OVwidth - fieldWidth, m);
			}
		}
	}
}