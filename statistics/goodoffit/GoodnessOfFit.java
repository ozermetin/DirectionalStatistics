package statistics.goodoffit;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.inference.ChiSquareTest;

import tools.FileInput;


import numerics.BinghamDistribution;
import numerics.BinghamFitter;
import numerics.ConvergenceException;
import numerics.EigenSystem3D;
import numerics.RealMatrix;
import numerics.Rotations;
import numerics.Vector3D;
import numerics.WatsonDistribution;

public class GoodnessOfFit {

    Vector3D[] sampleVectors; 
    String distributionVectorFile;
    DistributionData[] observedDistData = null;
    
    public GoodnessOfFit(Vector3D[] sampleVectors, String distributionVectorFile) {
	this.sampleVectors = sampleVectors;
	this.distributionVectorFile = distributionVectorFile;
	observedDistData = getDistributionBins();
    }
    
    //If not rotate put rotationVector to z;
    public void calculateDistributionOfSample(Vector3D rotationVector)
    {
	clearObservedDistData();
	Vector3D[] rotatedVectors = new Vector3D[sampleVectors.length];
	//Put rotation vector to z axis+
	RealMatrix rot = Rotations.getRotMat(rotationVector, new Vector3D(0.0, 0.0, 1.0));
	for (int i = 0; i < sampleVectors.length; i++) {
	    rotatedVectors[i] = Rotations.rotateVector(sampleVectors[i], rot);
	    double xi=findPhi(rotatedVectors[i]);
	    calculateObserved(xi);
	}
	
	EigenSystem3D eigSample = BinghamFitter.tBarEigenSystem(rotatedVectors);
	for (int i = 0; i < 3; i++) {
	    System.out.println("Rotated Eig:" + eigSample.eigenvalues[i] + " "
		    + eigSample.eigenvectors[i]);
	}
    }
    
    // First vector puts to z then to the x
    public void calculateDistributionOfSample(Vector3D firstRotationVector, Vector3D secondRotationVector)
    {
	clearObservedDistData();
	//Put rotation vector to z axis+
	Vector3D[] rotatedVectors = new Vector3D[sampleVectors.length];
	RealMatrix rot = Rotations.getRotMat(firstRotationVector, new Vector3D(0.0, 0.0, 1.0));
	for (int i = 0; i < sampleVectors.length; i++) {
	    rotatedVectors[i]= Rotations.rotateVector(sampleVectors[i], rot);
	}
	
	//Put second eigenvector to y
	rot = Rotations.getRotMat(secondRotationVector, new Vector3D(0.0, 1.0, 0.0));
	for (int i = 0; i < rotatedVectors.length; i++) {
	    rotatedVectors[i] = Rotations.rotateVector(rotatedVectors[i], rot);
	    double xi=findPhi(rotatedVectors[i]);
	    calculateObserved(xi);
	}
	/*
	EigenSystem3D eigSample = BinghamFitter.tBarEigenSystem(rotatedVectors);
	for (int i = 0; i < 3; i++) {
	    System.out.println("Rotated Eig:" + eigSample.eigenvalues[i] + " "
		    + eigSample.eigenvectors[i]);
	} */
    }
    
    //Initially around mean
    public double[] getWatsonDistributionPDF(Vector3D meanAxis, double k)
    {
	double[] pdf= new double[observedDistData.length];
	WatsonDistribution wDist = new WatsonDistribution(meanAxis,k,new java.util.Random());
	for(int i=0;i<observedDistData.length;i++)
	{
	    Vector3D rotated = Vector3D.vectorFromSPC(1, observedDistData[i].theta, observedDistData[i].phi);
	    pdf[i]=wDist.pdf(rotated)*observedDistData[i].weight;
	}
	return pdf;
    }

    //Initally aour 1,0,0;0,1,0;0,0,1
    public double[] getBinghamDistributionPDF(Vector3D[] axes, double k1, double k2)
    {
	double[] pdf= new double[observedDistData.length];

	/*axes[0] = new Vector3D(1.0, 0.0, 0.0);
	axes[1] = new Vector3D(0.0, 1.0, 0.0);
	axes[2] = new Vector3D(0.0, 0.0, 1.0); */

	BinghamDistribution bDist=null;
	try {
	    bDist = BinghamDistribution.getBinghamDistribution(axes, k1, k2, new Random());
	} catch (ConvergenceException e) {
	    fail(e.toString());
	}
	
	for(int i=0;i<observedDistData.length;i++)
	{
	    Vector3D rotated = Vector3D.vectorFromSPC(1, observedDistData[i].theta, observedDistData[i].phi);
	    pdf[i]=bDist.pdf(rotated)*observedDistData[i].weight;
	}
	
	System.out.print("PDF: ");
	for(int i=0;i<observedDistData.length;i++)
	{
	    System.out.print(pdf[i]+" ");
	}
	System.out.println();
	return pdf;
    }
    
