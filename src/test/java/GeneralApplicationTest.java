import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCaptureException;

import processing.sound.AudioIn;

public class GeneralApplicationTest {
	
	public static void main(String[] args){
		MediaInterface audio = new AudioDevice();
		MediaInterface video = new VideoDevice();
		
		Simulation sim = new Simulation((AudioIn)audio.getStream(0),(Device)video.getStream(1),SimLocation.THEATER);
		try {
			sim.start();
		} catch (VideoCaptureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
