package statistics.utils;



import apps.FracAnis;
import tools.CL_Initializer;
import misc.DT;
import misc.LoggedException;
import numerics.Point3D;
import numerics.Vector3D;

public class VoxelOperations {

    public VoxelOperations() {
	// TODO Auto-generated constructor stub
    }

    public static int[] convertPointToVoxel(Point3D p, double[] voxelDims) {
	if (p.x < 0 || p.y < 0 || p.z < 0
		|| p.x > CL_Initializer.dataDims[0] * voxelDims[0]
		|| p.y > CL_Initializer.dataDims[1] * voxelDims[1]
		|| p.z > CL_Initializer.dataDims[2] * voxelDims[2])
	    return new int[] { 0, 0, 0 };

	int xInd = (int) (p.x / voxelDims[0]);
	int yInd = (int) (p.y / voxelDims[1]);
	int zInd = (int) (p.z / voxelDims[2]);
	xInd = (xInd < 0) ? 0 : xInd;
	yInd = (yInd < 0) ? 0 : yInd;
	zInd = (zInd < 0) ? 0 : zInd;
	xInd = (xInd >= CL_Initializer.dataDims[0]) ? (CL_Initializer.dataDims[0] - 1)
		: xInd;
	yInd = (yInd >= CL_Initializer.dataDims[1]) ? (CL_Initializer.dataDims[1] - 1)
		: yInd;
	zInd = (zInd >= CL_Initializer.dataDims[2]) ? (CL_Initializer.dataDims[2] - 1)
		: zInd;
	return new int[] { xInd, yInd, zInd };
    }
    
    public static double[] getDTIEigsForVoxel(double[] voxel) {
	    DT[] dts = FracAnis.getTensorList(voxel, "dt");

	    // Remove background check
	    if (voxel[0] >= 0) {
	    	for (int i = 0; i < dts.length; i++) {
	    		try {
	    			double[][] sEig = dts[i].sortedEigenSystem();
	    			// Flatten into an array
	    			double[] eigFlat = new double[12];
	    			for (int j = 0; j < 3; j++) {
	    				for (int k = 0; k < 4; k++) {
	    					eigFlat[4 * j + k] = sEig[k][j];
	    				}
	    			}
	    			return eigFlat;
	    		} catch (Exception e) {
	    			LoggedException.logExceptionWarning(e, Thread
	    					.currentThread().getName());
	    			double[] zeros = new double[12];
	    			return zeros;
	    		}

	    	}
	    }
	    return null;
	}
    
    public static double getFAForVoxel(double[] voxel) {
	    DT[] dts = FracAnis.getTensorList(voxel, "dt");
	    // Remove background check
	    if (voxel[0] >= 0) { 
		return dts[0].fa();
	    }
	    return 0;
    }
    
    public static Vector3D getPrincipalDirection(double[] DTIeigs)
    {
	return new Vector3D(DTIeigs[1],DTIeigs[2],DTIeigs[3]);
    }

}