    private void clearObservedDistData()
    {
	for(int i=0;i<observedDistData.length;i++)
	{
	    observedDistData[i].observed=0;
	}
    }
    
    
    //return[0]: chiSquare Stat, return[1]: p value
    public ChiSquareStatistic chiSquareStatisticTest(double[] pdfOfDistribution , int shrinkFactor)
    {
	int bins = observedDistData.length/shrinkFactor;
	ChiSquareStatistic stat = new ChiSquareStatistic();
	
	double[] chiSquareStat= new double[3];
	long[] observedBins = new long[bins];
	double[] expectedBins= new double[bins];	
	long sumObs=0;
	double sumExp=0;
	for(int i=0,j=0; i<observedDistData.length;i++)
	{
	    sumObs += observedDistData[i].observed;
	    sumExp += pdfOfDistribution[i]*sampleVectors.length;  //Number of Samples	    
	    if((i+1) % (700/bins) == 0)
	    {
		observedBins[j] = sumObs;
		expectedBins[j] = sumExp;
		sumObs = 0;
		sumExp = 0;
		j++;
	    }
	}
	/*
	for(int i=0;i<observedBins.length;i++)
	{
	    System.out.println("Ob"+observedBins[i]+" Ex:"+expectedBins[i]);
	} */
	ChiSquareTest chiSquare = new ChiSquareTest();
	chiSquareStat[0]=chiSquare.chiSquare(expectedBins, observedBins);
	chiSquareStat[1]=chiSquare.chiSquareTest(expectedBins, observedBins);
	if(chiSquare.chiSquareTest(expectedBins, observedBins,0.1))
	    chiSquareStat[2]=1;
	else
	    chiSquareStat[2]=0;
	stat.setChiSquareStat(chiSquareStat[0]);
	stat.setpValue(chiSquareStat[1]);
	stat.setTestResult(chiSquareStat[2]);
	stat.setObservedBins(observedBins);
	stat.setExpectedBins(expectedBins);
	stat.setDistributionData(observedDistData);
	return stat;
    }
    
    // Find nearest xi and increment it.
    private void calculateObserved(double xi)
    {
	int location=Arrays.binarySearch(observedDistData, new DistributionData(xi,0,0,0,0));
	    if(location>0) // Direct Hit!!!
	    {
		observedDistData[-location].incrementObserved();
	    }
	    else
	    { // No Hit Search for Nearest Bin
		location*=-1;
		location-=2;
		if(location<=0)
		{
		    observedDistData[0].incrementObserved();
		}
		else if(location<699) //Not Last
		    if((xi - observedDistData[location].xi)<(observedDistData[location+1].xi-xi))
			observedDistData[location].incrementObserved();
		    else
			observedDistData[location+1].incrementObserved();
		else
		    observedDistData[699].incrementObserved();
	    }
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
    
    
    private DistributionData[] getDistributionBins()
    {
	FileInput in = new FileInput(distributionVectorFile);
	DistributionData[] orgDataDist = new DistributionData[700];
	double[] weight = new double[700];
	double[] phi = new double[700];
	double[] theta = new double[700];
	double[] xi = new double[700];
	
	for (int i = 0; i < 700; i++) {
	    StringTokenizer tokens = new StringTokenizer(in.readString(), " ");
	    phi[i] = Double.parseDouble(tokens.nextToken());
	    theta[i] = Double.parseDouble(tokens.nextToken());
	    weight[i] = Double.parseDouble(tokens.nextToken());
	}
	
	for (int i = 0; i < phi.length; i++) {
	    Vector3D vector = Vector3D.vectorFromSPC(1, theta[i], phi[i]);
	    // Rotate if needed
	    
	    xi[i] = findPhi(vector);
	    orgDataDist[i] = new DistributionData(xi[i], phi[i], theta[i],weight[i],
		    0);
	}
	Arrays.sort(orgDataDist);
	return orgDataDist;
    }

    public DistributionData[] getObservedDistData() {
        return observedDistData;
    }
}
