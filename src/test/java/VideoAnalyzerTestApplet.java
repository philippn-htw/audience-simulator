import java.util.Scanner;
import java.util.StringTokenizer;

import processing.core.PApplet;
import processing.video.*;

public class VideoAnalyzerTestApplet extends PApplet{
	Capture video;
	
	/**
	 * Set the window size of the processing application
	 */
	public void settings() {  size(640, 360); }
	
	
	/**
	 * Set attributes before start
	 */
	public void setup() {
//		String[] cameras = Capture.list();
//		int n=0;
//		for(String cam : cameras) {
//			System.out.println(n + " " + cam);
//			n++;
//		}
		
		video = new Capture(this, 320, 240);
		video.start();
		
	};
	
	
	/**
	 * Draw and update window
	 */
	public void draw() {
//		if (video.available()) {    
//		    video.read(); 
//		    image(video,0,0);
//		  }
	};
}
