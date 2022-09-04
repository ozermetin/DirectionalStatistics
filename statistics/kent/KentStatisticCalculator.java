package statistics.kent;

import java.util.List;

import stat.kent.input.KentStatistic;

import numerics.Vector3D;

public class KentStatisticCalculator {

    public KentStatisticCalculator() {
	// TODO Auto-generated constructor stub
    }

    public void calculate(List<Vector3D> principalList) {
	KentStatistic kentStat = new KentStatistic();
	for (Vector3D principal : principalList) {
	    kentStat.addVectorAsInput(principal.x, principal.y, principal.z);
	}
	
	/*kent.calculateMeanR();
	kent.calculateCovarianceMatrix();
	kent.calculateSumThetaandPhi();
	*/
	kentStat.calculateAllVectorOperations();
	System.out.println("***************************");
	System.out.println("RVectorNormLength:"+kentStat.getR1Vector().getNorm()/kentStat.vectorList.size());
	System.out.println("Avg Theta="+kentStat.getAvgTheta()+" Avg Phi="+kentStat.getAvgPhi());
	System.out.println("***************************");

	kentStat.calculateHMatrix();
	kentStat.calculateBMatrix();
	kentStat.calculateKMatrix();
	kentStat.calculateVMatrix();
	kentStat.calculateKappaBeta();
	System.out.println("Kappa="+kentStat.getKappa()+" Betta="+kentStat.getBetta());
	System.out.println("***************************");
	
    }

}
