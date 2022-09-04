package statistics;

import imaging.ImageHeader;
import inverters.DT_Inversion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import numerics.ConvergenceException;
import numerics.Vector3D;

import misc.LoggedException;

import statistics.goodoffit.BinghamStatisticCalculator;
import statistics.goodoffit.WatsonStatisticCalculator;
import statistics.kent.KentStatisticCalculator;
import statistics.report.GoFReport;
import statistics.utils.DirectionalStatUtils;
import statistics.utils.VoxelOperations;
import tools.CL_Initializer;

import data.OutputManager;
import data.VoxelOrderDataSource;
import apps.Executable;
import apps.ProcessStreamlines;

public class DirectionalAnalysis extends Executable {

    public static final int NUMBEROFBOOTSTRAPS = 0;
    // public static final String basePath="home/ozermetin/MIN/subjects/";

    public static String inputDataType = "double";
    public static double FAThreshold = 0.7;
    public static Logger logger = Logger
	    .getLogger("camino.apps.DirectionalStatistics");
    private String dtifile;
    private String roifile;

    private VoxelOrderDataSource dtiData;
    private ImageHeader targetHdr;
    private String outputpath;
    private String method;
    private String reportfile;
    private String subject;
    private String area;
    private String baseSubjectPath;
    private String save;

    public static final int DownSamplePDFFactor = 15;

