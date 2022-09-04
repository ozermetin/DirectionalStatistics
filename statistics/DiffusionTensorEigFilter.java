package statistics;

import org.apache.commons.math3.linear.ArrayRealVector;

import statistics.utils.VoxelOperations;

import misc.DT;
import misc.LoggedException;
import apps.FracAnis;

public class DiffusionTensorEigFilter extends MatcherFilter {

	public DiffusionTensorEigFilter() {
		super();
		addCommands(new NormalizeEigCommand());
	}

	@Override
	public double[] filter(double[] voxel) {
		return VoxelOperations.getDTIEigsForVoxel(voxel);
	}

	
	
	protected class NormalizeEigCommand implements StatisticCommand {

		@Override
		public void command(double[] eigs) {
			for (int i = 0; i < 12; i += 4) {
				ArrayRealVector vector = new ArrayRealVector(eigs, i + 1, 3);
				vector.unitize();
				double[] firstEig = vector.toArray();
				eigs[i + 1] = firstEig[0];
				eigs[i + 2] = firstEig[1];
				eigs[i + 3] = firstEig[2];
			}
		}

	}

}
