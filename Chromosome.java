import java.util.*;

/*
	Programmer: Daniel J. Boston
	Date: May 7, 2007
	Class: CS 370

	A sequence of genes and associated support, creation, and output routines.
*/
public class Chromosome
{
	private Vector<Gene> genes; // Bag of genes.
	private int nGenes;			// number of genes.

	public Chromosome()
	{
		genes = new Vector<Gene>();
		nGenes = 0;
	}

	// Read in a Chromosome based on the output standard of Chromosome.toString()
	public void setGenes(String geneString)
	{
		if (geneString.length() > 0)
		{
			int p = 0;
			int n = 0;
			String geneSet;

			while (p < geneString.length())
			{
				p = geneString.indexOf('[',p) + 1; // find next sequence

				n = geneString.indexOf(']',p);

				geneSet = geneString.substring(p,n); // isolate it

				addGene(new Gene(geneSet)); // add it to the gene pool.

				p = n + 1; // find next.
			}
		}
	}

	// Read in a Chromosome based on the output standard of Chromosome.toEncodedString()
	public void setGenesEncoded(String geneString)
	{
		if (geneString.length() > 0)
		{
			// First little bit is encoding size per gene.
			int p = geneString.indexOf('[',0) + 1;
			int n = geneString.indexOf(']',0);
			String geneS = geneString.substring(p,n);

			int geneSize = Integer.parseInt(geneS);

			p = n+1;
			// Next little bit is size of boolean extraction per gene.
			p = geneString.indexOf('[',p) + 1;
			n = geneString.indexOf(']',p);
			geneS = geneString.substring(p,n);

			int geneBits = Integer.parseInt(geneS);

			p = n+1;

			while (p < geneString.length())
			{
				geneS = geneString.substring(p, p + geneSize); // get the encoded string.

				addGene(new Gene(geneS, geneBits)); // Gene does the decoding.

				p += geneSize;
			}
		}
	}

	// append a gene to the end of this chromosome
	public boolean addGene(Gene toAdd)
	{
		if (toAdd != null)
		{
			genes.add(toAdd);
			nGenes ++;
			return true;
		}
		return false;
	}

	// get a specific gene from the chromosome, or null if index is out of bounds.
	public Gene getGene(int _idx)
	{
		if ((_idx >= 0) && (_idx < nGenes))
		{
			return genes.get(_idx);
		}
		else
			return null;
	}

	public int numGenes()
	{
		return nGenes;
	}

	/*
		Crossover as defined in the notes, with the following exception -- the notes only talk about
		  single point crossover. This function allows you to specify how many times to crossover the chromosomes,
		  so that you can have more or less crossovers, giving lots of control to interested parties.
	*/
	public Chromosome crossover(Chromosome _b, int times)
	{
		if (times > 0) // need to crossover at least once!
		{
			int[] crosspoints = new int[times]; // The points inside the chromosome at which to initiate crossover.

			boolean longer = (this.nGenes >= _b.numGenes());
			int maxPoint = (longer) ? _b.numGenes() : this.nGenes; // uses the smaller of the two
			int longest = (longer) ? this.nGenes : _b.numGenes();

			boolean curChromo = true; // true = this, false = _b

			Chromosome source = (curChromo) ? this: _b;

			// pick the crossover points
			for (int l = 0; l < times; l++)
			{
				crosspoints[l] = (int) (Math.random() * maxPoint);
			}

			// sort them, so that they occur in a line
			if (times > 2)
			{
				for (int a = 0; a < times - 1; a ++) // insertion sort
				{
					for (int b = a + 1; b > 0; b--)
					{
						if (crosspoints[b] < crosspoints[b - 1])
						{
							int temp = crosspoints[b-1];
							crosspoints[b-1] = crosspoints[b];
							crosspoints[b] = temp;
						}
					}
				}
			}
			else if (times == 2)
			{
				if (crosspoints[0] > crosspoints[1])
				{
					int temp = crosspoints[0];
					crosspoints[0] = crosspoints[1];
					crosspoints[1] = temp;
				}
			}

			// The contract of this method will return a chromosome equal in length to the longest chromosome.
			Chromosome crossed = new Chromosome();

			int j = 0;
			for (int i = 0; i < longest; i++) // crossover -- start with the one chromosome, then swap to the other.
			{
				if ((j < times) && ( i < maxPoint))
				{
					if (i == crosspoints[j])
					{
						curChromo = !curChromo;
						j++;
					}
				}
				if( i == maxPoint )
				{
					curChromo = longer;
				}

				source = (curChromo) ? this: _b;

				crossed.addGene(source.getGene(i).clone());
			}

			return crossed;
		}
		return null;
	}

