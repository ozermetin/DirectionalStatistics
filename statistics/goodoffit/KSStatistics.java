package statistics.goodoffit;

public class KSStatistics {

    private static final long serialVersionUID = 1L;
    private double ksStat;
    private double pValue;
    private double testResult;
    private double[] observedBins;
    private double[] expectedBins;

    public KSStatistics() {
	// TODO Auto-generated constructor stub
    }

    public double getKsStat() {
        return ksStat;
    }

    public void setKsStat(double ksStat) {
        this.ksStat = ksStat;
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

    public double[] getObservedBins() {
        return observedBins;
    }

    public void setObservedBins(double[] observedBins) {
        this.observedBins = observedBins;
    }

    public double[] getExpectedBins() {
        return expectedBins;
    }

    public void setExpectedBins(double[] expectedBins) {
        this.expectedBins = expectedBins;
    }
    
    public String toString()
    {
	StringBuffer buffer = new StringBuffer();
	buffer.append("KS Stat: "+ksStat);
	buffer.append(" pValue: "+pValue);
	buffer.append(" Test: "+testResult);
	buffer.append("\n"); 
	return buffer.toString();
    }

}
