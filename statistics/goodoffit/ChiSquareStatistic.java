package statistics.goodoffit;

import java.io.Serializable;

public class ChiSquareStatistic implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double chiSquareStat;
    private double pValue;
    private double testResult;
    private long[] observedBins;
    private double[] expectedBins;
    private DistributionData[] distributionData;
    
    public ChiSquareStatistic() {
	// TODO Auto-generated constructor stub
    }

    public double getChiSquareStat() {
        return chiSquareStat;
    }

    public void setChiSquareStat(double chiSquareStat) {
        this.chiSquareStat = chiSquareStat;
    }

    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }

    public double getTestResult() {
        return testResult;
    }

    public void setTestResult(double testResult) {
        this.testResult = testResult;
    }

    public long[] getObservedBins() {
        return observedBins;
    }

    public void setObservedBins(long[] observedBins) {
        this.observedBins = observedBins;
    }

    public double[] getExpectedBins() {
        return expectedBins;
    }

    public void setExpectedBins(double[] expectedBins) {
        this.expectedBins = expectedBins;
    }

    public DistributionData[] getDistributionData() {
        return distributionData;
    }

    public void setDistributionData(DistributionData[] distributionData) {
        this.distributionData = distributionData;
    }
    
    public String toString()
    {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Chi Stat: "+chiSquareStat);
	buffer.append(" pValue: "+pValue);
	buffer.append(" Test: "+testResult);
	buffer.append("\n"); 
	return buffer.toString();
    }

}