	// Clone is the other primary operation. Just builds a new chromosome that duplicates the old one.
	public Chromosome clone()
	{
		Chromosome ret = new Chromosome();

		for (int i = 0; i < nGenes; i++)
			ret.addGene(this.getGene(i).clone());

		return ret;
	}

	// Mutate the chromosomes -- passes the mutation along to a random gene in the chromosome.
	public Chromosome mutate()
	{
		int mutatepoint = (int) (Math.random() * nGenes);

		Chromosome ret = this.clone();

		ret.getGene(mutatepoint).mutate();

		return ret;
	}

	// Build a new, random chromosome -- a "factory" method.
	public static Chromosome randomChromosome(int nGenes, int nSize)
	{
		Chromosome ret = new Chromosome();

		for (int b = 0; b < nGenes; b ++)
		{
			ret.addGene(new Gene(nSize));
		}

		return ret;
	}

	// Output this chromosome as a string.
	public String toString()
	{
		StringBuffer ret = new StringBuffer();

		for (int b = 0; b < nGenes; b ++)
		{
			ret.append("[");
			ret.append( getGene(b).toString() );
			ret.append("]");
		}

		return ret.toString();
	}

	// Output this chromosome as an encoded string.
	public String toEncodedString()
	{
		StringBuffer ret = new StringBuffer();

		ret.append("[");
		ret.append(getGene(0).encodingByteSize());
		ret.append("][");
		ret.append(getGene(0).encodingFieldSize());
		ret.append("]");

		for (int b = 0; b < nGenes; b++)
		{
			ret.append( getGene(b).toEncodedString() );
		}

		return ret.toString();
	}

	// Output this chromosome as a sequence of double values.
	public String toStringDouble()
	{
		StringBuffer ret = new StringBuffer();

		for (int b = 0; b < nGenes; b ++)
		{
			ret.append("[");
			ret.append( getGene(b).toDouble() );
			ret.append("]");
		}

		return ret.toString();
	}
}

/*
	Gene subclass -- it is stored as a sequence of binary values.
*/
class Gene
{
	private boolean[] geneValues;

	// Builds a random Gene of size "size"
	public Gene(int size)
	{
		geneValues = new boolean[size];

		fillGenes();
	}

