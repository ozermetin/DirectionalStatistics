package statistics.goodoffit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import statistics.DirectionalAnalysis;
import statistics.report.GoFReport;

import numerics.BinghamDistribution;
import numerics.Vector3D;
import numerics.WatsonDistribution;
import numerics.WatsonFitter;

public class WatsonStatisticCalculator {

    List<Double> bootList = new ArrayList<Double>();
    double kappa;
    Vector3D mean;
    
    private static final double LevelOfConfidence = 99;
    public WatsonStatisticCalculator() {
	// TODO Auto-generated constructor stub
    }
    
    public void calculate(List<Vector3D> principalList)
    {
	Vector3D[] samples= principalList.toArray(new Vector3D[]{});
	double kappa=WatsonFitter.fitKappa(WatsonFitter.tBarEigenSystem(samples), samples);
	
	System.out.println("Kappa="+kappa);
    }
    
    private ChiSquareStatistic calculateGoodnessOfFit(Vector3D[] samples, Vector3D rotationAxis, double k)
    {
	GoodnessOfFit goF = new GoodnessOfFit(samples, "/home/ozermetin/MIN/Infrastructures/camino/test/test/numerics/SHREWD_ZCW700.txt");
	goF.calculateDistributionOfSample(rotationAxis);
	Vector3D zAxis = new Vector3D(0,0,1);
	double[] watsonPDF=goF.getWatsonDistributionPDF(zAxis, k);
	return goF.chiSquareStatisticTest(watsonPDF, DirectionalAnalysis.DownSamplePDFFactor);
    }
    
    private KSStatistics calculateKSGoodnessOfFit(Vector3D[] samples, WatsonDistribution wDist)
    {
	KSGoodnessOfFit ksGoodnessOfFit = new KSGoodnessOfFit();
	return ksGoodnessOfFit.getKSStatistics(samples, wDist);
    }
    
    private ChiSquareStatistic calculateChiGoodnessOfFitDynamicBins(Vector3D[] samples, WatsonDistribution bDist)
    {
	ChiGoodnessOfFit chi = new ChiGoodnessOfFit(1000);
	return chi.getChiStatistics(samples, bDist);
    }
    
    public void bootstrap(List<Vector3D> principalList, int numberofBoots) 
    {
	
	Vector3D[] samples= principalList.toArray(new Vector3D[]{});
	mean = calculateWatsonMean(principalList);
	kappa=WatsonFitter.fitKappa(WatsonFitter.tBarEigenSystem(samples), samples);
	GoFReport.getInstance().getLastSubject().setDistributionMean(mean);
	GoFReport.getInstance().getLastSubject().setDistributionKappa(kappa);

	double ksTestStat=WatsonFitter.ksTest(samples, mean, kappa);
	double kuiperTestStat=WatsonFitter.kuiperTest(mean, samples);

	
	ChiSquareStatistic chiOfDistribution = calculateGoodnessOfFit(samples, mean,kappa);
	System.out.println("Watson Chi of Distribution: "+chiOfDistribution.getChiSquareStat());
	System.out.println("Watson p Chi of Distribution: "+chiOfDistribution.getpValue());
	GoFReport.getInstance().getLastSubject().setChiSquareStat(chiOfDistribution);
	GoFReport.getInstance().getLastSubject().setNumberofVoxels(samples.length);
	GoFReport.getInstance().getLastSubject().setDownSampleFactor(DirectionalAnalysis.DownSamplePDFFactor);
	
	/*
	KSStatistics ksStat = calculateKSGoodnessOfFit(samples, new WatsonDistribution(mean, kappa, new Random()));
	System.out.println("Watson KS of Distribution: "+ksStat.getKsStat());
	System.out.println("Watson KS-p Chi of Distribution: "+ksStat.getpValue());
	*/
	
	ChiSquareStatistic fit= calculateChiGoodnessOfFitDynamicBins(samples,new WatsonDistribution(mean, kappa, new Random()));
	System.out.println("Watson Dynamic Chi of Distribution: " + fit.getChiSquareStat());
	System.out.println("Watson Dynamic Chi-p Chi of Distribution: " + fit.getpValue());
	
	StringBuffer output = new StringBuffer("Watson Observed Bins: ");
	for(long obs:fit.getObservedBins())
	{
	    output.append(obs).append("|");
	}
	System.out.println(output);
	
	output = new StringBuffer("Watson Expected Bins: ");
	for(double obs:fit.getExpectedBins())
	{
	    output.append(obs).append("|");
	}
	System.out.println(output);
	
	System.out.println("Org Mean Theta="+Vector3D.thetaPhi(mean)[0]+" Phi="+Vector3D.thetaPhi(mean)[1] +" Kappa="+kappa +" KS Stat="+ksTestStat+" Kuiper Stat="+kuiperTestStat+" Chi Stat"+chiOfDistribution);
	
	int ksSumforP=0;
	int kuiperSumforP=0;
	int chiSumforP=0;
	
	for(int i =0; i< numberofBoots;i++)
	{
	    
	    Vector3D[] sampleArray = getWatsonGeneratedData(mean,kappa,principalList.size());
	    double sampleKappa= WatsonFitter.fitKappa(WatsonFitter.tBarEigenSystem(sampleArray), sampleArray);
	    
	    bootList.add(sampleKappa);
	    
	    Vector3D sampleMean = calculateWatsonMean(sampleArray);
	    
	    double ksTestStatSample=WatsonFitter.ksTest(sampleArray, sampleMean, sampleKappa);
	    double kuiperTestStatSample=WatsonFitter.kuiperTest(sampleMean, sampleArray);	    
	    ChiSquareStatistic chiOfSample = calculateGoodnessOfFit(sampleArray,sampleMean,sampleKappa);
	    //double chiOfSample = calculateGoodnessOfFit(sampleArray,mean,kappa)[0];
	    
	    double sumPDF=0;
	    for(int j=0; j<sampleArray.length; j++)
	    {
		sumPDF+=WatsonDistribution.pdf(sampleMean, sampleArray[j], sampleKappa);
	    }
	    
	    //double ksTestStatSample=WatsonFitter.ksTest(sampleArray, mean, kappa);
	    System.out.println("Sample Mean Theta="+Vector3D.thetaPhi(sampleMean)[0]+" Phi="+Vector3D.thetaPhi(sampleMean)[1] +" Kappa="+sampleKappa + " KS Stat="+ksTestStatSample+" Kuiper Stat="+kuiperTestStatSample+" sumPDF:"+sumPDF+" Chi Stat"+chiOfSample);
	    ksSumforP += ksTestStatSample > ksTestStat? 1 : 0; 
	    kuiperSumforP += kuiperTestStatSample > kuiperTestStat? 1 : 0;
	    chiSumforP += chiOfSample.getChiSquareStat() > chiOfDistribution.getChiSquareStat()? 1 : 0;
	}
	double kspVal=(1+ksSumforP)/(double)(numberofBoots+1);
	double kuiperpVal=(1+kuiperSumforP)/(double)(numberofBoots+1);
	double chipVal=(1+chiSumforP)/(double)(numberofBoots+1);
	System.out.println("P KS Calc="+kspVal+" Kuiper Calc="+kuiperpVal+" chi Calc="+chipVal);
    }
    
