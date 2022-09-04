package statistics.goodoffit;

import numerics.AxialDistribution;
import numerics.RealMatrix;
import numerics.Rotations;
import numerics.Vector3D;

import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;

public class ChiGoodnessOfFit {

    private int numberOfGeneratedSamples;
    
    public ChiGoodnessOfFit(int numberOfGeneratedSamples) {
	this.numberOfGeneratedSamples=numberOfGeneratedSamples;
    }
    
    public ChiSquareStatistic getChiStatistics(Vector3D sample[], AxialDistribution dist)
    {
	return getChiStatistics(sample, dist, new Vector3D(0.0, 0.0, 1.0));
    }
    
    public ChiSquareStatistic getChiStatistics(Vector3D sample[], AxialDistribution dist, Vector3D rotationVector)
    {
	double[] expPhi = new double[numberOfGeneratedSamples];
	double[] obsPhi = new double[sample.length];
	RealMatrix rot = Rotations.getRotMat(rotationVector, new Vector3D(0.0, 0.0, 1.0));
	ChiSquareStatDynamicBins dynamicBins = new ChiSquareStatDynamicBins(50, 5);
	System.out.print("\nChi Xi Exp: ");
	for(int i=0;i<numberOfGeneratedSamples;i++)
	{
	    Vector3D vector=dist.nextVector();
	    Vector3D rotatedExpVector =Rotations.rotateVector(vector, rot); 
	    //expPhi[i]=findPhi(rotatedExpVector);
	    //expPhi[i]=Vector3D.thetaPhi(rotatedExpVector)[1];
	    org.apache.commons.math3.geometry.euclidean.threed.Vector3D vectorApache  = new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(rotatedExpVector.x,rotatedExpVector.y,rotatedExpVector.z);
	    expPhi[i] = new SphericalCoordinates(vectorApache).getTheta();
	    
	    //TODO: check for negating expPhi this is not true
	    if(expPhi[i]<0) expPhi[i]+=Math.PI;
	    System.out.print(expPhi[i]+" | ");
	}
	
	System.out.print("\nChi Xi Obj: ");
	for(int i=0;i<sample.length;i++)
	{
	    Vector3D rotatedObsVector =Rotations.rotateVector(sample[i], rot); 
	    //obsPhi[i]=findPhi(rotatedObsVector);
	    //obsPhi[i]=Vector3D.thetaPhi(rotatedObsVector)[1];
	    org.apache.commons.math3.geometry.euclidean.threed.Vector3D vectorApache = new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(rotatedObsVector.x,rotatedObsVector.y,rotatedObsVector.z);
	    obsPhi[i] = new SphericalCoordinates(vectorApache).getTheta();
	    
	    //TODO: check for negating expPhi this is not true
	    if(obsPhi[i]<0) obsPhi[i]+=Math.PI;
	    System.out.print(obsPhi[i]+" | ");
	}
	System.out.print("\n");
	
	Double[] ranges=dynamicBins.selectRanges(expPhi);
	return dynamicBins.doChiSquareStat(ranges, obsPhi, expPhi);
    }

}
