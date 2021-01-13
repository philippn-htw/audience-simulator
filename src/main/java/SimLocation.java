
/**
 * Enum to define the possible event locations for the simulation
 * @author Philipp Nitsche
 *
 */
public enum SimLocation {
	STADIUM("\\stadium\\"),
	THEATER("\\theater\\"),
	CLUB("/path/to/sound/files"),
	BAR("\\bar\\");
	
	private String loc;
	
	private SimLocation(String loc) {
		this.loc = loc;
	}
	
	public String getPath() {
		return loc;
	}
}