    private Vector3D[] getWatsonGeneratedData(Vector3D mean, double kappa, int numberofData)
    {
	Vector3D[] sampleData = new Vector3D[numberofData];
	WatsonDistribution wDist = new WatsonDistribution(mean,kappa,new java.util.Random());
	for(int i=0; i< numberofData;i++)
	{
	    sampleData[i]=wDist.nextVector();   
	}
	
	return sampleData;
    }
    
    public double[] calculateConfidenceInterval()
    {
	double[] confInterval= new double[2];
	double[] samples = new double[bootList.size()];
	int i=0;
	for(Double sampleQ : bootList)
	{
	    samples[i++] = sampleQ.doubleValue();   
	}
	
	Percentile percentile = new Percentile();
	double upper=percentile.evaluate(samples, LevelOfConfidence);
	double lower=percentile.evaluate(samples, 100 - LevelOfConfidence);
	
	confInterval[0] = 2*kappa-upper;
	confInterval[1] = 2*kappa-lower;
	System.out.println("Kappa: "+kappa+" Low:"+lower+" Upper:"+ upper+ " CL:"+ confInterval[0]+" CH:"+confInterval[1]);
	return confInterval;
    }

    private Vector3D calculateWatsonMean(List<Vector3D> principalList) {
	double sumCosX=0, sumCosY=0, sumCosZ=0;
	for(int i=0; i< principalList.size(); i++)
	{
	    sumCosX+=principalList.get(i).x;
	    sumCosY+=principalList.get(i).y;
	    sumCosZ+=principalList.get(i).z;
	}
	return new Vector3D(sumCosX/principalList.size(),sumCosY/principalList.size(),sumCosZ/principalList.size()).normalized();
    }
    
    private Vector3D calculateWatsonMean(Vector3D[] principalList) {
	double sumCosX=0, sumCosY=0, sumCosZ=0;
	for(int i=0; i< principalList.length; i++)
	{
	    sumCosX+=principalList[i].x;
	    sumCosY+=principalList[i].y;
	    sumCosZ+=principalList[i].z;
	}
	return new Vector3D(sumCosX/principalList.length,sumCosY/principalList.length,sumCosZ/principalList.length).normalized();
    }

    public double getKappa() {
        return kappa;
    }

    public Vector3D getMean() {
        return mean;
    }

}
