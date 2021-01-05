
/**
 * Enum to define the possible event locations for the simulation
 * @author Philipp Nitsche
 *
 */
public enum SimLocation {
	STADIUM("/path/to/sound/files"),
	THEATER("/path/to/sound/files"),
	CLUB("/path/to/sound/files"),
	BAR("/path/to/sound/files");
	
	private String loc;
	
	private SimLocation(String loc) {
		this.loc = loc;
	}
	
	public String getPath() {
		return loc;
	}
}
