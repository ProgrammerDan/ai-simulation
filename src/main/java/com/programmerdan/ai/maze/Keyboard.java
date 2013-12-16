package com.programmerdan.ai.maze;

import java.io.*;

/**
 * This is a generalized support class to handle keyboard input, and output to a screen in a pretty fashion.
 *
 * @author Daniel Boston <programmerdan@gmail.com>
 * @version 1.0 April 17, 2007
 *   Initial version
 * @version 1.01 December 15, 2013
 *   Cosmetic and other updates, comments, etc.
 */
public class Keyboard
{
	private BufferedReader in;

	/**
	 * Standard build using System.in
	 */
	public Keyboard()
	{
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
	 * Build using a specified {@link InputStream}
	 *
	 * @param	in		The InputStream to use.
	 */
	public Keyboard(InputStream in)
	{
		this.in = new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Shortcut for {@link printfull(String)}
	 *
	 * @param	token	the String to print.
	 * @see {@link printfull(String)}
	 */
	public void printf(String token)
	{
		printfull( token );
	}

	/**
	 * Prints a token repeatedly to fill a single row of screen text.
	 *   A screen is assumed to be 80 characters in width.
	 *
	 * @param	token	the String token to print repeatedly.
	 */
	public void printfull(String token)
	{
		int len = token.length();
		int repeat = 79 / len;

		StringBuffer temp = new StringBuffer();
		for (int a = 0; a < repeat; a++)
			temp.append(token);

		System.out.println(temp.toString());
	}

	/**
	 * Shortcut to {@link print(String, int)}, setting alignment to "left"
	 *
	 * @param	s	The string to print, left aligned.
	 */
	public void printl(String s)
	{
		print(s, -1);
	}

	/**
	 * Shortcut to {@link print(String, int)}, setting alignment to "right"
	 *
	 * @param	s	The string to print, right aligned.
	 */
	public void printr(String s)
	{
		print(s, 1);
	}

	/**
	 * Shortcut to {@link print(String, int)}, setting alignment to "center"
	 *
	 * @param	s	The string to print, center aligned.
	 */
	public void printc(String s)
	{
		print(s, 0);
	}

	/**
	 * Prints a string with the specified alignment -- to the left, centered, or to the right.
	 *
	 * @param	s	The String to print, aligned.
	 * @param	n 	The alignment control -- 0 for centered, 1 for right justify, and -1 (or else) for
	 *				  left justify.
	 */
	public void print(String s, int n)
	{
		int len = s.length();
		if (len > 79)
		{
			print(s.substring(0,79), n);
			print(s.substring(79,len), n); // recursive
			return;
		}
		StringBuffer temp = new StringBuffer();
		int border;
		switch (n)
		{
		case 0: // center justify
			border = (79 - len) / 2;

			for (int a = 0; a < border; a++)
				temp.append(" ");

			System.out.println(temp.toString() + s);
			break;
		case 1: // right justify
			border = (79 - len);

			for (int a = 0; a < border; a++)
				temp.append(" ");

			System.out.println(temp.toString() + s);
			break;
		default: // left justify
			System.out.println(s);
			break;
		}
	}

	/**
	 * Gets a character from the input stream, and returns it as an integer.
	 *
	 * @return	The character returned
	 * @throw	IOException	based on a failure to read a character from the InputStream.
	 */
	public int getChr() throws IOException
	{
		int inchr = in.read();
		while (inchr < 0 || inchr == 10 || inchr == 13)
		{
			inchr = in.read();
		}

		return inchr;
	}

	/**
	 * Gets a string representing a line of input (a sequence of characters terminated by a carriage return) from the input stream.
	 *
	 * @return	The String line to return
	 * @throw	IOException based on a failure to read a String line from the InputStream.
	 */
	public String getLine() throws IOException
	{
		return in.readLine();
	}
}