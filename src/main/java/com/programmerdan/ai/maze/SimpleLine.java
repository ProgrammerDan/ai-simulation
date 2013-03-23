/**
 * A simple line, just the four data points and accessors/mutators.
 */
public class SimpleLine
{
	private double x1 = 0.0;
	public  double x1   ()            { return x1;    }
	public  void   setX1( double x1 ) { this.x1 = x1; }
	private double y1 = 0.0;
	public  double y1   ()            { return y1;    }
	public  void   setY1( double y1 ) { this.y1 = y1; }
	private double x2 = 0.0;
	public  double x2   ()            { return x2;    }
	public  void   setX2( double x2 ) { this.x2 = x2; }
	private double y2 = 0.0;
	public  double y2   ()            { return y2;    }
	public  void   setY2( double y2 ) { this.y2 = y2; }

	/**
	 * Creates a new simple line
	 */
	public SimpleLine(double x1, double y1, double x2, double y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public SimpleLine()
	{
		x1 = 0.0;
		x2 = 0.0;
		y1 = 0.0;
		y2 = 0.0;
	}
}
