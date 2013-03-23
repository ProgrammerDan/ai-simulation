import java.io.*;

/*
	Programmer: Daniel J. Boston
	Date: April 17, 2007
	Class: ---all---

	This is a generalized support class to handle keyboard input, and output to a screen in a pretty fashion.
	To include it in a compile, make sure the Keyboard.java file is in the same directory as the referencing
	source file, and include it in the javac input line.

	Example:
		javac Simulation.java Keyboard.java

*/
public class Keyboard
{
	BufferedReader in;

	/*
		Build using System.in
	*/
	public Keyboard()
	{
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	/*
		Build using a specified System.in
	*/
	public Keyboard(InputStream _in)
	{
		in = new BufferedReader(new InputStreamReader(_in));
	}

	public void printf(String _token)
	{
		printfull( _token );
	}

	/*
		Prints a token repeatedly to fill a single row of screen text.
	*/
	public void printfull(String _token)
	{
		int len = _token.length();
		int repeat = 79 / len;

		StringBuffer temp = new StringBuffer();
		for (int a = 0; a < repeat; a++)
			temp.append(_token);

		System.out.println(temp.toString());
	}

	public void printl(String _s)
	{
		print(_s, -1);
	}

	public void printr(String _s)
	{
		print(_s, 1);
	}

	public void printc(String _s)
	{
		print(_s, 0);
	}

	/*
		Prints a string with the specified alignment -- to the left, centered, or to the right.
		_n is the alignment control -- 0 for centered, 1 for right justify, and -1 (or else) for
		left justify.
	*/
	public void print(String _s, int _n)
	{
		int len = _s.length();
		if (len > 79)
		{
			print(_s.substring(0,79), _n);
			print(_s.substring(79,len), _n); // recursive
			return;
		}
		StringBuffer temp = new StringBuffer();
		int border;
		switch (_n)
		{
		case 0: // center justify
			border = (79 - len) / 2;

			for (int a = 0; a < border; a++)
				temp.append(" ");

			System.out.println(temp.toString() + _s);
			break;
		case 1: // right justify
			border = (79 - len);

			for (int a = 0; a < border; a++)
				temp.append(" ");

			System.out.println(temp.toString() + _s);
			break;
		default: // left justify
			System.out.println(_s);
			break;
		}
	}

	/*
		Gets a character from the input stream, and returns it as an integer.
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

	/*
		Gets a string representing a line of input (a sequence of characters terminated by a carriage return) from the input stream.
	*/
	public String getLine() throws IOException
	{
		return in.readLine();
	}
}