    public DirectionalAnalysis(String[] args) {
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
    public void initOptions(String[] args) {
	OutputManager.outputDataType = "float";
	ConsoleHandler handler = new ConsoleHandler();
	handler.setLevel(Level.FINE);

	logger.addHandler(handler);
	logger.setLevel(Level.FINE);

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
	    if (args[i].equals("-outputpath")) {
		outputpath = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-method")) {
		method = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-reportfile")) {
		reportfile = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-subject")) {
		subject = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-area")) {
		area = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-subjectMode")) {
		baseSubjectPath = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	    if (args[i].equals("-save")) {
		save = args[i + 1];
		CL_Initializer.markAsParsed(i, 2);
	    }
	}
	
	if (baseSubjectPath != null && !baseSubjectPath.equals(""))
	    setSubjectBaseMode();

	CL_Initializer.headerTemplateFile = roifile;

	CL_Initializer.initInputSpaceAndHeaderOptions();

	CL_Initializer.checkParsing(args);
    }

    private void setSubjectBaseMode() {
	StringTokenizer tokenizer = new StringTokenizer(subject, "_");
	String subjectNumber = tokenizer.nextToken();
	String subjectName = tokenizer.nextToken();
	// dti_h1.Bdouble
	dtifile = baseSubjectPath + "/" + subject + "/Camino/dti_" + subjectNumber
		+ "_"+subjectName+".Bdouble";
	roifile = baseSubjectPath + "/" + subject + "/AtlasBased/ROI/ROI_" + area + ".nii.gz";
	outputpath = baseSubjectPath + "/directionalstats/results";
	reportfile = baseSubjectPath + "/gofreport_0.ser";
    }

    @Override
    public void execute(OutputManager om) {

	int[][][] roiImage = loadROIFile();
	double[][][][] dtiImage = loadDTIFile();
	List<Vector3D> principalDirectionList = new ArrayList<Vector3D>();

	PrintWriter writer = null, writerPhiTheta = null;
	try {
	    writer = new PrintWriter(outputpath + "/principalDirections.csv");
	    writerPhiTheta = new PrintWriter(outputpath
		    + "/principalDirectionsInPhiTheta.csv");
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	DescriptiveStatistics faStatForROI = new DescriptiveStatistics();
	
	for (int z = 0; z < roiImage[0][0].length; z++)
	    for (int y = 0; y < roiImage[0].length; y++)
		for (int x = 0; x < roiImage.length; x++) {
		    if (roiImage[x][y][z] > 0) {
			Vector3D principalVector = VoxelOperations
				.getPrincipalDirection(VoxelOperations
					.getDTIEigsForVoxel(dtiImage[x][y][z]));
			
			double faValue = VoxelOperations.getFAForVoxel(dtiImage[x][y][z]);
			//if(faValue>FAThreshold)
			if(faValue<FAThreshold)
			{
        			principalDirectionList.add(principalVector);
        			faStatForROI.addValue(faValue);
        			writer.format("%d,%d,%d,%f,%f,%f\n", x, y, z,
        				principalVector.x, principalVector.y,
        				principalVector.z);
        			numerics.Vector3D numVector = new numerics.Vector3D(
        				principalVector.x, principalVector.y,
        				principalVector.z);
        			writerPhiTheta.format("%f\t%f\n",
        				numerics.Vector3D.thetaPhi(numVector)[1],
        				numerics.Vector3D.thetaPhi(numVector)[0]);
			}
		    }
		}
	logger.log(Level.FINE, "Principal Direction List Size= {0}",
		principalDirectionList.size());
	writer.flush();
	writer.close();
	writerPhiTheta.flush();
	writerPhiTheta.close();

	GoFReport.getInstance().setFile(reportfile);
	GoFReport.getInstance().add(subject, area, method, roifile);
	
	if(method==null)
	{ // Run Both
	    GoFReport.getInstance().setFile(reportfile);
	    GoFReport.getInstance().add(subject, area, "bingham", roifile);
	    
	    BinghamStatisticCalculator calculator = new BinghamStatisticCalculator();
	    try {
		calculator.calculate(principalDirectionList);
		calculator
			.bootstrap(principalDirectionList, NUMBEROFBOOTSTRAPS);
	    } catch (ConvergenceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    
	    if (save != null && save.equals("true")) {
		    try {
			GoFReport.getInstance().save();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    GoFReport.dispose();
	    
	    GoFReport.getInstance().setFile(reportfile);
	    GoFReport.getInstance().add(subject, area, "watson", roifile);
	    
	    WatsonStatisticCalculator calculatorW = new WatsonStatisticCalculator();
	    calculatorW.calculate(principalDirectionList);
	    calculatorW.bootstrap(principalDirectionList, NUMBEROFBOOTSTRAPS);
	    
	    if (save != null && save.equals("true")) {
		    try {
			GoFReport.getInstance().save();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    GoFReport.dispose();
	    
	    GoFReport.getInstance().setFile(reportfile);
	    GoFReport.getInstance().add(subject, area, "fa", roifile);
	    GoFReport.getInstance().getLastSubject().setFaStats(faStatForROI);
	    
	}
	else if (method.equals("bingham")) {
	    BinghamStatisticCalculator calculator = new BinghamStatisticCalculator();
	    try {
		calculator.calculate(principalDirectionList);
		calculator
			.bootstrap(principalDirectionList, NUMBEROFBOOTSTRAPS);
	    } catch (ConvergenceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    double[] confInt = calculator.calculateConfidenceInterval();
	    System.out.println("p1="
		    + DirectionalStatUtils.confToP(new double[] { confInt[0],
			    confInt[1] }, calculator.getbDist().k1()));
	    System.out.println("p2="
		    + DirectionalStatUtils.confToP(new double[] { confInt[2],
			    confInt[3] }, calculator.getbDist().k2()));
	} else if (method.equals("kent")) {
	    KentStatisticCalculator calculator = new KentStatisticCalculator();
	    calculator.calculate(principalDirectionList);
	} else if (method.equals("watson")) {
	    WatsonStatisticCalculator calculator = new WatsonStatisticCalculator();
	    calculator.calculate(principalDirectionList);
	    calculator.bootstrap(principalDirectionList, NUMBEROFBOOTSTRAPS);
	    double[] confInt = calculator.calculateConfidenceInterval();
	    System.out.println("p="
		    + DirectionalStatUtils.confToP(confInt,
			    calculator.getKappa()));
	}
	else if (method.equals("fa")) {
	    GoFReport.getInstance().getLastSubject().setFaStats(faStatForROI);
	}
	if (save != null && save.equals("true")) {
	    try {
		GoFReport.getInstance().save();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private int[][][] loadROIFile() {
	try {
	    targetHdr = ImageHeader.readHeader(roifile);
	} catch (IOException e) {
	    throw new LoggedException(e);
	}

	return ProcessStreamlines.readIntVolume(roifile);
    }

    private double[][][][] loadDTIFile() {
	dtiData = new VoxelOrderDataSource(dtifile, DT_Inversion.ITEMSPERVOX,
		inputDataType);
	return dtiData.getVoxelsDoubleArray(CL_Initializer.dataDims);
    }

}
