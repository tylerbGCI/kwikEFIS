package player.ulib;


public class DigitalFilter
{
	private int BUF_SIZE = 16;
	private float[] x; 
	private float[] y; 
	
	public DigitalFilter()
	{
		x = new float[BUF_SIZE];  
		y = new float[BUF_SIZE];  
	}
	
	public DigitalFilter(int bufferSize)
	{
		BUF_SIZE = bufferSize;
		x = new float[BUF_SIZE];  
		y = new float[BUF_SIZE];  
	}
	
	// Low Pass filter
	// the smoothing factor, ALPHA = deltaT/(tau + deltaT)
	// If ALPHA =  0.5, then the tau time constant is equal to the sampling period. 
	// If ALPHA << 0.5  then tau is significantly larger than the sampling interval, and 
	// deltaT is approximately equal to ALPHA * tau
	private static final float ALPHA = 0.25f; // .25 if ALPHA = 1 OR 0, no filter applies.
	public float[] lowPass(float[] y, float[] x) 
	{
	    if ( y == null ) return x;     
	    y[0] = x[0];
	    for ( int i = 1; i < x.length; i++ ) {
	        y[i] = y[i-1] + ALPHA * (x[i] - y[i-1]);
	    }
	    return y; 
	}	

	
	private void push(float a)
	{
		x[0] = a;
	    for ( int i = BUF_SIZE-1; i > 0; i-- ) {
	    	x[i] = x[i-1];
	    }
	}
	
	// running BUF_SIZE point low pass filter
	public float lowPass(float a) 
	{
		return runningAverage(a);
	}	

	public float runningAverage(float a) 
	{
		float total = x[0] = a;
		
	    for ( int i = BUF_SIZE-1; i > 0; i-- ) {
	    	x[i] = x[i-1];
	    	total = total + x[i]; 
	    }
	    return (total / BUF_SIZE);
	}	

	public float runningAverage(double a)
	{
		double total = x[0] = (float) a;
		
	    for ( int i = BUF_SIZE-1; i > 0; i-- ) {
	    	x[i] = x[i-1];
	    	total = total + x[i]; 
	    }
	    return (float) (total / BUF_SIZE);
	}
	
	
}

