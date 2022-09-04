package statistics.goodoffit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class ChiSquareStatDynamicBins {

    private int numberofBins;
    private int minNumberofElements;
    
    public ChiSquareStatDynamicBins(int numberofBins, int minNumberofElements) {
	this.numberofBins = numberofBins;
	this.minNumberofElements = minNumberofElements;
    }
    
    public Double[] getExpectedCuts(double[] expV)
    {
	Double[] expectedCuts = new Double[numberofBins];
	double start = expV[0];
	double end= expV[expV.length-1];
	double expCutsInc=(end-start)/numberofBins;
	for(int i=0; i<numberofBins; i++)
	{
	  expectedCuts[i]=start+expCutsInc; 
	  start = expectedCuts[i];
	}
	//Because of rounding errors
	expectedCuts[numberofBins-1]=end;
	return expectedCuts;
    }
    
    public Integer[] getCountsInRange(double[] expV, Double[] expectedCuts)
    {
	Arrays.sort(expV);
	
	Integer[] counts = new Integer[numberofBins];
	
	/*
	int prevCount=0;
	for(int i=0;i<numberofBins;i++)
	{
	    int numberofelements=Arrays.binarySearch(expV, expectedCuts[i]);
	    if(numberofelements<0)
		counts[i]=(numberofelements+1)*-1-prevCount;
	    else
		counts[i]=numberofelements-prevCount;
	    prevCount=counts[i];
	}
	*/
	
	int currCount=0, currentIndex=0,i=0;
	//for(int i =0; i< expV.length; i++)
	while(currentIndex<numberofBins && i<expV.length)
	{
	    if(expV[i]<=expectedCuts[currentIndex])
	    {
		currCount++;
		i++;
	    }
	    else
	    {
		counts[currentIndex]=currCount;
		currCount=0;
		currentIndex++;
	    }
	}
	counts[numberofBins-1]=currCount;

	return counts;
    }
    
    public Double[] mergeRanges(Integer[] counts, Double[] expectedCuts)
    {
	
      ArrayList<Integer> countList=new ArrayList<Integer>(Arrays.asList(counts));
      ArrayList<Double> expectedCutsList=new ArrayList<Double>(Arrays.asList(expectedCuts));
      int currConsideration =0;
      while(currConsideration<countList.size())
      {
	  if(countList.get(currConsideration)<minNumberofElements)
	  {
	      if(currConsideration==0) //No prev add next
	      {
		  Integer count = countList.get(currConsideration);
		  countList.set(currConsideration+1, countList.get(currConsideration+1)+count);
		  countList.remove(currConsideration);
		  expectedCutsList.remove(currConsideration);
	      }
	      else if(currConsideration==countList.size()-1) //No next add prev
	      {
		  Integer count = countList.get(currConsideration);
		  countList.set(currConsideration-1, countList.get(currConsideration-1)+count);
		  countList.remove(currConsideration-1);
		  expectedCutsList.remove(currConsideration-1);
	      }
	      else
	      {
		  Integer count = countList.get(currConsideration);
		  Integer prev = countList.get(currConsideration-1);
		  Integer next = countList.get(currConsideration+1);
		  if(prev<next)
		  {
		      countList.set(currConsideration-1, prev+count);
		      countList.remove(currConsideration-1);
		      expectedCutsList.remove(currConsideration-1);
		  }
		  else
		  {
		      countList.set(currConsideration+1, next+count);
		      countList.remove(currConsideration);
		      expectedCutsList.remove(currConsideration);
		  }
	      }
	      
	  }
	  else
	  {
	      currConsideration++;
	  }
      }
      
      return expectedCutsList.toArray(new Double[]{});
    }
    
    public Double[] selectRanges(double[] expV)
    {
	Arrays.sort(expV);
	System.out.println("Values:"+expV.length+"|"+Arrays.toString(expV));
	Double[] cuts=getExpectedCuts(expV);
	System.out.println("Cuts:"+cuts.length+"|"+Arrays.toString(cuts));
	Integer[] counts = getCountsInRange(expV, cuts);
	System.out.println("Counts:"+counts.length+"|"+Arrays.toString(counts));
	Double[] ranges = mergeRanges(counts,cuts);
	System.out.println("Ranges:"+ranges.length+"|"+Arrays.toString(ranges));
	return ranges;
    }
    
    
    
    public ChiSquareStatistic doChiSquareStat(Double[] ranges, double[] valObs, double[] valExp)
    {
	FrequencyTable freTable = new FrequencyTable(ranges);
	for (double val: valObs)
	{
	    freTable.addObs(val);
	}
	for (double val: valExp)
	{
	    freTable.addExp(val);
	}
	/*
	 * chiSquareStat[0]=chiSquare.chiSquare(expectedBins, observedBins);
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
	 */
	
	ChiSquareStatistic stat = new ChiSquareStatistic();
	ChiSquareTest chiTest = new ChiSquareTest();
	stat.setpValue(chiTest.chiSquareTest(freTable.getCountsExp(), freTable.getCountsObs()));
	stat.setChiSquareStat(chiTest.chiSquare(freTable.getCountsExp(), freTable.getCountsObs()));
	if(chiTest.chiSquareTest(freTable.getCountsExp(), freTable.getCountsObs(),0.1))
	    stat.setTestResult(1);
	else
	    stat.setTestResult(0);
	stat.setObservedBins(freTable.getCountsObs());
	stat.setExpectedBins(freTable.getCountsExp());
	return stat;
    }
    
    public double[] countExp(Double[] ranges, double[] valExp)
    {
	FrequencyTable freTable = new FrequencyTable(ranges);
	for (double val: valExp)
	{
	    freTable.addExp(val);
	}
	return freTable.getCountsExp();
    }
    
    public long[] countObs(Double[] ranges, double[] valObs)
    {
	FrequencyTable freTable = new FrequencyTable(ranges);
	for (double val: valObs)
	{
	    freTable.addObs(val);
	}
	return freTable.getCountsObs();
    }
    
    
    private class FrequencyTable{
	private Double[] ranges;
	private double[] countsExp;
	private long[] countsObs;
	
	public FrequencyTable(Double[] ranges)
	{
	    this.ranges = ranges;
	    this.ranges[this.ranges.length-1]=Double.MAX_VALUE;
	    this.countsExp = new double[ranges.length];
	    this.countsObs = new long[ranges.length];
	}
	
	public void addExp(double value)
	{
	    int insertionPoint = Arrays.binarySearch(ranges, value);
	    if(insertionPoint<0)
		countsExp[~insertionPoint]++;
	    else
		countsExp[insertionPoint]++;
	}
	
	public void addObs(double value)
	{
	    int insertionPoint = Arrays.binarySearch(ranges, value);
	    if(insertionPoint<0)
		countsObs[~insertionPoint]++;
	    else
		countsObs[insertionPoint]++;
	    
	    
	}

	public double[] getCountsExp() {
	    return countsExp;
	}

	public long[] getCountsObs() {
	    return countsObs;
	}
    }

}
