package statistics;

import imaging.ImageHeader;
import inverters.DT_Inversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.DebugGraphics;

import misc.LoggedException;
import statistics.MatcherFilter.SeedOrTarget;
import statistics.goodoffit.Quaternion;
import tools.CL_Initializer;
import utils.BoundingSphereHolder;
import utils.ConnectionData;
import utils.SerializableEvaluatedResult;
import utils.SerializeFileOperations;
import utils.Shell;
import utils.SphereShell;
import apps.Executable;
import apps.ProcessStreamlines;
import data.OutputManager;
import data.VoxelOrderDataSource;

public class DirectionalStatistics extends Executable {

    public static String inputDataType = "double";
    public static Logger logger = Logger
	    .getLogger("camino.apps.DirectionalStatistics");
    private String dtifile;
    private String roifile;
    private int seedindex;
    private int targetindex;
    private String tractfile;
    private VoxelOrderDataSource dtiData;
    private int numberofshells;
    private ImageHeader targetHdr;
    private String wmmask = null; // If null WM Mask Does not Apply
    private String state;
    private String patientname;
    private String outputfolder;

    public DirectionalStatistics(String[] args) {
	super(args);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void initDefaultVals() {
	// TODO Auto-generated method stub

    }

    @Override
    public void initVariables() {
	// TODO Auto-generated method stub

    }

    @Override
    /**
     * -dtifile : Diffusion Tensor File (Bfloat)
     * -roifile : ROI File (nii.gz)
     * -tractfile : Streamline Tract File (Blevelfloat)
     * -seedindex : ID of the seedROI
     * -targetindex: ID of the target 
     */
    public void initOptions(String[] args) {

	OutputManager.outputDataType = "float";
	ConsoleHandler handler = new ConsoleHandler();
	handler.setLevel(Level.FINE);

	logger.addHandler(handler);
	logger.setLevel(Level.FINE);
	logger.log(Level.INFO,
		"Logger Initialized with {0} and console with {1}",
		new Object[] { Level.FINEST, Level.FINEST });

	CL_Initializer.CL_init(args);

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-dtifile")) {
		dtifile = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-roifile")) {
		roifile = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);

	    }
	    if (args[i].equals("-tractfile")) {
		tractfile = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-seedindex")) {
		seedindex = Integer.parseInt(args[i + 1]);
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-targetindex")) {
		targetindex = Integer.parseInt(args[i + 1]);
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-numberofshells")) {
		numberofshells = Integer.parseInt(args[i + 1]);
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-wmmask")) {
		wmmask = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-state")) {
		state = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-patientname")) {
		patientname = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-outputfolder")) {
		outputfolder = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }

	}

	CL_Initializer.headerTemplateFile = roifile;

	if (!state.equals("compare"))
	    CL_Initializer.initInputSpaceAndHeaderOptions();

	CL_Initializer.checkParsing(args);
    }

    private int[][][] loadROIFile() {
	try {
	    targetHdr = ImageHeader.readHeader(roifile);
	} catch (IOException e) {
	    throw new LoggedException(e);
	}

	return ProcessStreamlines.readIntVolume(roifile);
    }

    private int[][][] loadWMMaskFile() {
	if (wmmask != null) {
	    try {
		targetHdr = ImageHeader.readHeader(wmmask);
	    } catch (IOException e) {
		throw new LoggedException(e);
	    }
	    int[][][] wmMaskImage = ProcessStreamlines.readIntVolume(wmmask);
	    int i = 0;
	    for (int z = 0; z < wmMaskImage[0][0].length; z++)
		for (int y = 0; y < wmMaskImage[0].length; y++)
		    for (int x = 0; x < wmMaskImage.length; x++) {
			if (wmMaskImage[x][y][z] > 0)
			    i++;
		    }
	    System.out.println("WM Mask Size:" + i);
	    return wmMaskImage;
	} else
	    return null;
    }

    private double[][][][] loadDTIFile() {
	dtiData = new VoxelOrderDataSource(dtifile, DT_Inversion.ITEMSPERVOX,
		inputDataType);
	return dtiData.getVoxelsDoubleArray(CL_Initializer.dataDims);
    }

    @Override
    public void execute(OutputManager om) {

	if (state.equals("evaluate")) {
	    executeEvaluateCommand();

	    /*
	     * try { fileOperations.writeSerializeFile(evaluatedResult); } catch
	     * (IOException e) { // TODO Auto-generated catch block
	     * e.printStackTrace(); }
	     */
	} else if (state.equals("compare")) {
	    // Read and Concat Healty Populations
	    executeCompareOperation();

	}

	/*
	 * for (FilterReturnHolder tract : tractCollection) { Shell shell =
	 * sShell.getShellofCoordinates(tract.coordinates);
	 * tract.setShell(shell); returnHolderList[shell.getShellNumber() -
	 * 1].add(tract); }
	 */

	// logger.log(Level.FINE, "Seed Center X:{0} Y:{1} z:{2}", new Object[]
	// { seedHolder.getCenterCoordinates()[0],
	// seedHolder.getCenterCoordinates()[1],
	// seedHolder.getCenterCoordinates()[2] });
	// logger.log(Level.FINE, "Seed Voxels={0}", seedCollection.size());
	// logger.log(Level.FINE, "Seed Radius{0}:", seedHolder.getRadius());
	//
	// logger.log(Level.FINE, "Target CenterX:{0} Y:{1} z:{2}", new Object[]
	// { targetHolder.getCenterCoordinates()[0],
	// targetHolder.getCenterCoordinates()[1],
	// targetHolder.getCenterCoordinates()[2] });
	// logger.log(Level.FINE, "Target Voxels={0}", targetCollection.size());
	// logger.log(Level.FINE, "Target Radius{0}:",
	// targetHolder.getRadius());
	// logger.log(Level.FINE, "Distance: {0}, IsIntercept:{1}",
	// new Object[] { seedHolder.distanceBetween(targetHolder),
	// seedHolder.isIntercept(targetHolder) });
	//
	// FisherStatistics fSeedStatistics = new
	// FisherStatistics(seedCollection);
	// logger.log(Level.FINE,
	// "Seed K={0} C={1} CSD={2} Mean_X={3} Mean_Y={4} Mean_Z={5}", new
	// Object[]{fSeedStatistics.getPrecision(),fSeedStatistics.getConfidenceInterval(0.05),fSeedStatistics.getCSD(),
	// fSeedStatistics.getEstimatedMean()[0],
	// fSeedStatistics.getEstimatedMean()[1],
	// fSeedStatistics.getEstimatedMean()[2] });
	// logger.log(Level.FINE, "Filter applied to seed #voxels:{0}",
	// seedCollection.size());
	//
	// FisherStatistics fTargetStatistics = new
	// FisherStatistics(targetCollection);
	// logger.log(Level.FINE,
	// "Target K={0} C={1} CSD={2} Mean_X={3} Mean_Y={4} Mean_Z={5}", new
	// Object[]{fTargetStatistics.getPrecision(),fTargetStatistics.getConfidenceInterval(0.05),fTargetStatistics.getCSD(),
	// fTargetStatistics.getEstimatedMean()[0],
	// fTargetStatistics.getEstimatedMean()[1],
	// fTargetStatistics.getEstimatedMean()[2]});
	// logger.log(Level.FINE, "Filter applied to target #voxels:{0}",
	// targetCollection.size());
	//
	// logger.log(Level.INFO, "# of Tracts ={0}",
	// tractVoxelCollection.size());
	// logger.log(Level.INFO,"# of NumberofEmptyDTVoxels = {0}" ,
	// diffusionEigFilter.NumberofEmptyDTVoxels);
	// for (int i = 0; i < numberofshells; i++) {
	// FisherStatistics fTractStatistics = new
	// FisherStatistics(returnHolderList[i]);
	// BestFlow bestFlow = fTractStatistics.getBestFlow();
	// System.out.println("----------------------");
	// logger.log(Level.FINE,
	// "Shell #={0} Init # Voxels={1}, K={2}: Result # Voxels={3}, K={4} %={5}",
	// new Object[]{new
	// Integer(i),bestFlow.getNumberOfElements(),bestFlow.getInitialK(),bestFlow.getNumberOfbestFlowElements(),bestFlow.getResultK(),(float)bestFlow.getNumberOfbestFlowElements()/bestFlow.getNumberOfElements()});
	// logger.log(Level.FINE, "Mean_X={0} Mean_Y={1} Mean_Z={2}", new
	// Object[]{fTractStatistics.getEstimatedMean()[0],fTractStatistics.getEstimatedMean()[1],fTractStatistics.getEstimatedMean()[2]});
	// logger.log(Level.FINE, "Filter applied to #voxels:{0}" ,
	// returnHolderList[i].size());
	// }
    }

    private void executeCompareOperation() {
	String connectionKey = seedindex + "-" + targetindex;
	SerializeFileOperations fileOperations = new SerializeFileOperations(
		outputfolder);
	SerializableEvaluatedResult evaluatedResult = new SerializableEvaluatedResult();
	try {
	    fileOperations.readSerializeConnectionFiles(evaluatedResult,
		    connectionKey);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	String[] patientKeys = evaluatedResult.getPatientKeys();
	FisherStatistics fisherForPatients = new FisherStatistics(
		new ArrayList<FilterReturnHolder>());
	for (String patientName : patientKeys) {
	    ConnectionData conData = evaluatedResult.getPatientConnection(
		    patientName, connectionKey);
	    FisherStatistics patienFisher = new FisherStatistics(
		    conData.getSeedCollection());
	    fisherForPatients.combine(patienFisher);
	}

	String[] subjectKeys = evaluatedResult.getSubjectKeys();
	FisherStatistics fisherForSubjects = new FisherStatistics(
		new ArrayList<FilterReturnHolder>());
	for (String subjectName : subjectKeys) {
	    ConnectionData conData = evaluatedResult.getSubjectConnection(
		    subjectName, connectionKey);
	    FisherStatistics subjectFisher = new FisherStatistics(
		    conData.getSeedCollection());
	    fisherForSubjects.combine(subjectFisher);
	}
	System.out.println("Are Seeds Different?"
		+ fisherForSubjects.areStatDiffers(fisherForPatients));

	fisherForPatients.clearAll();
	fisherForSubjects.clearAll();

	for (String patientName : patientKeys) {
	    ConnectionData conData = evaluatedResult.getPatientConnection(
		    patientName, connectionKey);
	    FisherStatistics patienFisher = new FisherStatistics(
		    conData.getTargetCollection());
	    fisherForPatients.combine(patienFisher);
	}

	for (String subjectName : subjectKeys) {
	    ConnectionData conData = evaluatedResult.getSubjectConnection(
		    subjectName, connectionKey);
	    FisherStatistics subjectFisher = new FisherStatistics(
		    conData.getTargetCollection());
	    fisherForSubjects.combine(subjectFisher);
	}
	System.out.println("Are Target Different?"
		+ fisherForSubjects.areStatDiffers(fisherForPatients));

	for (int i = 0; i < evaluatedResult.getPatientConnection(
		patientKeys[0], connectionKey).getNumberofShelves(); i++) {
	    fisherForPatients.clearAll();
	    fisherForSubjects.clearAll();

	    for (String patientName : patientKeys) {
		ConnectionData conData = evaluatedResult.getPatientConnection(
			patientName, connectionKey);
		FisherStatistics patienFisher = new FisherStatistics(
			conData.getReturnHolderList()[i]);
		fisherForPatients.combine(patienFisher);
	    }

	    for (String subjectName : subjectKeys) {
		ConnectionData conData = evaluatedResult.getSubjectConnection(
			subjectName, connectionKey);
		FisherStatistics subjectFisher = new FisherStatistics(
			conData.getReturnHolderList()[i]);
		fisherForSubjects.combine(subjectFisher);

		BestFlow bestFlow = subjectFisher.getBestFlow();
		System.out.println("----------------------");
		logger.log(
			Level.FINE,
			"Shell #={0} Init # Voxels={1}, K={2}: Result # Voxels={3}, K={4} %={5}",
			new Object[] {
				new Integer(i),
				bestFlow.getNumberOfElements(),
				bestFlow.getInitialK(),
				bestFlow.getNumberOfbestFlowElements(),
				bestFlow.getResultK(),
				(float) bestFlow.getNumberOfbestFlowElements()
					/ bestFlow.getNumberOfElements() });
		logger.log(Level.FINE, "Mean_X={0} Mean_Y={1} Mean_Z={2}",
			new Object[] { subjectFisher.getEstimatedMean()[0],
				subjectFisher.getEstimatedMean()[1],
				subjectFisher.getEstimatedMean()[2] });
	    }

	    System.out.println("Are " + i + " length of Connections Different?"
		    + fisherForSubjects.areStatDiffers(fisherForPatients));

	}

    }

    private void writeFilteredVoxelsToImage(
	    List<FilterReturnHolder> tractVoxelCollection, String imagePath) {

	int[] dataDims = targetHdr.getDataDims();
	double[][][] dataImage = new double[dataDims[0]][dataDims[1]][dataDims[2]];
	for (int k = 0; k < dataDims[2]; k++) {
	    for (int j = 0; j < dataDims[1]; j++) {
		for (int i = 0; i < dataDims[0]; i++) {
		    dataImage[i][j][k] = 0;
		}
	    }
	}

	File dubFile = new File(imagePath + ".csv");
	File undubFile = new File(imagePath + "_undub.csv");
	PrintWriter dubWriter = null, undubWriter = null;
	try {
	    dubWriter = new PrintWriter(dubFile);
	    undubWriter = new PrintWriter(undubFile);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	Set<String> listofCoordinates = new HashSet<String>();
	for (FilterReturnHolder tractVoxels : tractVoxelCollection) {
	    int x = tractVoxels.getCoordinates()[0];
	    int y = tractVoxels.getCoordinates()[1];
	    int z = tractVoxels.getCoordinates()[2];

	    dubWriter.format("%d,%d,%d,%f,%f,%f\n", x, y, z,
		    tractVoxels.filterResults[1], tractVoxels.filterResults[2],
		    tractVoxels.filterResults[3]);
	    if (!listofCoordinates.contains(String.valueOf(x)
		    + String.valueOf(y) + String.valueOf(z))) {
		undubWriter.format("%d,%d,%d,%f,%f,%f\n", x, y, z,
			tractVoxels.filterResults[1],
			tractVoxels.filterResults[2],
			tractVoxels.filterResults[3]);
		listofCoordinates.add(String.valueOf(x) + String.valueOf(y)
			+ String.valueOf(z));
	    }

	    dataImage[x][y][z] = dataImage[x][y][z] + 1;
	}
	dubWriter.flush();
	dubWriter.close();
	undubWriter.flush();
	undubWriter.close();
	targetHdr.writeScalarImage(dataImage, imagePath);

    }

    private void writeQuaternionByTract(
	    List<List<Quaternion>> tracksQuaternionList, String imagePath) {
	int i = 0, totalNumber = 0;
	for (List<Quaternion> quarternionByTract : tracksQuaternionList) {
	    /*File outFile = new File(imagePath + "_Tract_" + (i++)
		    + "_Quaternion.txt");
	    PrintWriter outWriter = null;
	    try {
		outWriter = new PrintWriter(outFile);
	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    }
	    outWriter.format("%d %d\n", quarternionByTract.size(), 4); */
	    for (Quaternion quat : quarternionByTract) {
		/*outWriter.format("%f %f %f %f\n", quat.getW(), quat.getX(),
			quat.getY(), quat.getZ()); */
		totalNumber++;
	    }
	    //outWriter.flush();
	    //outWriter.close();
	}

	File outFile = new File(imagePath + "_AllQuaternions.txt");
	PrintWriter outWriter = null;
	try {
	    outWriter = new PrintWriter(outFile);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
	outWriter.format("%d %d\n", totalNumber, 4);
	for (List<Quaternion> quarternionByTract : tracksQuaternionList) {
	    //outWriter.format("----------------------------------------\n");
	    for (Quaternion quat : quarternionByTract) {
		outWriter.format("%f %f %f %f\n", quat.getW(), quat.getX(),
			quat.getY(), quat.getZ());
		totalNumber++;
	    }
	}
	outWriter.flush();
	outWriter.close();
    }

    private void executeEvaluateCommand() {
	int[][][] roiImage = loadROIFile();
	int[][][] wmMaskData = loadWMMaskFile();
	double[][][][] dtiImage = loadDTIFile();
	List<FilterReturnHolder> seedCollection = new ArrayList<FilterReturnHolder>();
	List<FilterReturnHolder> targetCollection = new ArrayList<FilterReturnHolder>();
	List<FilterReturnHolder> tractVoxelCollection = new ArrayList<FilterReturnHolder>();
	List<List<Quaternion>> tracksQuaternionList = new ArrayList<List<Quaternion>>();

	ReturnHolderList[] returnHolderList = new ReturnHolderList[numberofshells];
	for (int i = 0; i < numberofshells; i++) {
	    returnHolderList[i] = new ReturnHolderList();
	}

	DiffusionTensorEigFilter diffusionEigFilter = new DiffusionTensorEigFilter();
	diffusionEigFilter.setROIData(roiImage, seedindex, targetindex);
	diffusionEigFilter.setTargetData(dtiImage);
	diffusionEigFilter.setSeedCollection(seedCollection);
	diffusionEigFilter.setTargetCollection(targetCollection);
	diffusionEigFilter.setTractCollection(tractVoxelCollection);
	diffusionEigFilter.setTractData(tractfile, targetHdr);
	diffusionEigFilter.setTracksQuaternionList(tracksQuaternionList);
	diffusionEigFilter.setWmMaskData(wmMaskData);

	diffusionEigFilter.processROI();
	diffusionEigFilter.processTracts(true);

	BoundingSphereHolder seedHolder = diffusionEigFilter
		.getBoundingSphere(SeedOrTarget.SEED);
	BoundingSphereHolder targetHolder = diffusionEigFilter
		.getBoundingSphere(SeedOrTarget.TARGET);
	SphereShell sShell = new SphereShell(numberofshells);
	sShell.setSpheres(seedHolder, targetHolder);

	for (FilterReturnHolder tractVoxels : tractVoxelCollection) {
	    int distanceIndex = (int) (tractVoxels.getDistanceFromSeed() / (diffusionEigFilter
		    .getMaxConnectionDistance() / (numberofshells - 1)));
	    // distanceIndex = distanceIndex >= numberofshells?
	    // numberofshells-1: distanceIndex;
	    returnHolderList[distanceIndex].add(tractVoxels);
	}

	SerializeFileOperations fileOperations = new SerializeFileOperations(
		outputfolder);
	/*
	 * SerializableEvaluatedResult evaluatedResult = null; try {
	 * evaluatedResult = fileOperations.readSerializeFile(); } catch
	 * (ClassNotFoundException e1) { // TODO Auto-generated catch block
	 * e1.printStackTrace(); } catch (IOException e1) { // TODO
	 * Auto-generated catch block e1.printStackTrace(); } if
	 * (evaluatedResult == null) evaluatedResult = new
	 * SerializableEvaluatedResult();
	 */
	SerializableEvaluatedResult evaluatedResult = new SerializableEvaluatedResult();

	if (patientname.startsWith("h")
		&& !evaluatedResult.existPatient(patientname))
	    try {
		evaluatedResult.addPatient(patientname);
	    } catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	else if (patientname.startsWith("s")
		&& !evaluatedResult.existSubject(patientname))
	    try {
		evaluatedResult.addSubject(patientname);
	    } catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	ConnectionData conData = new ConnectionData();
	conData.setStartROI(seedindex);
	conData.setEndROI(targetindex);
	conData.setNumberofShelves(numberofshells);
	conData.setReturnHolderList(returnHolderList);
	conData.setSeedCollection(seedCollection);
	conData.setTargetCollection(targetCollection);
	conData.setTractVoxelCollection(tractVoxelCollection);

	if (patientname.startsWith("h"))
	    try {
		evaluatedResult.addPatientConnection(patientname, conData);
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	    }
	else if (patientname.startsWith("s"))
	    try {
		evaluatedResult.addSubjectConnection(patientname, conData);
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	    }

	try {
	    fileOperations.writeSerializeConnectionFile(conData, patientname);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	writeFilteredVoxelsToImage(tractVoxelCollection,
		"/home/ozermetin/MIN/subjects/directionalstats/results/FilteredVoxelImage_"
			+ seedindex + "_" + targetindex);
	
	writeQuaternionByTract(tracksQuaternionList,
		"/home/ozermetin/MIN/subjects/directionalstats/results/Quaternions_"
			+ seedindex + "_" + targetindex);
    }

}
