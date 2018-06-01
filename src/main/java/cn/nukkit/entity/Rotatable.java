package cn.nukkit.entity;

/**
 * Represents something that can be rotated.
 * 
 */
public interface Rotatable extends Positioned {
	void setRotation(double yaw, double pitch);

	double getPitch();
	
	double getYaw();
}
