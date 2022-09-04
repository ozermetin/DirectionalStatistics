package utils;

public class BoundingSphere {
	
	int[][] points;
	double[] centerCoordinates;
	double radius;
	int numberofElements;
	int currElementSize;
	
	/*
	 * nx3 matrix
	 */
	public BoundingSphere(int[][] points)
	{
		this.points=points;
		centerCoordinates = new double[3];
		this.numberofElements = points.length;
		currElementSize=points.length;
	}
	
	public BoundingSphere(int numberofElements)
	{
		this.points= new int[numberofElements][3] ;
		centerCoordinates = new double[3];
		this.numberofElements = points.length;
		currElementSize = 0;
	}
	
	public void build(int[] coordinates)
	{
		this.points[currElementSize]=coordinates;
		this.currElementSize++;
	}
	
	public void calculate()
	{
		double currDist=-1;
		int index=0;
		for(int i=1;i<numberofElements;i++)
		{
			double d2=dist(points[0],points[i]);
			if(d2>currDist)
			{
				currDist=d2;
				index=i;
			}
		}
		
		int secondIndex=index;
		currDist=-1;
		for(int i=0;i<numberofElements;i++)
		{
			double d2=dist(points[index],points[i]);
			if(d2>currDist)
			{
				currDist=d2;
				secondIndex=i;
			}
		}
		
		radius=dist(points[index],points[secondIndex]);
		
		centerCoordinates[0]=(points[index][0]+points[secondIndex][0])/2.;
		centerCoordinates[1]=(points[index][1]+points[secondIndex][1])/2.;
		centerCoordinates[2]=(points[index][2]+points[secondIndex][2])/2.;
		
		for(int i=0;i<numberofElements;i++)
		{
			if(!isInside(points[i]))
			{
				radius=dist(centerCoordinates,points[i]);
			}
		}
	}
	
	
	
	public static double dist(double[] x, double[]y)
	{
		return(Math.sqrt((double)(Math.pow((x[0]-y[0]),2)+Math.pow((x[1]-y[1]),2)+Math.pow((x[2]-y[2]),2))));
	}
	
	public static double dist(double[] x, int[]y)
	{
		return(Math.sqrt((double)(Math.pow((x[0]-y[0]),2)+Math.pow((x[1]-y[1]),2)+Math.pow((x[2]-y[2]),2))));
	}
	public static double dist(int[] x, int[]y)
	{
		return(Math.sqrt((double)(Math.pow((x[0]-y[0]),2)+Math.pow((x[1]-y[1]),2)+Math.pow((x[2]-y[2]),2))));
	}
	
	private boolean isInside(int[] x)
	{
		return radius>dist(centerCoordinates,x);
	}

	public double[] getCenterCoordinates() {
		return centerCoordinates;
	}

	public double getRadius() {
		return radius;
	}
	
	public BoundingSphereHolder getBoundingSphere() {
		return new BoundingSphereHolder(centerCoordinates, radius);
	}

}
