package utils;

import java.io.Serializable;

public class Shell  implements Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = -5055734884384747510L;
	int shellNumber;
	BoundingSphereHolder seedSphereeHolder, targetSphereHolder;
	
	public Shell(int shellNumber, BoundingSphereHolder seedSphereeHolder, BoundingSphereHolder targetSphereHolder)
	{
		this.shellNumber=shellNumber;
		this.seedSphereeHolder=seedSphereeHolder;
		this.targetSphereHolder=targetSphereHolder;
	}

	public int getShellNumber() {
		return shellNumber;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		/*byte [] bytes= new byte[4];
		bytes[0] = (byte)shellNumber;
		bytes[1] = (byte)seedSphereeHolder.centerCoordinates[0];
		bytes[2] = (byte)seedSphereeHolder.centerCoordinates[1];
		bytes[3] = (byte)seedSphereeHolder.centerCoordinates[2]; */
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
}
