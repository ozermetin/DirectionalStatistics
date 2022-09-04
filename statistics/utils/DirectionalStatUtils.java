package statistics.utils;

public  class DirectionalStatUtils {

    public static double confToP(double[] conf, double est)
    {
	/*
	 *  1 calculate the standard error: SE = (u − l)/(2×1.96)

	    2 calculate the test statistic: z = Est/SE

            3 calculate the P value2: P = exp(−0.717×z − 0.416×z2)
	 */
	
	double SE=(conf[1]-conf[0])/(2*1.96);
	double z = est/SE;
	//−0.717×z − 0.416×
	return Math.exp(-0.717*z - 0.416*z*z);
    }

}
