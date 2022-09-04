package utils;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SubjectData implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -4394401857782601707L;
    private Map<String,ConnectionData> connectionDataMap;
    private String subjectName;

    public SubjectData(String subjectName) {
	this.subjectName = subjectName;
	
	connectionDataMap = new HashMap<String,ConnectionData>();
    }
    
    public void addConnectionData(ConnectionData conData)
    {
	connectionDataMap.put(conData.getConnectionKey(), conData);
    }
    
    public ConnectionData getConnectionData(String key)
    {
	return connectionDataMap.get(key);
    }
    
    public ConnectionData removeConnectionData(String key)
    {
	return connectionDataMap.remove(key);
    }

    public String getSubjectName() {
        return subjectName;
    }
    
    public String[] getConnectionKeys()
    {
	 Set<String> keySet = connectionDataMap.keySet();
	 return keySet.toArray(new String[0]);
    }
   
}