package statistics.goodoffit;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import statistics.DirectionalAnalysis;
import statistics.report.GoFReport;

import numerics.BinghamDistribution;
import numerics.BinghamFitter;
import numerics.ConvergenceException;
import numerics.EigenSystem3D;
import numerics.RealMatrix;
import numerics.Rotations;
import numerics.Vector3D;
import numerics.WatsonDistribution;
import numerics.WatsonFitter;

public class BinghamStatisticCalculator {

    List<double[]> bootList;
    BinghamDistribution bDist;
    private static final double LevelOfConfidence = 99;

    public BinghamStatisticCalculator() {
	// TODO Auto-generated constructor stub
    }

    public void calculate(List<Vector3D> principalList)
	    throws ConvergenceException {
	EigenSystem3D eig = BinghamFitter.tBarEigenSystem(principalList
		.toArray(new Vector3D[] {}));

	BinghamDistribution bDist = BinghamFitter.getBinghamDistribution(eig);
    }
    
    private KSStatistics calculateKSGoodnessOfFit(Vector3D[] samples, BinghamDistribution bDist)
    {
	KSGoodnessOfFit ksGoodnessOfFit = new KSGoodnessOfFit();
	return ksGoodnessOfFit.getKSStatistics(samples, bDist);
    }
    
    private ChiSquareStatistic calculateChiGoodnessOfFitDynamicBins(Vector3D[] samples, BinghamDistribution bDist)
    {
	ChiGoodnessOfFit chi = new ChiGoodnessOfFit(1000);
	return chi.getChiStatistics(samples, bDist);
    }

