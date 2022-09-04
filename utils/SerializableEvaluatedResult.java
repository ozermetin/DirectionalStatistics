package utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SerializableEvaluatedResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8326012313479717035L;

    public enum Data{
	PATIENTDATA, NORMALDATA
    }
    
    Map<String, SubjectData> patientData ;
    Map<String, SubjectData> normalData ;
    
    public SerializableEvaluatedResult() {
	patientData = new HashMap<String, SubjectData>();
	normalData = new HashMap<String, SubjectData>();
    }
    
    public boolean existPatient(String patientName)
    {
	return exists(Data.PATIENTDATA,patientName);
    }
    
    public boolean existSubject(String subjectName)
    {
	return exists(Data.NORMALDATA,subjectName);
    }
    
    private boolean exists(Data data, String subjectName)
    {
	return getDataMap(data).containsKey(subjectName);
    }
    
    
    private Map<String, SubjectData> getDataMap(Data data)
    {

	switch(data){
	case PATIENTDATA:
	    return patientData;
	case NORMALDATA:
	    return normalData;
	default:
	    return null;
	}
    }
    
    public void addSubject(String subjectName) throws Exception
    {
	addSubject(Data.NORMALDATA,subjectName);
    }
    
    public void addPatient(String patientName) throws Exception
    {
	addSubject(Data.PATIENTDATA,patientName);
    }
    
    private void addSubject(Data data, String subjectName) throws Exception
    {
	if(getDataMap(data).containsKey(subjectName))
		throw new Exception("Patient is already defined!");
	getDataMap(data).put(subjectName, new SubjectData(subjectName));
    }
    
    public void addPatientConnection(String patientName, ConnectionData conData) throws Exception
    {
	addConnection(Data.PATIENTDATA,patientName,conData);
    }
    
    public void addSubjectConnection(String subjectName, ConnectionData conData) throws Exception
    {
	addConnection(Data.NORMALDATA,subjectName,conData);
    }
    
    private void addConnection(Data data, String subjectName, ConnectionData conData) throws Exception
    {
	if(getDataMap(data).containsKey(subjectName))
	{
	    String[] connectionKeys=getDataMap(data).get(subjectName).getConnectionKeys();
	    for(String key : connectionKeys)
	    {
		if(key.equals(conData.getConnectionKey()))
		    throw new Exception("Connection already added!");
	    }
	    getDataMap(data).get(subjectName).addConnectionData(conData);
	}
	else throw new Exception("Patient not found!");
    }
    
    public ConnectionData getPatientConnection(String patientName, String connectionKey)
    {
	return getDataMap(Data.PATIENTDATA).get(patientName).getConnectionData(connectionKey);
    }
    
    public ConnectionData getSubjectConnection(String subjectName, String connectionKey)
    {
	return getDataMap(Data.NORMALDATA).get(subjectName).getConnectionData(connectionKey);
    }
    
    public String[] getPatientConnectionKeys(String patientName)
    {
	return getDataMap(Data.PATIENTDATA).get(patientName).getConnectionKeys();
    }
    
    public String[] getSubjectConnectionKeys(String subjectName)
    {
	return getDataMap(Data.NORMALDATA).get(subjectName).getConnectionKeys();
    }
    
    public String[] getPatientKeys()
    {
	Set<String> keySet = getDataMap(Data.PATIENTDATA).keySet();
	return keySet.toArray(new String[0]);
    }
    
    public String[] getSubjectKeys()
    {
	Set<String> keySet = getDataMap(Data.NORMALDATA).keySet();
	return keySet.toArray(new String[0]);
    }
    
    public void removeSubject(String subjectName)
    {
	getDataMap(Data.NORMALDATA).remove(subjectName);
    }
    
    public void removePatient(String patientName)
    {
	getDataMap(Data.PATIENTDATA).remove(patientName);
    }
    
    public void removeSubjectConnection(String subjectName, String conName)
    {
	getDataMap(Data.NORMALDATA).get(subjectName).removeConnectionData(conName);
    }
    
    public void removePatientConnection(String patientName, String conName)
    {
	getDataMap(Data.PATIENTDATA).remove(patientName).removeConnectionData(conName);
    }

    
}
