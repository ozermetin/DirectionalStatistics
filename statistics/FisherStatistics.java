package statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.math3.linear.ArrayRealVector;

import utils.Antipode;

import misc.DT;
import misc.LoggedException;

import apps.FracAnis;

public class FisherStatistics {

    double resulttantVector = -1;
    List<FilterReturnHolder> returnHolderCollection;

    public FisherStatistics(List<FilterReturnHolder> returnHolderCollection) {
	this.returnHolderCollection = returnHolderCollection;
    }
    
    public void combine(FisherStatistics stat)
    {
	this.returnHolderCollection.addAll(stat.returnHolderCollection);
	this.resetFisher();
    }
    
    private double getFTestResult(FisherStatistics stat)
    {
	List<FilterReturnHolder> combinedList = new ArrayList<FilterReturnHolder>(this.returnHolderCollection.size());
	combinedList.addAll(this.returnHolderCollection);
	combinedList.addAll(stat.returnHolderCollection);
	FisherStatistics combinedStat = new FisherStatistics(combinedList);
	double upper=this.getResultantVector()+stat.getResultantVector()-combinedStat.getResultantVector();
	double lower=combinedList.size()-this.getResultantVector()-stat.getResultantVector();
	return (combinedList.size()*(upper/lower));
    }
    
    public boolean areStatDiffers(FisherStatistics stat)
    {
	//If the observed F statistic exceeds the tabulated value at the chosen significance level, 
	//then these two mean directions are different at that level of significance 2 to N-2~ inf
	// F value is 3
	double fValue = getFTestResult(stat);
	System.out.println("F value="+fValue);
	return(fValue>3);
    }

    private void resetFisher() {
	resulttantVector = -1;
    }
    
    public void clearAll()
    {
	returnHolderCollection.clear();
	resetFisher();
    }

    public double getResultantVector() {
	if (resulttantVector < 0) {
	    ArrayRealVector rVector = new ArrayRealVector(new double[] { 0.,
		    0., 0. });
	    for (FilterReturnHolder holder : returnHolderCollection) {
		ArrayRealVector vector = new ArrayRealVector(
			holder.filterResults, 1, 3);
		
		/*double yValue = vector.getEntry(1);
		if (yValue < 0)
		    vector.setEntry(1, -yValue);  
		    rVector = rVector.add(vector);
		  */  
		rVector = rVector.add(Antipode.convertAntipode(vector));
	    }
	    resulttantVector = rVector.getNorm();
	}
	return resulttantVector;
    }

    public double[] getEstimatedMean() {
	ArrayRealVector rVector = new ArrayRealVector(
		new double[] { 0., 0., 0. });
	for (FilterReturnHolder holder : returnHolderCollection) {
	    ArrayRealVector vector = new ArrayRealVector(holder.filterResults,
		    1, 3);
	    rVector = rVector.add(vector);
	}
	double[] mean = new double[3];
	mean[0] = rVector.getEntry(0) / getResultantVector();
	mean[1] = rVector.getEntry(1) / getResultantVector();
	mean[2] = rVector.getEntry(2) / getResultantVector();
	return mean;
    }

    public double getR0() {
	return Math.sqrt(7.815 * returnHolderCollection.size() / 3);
    }

    public boolean rejectRandom() {
	return getResultantVector() > getR0();
    }

    // USe only biggest Eigenvalues
    public double getPrecision() {
	int numberOfElements = returnHolderCollection.size();
	return (numberOfElements - 1)
		/ (numberOfElements - getResultantVector());
    }

    /*
     * In Radian
     */
    public double getConfidenceInterval(double pValue) {
	int numberOfElements = returnHolderCollection.size();
	double rightInner = Math.pow(1 / pValue, 1.0 / (numberOfElements - 1)) - 1;
	double inner = 1 - (((numberOfElements - getResultantVector()) / getResultantVector()) * rightInner);
	return Math.toDegrees(Math.acos(inner));
    }

    public double getCSD() {
	int numberOfElements = returnHolderCollection.size();
	return Math.toDegrees(Math
		.acos(getResultantVector() / numberOfElements));
    }

