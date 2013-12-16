package com.programmerdan.ai.maze;

import java.util.*;

/**
 * A Chromosome is traditionally a sequence of genes. This class mirrors that concept,
 *   and adds in a host of support, creation, and output routines.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 May 7, 2007
 *    Initial version, basic Chromosome implementation with encoding and decoding
 * @version 1.01 December 15, 2013
 *    Cleanup, comments, logger, and related.
 * @see {@link Gene}
 */
public class Chromosome implements Cloneable
{
	/**
	 * Bag of genes.
	 */
	private Vector<Gene> genes;
	/**
	 * Current number of genes.
	 */
	private int nGenes;

	/**
	 * Instantiates a new Chromosome with no genes.
	 */
	public Chromosome()
	{
		genes = new Vector<Gene>();
		nGenes = 0;
	}

	/**
	 * Append to this Chromosome a set of Genes based on the output standard of Chromosome.toString()
	 *
	 * @param	geneString	A String of genes
	 * @see {@link toString()} describes the input format of the String for this function.
	 *
	 * TODO: This is an append, not a set. Refactor.
	 */
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

	/**
	 * Append to this Chromosome a set of Genes based on the output standard of Chromosome.toEncodedString()
	 *
	 * @param	geneString	A String of encoded genes
	 * @see {@link toEncodedString()} describes the input format of the String for this function.
	 */
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

