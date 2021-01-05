
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import java.util.List;


/**
 * Main Class for visual testing of the video analyzer
 * 
 * @author Philipp Nitsche
 *
 */
public class VideoAnalyzerMainTest {
	
	
	public static void main(String[] args) {
		List<Device> cams = VideoCapture.getVideoDevices();
		for (Device cam : cams) {
			System.out.println(cam);
		}
		VideoAnalyzer vanalyzer = new VideoAnalyzer(cams.get(1), new SamplePlayer());
		try {
			vanalyzer.startCapture();
			Thread vaThread = new Thread(vanalyzer);
			vaThread.start();
		} catch (VideoCaptureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
