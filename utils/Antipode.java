package utils;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Antipode {

    public Antipode() {
	// TODO Auto-generated constructor stub
    }
    
    // Azi (-) Inc  (+180)
    public static RealVector convertAntipode (RealVector inputVector)
    {
	Spherical spherical = toSpherical(inputVector);
	if(spherical.getInc()>Math.PI/2)
	{
	    spherical.setInc(spherical.getInc()-Math.PI);
	    //spherical.setAzi(spherical.getAzi()+Math.PI);
	    //spherical.setInc(spherical.getInc()+Math.PI);
	}
	return toVector(spherical);
    }
    
    public static Spherical toSpherical(RealVector inputVector)
    {
	Spherical spherical = new Spherical();
	spherical.setInc(Math.acos(inputVector.getEntry(2)));
	spherical.setAzi(Math.atan2(inputVector.getEntry(1), inputVector.getEntry(0)));
	//spherical.setAzi(Math.atan(inputVector.getEntry(1)/ inputVector.getEntry(0)));
	return spherical;
    }
    
    public static RealVector toVector(Spherical sphericalVal)
    {
	
	double x = Math.sin(sphericalVal.getInc())* Math.cos(sphericalVal.getAzi());
	double y = Math.sin(sphericalVal.getInc())* Math.sin(sphericalVal.getAzi());
	double z = Math.cos(sphericalVal.getInc());
	
	ArrayRealVector returnVector = new ArrayRealVector(new double[] { x, y, z });
	return returnVector;
    }
    
    

    
}
