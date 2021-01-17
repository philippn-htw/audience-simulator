import java.util.ArrayList;
import java.util.List;
import processing.sound.*;

/**
 * Audio specific media device
 *
 */
public class AudioDevice implements MediaInterface{

	@Override
	public List<String> getDeviceList() {
		// TODO Auto-generated method stub
		List<String> devices = new ArrayList<>();
		for(int i=0;i<Sound.list().length;i++) {
			devices.add(Sound.list()[i]);
		}
		return devices;
	}

	@Override
	public Object getStream(int deviceIndex) {
		// TODO Auto-generated method stub
		MainApplet applet = new MainApplet();
		AudioIn in = new AudioIn(applet,deviceIndex);
		return in;
	}

}
