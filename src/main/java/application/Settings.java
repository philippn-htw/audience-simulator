package application;
import simulation.SimLocation;
import simulation.Simulation;

/**
 * Holds all settings specified in the GUI
 *
 */
public class Settings {
	private static int audioDeviceIndex = 0;
	private static int videoDeviceIndex = 0;
	private static SimLocation location = SimLocation.STADIUM;
	private static Simulation sim;
	private static boolean isRunning=false;
	
	/**
	 * Set the index of the choosen audio input device
	 * @param index
	 * @throws IllegalArgumentException if index is negative
	 */
	public static void setAudioDeviceIndex(int index) {
		if(index<0) {
			throw new IllegalArgumentException("invalid device index");
		}
		audioDeviceIndex = index;
	}
	
	/**
	 * Get the index of the choosen audio input device
	 * @returns index of choosen device
	 */
	public static int getAudioDeviceIndex() {
		return audioDeviceIndex;
	}
	
	/**
	 * Set the index of the choosen video input device
	 * @param index
	 * @throws IllegalArgumentException if index is negative
	 */
	public static void setVideoDeviceIndex(int index) {
		if(index<0) {
			throw new IllegalArgumentException("invalid device index");
		}
		videoDeviceIndex = index;
	}
	
	/**
	 * Get the index of the choosen audio input device
	 * @returns index of choosen device
	 */
	public static int getVideoDeviceIndex() {
		return videoDeviceIndex;
	}
	
	/**
	 * Set the choosen location
	 * @param index
	 * @throws IllegalArgumentException if index is negative or greater than the available location count
	 */
	public static void setLocation(int index) {
		if(index<0 || index > 3) {
			throw new IllegalArgumentException("invalid location index");
		}
		switch(index) {
		case 0:
			location = SimLocation.STADIUM;
			break;
		case 1:
			location = SimLocation.THEATER;
			break;
		case 2:
			location = SimLocation.CLUB;
			break;
		case 3:
			location = SimLocation.BAR;
		}
	}
	
	/**
	 * Get the choosen location
	 * @returns index of choosen device
	 */
	public static SimLocation getLocation() {
		return location;
	}
	
	/**
	 * Set the currently configured simulation
	 * @param Simulation
	 */
	public static void setSim(Simulation simulation) {
		sim = simulation;
	}
	
	/**
	 * Get the currently configured simulation
	 * @return Simulation
	 */
	public static Simulation getSim() {
		return sim;
	}
	
	/**
	 * Set if simulation is running
	 * @param value
	 */
	public static void setIsRunning(boolean value) {
		isRunning=value;
	}
	
	/**
	 * Get if a simulation is running
	 * @@return boolean true if a simulation is running
	 */
	public static boolean getIsRunning() {
		return isRunning;
	}
}
