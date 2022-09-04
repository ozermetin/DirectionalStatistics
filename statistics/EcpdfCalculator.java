package statistics;

import java.util.Comparator;

public class EcpdfCalculator {

    public EcpdfCalculator() {
	// TODO Auto-generated constructor stub
    }
    
    
    
    private class ThetaPhiHolder implements Comparator
    {
	double theta, phi;
	
	ThetaPhiHolder(double theta, double phi)
	{
	    this.theta=theta;
	    this.phi=phi;
	}

	@Override
	public int compare(Object arg0, Object arg1) {
	    // TODO Auto-generated method stub
	    if(arg0 instanceof ThetaPhiHolder && arg1 instanceof ThetaPhiHolder)
	    {
		/*if(((ThetaPhiHolder)arg0).theta > ((ThetaPhiHolder)arg1).theta)
		    return 1;
		else if (((ThetaPhiHolder)arg0).theta == ((ThetaPhiHolder)arg1).theta)
		    if ((ThetaPhiHolder)arg0).phi == ((ThetaPhiHolder)arg1).phi
		    return  */
	    }
	    return 0;
	}


	public double getTheta() {
	    return theta;
	}


	public void setTheta(double theta) {
	    this.theta = theta;
	}


	public double getPhi() {
	    return phi;
	}


	public void setPhi(double phi) {
	    this.phi = phi;
	}
	
    }

}
