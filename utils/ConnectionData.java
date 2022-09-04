package utils;

import java.io.Serializable;
import java.util.List;

import statistics.FilterReturnHolder;
import statistics.ReturnHolderList;

public class ConnectionData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 9214847026537536663L;
    private List<FilterReturnHolder> seedCollection;
    private List<FilterReturnHolder> targetCollection;
    private List<FilterReturnHolder> tractVoxelCollection;
    private ReturnHolderList[] returnHolderList;
    
    private int startROI;
    private int endROI;
    private int numberofShelves;

    public ConnectionData() {
    }

    public List<FilterReturnHolder> getSeedCollection() {
	return seedCollection;
    }

    public void setSeedCollection(List<FilterReturnHolder> seedCollection) {
	this.seedCollection = seedCollection;
    }

    public List<FilterReturnHolder> getTargetCollection() {
	return targetCollection;
    }

    public void setTargetCollection(List<FilterReturnHolder> targetCollection) {
	this.targetCollection = targetCollection;
    }

    public List<FilterReturnHolder> getTractVoxelCollection() {
	return tractVoxelCollection;
    }

    public void setTractVoxelCollection(
	    List<FilterReturnHolder> tractVoxelCollection) {
	this.tractVoxelCollection = tractVoxelCollection;
    }

    public ReturnHolderList[] getReturnHolderList() {
	return returnHolderList;
    }

    public void setReturnHolderList(ReturnHolderList[] returnHolderList) {
	this.returnHolderList = returnHolderList;
    }

    public int getStartROI() {
	return startROI;
    }

    public void setStartROI(int startROI) {
	this.startROI = startROI;
    }

    public int getEndROI() {
	return endROI;
    }

    public void setEndROI(int endROI) {
	this.endROI = endROI;
    }

    public int getNumberofShelves() {
	return numberofShelves;
    }

    public void setNumberofShelves(int numberofShelves) {
	this.numberofShelves = numberofShelves;
    }
    
    public String getConnectionKey()
    {
	return Integer.toString(startROI)+"-"+Integer.toString(endROI);
    }
}