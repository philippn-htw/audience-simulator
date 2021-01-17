
/**
 * Enum to define the possible event locations for the simulation
 * @author Philipp Nitsche
 *
 */
public enum SimLocation {
	STADIUM("src/main/resources/sounds/stadium/"),
	THEATER("src/main/resources/sounds/theater/"),
	//CLUB("/path/to/sound/files"),
	BAR("src/main/resources/sounds/bar/");
	
	private String loc;
	
	private SimLocation(String loc) {
		this.loc = loc;
	}
	
	public String getPath() {
		return loc;
	}
}
