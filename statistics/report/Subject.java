package statistics.report;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import statistics.goodoffit.ChiSquareStatistic;
import statistics.goodoffit.DistributionData;

import numerics.Vector3D;

public class Subject implements Comparable<Subject> , Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8886918877189732881L;

    private String subjectName, areaName, method, filepath;
    private int numberofVoxels, downSampleFactor;

    private Vector3D[] distributionEigenVectors;
    private double[] distributionEigenValues;
    
    private Vector3D distributionMean;
    private double distributionK1;
    private double distributionK2;
    private double distributionKappa;
    
    private DescriptiveStatistics faStats;
    
    private ChiSquareStatistic chiSquareStat;
    

    public Subject(String subjectName, String areaName, String method,
	    String filepath) {
	this.subjectName = subjectName;
	this.areaName = areaName;
	this.method = method;
	this.filepath = filepath;
    }

    public String getSubjectName() {
	return subjectName;
    }

    public void setSubjectName(String subjectName) {
	this.subjectName = subjectName;
    }

    public String getAreaName() {
	return areaName;
    }

    public void setAreaName(String areaName) {
	this.areaName = areaName;
    }

    public String getMethod() {
	return method;
    }

    public void setMethod(String method) {
	this.method = method;
    }

    public String getFilepath() {
	return filepath;
    }

    public void setFilepath(String filepath) {
	this.filepath = filepath;
    }

    public int getNumberofVoxels() {
	return numberofVoxels;
    }

    public void setNumberofVoxels(int numberofVoxels) {
	this.numberofVoxels = numberofVoxels;
    }

   

    public int getDownSampleFactor() {
	return downSampleFactor;
    }

    public void setDownSampleFactor(int downSampleFactor) {
	this.downSampleFactor = downSampleFactor;
    }

    

    public boolean equals(Subject obj) {
	if (obj.areaName == null && obj.method == null)
	    return this.subjectName.equals(obj.subjectName);
	else if (obj.method == null)
	    return this.subjectName.equals(obj.subjectName)
		    && this.areaName.equals(obj.areaName);
	else
	    return this.subjectName.equals(obj.subjectName)
		    && this.areaName.equals(obj.areaName)
		    && this.method.equals(obj.method);
    }

    public Vector3D[] getDistributionEigenVectors() {
        return distributionEigenVectors;
    }

    public void setDistributionEigenVectors(Vector3D[] distributionEigenVector) {
        this.distributionEigenVectors = distributionEigenVector;
    }

    public double[] getDistributionEigenValues() {
        return distributionEigenValues;
    }

    public void setDistributionEigenValues(double[] distributionEigenValues) {
        this.distributionEigenValues = distributionEigenValues;
    }

    public Vector3D getDistributionMean() {
        return distributionMean;
    }

    public void setDistributionMean(Vector3D distributionMean) {
        this.distributionMean = distributionMean;
    }

    public double getDistributionK1() {
        return distributionK1;
    }

    public void setDistributionK1(double distributionK1) {
        this.distributionK1 = distributionK1;
    }

    public double getDistributionK2() {
        return distributionK2;
    }

    public void setDistributionK2(double distributionK2) {
        this.distributionK2 = distributionK2;
    }

    public double getDistributionKappa() {
        return distributionKappa;
    }

    public void setDistributionKappa(double distributionKappa) {
        this.distributionKappa = distributionKappa;
    }

    public ChiSquareStatistic getChiSquareStat() {
        return chiSquareStat;
    }

    public void setChiSquareStat(ChiSquareStatistic chiSquareStat) {
        this.chiSquareStat = chiSquareStat;
    }
    
    public String toString()
    {
	StringBuffer buffer = new StringBuffer();
	buffer.append("SubjectName: "+subjectName);
	buffer.append(" AreaName: "+areaName);
	buffer.append(" Method: "+method);
	buffer.append(" Filepath: "+filepath);
	buffer.append(" NumberofVoxels: "+numberofVoxels);
	buffer.append(" DownSampleFactor: "+downSampleFactor);
	buffer.append("\n");
	
	if(distributionEigenValues!=null)
	{
    		buffer.append("EigenValues [0]"+distributionEigenValues[0]);
    		buffer.append(" [1]: "+distributionEigenValues[1]);
    		buffer.append(" [2]: "+distributionEigenValues[2]);
    		buffer.append("\n");
	}
	if(distributionEigenVectors!=null)
	{
	    buffer.append("EigenVectors [0]: "+distributionEigenVectors[0]);
	    buffer.append(" [1]: "+distributionEigenVectors[1]);
	    buffer.append(" [2]: "+distributionEigenVectors[2]);
	    buffer.append("\n");
	}
	buffer.append("Mean: "+distributionMean);
	buffer.append(" K1: "+distributionK1);
	buffer.append(" K2: "+distributionK2);
	buffer.append(" Kappa: "+distributionKappa);
	buffer.append("\n");
	buffer.append(chiSquareStat.toString());
	return buffer.toString();
    }

    @Override
    public int compareTo(Subject arg0) {
	int areaThis = Integer.parseInt(this.areaName);
	int areaSub = Integer.parseInt(arg0.areaName);
	if(areaThis == areaSub && this.subjectName.equals(arg0.subjectName))
	{
	    if(this.method.equals("bingham"))
		    return -1;
	    else return 1;
	}
	else if (areaThis == areaSub)
	{
	    return this.subjectName.compareTo(arg0.subjectName);
	}
	else return Integer.compare(areaThis, areaSub);
    }

    public DescriptiveStatistics getFaStats() {
        return faStats;
    }

    public void setFaStats(DescriptiveStatistics faStats) {
        this.faStats = faStats;
    }

}