    private ChiSquareStatistic calculateGoodnessOfFit(Vector3D[] samples,
	    Vector3D[] distibutionAxes, double k1, double k2) {
	GoodnessOfFit goF = new GoodnessOfFit(
		samples,
		"/home/ozermetin/MIN/Infrastructures/camino/test/test/numerics/SHREWD_ZCW700.txt");
	double bestScore = Double.MAX_VALUE;
	ChiSquareStatistic bestStat = new ChiSquareStatistic();
	// goF.calculateDistributionOfSample(distibutionAxes[0]);
	System.out.println("*****************************");
	for (int i = 0; i < 3; i++) {
	    goF.calculateDistributionOfSample(distibutionAxes[i]);
	    Vector3D[] axesN = new Vector3D[3];
	    axesN[0] = new Vector3D(1.0, 0.0, 0.0);
	    axesN[1] = new Vector3D(0.0, 1.0, 0.0);
	    axesN[2] = new Vector3D(0.0, 0.0, 1.0);
	    // double[]
	    // binghamPDF=goF.getBinghamDistributionPDF(alignAxesOfDistribution(distibutionAxes,axesN),
	    // k1, k2);
	    double[] binghamPDF = goF.getBinghamDistributionPDF(axesN, k1, k2);
	    if(k1<-400 || k2<-100)
	    {
		System.out.println("Error: Convergence");
		continue;
	    }
	    ChiSquareStatistic stat = goF.chiSquareStatisticTest(binghamPDF,
		    DirectionalAnalysis.DownSamplePDFFactor);
	    if (stat.getChiSquareStat() < bestScore) {
		bestScore = stat.getChiSquareStat();
		bestStat = stat;
	    }
	    System.out.println(i + ".Direction Chi Score: " + stat.getChiSquareStat()
		    + " p:" + stat.getpValue()+" Stat:"+stat.getTestResult());
	}

	System.out.println("*****************************");
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if(i==j) continue;
		goF.calculateDistributionOfSample(distibutionAxes[i],distibutionAxes[j]);
		Vector3D[] axesN = new Vector3D[3];
		axesN[0] = new Vector3D(1.0, 0.0, 0.0);
		axesN[1] = new Vector3D(0.0, 1.0, 0.0);
		axesN[2] = new Vector3D(0.0, 0.0, 1.0);
		// double[]
		// binghamPDF=goF.getBinghamDistributionPDF(alignAxesOfDistribution(distibutionAxes,axesN),
		// k1, k2);
		double[] binghamPDF = goF.getBinghamDistributionPDF(axesN, k1,
			k2);
		if(k1<-400 || k2<-100)
		    {
			System.out.println("Error: Convergence");
			continue;
		    }
		ChiSquareStatistic stat = goF.chiSquareStatisticTest(binghamPDF,
			    DirectionalAnalysis.DownSamplePDFFactor);
		    if (stat.getChiSquareStat() < bestScore) {
			bestScore = stat.getChiSquareStat();
			bestStat = stat;
		    }
		System.out.println(i + ".First Direction "+j + ".Second Direction Chi Score: " + stat.getChiSquareStat()
			+ " p:" + stat.getpValue()+" Stat:"+stat.getTestResult());
	    }
	}

	System.out.println("*****************************");
	
	return bestStat;
    }

    private Vector3D[] alignAxesOfDistribution(Vector3D[] distributionAxes) {
	Vector3D[] axesN = new Vector3D[3];

	for (int i = 0; i < 3; i++) {
	    if (Math.abs(distributionAxes[i].x) > Math
		    .abs(distributionAxes[i].y)
		    && Math.abs(distributionAxes[i].x) > Math
			    .abs(distributionAxes[i].z)) {
		axesN[i] = distributionAxes[0];
	    } else if (Math.abs(distributionAxes[i].y) > Math
		    .abs(distributionAxes[i].x)
		    && Math.abs(distributionAxes[i].y) > Math
			    .abs(distributionAxes[i].z)) {
		axesN[i] = distributionAxes[1];
	    } else if (Math.abs(distributionAxes[i].z) > Math
		    .abs(distributionAxes[i].x)
		    && Math.abs(distributionAxes[i].z) > Math
			    .abs(distributionAxes[i].y)) {
		axesN[i] = distributionAxes[2];
	    }
	}
	return axesN;
    }

    public void bootstrap(List<Vector3D> principalList, int numberofBoots)
	    throws ConvergenceException {
	bootList = new ArrayList<double[]>();

	// Estimate sample distribution parameters
	Vector3D[] samples = principalList.toArray(new Vector3D[] {});
	bDist = estimateBinghamDistribution(samples);
	System.out.println("ORG:" + bDist);
	
	// Estimate chi GOF using estimated distribution parameters
	ChiSquareStatistic chiOfDistribution = calculateGoodnessOfFit(samples,
		new Vector3D[] { bDist.e1(), bDist.e2(), bDist.e3() },
		bDist.k1(), bDist.k2());
	
	GoFReport.getInstance().getLastSubject().setChiSquareStat(chiOfDistribution);
	GoFReport.getInstance().getLastSubject().setNumberofVoxels(samples.length);
	
	System.out.println("Bingham Chi of Distribution: " + chiOfDistribution.getChiSquareStat());
	System.out.println("Bingham p Chi of Distribution: " + chiOfDistribution.getpValue());
	/*
	KSStatistics stat= calculateKSGoodnessOfFit(samples,bDist);
	System.out.println("Bingham KS of Distribution: " + stat.getKsStat());
	System.out.println("Bingham KS-p Chi of Distribution: " + stat.getpValue());
	*/
	
	ChiSquareStatistic fit= calculateChiGoodnessOfFitDynamicBins(samples,bDist);
	System.out.println("Bingham Dynamic Chi of Distribution: " + fit.getChiSquareStat());
	System.out.println("Bingham Dynamic Chi-p Chi of Distribution: " + fit.getpValue());
	StringBuffer output = new StringBuffer("Observed Bins: ");
	for(long obs:fit.getObservedBins())
	{
	    output.append(obs).append("|");
	}
	System.out.println(output);
	
	output = new StringBuffer("Expected Bins: ");
	for(double obs:fit.getExpectedBins())
	{
	    output.append(obs).append("|");
	}
	System.out.println(output);
	int kuiperSumforP = 0;
	int chiSumforP = 0;

	Vector3D mean = calculateWatsonMean(principalList);

	double kuiperTestStat = WatsonFitter.kuiperTest(bDist.e1(), samples);
	// double kuiperTestStat=WatsonFitter.kuiperTest(mean,
	// principalList.toArray(new Vector3D[]{}));

	// System.out.println("Org Sample:"+bDist+" Kuiper Stat:"+kuiperTestStat);

	for (int i = 0; i < numberofBoots; i++) {

	    Vector3D[] sampleArray = getBinghamGeneratedData(
		    alignAxesOfDistribution(new Vector3D[] { bDist.e1(),
			    bDist.e2(), bDist.e3() }), bDist.k1(), bDist.k2(),
		    principalList.size());
	    BinghamDistribution bDistSample = estimateBinghamDistribution(sampleArray);

	    // double[] chiOfSample =
	    // calculateGoodnessOfFit(sampleArray,bDist.e2(), new Vector3D[]
	    // {bDist.e1(), bDist.e2(), bDist.e3()}, bDist.k1(),bDist.k2());
	    ChiSquareStatistic chiOfSample = calculateGoodnessOfFit(sampleArray,
		    new Vector3D[] { bDist.e1(), bDist.e2(), bDist.e3() },
		    bDistSample.k1(), bDistSample.k2());
	    // double[] chiOfSample =
	    // calculateGoodnessOfFit(sampleArray,bDistSample.e2(), new
	    // Vector3D[] {bDistSample.e1(), bDistSample.e2(),
	    // bDistSample.e3()}, bDistSample.k1(),bDistSample.k2());
	    System.out.println("Chi of Sample: " + chiOfSample.getChiSquareStat());

	    double kuiperSampleStat = WatsonFitter.kuiperTest(bDistSample.e1(),
		    sampleArray);
	    // double kuiperSampleStat=WatsonFitter.kuiperTest(sampleMean,
	    // sampleArray);

	    double[] sampleBingPars = { bDistSample.k1(), bDistSample.k2() };

	    kuiperSumforP += kuiperSampleStat > kuiperTestStat ? 1 : 0;

	    System.out.println("Sample:" + bDistSample + " Kuiper Stat:"
		    + kuiperSampleStat);
	    bootList.add(sampleBingPars);
	    chiSumforP += chiOfSample.getChiSquareStat() > chiOfDistribution.getChiSquareStat() ? 1 : 0;
	}

	double kuiperpVal = (1 + kuiperSumforP) / (double) (numberofBoots + 1);
	double chipVal = (1 + chiSumforP) / (double) (numberofBoots + 1);
	System.out.println("P Kuiper Calc=" + kuiperpVal);
	System.out.println("P Chi Calc=" + chipVal);
    }

    private BinghamDistribution estimateBinghamDistribution(
	    Vector3D[] sampleData) throws ConvergenceException {
	EigenSystem3D eigSample = BinghamFitter.tBarEigenSystem(sampleData);
	for (int i = 0; i < 3; i++) {
	    System.out.println("Eig:" + eigSample.eigenvalues[i] + " "
		    + eigSample.eigenvectors[i]);
	}
	
	// Put GoF Report
	GoFReport.getInstance().getLastSubject().setDistributionEigenValues(eigSample.eigenvalues);
	GoFReport.getInstance().getLastSubject().setDistributionEigenVectors(eigSample.eigenvectors);

	BinghamDistribution bDistSample = BinghamFitter
		.getBinghamDistribution(eigSample);
	GoFReport.getInstance().getLastSubject().setDistributionK1(bDistSample.k1());
	GoFReport.getInstance().getLastSubject().setDistributionK2(bDistSample.k2());
	GoFReport.getInstance().getLastSubject().setDistributionMean(calculateWatsonMean(sampleData));
	GoFReport.getInstance().getLastSubject().setDownSampleFactor(DirectionalAnalysis.DownSamplePDFFactor);
	return bDistSample;
    }

    private Vector3D[] getBinghamGeneratedData(Vector3D[] axis, double k1,
	    double k2, int numberofData) {
	Vector3D[] sampleData = new Vector3D[numberofData];
	BinghamDistribution bDist = null;
	try {
	    bDist = BinghamDistribution.getBinghamDistribution(axis, k1, k2,
		    new Random());
	} catch (ConvergenceException e) {
	    fail(e.toString());
	}

	for (int i = 0; i < numberofData; i++) {
	    sampleData[i] = bDist.nextVector();
	}
	return sampleData;
    }

    /*
     * double[0]: CL k1 double[1]: CH k1 double[2]: CL k2 double[3]: CH k2
     */
    public double[] calculateConfidenceInterval() {
	double[] confInterval = new double[4];
	double[] samplesK1 = new double[bootList.size()];
	double[] samplesK2 = new double[bootList.size()];
	int i = 0;
	for (double[] sampleQ : bootList) {
	    samplesK1[i] = sampleQ[0];
	    samplesK2[i++] = sampleQ[1];
	}

	Percentile percentile = new Percentile();
	double upper = percentile.evaluate(samplesK1, LevelOfConfidence);
	double lower = percentile.evaluate(samplesK1, 100 - LevelOfConfidence);

	confInterval[0] = 2 * bDist.k1() - upper;
	confInterval[1] = 2 * bDist.k1() - lower;

	System.out.println("k1: " + bDist.k1() + "k2: " + bDist.k2() + " Low:"
		+ lower + " Upper:" + upper + " CL:" + confInterval[0] + " CH:"
		+ confInterval[1]);
	upper = percentile.evaluate(samplesK2, LevelOfConfidence);
	lower = percentile.evaluate(samplesK2, 100 - LevelOfConfidence);

	confInterval[2] = 2 * bDist.k2() - upper;
	confInterval[3] = 2 * bDist.k2() - lower;

	System.out.println("k1: " + bDist.k1() + "k2: " + bDist.k2() + " Low:"
		+ lower + " Upper:" + upper + " CL:" + confInterval[2] + " CH:"
		+ confInterval[3]);
	return confInterval;
    }

    private Vector3D calculateWatsonMean(List<Vector3D> principalList) {
	double sumCosX = 0, sumCosY = 0, sumCosZ = 0;
	for (int i = 0; i < principalList.size(); i++) {
	    sumCosX += principalList.get(i).x;
	    sumCosY += principalList.get(i).y;
	    sumCosZ += principalList.get(i).z;
	}
	return new Vector3D(sumCosX / principalList.size(), sumCosY
		/ principalList.size(), sumCosZ / principalList.size())
		.normalized();
    }

    private Vector3D calculateWatsonMean(Vector3D[] principalList) {
	double sumCosX = 0, sumCosY = 0, sumCosZ = 0;
	for (int i = 0; i < principalList.length; i++) {
	    sumCosX += principalList[i].x;
	    sumCosY += principalList[i].y;
	    sumCosZ += principalList[i].z;
	}
	return new Vector3D(sumCosX / principalList.length, sumCosY
		/ principalList.length, sumCosZ / principalList.length)
		.normalized();
    }

    public BinghamDistribution getbDist() {
	return bDist;
    }

}
