package statistics;

import java.io.Serializable;

import utils.Shell;

public class FilterReturnHolder  implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = -6145859634088701583L;
    
	int [] coordinates;
	double[] filterResults;
	double distanceFromSeed;
	boolean isForwardTract;
	private Shell shell;
	
	public FilterReturnHolder(int[] coordinates, double[] filterResult) {
		this.coordinates=coordinates;
		this.filterResults=filterResult;
	}
	
	public int[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(int[] coordinates) {
		this.coordinates = coordinates;
	}

	public double[] getFilterResults() {
		return filterResults;
	}

	public void setFilterResults(double[] filterResult) {
		this.filterResults = filterResult;
	}
	
	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this==obj;
	}

	public double getDistanceFromSeed() {
	    return distanceFromSeed;
	}

	public void setDistanceFromSeed(double distanceFromSeed) {
	    this.distanceFromSeed = distanceFromSeed;
	}

	public boolean isForwardTract() {
	    return isForwardTract;
	}

	public void setForwardTract(boolean isForwardTract) {
	    this.isForwardTract = isForwardTract;
	}
	
}
