package com.programmerdan.ai.maze;

/**
 * Gene subclass. Every {@link Chromosome} is composed of many Genes.
 *   Each Gene is stored as a sequence of binary values, this allows easy mutations.
 *
 * @author Daniel Boston
 * @version 1.0 May 7, 2007
 *   Initial release
 * @version 1.01 December 17, 2013
 *   Refactor into its own class.
 *
 */
public class Gene implements Cloneable
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