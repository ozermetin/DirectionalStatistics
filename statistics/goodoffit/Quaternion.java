package statistics.goodoffit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Quaternion {
	private double w,x,y,z;
	
	public Quaternion() {
		// TODO Auto-generated constructor stub
	}

	public Quaternion(double w, double x, double y,double z) {
		this.w=w;
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	public Quaternion inverse()
	{
		return new Quaternion(w,-x,-y,-z);
	}
	
	public Quaternion multiply(Quaternion by)
	{
		double a1=this.w,b1=this.x,c1=this.y,d1=this.z,a2=by.w,b2=by.x,c2=by.y,d2=by.z;
		
		Quaternion result = new Quaternion();
		result.w=a1*a2-b1*b2-c1*c2-d1*d2;
		result.x=a1*b2+b1*a2+c1*d2-d1*c2;
		result.y=a1*c2-b1*d2+c1*a2+d1*b2;
		result.z=a1*d2+b1*c2-c1*b2+d1*a2;
		return result;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	public static Quaternion convertToQuaternion(Vector3D initVector, Vector3D rotVector )
	{
	    	Vector3D vector3 = initVector.crossProduct(rotVector);
		
	    	double rotation = Vector3D.angle(initVector, rotVector);
	    	
	    	if(rotation==0)
	    	  return new Quaternion(0,0,0,0);  
		Vector3D rotationVector = vector3.normalize();		
		
		Quaternion quaRot=new Quaternion();
		quaRot.setX(rotationVector.getX()*Math.sin(rotation/2));
		quaRot.setY(rotationVector.getY()*Math.sin(rotation/2));
		quaRot.setZ(rotationVector.getZ()*Math.sin(rotation/2));
		quaRot.setW(Math.cos(rotation/2));
		return quaRot;	
	}

}
