package statistics;

public class BestFlow {

	public static double minElementRatio = 0.65;
	public static int NumberofIterations = 20;

	public BestFlow() {
		// TODO Auto-generated constructor stub
	}

	int numberOfElements;
	int numberOfbestFlowElements;
	double initialK;
	double initialR;
	double[] initialMean;
	double resultK;
	double resultR;
	double[] resultMean;

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public int getNumberOfbestFlowElements() {
		return numberOfbestFlowElements;
	}

	public void setNumberOfbestFlowElements(int numberOfbestFlowElements) {
		this.numberOfbestFlowElements = numberOfbestFlowElements;
	}

	public double getInitialK() {
		return initialK;
	}

	public void setInitialK(double initialK) {
		this.initialK = initialK;
	}

	public double getInitialR() {
		return initialR;
	}

	public void setInitialR(double initialR) {
		this.initialR = initialR;
	}

	public double[] getInitialMean() {
		return initialMean;
	}

	public void setInitialMean(double[] initialMean) {
		this.initialMean = initialMean;
	}

	public double getResultK() {
		return resultK;
	}

	public void setResultK(double resultK) {
		this.resultK = resultK;
	}

	public double[] getResultMean() {
		return resultMean;
	}

	public void setResultMean(double[] resultMean) {
		this.resultMean = resultMean;
	}

	public double getResultR() {
		return resultR;
	}

	public void setResultR(double resultR) {
		this.resultR = resultR;
	}

}
