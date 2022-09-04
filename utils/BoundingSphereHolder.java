package utils;

import java.io.Serializable;

public class BoundingSphereHolder implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = -1454843653912395784L;
	double[] centerCoordinates;
	double radius;
	
	public BoundingSphereHolder(double[] centerCoordinates,double radius)
	{
		this.centerCoordinates=centerCoordinates;
		this.radius=radius;
	}

	public double[] getCenterCoordinates() {
		return centerCoordinates;
	}

	public double getRadius() {
		return radius;
	}
	
	public double distanceBetween(BoundingSphereHolder holder)
	{
		return BoundingSphere.dist(this.getCenterCoordinates(), holder.getCenterCoordinates());
	}
	
	public boolean isIntercept(BoundingSphereHolder holder)
	{
		double dist=BoundingSphere.dist(this.getCenterCoordinates(), holder.getCenterCoordinates());
		return this.radius/2+holder.getRadius()/2>=dist;
	}
}
