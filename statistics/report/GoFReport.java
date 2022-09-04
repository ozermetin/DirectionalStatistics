package statistics.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class GoFReport {

    private static GoFReport instance;
    List<Subject> subjectList;
    String reportFile;
    Subject lastSubject;

    private GoFReport() {

    }

    public static GoFReport getInstance() {
	if (instance == null) {
	    instance = new GoFReport();
	}
	return instance;
    }

    public static void dispose() {
	instance = null;
    }

    public void setFile(String reportFile) {
	this.reportFile = reportFile;
	// TODO Auto-generated constructor stub
	if (reportFile != null && new File(reportFile).exists()) {
	    FileInputStream fileIn;
	    try {
		fileIn = new FileInputStream(reportFile);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		subjectList = (List<Subject>) in.readObject();
		in.close();
		fileIn.close();
	    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else {
	    subjectList = new ArrayList<Subject>();
	}
    }

    public void add(String subjectName, String areaName, String method,
	    String filepath) {
	Subject newComer = new Subject(subjectName, areaName, method, filepath);
	boolean found = false;
	for (Subject subject : subjectList) {
	    if (subject.equals(newComer)) {
		found = true;
		lastSubject = subject;
		break;
	    }
	}
	if (!found)
	    subjectList
		    .add(new Subject(subjectName, areaName, method, filepath));
    }

    public Subject getLastSubject() {
	if (lastSubject == null)
	    return subjectList.get(subjectList.size() - 1);
	else
	    return lastSubject;
    }

    public List<Subject> get(String subjectName) {
	List<Subject> subjects = new ArrayList<Subject>();
	for (Subject subject : subjectList) {
	    if (subject.equals(new Subject(subjectName, null, null, null)))
		subjects.add(subject);
	}
	return subjects;
    }

    public void remove(String subjectName) {
	for (int i = 0; i < subjectList.size(); i++) {
	    Subject subject = subjectList.get(i);
	    if (subject.equals(new Subject(subjectName, null, null, null))) {
		subjectList.remove(subject);
		i--;
	    }
	}
    }

    public void remove(String subjectName, String areaName) {
	for (int i = 0; i < subjectList.size(); i++) {
	    Subject subject = subjectList.get(i);
	    if (subject.equals(new Subject(subjectName, areaName, null, null))) {
		subjectList.remove(subject);
		i--;
	    }
	}
    }

    public void remove(String subjectName, String areaName, String method) {
	for (int i = 0; i < subjectList.size(); i++) {
	    Subject subject = subjectList.get(i);
	    if (subject
		    .equals(new Subject(subjectName, areaName, method, null))) {
		subjectList.remove(subject);
		i--;
	    }
	}
    }

    public void save() throws IOException {
	FileOutputStream fileOut = new FileOutputStream(reportFile);
	ObjectOutputStream out = new ObjectOutputStream(fileOut);
	out.writeObject(subjectList);
	out.close();
	fileOut.close();
    }

    public void report() {
	System.out.println("Starting Report with Size: " + subjectList.size());
	Collections.sort(subjectList);
	for (Subject subject : subjectList) {
	    System.out.println(subject);
	    System.out.println("**************************");
	}
    }

    public void summaryReport(List<String> subjectToReport) {
	System.out.println("Starting Summary Report with Size: "
		+ subjectList.size());
	Collections.sort(subjectList);
	String prevArea = "";
	for (Subject subject : subjectList) {
	    if (subjectToReport == null
		    || subjectToReport.contains(subject.getSubjectName())) {
		if (!prevArea.equals(subject.getAreaName())) {
		    System.out.println("**************************");
		    prevArea = subject.getAreaName();
		}
		if (subject.getMethod().equals("fa")) {
		    System.out.println("Area: " + subject.getAreaName()
			    + " Subject: " + subject.getSubjectName()
			    + " Voxels: " + subject.getNumberofVoxels()
			    + " Method: " + subject.getMethod() + " :"
			    + subject.getFaStats().toString());
		} else {
		    System.out.println("Area: " + subject.getAreaName()
			    + " Subject: " + subject.getSubjectName()
			    + " Voxels: " + subject.getNumberofVoxels()
			    + " Method: " + subject.getMethod() + " :"
			    + subject.getChiSquareStat().getChiSquareStat()
			    + " : "
			    + subject.getChiSquareStat().getTestResult());
		}
	    }
	}
    }

    public void reportToExcel(List<String> subjectToReport) {
	System.out.println("Starting Excel Report with Size: "
		+ subjectList.size());

	HashMap<String, List<Double>> map = new HashMap<String, List<Double>>();
	Collections.sort(subjectList);

	for (Subject subject : subjectList) {
	    if (subjectToReport == null
		    || subjectToReport.contains(subject.getSubjectName())) {
		String key = subject.getAreaName() + "_" + subject.getMethod();
		if (!map.containsKey(key)) {
		    if (subject.getMethod().equals("fa")) {
			List<Double> tempList = new ArrayList<Double>();
			tempList.add(subject.getFaStats().getMean());
			map.put(key, tempList);
		    } else {
			List<Double> tempList = new ArrayList<Double>();
			tempList.add(subject.getChiSquareStat()
				.getChiSquareStat());
			map.put(key, tempList);
		    }
		} else {
		    if (subject.getMethod().equals("fa")) {
			map.get(key).add(subject.getFaStats().getMean());
		    } else {
			map.get(key).add(
				subject.getChiSquareStat().getChiSquareStat());
		    }
		}
	    }
	}

	for (Entry<String, List<Double>> entry : map.entrySet()) {
	    StringBuffer buffer = new StringBuffer(entry.getKey());
	    for (Double stat : entry.getValue()) {
		buffer.append("\t" + stat);
	    }
	    System.out.println(buffer.toString());
	}
	System.out.println("--EOF--");
    }

    public boolean clear() {
	if (reportFile != null && new File(reportFile).exists()) {
	    new File(reportFile).delete();
	    return true;
	}
	return false;
    }

}
