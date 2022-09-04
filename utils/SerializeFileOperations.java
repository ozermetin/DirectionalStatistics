package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

public class SerializeFileOperations {

    String outputfolder;

    
    public SerializeFileOperations(String outputfolder) {
	this.outputfolder=outputfolder;
    }

    public void writeSerializeFile(SerializableEvaluatedResult evaluatedResult) throws IOException {
	FileOutputStream fos = new FileOutputStream(outputfolder+File.separator+ "DirectionalStats.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(evaluatedResult);
        oos.close(); 
    }
    
    public SerializableEvaluatedResult readSerializeFile() throws IOException, ClassNotFoundException
    {
	FileInputStream fis = new FileInputStream(outputfolder+File.separator+  "DirectionalStats.ser");
	ObjectInputStream ois = new ObjectInputStream(fis);
	return (SerializableEvaluatedResult)ois.readObject(); 
    }
    
    
    public void writeSerializeConnectionFile(final ConnectionData conData, String patientName) throws IOException {
	File outputfolderFile = new File(outputfolder);
	if(outputfolderFile.isDirectory())
	{
	    FileFilter filterDirectory = new FileFilter() {
		    @Override
		    public boolean accept(File arg0) {
			return arg0.getName().equalsIgnoreCase(conData.getStartROI()+"-"+conData.getEndROI());
		    }
	    };
	    File[] dataFolders=outputfolderFile.listFiles(filterDirectory);
	    File dataDir;
	    if(dataFolders.length==0)
	    {
		//Create Directory
		dataDir = new File(outputfolderFile.getAbsolutePath()+File.separator+conData.getStartROI()+"-"+conData.getEndROI());
		dataDir.mkdir();
	    }
	    else
	    {
		dataDir = dataFolders[0];
	    }
	    
	    FileOutputStream fos = new FileOutputStream(dataDir.getAbsolutePath()+File.separator+ patientName+".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conData);
            oos.close();   
	}
    }
    
    /**
     * Read patients from directory represented by connectionKey
     * @param toEvaluatedResult
     * @param connectionKey
     * @throws Exception 
     */
    public void readSerializeConnectionFiles(SerializableEvaluatedResult toEvaluatedResult,  String connectionKey) 
	    throws Exception
    {
	File outputfolderFile = new File(outputfolder+File.separator+connectionKey);
	if(outputfolderFile.isDirectory())
	{
	    File[] dataFolders=outputfolderFile.listFiles();
	    for(File file:dataFolders)
	    {
		FileInputStream ios = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(ios);
		ConnectionData conData = (ConnectionData) ois.readObject();
		StringTokenizer tokenizer = new StringTokenizer(file.getName(),".");
		String patientName = tokenizer.nextToken();
		if(patientName.startsWith("s"))
		{
		    if(!toEvaluatedResult.existSubject(patientName))
		    	toEvaluatedResult.addSubject(patientName);
		    	
		    toEvaluatedResult.addSubjectConnection(patientName, conData);
		    System.out.println("Subject added:"+patientName+" with connection "+conData.getConnectionKey());
		}
		else if(file.getName().startsWith("h"))
		{
		    if(!toEvaluatedResult.existPatient(patientName))
		    	toEvaluatedResult.addPatient(patientName);
		    	
		    toEvaluatedResult.addPatientConnection(patientName, conData);
		    System.out.println("Patient added:"+patientName+" with connection"+conData.getConnectionKey());
		}
	    }
	}
    }
    
}
