package simulation;
import java.util.ArrayList;
import java.util.List;

import application.Settings;
import processing.core.PApplet;
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
		String[] audioDevices = Sound.list();
		for(int i=0;i< audioDevices.length;i++) {
			devices.add( audioDevices[i]);
		}
		return devices;
	}

	@Override
	public Object getStream(int deviceIndex) {
		System.setProperty("java.version", "13.0.0");
		MainApplet applet = new MainApplet();
		AudioIn in = new AudioIn(applet,deviceIndex);
		return in;
	}

}