	/**
	 * Append a {@link Gene} to the end of this Chromosome.
	 *
	 * @param	toAdd	the Gene to add.
	 * @return			True if successful, False otherwise.
	 */
	public boolean addGene(Gene toAdd)
	{
		if (toAdd != null)
		{
			if (genes.add(toAdd)) {
				nGenes ++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Append a {@link Gene} to the end of this Chromosome, where
	 * the gene is described by its Double (encoded) value, and the
	 * target size of the resulting Gene (a series of bits).
	 *
	 * @param	toAdd	the "value" of the Gene. Must be in the range [0.0, 1.0).
	 * @param	size	the size of the Gene.
	 * @return			True if successfully added, False otherwise.
	 */
	public boolean addGene(double toAdd, int size)
	{
		if ( toAdd >= 0.0 && toAdd < 1.0 && size > 0) {
			if ( genes.add(new Gene(toAdd, size)) ) {
				nGenes ++;
				return true;
			}
		}

		return false;
	}

	/**
	 * Get a specific gene from the chromosome, or null if index is out of bounds.
	 *
	 * @param	idx	The Gene at index idx to retrieve.
	 * @return		A {@link Gene} at index idx, or exception
	 * @throws	IndexOutOfBoundsException if idx cannot be found.
	 */
	public Gene getGene(int idx)
	{
		if ((idx >= 0) && (idx < nGenes))
		{
			return genes.get(idx);
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Returns the number of genes in this Chromosome.
	 *
	 * @return	the number of Genes in this Chromosome.
	 * TODO: Why do I keep a separate index when I can query the list itself?
	 */
	public int numGenes()
	{
		return nGenes;
	}

	/**
	 * Crossover is simple -- it is a point where two Chromosomes, during reproduction, "cross" -- e.g. where the
	 *   first Chromosome stops contributing fully to the new Chromosome, and the second Chromosome takes over.
	 * Many standard models of crossover are limited to single point crossover. This function allows you to specify
	 *   how many times to crossover the chromosomes, so that you can have more or less crossovers, giving lots of
	 *   control to interested parties.
	 *
	 * @param	b		The second Chromosome to involve in crossover. Implied is {@code this} is the first Chromosome.
	 * @param	times	The number of times to crossover. Must be a counting integer (greater than 0).
	 * @return			A new Chromosome which is a blend of both original Chromosomes.
	 * @throws RuntimeException if times is less than one
	 */
	public Chromosome crossover(Chromosome b, int times)
	{
		if (times > 0) // need to crossover at least once!
		{
			int[] crosspoints = new int[times]; // The points inside the chromosome at which to initiate crossover.

			boolean longer = (this.nGenes >= b.numGenes());
			int maxPoint = (longer) ? b.numGenes() : this.nGenes; // uses the smaller of the two
			int longest = (longer) ? this.nGenes : b.numGenes();

			//random starting chromosome.
			boolean curChromo = (Math.random() > 0.5) ? true : false ; // true = this, false = b

			Chromosome source = (curChromo) ? this: b;

			// pick the crossover points
			for (int l = 0; l < times; l++)
			{
				crosspoints[l] = (int) (Math.random() * maxPoint);
			}

			// sort them, so that they occur in a line
			Arrays.sort(crosspoints);

			// The contract of this method will return a chromosome equal in length to the longest chromosome.
			Chromosome crossed = new Chromosome();

			int j = 0;
			for (int i = 0; i < longest; i++) // crossover -- start with the one chromosome, then swap to the other.
			{
				if ((j < times) && ( i < maxPoint))
				{
					/* Must be >= to adjust for instance where multiple cross points are equal.
					 * In this way, crossovers will happen successively.
					 */
					if (i >= crosspoints[j])
					{
						curChromo = !curChromo;
						j++;
					}
				}
				if( i == maxPoint )
				{
					curChromo = longer;
				}

				source = (curChromo) ? this: b;

				crossed.addGene(source.getGene(i).clone());
			}

			return crossed;
		}

		throw new RuntimeException("Cannot have less than one length crossover!");
	}

	/**
	 * Clone is the other primary operation beyond crossover. Just builds a new chromosome that duplicates the old one.
	 *   Note this is a deep copy (each object member is also cloned).
	 *
	 * @return	A new Chromosome having clones of each Gene of the original Chromosome.
	 */
	@Override
	public Chromosome clone()
	{
		final Chromosome ret;

		try {
			ret = (Chromosome) super.clone();
		} catch ( CloneNotSupportedException cnse) {
			throw new RuntimeException("Failed to clone Chromosome");
		}

		for (int i = 0; i < nGenes; i++) {
			ret.addGene(this.getGene(i).clone());
		}

		return ret;
	}

	/**
	 * This uses {@link clone()} with one alteration -- after cloning, a single random gene is mutated.
	 *
	 * @return	A new Chromosome having clones of each Gene of the original Chromosome, but with one mutation.
	 * TODO: Only a single mutation? Perhaps should allow multiple mutations, or probability model injection to control mutation.
	 *         Otherwise multiple mutations leads to huge object churn.
	 */
	public Chromosome mutate()
	{
		int mutatepoint = (int) (Math.random() * nGenes);

		Chromosome ret = this.clone();

		ret.getGene(mutatepoint).mutate();

		return ret;
	}

	/**
	 * Build a new, random chromosome -- a "factory" method.
	 *
	 * @param	nGenes	Number of random genes to generate
	 * @param	nSize	Size of each gene to generate.
	 * @return			A new Chromosome
	 * @throws RandomException if unable to generate a gene in the chromosome
	 */
	public static Chromosome randomChromosome(int nGenes, int nSize)
	{
		Chromosome ret = new Chromosome();

		for (int b = 0; b < nGenes; b ++)
		{
			if (!ret.addGene(new Gene(nSize))) {
				throw new RuntimeException("Failed to generate a gene in the chromosome!");
			}
		}

		return ret;
	}

	/**
	 * Output this chromosome as a String, encoding each Gene with the {@link Gene.toString()} method,
	 *   in the following form:
	 * {@code [gene1][gene2][gene3]...[geneN]}
	 *
	 * @return	A string representation of the Genes in this Chromosome.
	 */
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

	/**
	 * Output this chromosome as an encoded String, where each Gene is encoded using the {@link Gene.toEncodedString()} method,
	 *   in the following form:
	 * {@code [num bytes][field size]encodedGene1encodedGene2encodedGene3encodedGene4...}
	 *
	 * @return	A string representation of the Genes in this Chromosome, encoded.
	 * TODO: What if genes have different sizes in the Chromosome? This encoding seems very rudimentary.
	 */
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

	/**
	 * Output this chromosome as a sequence of double values, coded as a String, in the following format:
	 * {@code [geneAsDouble1][geneAsDouble2][geneAsDouble3]...}
	 *
	 * @return	A string representation of the Genes in this Chromosome, as Doubles.
	 */
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

/**
 * Gene subclass. Every {@link Chromosome} is composed of many Genes.
 *   Each Gene is stored as a sequence of binary values, this allows easy mutations.
 *
 * @author Daniel Boston
 * @version 1.0 May 7, 2007
 */
class Gene implements Cloneable
{
	private boolean[] geneValues;

	/**
	 * Builds a random Gene of size {@code size}.
	 *
	 * @param	size	the size of the random Gene.
	 * @see {@link fillGenes()}
	 */
	public Gene(int size)
	{
		geneValues = new boolean[size];

		fillGenes();
	}

	/**
	 * Builds a Gene based on the passed boolean array. Note that the passed in array
	 *   is not modified, a new array is built for the Gene to use.
	 *
	 * @param	genevals	the array of boolean values to base a Gene on.
	 */
	public Gene(boolean[] genevals)
	{
		geneValues = new boolean[genevals.length];

		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = genevals[a];
		}
	}

	/**
	 * Builds a gene based on the passed string with a very trivial encoding:
	 *   {@code 100101010} -- allowing a simple mapping of "binary" strings to a Gene.
	 *
	 * @param	geneval		the String to build a Gene from.
	 */
	public Gene(String geneval)
	{
		geneValues = new boolean[geneval.length()];

		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = (geneval.charAt(a) == '1') ? true : false;
		}
	}

	/**
	 * Builds a gene based on a double value. Note that all values are first made positive (absolute value)
	 *   and then reduced to their decimal portions only. This modified value is the one used for Gene creation.
	 * It is encoded into Binary with a trivial encoding -- multiply the value by two, if the result is above
	 *   one, add a "1" to the Gene sequence and subtract 1 from the value. Otherwise, add a "0". Continue
	 *   for the desired bitlength.
	 *
	 * @param	geneval		The double value to encode as a Gene.
	 * @param	size		The size of the encoded value in bits.
	 */
	public Gene(double geneval, int size)
	{
		geneValues = new boolean[size];

		geneval = Math.abs(geneval);
		if (geneval >= 1.0) geneval = geneval - Math.floor(geneval);

		for (int a = 0; a < size; a++)
		{
			geneval *= 2.0;

			if (geneval >= 1.0)
			{
				geneValues[a] = true;
				geneval -= 1.0;
			}
			else
			{
				geneValues[a] = false;
			}
		}
	}

	/**
	 * Builds a gene based on an encoded string (for compactness in storage). The encoding handles
	 *   the fact that the Bytes returned from a string may be both positive and negative, and that
	 *   the first 33 (control) characters should be skipped in any reasonable encoding.
	 * Review the code for more details.
	 * Note: I think I accidentally re-implemented Base64, or a variant.
	 *
	 * @param	cod		The coded String from which to construct a Gene
	 * @param	enc		The length of the Gene to produce from the encoded string
	 */
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

				if (!(b == 0 && i < ( ( 7 - leftover) % 7 ) ))
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

	/**
	 * Fill up the new empty gene -- an initialization method -- with random values.
	 */
	private void fillGenes()
	{
		for (int a = 0; a < geneValues.length; a++)
		{
			geneValues[a] = (Math.random() < .5) ? false : true;
		}
	}

	/**
	 * Reduced precision output of this Gene as a Float.
	 *
	 * @return	Float value of this Gene. May lose some precision.
	 * @see {@link toDouble()}
	 */
	public float toFloat()
	{
		return (float) toDouble();
	}

	/**
	 * Outputs a number based on adding up negative powers of two based on the values of genes
	 *   1 -- add that power, 0 -- don't add it.
	 *
	 * @return	Double value of this Gene. Might lose some precision, if the Gene's length is especially long.
	 */
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

	/**
	 * Return the boolean array that this gene is based on.
	 *
	 * @return	The array of boolean values that form this Gene.
	 */
	public boolean[] getGene()
	{
		return geneValues;
	}

	/**
	 * Output a String representation of this Gene. This is a trivial String encoding,
	 *   where for each boolean, a single character is output -- 1 for "True", 0 for "False".
	 *
	 * @return	the trivial String encoding of this Gene.
	 */
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

	/**
	 * Output a more complex encoded String representation of this Gene. The encoding is clever. For every seven bits,
	 *   we encode one byte. Positive and negative values are used, and the bytes values [0,32] are skipped, as they
	 *   form the control characters of ASCII when encoded into a String. This can cause display and other irregularities
	 *   so this encoding skips them -- which is also why negative numbers are used (to allow 127 values to be encoded into
	 *   a single Signed Byte).
	 *
	 * @return	A String encoded using this more complex and dense scheme.
	 */
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

	/**
	 * Returns the number of Bytes necessary to encode this Gene.
	 *
	 * @return The number of bytes to encode
	 */
	public int encodingByteSize()
	{
		return (int) Math.ceil((double) geneValues.length / 7.0);
	}

	/**
	 * Returns the number of bits in this Gene.
	 *
	 * @return Field size (number of bits)
	 */
	public int encodingFieldSize()
	{
		return geneValues.length;
	}

	/**
	 * Mutate the gene at some random point. This mutation is in-place, meaning that
	 *   the active Gene is modified.
	 */
	public void mutate()
	{
		// pick a random spot to mutate.

		int b = (int) Math.floor(Math.random() * (double) geneValues.length);

		geneValues[b] = !geneValues[b]; // flip the bit!
	}

	/**
	 * Clone the gene -- make an exact duplicate, using the boolean array that backs the Gene.
	 *
	 * @return a new Gene that duplicates this Gene.
	 *
	 * @see {@link Gene(boolean[])}
	 */
	@Override
	public Gene clone()
	{
		final Gene ret;

		try {
			ret = (Gene) super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new RuntimeException("Gene clone not supported.");
		}

		return ret;
	}
}