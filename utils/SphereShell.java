package utils;

public class SphereShell {

	private BoundingSphereHolder seedSphereHolder, targetSphereHolder;
	private int numberofShells;
	private double distofCenters;

	public SphereShell(int numberofShells) {
		this.numberofShells = numberofShells;
	}

	public void setSpheres(BoundingSphereHolder seedSphereHolder,
			BoundingSphereHolder targetSphereHolder) {
		this.seedSphereHolder = seedSphereHolder;
		this.targetSphereHolder = targetSphereHolder;
		distofCenters = BoundingSphere.dist(seedSphereHolder.centerCoordinates,
				targetSphereHolder.centerCoordinates);
	}

	public Shell getShellofCoordinates(int[] coordinates) {
		double totalDist = BoundingSphere.dist(
				seedSphereHolder.centerCoordinates, coordinates);
		int shellNumber = (int) (totalDist / ((seedSphereHolder.radius+targetSphereHolder.radius)/ (2 * numberofShells)));
		shellNumber = (shellNumber>=numberofShells?numberofShells:shellNumber+1);
		return new Shell(shellNumber, seedSphereHolder, targetSphereHolder);
	}
}
