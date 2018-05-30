package cn.nukkit.entity;

/**
 * Represents something that can be rotated.
 * 
 */
public interface Rotatable extends Positioned {
	void setRotation(double pitch, double yaw);

	double getPitch();
	
	double getYaw();
}
