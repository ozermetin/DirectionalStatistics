package statistics;

import imaging.ImageHeader;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import numerics.Point3D;
import numerics.RealMatrix;

import statistics.goodoffit.Quaternion;
import statistics.utils.VoxelOperations;
import tools.CL_Initializer;
import tractography.Tract;
import tractography.TractCollection;
import tractography.TractSource;
import tractography.Voxel;
import tractography.VoxelList;
import utils.BoundingSphere;
import utils.BoundingSphereHolder;

public abstract class MatcherFilter {
    public enum SeedOrTarget {
	SEED, TARGET
    }

    public static int NumberofEmptyDTVoxels = 0;

    private static Logger logger = Logger
	    .getLogger("camino.statistics.MatcherFilter");

    int[][][] roiData;
    int[][][] wmMaskData;
    double[][][][] targetData;
    int seedROICode;
    List<StatisticCommand> commandList;

    double maxConnectionDistance = 0;

    private List<FilterReturnHolder> seedFilterResults;
    private List<FilterReturnHolder> targetFilterResults;
    private List<FilterReturnHolder> tractFilterResults;
    private List<List<Quaternion>> tracksQuaternionList;

    private int targetROICode;

    private String tractfileName;

    private ImageHeader roiHeader;

    public MatcherFilter() {
	commandList = new ArrayList<StatisticCommand>();
    }

    public void setROIData(int[][][] roiData, int seedROICode, int targetROICode) {
	this.roiData = roiData;
	this.seedROICode = seedROICode;
	this.targetROICode = targetROICode;
    }

    public void setTargetData(double[][][][] targetData) {
	this.targetData = targetData;
    }

    public void setSeedCollection(List<FilterReturnHolder> filterResults) {
	this.seedFilterResults = filterResults;
    }

    public void setTargetCollection(List<FilterReturnHolder> filterResults) {
	this.targetFilterResults = filterResults;
    }

    public void setTractCollection(List<FilterReturnHolder> filterResults) {
	this.tractFilterResults = filterResults;
    }

    public abstract double[] filter(double[] voxel);

    public void addCommands(StatisticCommand command) {
	commandList.add(command);
    }

    public BoundingSphereHolder getBoundingSphere(SeedOrTarget seed) {
	List<FilterReturnHolder> returnHolder;
	switch (seed) {
	case SEED:
	    returnHolder = seedFilterResults;
	    break;
	case TARGET:
	    returnHolder = targetFilterResults;
	    break;
	default:
	    returnHolder = seedFilterResults;
	}

	BoundingSphere bSphere = new BoundingSphere(returnHolder.size());
	for (FilterReturnHolder holder : returnHolder) {
	    bSphere.build(holder.getCoordinates());
	}
	bSphere.calculate();
	return bSphere.getBoundingSphere();
    }

    protected List<FilterReturnHolder> getSeedCollection() {
	return seedFilterResults;
    }

    protected List<FilterReturnHolder> getTargetCollection() {
	return targetFilterResults;
    }

    protected List<FilterReturnHolder> getTractCollection() {
	return tractFilterResults;
    }

    protected double getMaxConnectionDistance() {
	return maxConnectionDistance;
    }

    public void processROI() {

	DirectionalStatistics.logger.log(Level.FINE, "Z="
		+ roiData[0][0].length + " Y=" + roiData[0].length + " "
		+ " X=" + roiData.length);
	int seedFilterEmpty = 0;
	int targetFilterEmpty = 0;
	for (int z = 0; z < roiData[0][0].length; z++)
	    for (int y = 0; y < roiData[0].length; y++)
		for (int x = 0; x < roiData.length; x++) {
		    if (seedROICode == roiData[x][y][z]
			    || targetROICode == roiData[x][y][z]) {
			double[] filterResult = filter(targetData[x][y][z]);
			if (filterResult != null) {
			    for (StatisticCommand command : commandList)
				command.command(filterResult);
			    FilterReturnHolder holder = new FilterReturnHolder(
				    new int[] { x, y, z }, filterResult);
			    if (seedROICode == roiData[x][y][z])
				seedFilterResults.add(holder);
			    else
				targetFilterResults.add(holder);
			} else {
			    if (seedROICode == roiData[x][y][z])
				seedFilterEmpty++;
			    else if (targetROICode == roiData[x][y][z])
				targetFilterEmpty++;
			}

		    }

		}
	System.out.println("Number of Empty Seed Voxels" + seedFilterEmpty);
	System.out.println("Number of Empty Target Voxels" + targetFilterEmpty);
    }

