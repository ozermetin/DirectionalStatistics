package statistics.goodoffit;

import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import numerics.AxialDistribution;
import numerics.RealMatrix;
import numerics.Rotations;
import numerics.Vector3D;

public class KSGoodnessOfFit {

    public KSGoodnessOfFit() {
	// TODO Auto-generated constructor stub
    }
    
    public KSStatistics getKSStatistics(Vector3D sample[], AxialDistribution dist)
    {
	return getKSStatistics(sample, dist, new Vector3D(0.0, 0.0, 1.0));
    }
    
    public KSStatistics getKSStatistics(Vector3D sample[], AxialDistribution dist, Vector3D rotationVector)
    {
	KSStatistics stat = new KSStatistics();
	double[] expPhi = new double[sample.length];
	double[] obsPhi = new double[sample.length];
	RealMatrix rot = Rotations.getRotMat(rotationVector, new Vector3D(0.0, 0.0, 1.0));
	System.out.print("Xi Obj: ");
	for(int i=0;i<sample.length;i++)
	{
	    Vector3D rotatedObsVector =Rotations.rotateVector(sample[i], rot); 
	    //obsPhi[i]=findPhi(rotatedObsVector);
	    //obsPhi[i]=Vector3D.thetaPhi(rotatedObsVector)[1];
	    org.apache.commons.math3.geometry.euclidean.threed.Vector3D vectorApache = new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(rotatedObsVector.x,rotatedObsVector.y,rotatedObsVector.z);
	    obsPhi[i] = new SphericalCoordinates(vectorApache).getTheta();
	    Vector3D vector=dist.nextVector();
	    Vector3D rotatedExpVector =Rotations.rotateVector(vector, rot); 
	    //expPhi[i]=findPhi(rotatedExpVector);
	    //expPhi[i]=Vector3D.thetaPhi(rotatedExpVector)[1];
	    vectorApache = new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(rotatedExpVector.x,rotatedExpVector.y,rotatedExpVector.z);
	    expPhi[i] = new SphericalCoordinates(vectorApache).getTheta();
	    
	    System.out.print(obsPhi[i]+" | ");
	}
	
	System.out.print("\nXi Exp: ");
	for(int i=0;i<sample.length;i++)
	{
	    System.out.print(expPhi[i]+" | ");
	}
	System.out.print("\n");
	
	KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
	stat.setKsStat(ksTest.kolmogorovSmirnovStatistic(obsPhi, expPhi));
	stat.setpValue(ksTest.kolmogorovSmirnovTest(obsPhi, expPhi));
	stat.setExpectedBins(expPhi);
	stat.setObservedBins(obsPhi);
	return stat;
    }
    
    private double findPhi(Vector3D vector)
    {
	double phiData = 0.0;

	    if (vector.x == 0.0) {
		phiData = vector.y > 0.0 ? Math.PI / 2.0 : -1.0 * Math.PI
			/ 2.0;
	    } else {
		if (vector.x < 0.0) {
		    phiData = Math.PI + Math.atan(vector.y / vector.x);
		} else {
		    phiData = Math.atan(vector.y / vector.x);
		}
	    }
	    return (phiData / (2.0 * Math.PI));
    }

}
