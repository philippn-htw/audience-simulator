package simulation;
import java.util.List;

/**
 * General Interface of a media device
 * @author Nitsche
 *
 */
public interface MediaInterface {
	
	/**
	 * get the List of all currently found audio/video devices connected to the System.
	 * @return String[] array of the names of all currently found deviecs.
	 */
	public abstract List<String> getDeviceList();
	
	/**
	 * get the audio stream or video capture device at the specified index
	 * @param deviceIndex index of the device to get the audiostream/capture device from
	 * @return AudioIn or Device at the specified index
	 */
	public abstract Object getStream(int deviceIndex);
}