    public BestFlow getBestFlow() {
	BestFlow bestFlow = new BestFlow();
	FisherStatistics bestFisher = this;
	int numberOfElements = returnHolderCollection.size();
	if (numberOfElements > 0) {
	    DirectionalStatistics.logger.log(Level.FINER,
		    "Initial Holder Size {0}", numberOfElements);
	    Random randomGenerator = new Random();
	    for (int i = 0; i < BestFlow.NumberofIterations; i++) {
		DirectionalStatistics.logger.log(Level.FINER, "{0}:Iteration ",
			i);
		FisherStatistics innerFisher = selectRandomInliners(
			numberOfElements, randomGenerator);
		DirectionalStatistics.logger.log(Level.FINER, "Inner Size:{0}",innerFisher.returnHolderCollection.size());
		extendInliers(innerFisher, numberOfElements, randomGenerator);
		DirectionalStatistics.logger.log(Level.FINER,
			"Extended Size:{0}",innerFisher.returnHolderCollection.size());
		double innerK = innerFisher.getPrecision();
		DirectionalStatistics.logger.log(Level.FINER,
			"Inner Precision:{0} Best Precision:{1}", new Object[] {
				innerK, bestFisher.getPrecision() });
		if (innerK > bestFisher.getPrecision()) {
		    bestFisher = innerFisher;
		    DirectionalStatistics.logger.log(Level.FINER,
			    "Best Updated!!!");
		}
		DirectionalStatistics.logger.log(Level.FINER,
			"*****End of Iteration*******");
	    }
	    bestFlow.setInitialK(this.getPrecision());
	    bestFlow.setInitialMean(this.getEstimatedMean());
	    bestFlow.setInitialR(this.getResultantVector());
	    bestFlow.setNumberOfbestFlowElements(bestFisher.returnHolderCollection
		    .size());
	    bestFlow.setNumberOfElements(this.returnHolderCollection.size());
	    bestFlow.setResultK(bestFisher.getPrecision());
	    bestFlow.setResultR(bestFisher.getResultantVector());
	    bestFlow.setResultMean(bestFlow.getResultMean());
	}
	return bestFlow;
    }

    private void extendInliers(FisherStatistics innerFisher,
	    int numberOfElements, Random randomGenerator) {

	double[] innerEstimatedMean = innerFisher.getEstimatedMean();
	double innerCSD = innerFisher.getCSD();

	int equalsCase = 0, notEqualAndAdd = 0, notEqualNotAdd = 0;

	for (int j = 0; j < numberOfElements; j++) {
	    if (innerFisher.returnHolderCollection
		    .contains(returnHolderCollection.get(j))) {
		equalsCase++;
		continue;
	    }
	    ArrayRealVector innerMeanVector = new ArrayRealVector(
		    innerEstimatedMean);
	    ArrayRealVector currVector = new ArrayRealVector(
		    returnHolderCollection.get(j).getFilterResults(), 1, 3);
	    double angleBetween = Math.toDegrees(Math.acos(innerMeanVector
		    .cosine(currVector)));
	    if (angleBetween < innerCSD) {
		innerFisher.returnHolderCollection.add(returnHolderCollection
			.get(j));
		innerFisher.resetFisher();
		notEqualAndAdd++;
	    } else {
		notEqualNotAdd++;
	    }
	}
	DirectionalStatistics.logger
		.log(Level.FINER,
			" Extending Inner Equals:{0} Not Equal and Add:{1} Not Equal and Not Add:{2}",
			new Object[] { equalsCase, notEqualAndAdd,
				notEqualNotAdd });
    }

    private FisherStatistics selectRandomInliners(int numberOfElements,
	    Random randomGenerator) {
	List<FilterReturnHolder> innerList = new ArrayList<FilterReturnHolder>();
	List<Integer> randomNumbers = getUniqueRandomNumbers(numberOfElements,
		randomGenerator);

	for (int i = 0; i < (int) Math.ceil(BestFlow.minElementRatio
		* numberOfElements); i++) {
	    innerList.add(returnHolderCollection.get(randomNumbers.get(i)
		    .intValue()));
	}
	FisherStatistics innerFisher = new FisherStatistics(innerList);
	return innerFisher;
    }

    private List<Integer> getUniqueRandomNumbers(int numberofElements,
	    Random randomGenerator) {
	List<Integer> list = new ArrayList<Integer>(numberofElements);
	for (int i = 0; i < numberofElements; i++) {
	    list.add(new Integer(i));
	}
	Collections.shuffle(list, randomGenerator);
	return list;
    }

}
