package com.programmerdan.ai.maze;

/**
 * Testing various encoding and decoding techniques, used by the Chromosome class.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 2010 (more recent than other code)
 * TODO: Refactor as actual tests!
 */
public class EncodeTest
{
	/**
	 * Main test function, should be in an actual JUnit test (TODO!)
	 *
	 * @param	args	Unused.
	 */
	public static void main(String[] args)
	{
		double aD = .52543;

		boolean[] aBB = new boolean[14];
		String aSS = code(aD, aBB);

		String aSD = deepcode(aBB);
		boolean[] aBD = deepdecode(aSD, aBB.length);

		String aSB = "";
		for (int i = 0; i < aBD.length; i++)
			aSB += (aBD[i]) ? "1" : "0";

		System.out.println("a) SS: " + aSS + " SP: " + decode(aBB) + " SD: [" + aSD + "]");
		System.out.println("a) SB: " + aSB + " SZ: " + decode(aBD));

		long k = 0; long p = 0;
		while (true)
		{
			double bD = Math.random();

			boolean[] bBB = new boolean[14];
			String bSS = code(bD, bBB);

			String bSD = deepcode(bBB);
			boolean[] bBD = deepdecode(bSD, bBB.length);

			String bSB = "";
			for (int i = 0; i < bBD.length; i++)
				bSB += (bBD[i]) ? "1" : "0";

			if (!(bSS.equals(bSB)) || bSD.indexOf("\r") >= 0 || bSD.indexOf("\n") >= 0)
			{
				System.out.println("Encoding error:");

				System.out.println("b) SS: " + bSS + " SP: " + decode(bBB) + " SD: [" + bSD + "]");
				System.out.println("b) SB: " + bSB + " SZ: " + decode(bBD));

				break;
			}

			if (k % 1000l == 0)
			{
				System.out.print(".");
				p++;

				if (p == 60)
				{
					System.out.println(k);
					p = 0;
				}
			}
			k++;
		}
	}

	/**
	 * Encode a double as a boolean, to the precision
	 *   indicated by the length of the boolean array.
	 *
	 * @param	num		The number to encode.
	 * @param	enc		The boolean array to fill.
	 * @return			String representation of the boolean Array.
	 */
	public static String code(double num, boolean[] enc)
	{
		String str = "";

		for (int k = 0; k < enc.length; k++)
		{
			num *= 2.0;

			if (num >= 1.0)
			{
				enc[k] = true;
				str += "1";
				num -= 1.0;
			}
			else
			{
				enc[k] = false;
				str += "0";
			}
		}

		return str;
	}

	/**
	 * Deep encode a boolean array as String (into a byte array)
	 *
	 * @param	enc		The boolean array to encode
	 * @return			A string formed from bytes based on the boolean array.
	 */
	public static String deepcode(boolean[] enc)
	{
		int leftover = enc.length % 7;

		byte[] arr = new byte[ (int) Math.ceil((double) enc.length / 7.0) ];

		int b = 0; int i = (7 - leftover) % 7;

		int thisbyte = 33;

		for (int a = 0; a < enc.length; a++)
		{
			thisbyte += (int) ( (enc[a]) ? Math.pow(2, i) : 0 );

			i++;
			if (i == 7)
			{
				arr[b] = (byte) ((thisbyte > (int) Byte.MAX_VALUE) ? (- (thisbyte - (int) Byte.MAX_VALUE)) : thisbyte);
				thisbyte = 33;
				b ++;
				i = 0;
			}
		}
		if (b < arr.length)
			arr[b] = (byte) ((thisbyte > (int) Byte.MAX_VALUE) ? (- (thisbyte - (int) Byte.MAX_VALUE)) : thisbyte);

		return new String(arr);
	}

	/**
	 * Decodes a deep encoded String (see {@link deepcode(boolean[])}) into a
	 * boolean array.
	 *
	 * @param	cod		The code String to decode.
	 * @param	enc		The size of the encoding
	 * @return			A boolean array representing the decoded String, to the precision of enc.
	 */
	public static boolean[] deepdecode(String cod, int enc)
	{
		byte[] arr = cod.getBytes();

		int leftover = enc % 7;

		boolean[] dec = new boolean[enc];

		int a = 0;

		int l = 0;

		for (int b = 0; b < arr.length; b++)
		{
			l = ((arr[b] < 0) ? ((- (int) arr[b]) + (int) Byte.MAX_VALUE) : (int) arr[b]) - (int) 33;

			for (int i = 0; i < 7; i++)
			{
				int k = l % 2;

				if (!(b == 0 && i < ((7 - leftover) % 7)))
				{
					if (k == 1)
						dec[a++] = true;
					else
						dec[a++] = false;
				}

				l = (int) Math.floor(l / 2.0);
			}
		}

		return dec;
	}

	/**
	 * Decodes a boolean array into a double value.
	 *
	 * @param	enc		The boolean array to decode.
	 * @return			The double value decoded from the array.
	 */
	public static double decode(boolean[] enc)
	{
		double dec = 0.0;

		for (int a = 0; a < enc.length; a++)
		{
			if (enc[a])
				dec += Math.pow(2, -(a + 1));
		}

		return dec;
	}

}