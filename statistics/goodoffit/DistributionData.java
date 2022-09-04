package statistics.goodoffit;

import java.io.Serializable;


public class DistributionData implements Comparable<DistributionData>, Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	public double xi, phi, theta, pdf, weight;
	public int observed=0;

	public DistributionData(double xi, double phi, double theta, double weight, double pdf) {
	    this.xi = xi;
	    this.phi = phi;
	    this.theta = theta;
	    this.pdf = pdf;
	    this.weight=weight;
	    this.observed=0;
	}
	
	public void incrementObserved()
	{
	    this.observed++;
	}

	@Override
	public int compareTo(DistributionData o) {
	    if (this.xi == o.xi)
		return 1;
	    else if (this.xi < o.xi)
		return -1;
	    else
		return 1;
	}
	
	public String toString()
	{
	    return xi+"\t"+phi*180/Math.PI+"\t"+theta*180/Math.PI+"\t"+pdf;
	}

}