    public void setTractData(String tractfileName, ImageHeader roiHeader) {
	this.tractfileName = tractfileName;
	this.roiHeader = roiHeader;
    }

    public void processTracts(boolean writeOutFilteredTracts) {
	// TODO Auto-generated method stub
	TractSource source = new TractSource(tractfileName, roiHeader);
	double[] voxelDims = roiHeader.getVoxelDims();
	FileOutputStream fout = null;
	DataOutputStream dout = null;

	if (writeOutFilteredTracts) {
	    try {
		fout = new FileOutputStream(
			"/home/ozermetin/MIN/subjects/directionalstats/results/filteredtracts.Bfloat");
		dout = new DataOutputStream(new BufferedOutputStream(fout,
			1024 * 1024 * 16));
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	int numberOfTotalTracts = 0;
	int numberOfTargetTracts = 0;

	double avgNumberOfTargetTractLengths = 0;
	// Get Tracts Starting at seed ROI
	while (source.more()) {
	    Tract tract = source.nextTract();
	    // VoxelList voxelList= tract.toVoxelList(voxelDims);
	    // Voxel voxel=voxelList.getVoxel(voxelList.seedPointIndex());

	    Point3D point = tract.getSeedPoint();
	    
	    int x = (int) (point.x / voxelDims[0]);
	    int y = (int) (point.y / voxelDims[1]);
	    int z = (int) (point.z / voxelDims[2]);

	    int voxelCoor[]=VoxelOperations.convertPointToVoxel(point, voxelDims);
	    
	    if (roiData[x][y][z] == seedROICode) {
		double tractLengthFromSeed = processTract(tract);
		if (tractLengthFromSeed > 0) {
		    // Use StreamlineTracography Line:889 move
		    // DirectionalStatistics

		    if (writeOutFilteredTracts)
			writeTract(voxelDims, fout, dout, tract);

		    avgNumberOfTargetTractLengths = (avgNumberOfTargetTractLengths
			    * numberOfTargetTracts + tractLengthFromSeed)
			    / ++numberOfTargetTracts;
		    maxConnectionDistance = tractLengthFromSeed > maxConnectionDistance ? tractLengthFromSeed
			    : maxConnectionDistance;
		}
	    }
	    numberOfTotalTracts++;
	}

	if (writeOutFilteredTracts) {
	    try {
		fout.flush();
		dout.flush();
		fout.close();
		dout.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	DirectionalStatistics.logger
		.log(Level.FINE,
			"Number of Total Tracts={0}, Number of Target Tracts={1}, Average Distance={2}",
			new Object[] { numberOfTotalTracts,
				numberOfTargetTracts,
				avgNumberOfTargetTractLengths });

    }

    private void writeTract(double[] voxelDims, FileOutputStream fout,
	    DataOutputStream dout, Tract tract) {

	RealMatrix voxelToPhysicalTrans = roiHeader
		.getVoxelToPhysicalTransform();
	tract.transformToPhysicalSpace(voxelToPhysicalTrans, voxelDims[0],
		voxelDims[1], voxelDims[2]);

	try {
	    tract.writeRaw(dout);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * Add a eigenvalues along tracts to tractFilterResults
     * 
     */
    private final double processTract(Tract t) {

	double[] voxelDims = roiHeader.getVoxelDims();
	int numPoints = t.numberOfPoints();
	VoxelList vList = t.toVoxelList(voxelDims);

	double forwardDistance = -1.0;
	double backwardDistance = -1.0;
	boolean isForward = false;

	int forwardTargetIndex = 0;
	int backwardTargetIndex = 0;
	// System.out.print("Voxel Codes: ");
	for (int p = t.seedPointIndex(); p < numPoints; p++) {

	    double length = t.pathLengthFromSeed(p);
	    Point3D point = t.getPoint(p);

	    int x = (int) (point.x / voxelDims[0]);
	    int y = (int) (point.y / voxelDims[1]);
	    int z = (int) (point.z / voxelDims[2]);

	    int voxelCoor[]=VoxelOperations.convertPointToVoxel(point, voxelDims);
	    
	    int voxelCode = roiData[x][y][z];
	    // System.out.print(voxelCode+" ");
	    if (voxelCode == targetROICode) {
		forwardDistance = t.pathLengthFromSeed(p);
		forwardTargetIndex = p;
		// System.out.print(" Target ");
		// break;
	    } else if (voxelCode > 0 && voxelCode != seedROICode) {
		break;
	    }
	}
	// System.out.println(" |Forward");

	// count down to 0 from seed
	for (int p = t.seedPointIndex(); p >= 0; p--) {

	    Point3D point = t.getPoint(p);

	    int x = (int) (point.x / voxelDims[0]);
	    int y = (int) (point.y / voxelDims[1]);
	    int z = (int) (point.z / voxelDims[2]);

	    int voxelCode = roiData[x][y][z];

	    if (voxelCode == targetROICode) {
		backwardDistance = t.pathLengthFromSeed(p);
		backwardTargetIndex = p;
		break;
	    } else if (voxelCode > 0 && voxelCode != seedROICode) {
		break;
	    }
	}

	List<Point3D> voxels = null;

	if (forwardDistance >= backwardDistance && forwardDistance > 0) {
	    isForward = true;
	    voxels = new ArrayList<Point3D>();
	    for (int i = t.seedPointIndex(); i <= forwardTargetIndex; i++) {
		voxels.add(t.getPoints()[i]);
		// voxel = t.getPoints()[forwardTargetIndex];
	    }
	} else if (forwardDistance <= backwardDistance && backwardDistance > 0) {
	    isForward = false;
	    voxels = new ArrayList<Point3D>();
	    for (int i = t.seedPointIndex(); i >= backwardTargetIndex; i--) {
		voxels.add(t.getPoints()[i]);
		// voxel = t.getPoints()[backwardTargetIndex];
	    }
	}

	if (voxels != null) {
	    List<Quaternion> quaternionListByTrack = new ArrayList<Quaternion>();
	    int i = 0;
	    FilterReturnHolder prevHolder = null;
	    for (Point3D voxel : voxels) {
		int x = (int) (voxel.x / voxelDims[0]);
		int y = (int) (voxel.y / voxelDims[1]);
		int z = (int) (voxel.z / voxelDims[2]);
		// WM Check

		if (wmMaskData == null || wmMaskData[x][y][z] > 0) {
		    double[] filterResult = filter(targetData[x][y][z]);
		    if (filterResult != null) {
			for (StatisticCommand command : commandList)
			    command.command(filterResult);
			FilterReturnHolder holder = new FilterReturnHolder(
				new int[] { x, y, z }, filterResult);
			holder.setDistanceFromSeed(i);
			holder.setForwardTract(isForward);
			tractFilterResults.add(holder);

			if (prevHolder != null)
			    quaternionListByTrack
				    .add(Quaternion
					    .convertToQuaternion(
						    new Vector3D(
							    prevHolder.filterResults[1],
							    prevHolder.filterResults[2],
							    prevHolder.filterResults[3]),
						    new Vector3D(
							    holder.filterResults[1],
							    holder.filterResults[2],
							    holder.filterResults[3])));
			prevHolder = holder;

		    } else
			NumberofEmptyDTVoxels++;
		}
		i++;
	    }
	   tracksQuaternionList.add(quaternionListByTrack); 
	}
	return isForward ? forwardDistance : backwardDistance;
    }

    public int[][][] getWmMaskData() {
	return wmMaskData;
    }

    public void setWmMaskData(int[][][] wmMaskData) {
	this.wmMaskData = wmMaskData;
    }

    public List<List<Quaternion>> getTracksQuaternionList() {
        return tracksQuaternionList;
    }

    public void setTracksQuaternionList(List<List<Quaternion>> tracksQuaternionList) {
        this.tracksQuaternionList = tracksQuaternionList;
    }
}