	// Builds a gene based on the passed boolean array
	public Gene(boolean[] genevals)
	{
		geneValues = new boolean[genevals.length];

		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = genevals[a];
		}
	}

	// Builds a gene based on the passed string -- allows to build based on an inputted string.
	public Gene(String geneval)
	{
		geneValues = new boolean[geneval.length()];

		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = (geneval.charAt(a) == '1') ? true : false;
		}
	}

	// Builds a gene based on an encoded string (for compactness in storage).
	public Gene(String cod, int enc)
	{
		byte[] arr = cod.getBytes();

		int leftover = enc % 7;

		geneValues = new boolean[enc];

		int a = 0;

		int l = 0;

		for (int b = 0; b < arr.length; b++)
		{
			l = ((arr[b] < 0) ? ((- (int) arr[b]) + (int) Byte.MAX_VALUE) : (int) arr[b]) - (int) 33; // skip ASCII control block (chars 0-32)

			for (int i = 0; i < 7; i++)
			{
				int k = l % 2;

				if (!(b == 0 && i < ((7 - leftover)%7)))
				{
					if (k == 1)
						geneValues[a++] = true;
					else
						geneValues[a++] = false;
				}

				l = (int) Math.floor(l / 2.0);
			}
		}
	}

	/*public Gene(String cod, int enc)
	{
		byte[] arr = cod.getBytes();

		int leftover = enc % 7;

		geneValues = new boolean[enc];

		int a = 0;

		int l = 0;

		for (int b = 0; b < arr.length; b++)
		{
			l = (int) arr[b];

			for (int i = 0; i < 7; i++)
			{
				int k = l % 2;

				if (!(b == 0 && i < 7 - leftover))
				{
					if (k == 1)
						geneValues[a++] = true;
					else
						geneValues[a++] = false;
				}

				l = (int) Math.floor(l / 2.0);
			}
		}
	}*/

	// Fill up the new empty gene -- an initialization method -- with random values.
	private void fillGenes()
	{
		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = (Math.random() < .5) ? false : true;
		}
	}

	// booleans = 0.[0][1][2][3][4][5][6][7] etc.
	public float toFloat()
	{
		return (float) toDouble();
	}

	// outputs a number based on adding up negative powers of two based on the values of genes
	//  1 -- add that power, 0 -- don't add it.
	public double toDouble()
	{
		double ret = 0.0;

		for (int a = 0; a < geneValues.length; a++)
		{
			if (geneValues[a])
				ret += Math.pow(2, -(a + 1)); // 2^(-a-1)
		}
		return ret;

	}

	// Return the boolean array that this gene is based on.
	public boolean[] getGene()
	{
		return geneValues;
	}

	// Output a string representation of this gene.
	public String toString()
	{
		StringBuffer ret = new StringBuffer();

		for (int a = 0; a < geneValues.length; a++)
		{
			if (geneValues[a])
				ret.append("1");
			else
				ret.append("0");
		}

		return ret.toString();
	}

	public String toEncodedString()
	{
		int leftover = geneValues.length % 7;

		byte[] arr = new byte[ (int) Math.ceil((double) geneValues.length / 7.0) ];

		int b = 0; int i = (7 - leftover) % 7;

		int thisbyte = 33;

		for (int a = 0; a < geneValues.length; a++)
		{
			thisbyte += (int) ( (geneValues[a]) ? Math.pow(2, i) : 0 );

			i++;
			if (i == 7)
			{
				arr[b] = (byte) ((thisbyte > (int) Byte.MAX_VALUE) ? (- (thisbyte - (int) Byte.MAX_VALUE)) : thisbyte);
				thisbyte = 33; // all this insanity to miss the ASCII control code block from 0-32. Also, we maximize usage of Byte by incorporating negative numbers :).
				b ++;
				i = 0;
			}
		}
		if (b < arr.length)
			arr[b] = (byte) ((thisbyte > (int) Byte.MAX_VALUE) ? (- (thisbyte - (int) Byte.MAX_VALUE)) : thisbyte);

		return new String(arr);
	}

/*	public String toEncodedString()
	{
		boolean[] enc = geneValues;

		int leftover = geneValues.length % 7;

		byte[] arr = new byte[ (int) Math.ceil((double) geneValues.length / 7.0) ];

		int b = 0; int i = 7 - leftover;

		for (int a = 0; a < geneValues.length; a++)
		{
			arr[b] += (byte) ( (geneValues[a]) ? Math.pow(2, i) : 0 );

			i++;
			if (i == 7)
			{
				b ++;
				i = 0;
			}
		}

		return new String(arr);
	}*/

	public int encodingByteSize()
	{
		return (int) Math.ceil((double) geneValues.length / 7.0);
	}

	public int encodingFieldSize()
	{
		return geneValues.length;
	}

	// Mutate the gene at some random point.
	public void mutate()
	{
		// pick a random spot to mutate.

		int b = (int) Math.floor(Math.random() * (double) geneValues.length);

		geneValues[b] = !geneValues[b]; // flip the bit!
	}

	// Clone the gene -- make an exact duplicate
	public Gene clone()
	{
		Gene ret = new Gene(getGene());
		return ret;
	}


}