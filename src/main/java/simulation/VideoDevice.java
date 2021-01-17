package simulation;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

/**
 * video specific media device
 *
 */
public class VideoDevice implements MediaInterface{

	@Override
	public List<String> getDeviceList() {
		List<String> devices = new ArrayList<>();
		List<Device> cams = VideoCapture.getVideoDevices();
		for (Device cam : cams) {
			devices.add(cam.getNameStr());
		}
		return devices;
	}

	@Override
	public Object getStream(int deviceIndex) {
		List<Device> cams = VideoCapture.getVideoDevices();
		return cams.get(deviceIndex);
	}

}
