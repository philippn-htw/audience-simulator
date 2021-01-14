import java.util.List;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import processing.core.PApplet;
import processing.sound.AudioIn;

public class GeneralApplicationTest {
	
	public static void main(String[] args){
		List<Device> cams = VideoCapture.getVideoDevices();
		for (Device cam : cams) {
			System.out.println(cam);
		}
		
		MainApplet applet = new MainApplet();
		AudioIn in = new AudioIn(applet, 0);
		
		Simulation sim = new Simulation(in,cams.get(1),SimLocation.STADIUM);
		try {
			sim.start();
		} catch (VideoCaptureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
