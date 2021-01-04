import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.*;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Rectangle;

public class VideoAnalyzer implements Runnable {
	private Device captureDevice;
	private VideoCapture capture;
	private SamplePlayer player;
	private int posX;
	private int posY;
	private int count;
	private ArrayList<TrackingBlob> bloblist = new ArrayList<>();
	
	/**
	 * analysingRegion is the Region to analyse motion in 
	 */
	private Rectangle analysingRegion = new Rectangle(0,0,100,100);
	
	private VideoMotionAnalyzer motionAnalyzer = new VideoMotionAnalyzer(analysingRegion, 100);
	
	/**
	 * facePredictionRegion is used to predict the face position by the last known position. This can increase performance.
	 */
	private Rectangle facePredictionRegion = new Rectangle();

	/**
	 * Constructor
	 * 
	 * @param in Capture object (video stream)
	 */
	public VideoAnalyzer(Device captureDevice, SamplePlayer player) {
		this.captureDevice = captureDevice;
		this.player = player;

	}


	/**
	 * Getter for the current video capture Device
	 * 
	 * @return current capture device
	 */
	public Device getVideoStream() {
		return this.captureDevice;
	}
	

	/**
	 * Starts the video capture
	 * 
	 * @throws VideoCaptureException
	 */
	public void startCapture() throws VideoCaptureException {
		capture = new VideoCapture(640, 480, captureDevice);
		analysingRegion.setBounds(capture.getWidth()/4, 0, capture.getWidth() / 2, capture.getHeight()/2);
	}
	

	/**
	 * get the Distance between two RGB color vectors.
	 * 
	 * @param r Red channel
	 * @param g Green channel
	 * @param b Blue channel
	 * @return distance between the custom color vector and red
	 */
	private double colorDist(float r, float g, float b) {
		float refRed = 0;
		float refGreen = 1;
		float refBlue = 0;

		// subtract vectors
		float rr = r - refRed;
		float rg = g - refGreen;
		float rb = b - refBlue;

		// resulting vector length
		double dist = Math.sqrt(Math.pow(rr, 2) + Math.pow(rg, 2) + Math.pow(rb, 2));
		return dist;
	}

	/**
	 * Updates the captured position
	 */
	private void updatePosition(MBFImage frame) {
		count = 0;
		posX = 0;
		posY = 0;
		
		for (int y = 0; y < frame.getHeight(); y++) {
			for (int x = 0; x < frame.getWidth(); x++) {
				float currentRed = frame.getBand(0).pixels[y][x];
				float currentGreen = frame.getBand(1).pixels[y][x];
				float currentBlue = frame.getBand(2).pixels[y][x];

				double dist = colorDist(currentRed, currentGreen, currentBlue);

				if (dist < 0.5) {
					posX += x;
					posY += y;
					count++;
				}
			}
		}
	}
	
	
	/**
	 * Get the average of the four surrounding pixels to reduce jittering
	 * @param frame
	 * @param x x-position of the pixel
	 * @param y y-position of the pixel
	 * @param band color channel to average.
	 * @return average pixel value
	 */
	private float pixelAverage(MBFImage frame, int x, int y, int band) {
		float average = 0;

		
		if(x-1 >= 0) {
			average += 0.2 * frame.getBand(band).pixels[y][x - 1];
		}
		if(x+1 < frame.getWidth()) {
			average += 0.2 * frame.getBand(band).pixels[y][x + 1];
		}
		if(y-1 >= 0) {
			average += 0.2 * frame.getBand(band).pixels[y - 1][x];
		}
		if(y+1 < frame.getHeight()) {
			average += 0.2 * frame.getBand(band).pixels[y + 1][x];
		}
		average += 0.2 * frame.getBand(band).pixels[y][x];

		return average;
	}

	/**
	 * Update Blobs (Count, size and position)
	 * 
	 * @param frame
	 */
	private void updateBlobs(MBFImage frame) {
		synchronized (bloblist) {
			bloblist.clear();
		}
		for (int y = 0; y < frame.getHeight(); y++) {
			for (int x = 0; x < frame.getWidth(); x++) {

				if(y%4==0 && x%4 == 0) {
					float currentRed = pixelAverage(frame, x, y, 0);
					float currentGreen = pixelAverage(frame, x, y, 1);
					float currentBlue = pixelAverage(frame, x, y, 2);

					double dist = colorDist(currentRed, currentGreen, currentBlue);
	
					if (dist < 0.6) {
						synchronized (bloblist) {
							if (bloblist.size() == 0) {
								TrackingBlob blob = new TrackingBlob(x, y);
								bloblist.add(blob);
							} else {
								boolean existing = false;
								for (TrackingBlob blob : bloblist) {
									if (blob.isNear(x, y, 100)) {
										blob.addToBlob(x, y);
										existing = true;
										break;
									}
								}
								if (existing == false) {
									TrackingBlob blob = new TrackingBlob(x, y);
									bloblist.add(blob);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * Detect Faces in frame
	 * @param frame
	 * @return List of faces in frame
	 */
	private List<DetectedFace> getFaces(MBFImage frame) {
		//analyse face prediction region first
		MBFImage tmp = frame;
	 	MBFImage predictionFrame = frame.extractROI((int)facePredictionRegion.x,(int)facePredictionRegion.y, (int)facePredictionRegion.getWidth(),(int)facePredictionRegion.getHeight());	 	
		FaceDetector<DetectedFace,FImage> fd = new HaarCascadeDetector(40);
		List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(predictionFrame));

		//if no face found in face prediction region analyse whole frame
		if(faces.size()<1) {
			fd = new HaarCascadeDetector(60);
			faces = fd.detectFaces(Transforms.calculateIntensity(tmp));
			facePredictionRegion.setBounds(0, 0, tmp.getWidth(), tmp.getHeight());
		}
		return faces;
	}
	
	
	/**
	 * Updates the region to analyse motion relative to faces
	 * @param faces found in frame
	 */
	private void updateAnalysingArea(List<DetectedFace> faces) {
		//extract the main face, if multiple faces are found
		DetectedFace mainFace = null;
		double area = 0;
		
		for(DetectedFace face : faces) {
			Rectangle bounds = face.getBounds();
			double currentArea = bounds.calculateArea();
			if(currentArea>area && face.getConfidence()>0.3) {
				area = currentArea;
				mainFace = face;
			}
		}
		
		// update region to fit main face or keep last known position
		if(mainFace != null) {
			Rectangle mainBounds = mainFace.getBounds();
			
			//Bounds must be set relative to facePredictionRegion
			mainBounds.setBounds(mainBounds.x + facePredictionRegion.x, mainBounds.y + facePredictionRegion.y, mainBounds.width, mainBounds.height);

			Point2d currentCenter = mainBounds.calculateCentroid();
			double analysingWidth = mainBounds.getWidth()*8;
			analysingRegion.setBounds((float)(currentCenter.getX()) - (float)(analysingWidth) / 2.0f, 0.0f, (float)analysingWidth, (float)currentCenter.getY());
			motionAnalyzer.setRegion(analysingRegion, (float)mainBounds.getWidth());
			facePredictionRegion.setBounds(mainBounds.x - mainBounds.width, mainBounds.y-mainBounds.height, mainBounds.width*3, mainBounds.height*3);
		}
		
	}
	

	/**
	 * Display the capture
	 */
	public void displayCapture() {
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(capture);

		display.addVideoListener(new VideoDisplayListener<MBFImage>() {
			public void beforeUpdate(MBFImage frame) {
				
				
				synchronized (bloblist) {
					if (bloblist.size() > 0) {
						for (TrackingBlob b : bloblist) {

							//frame.drawShapeFilled(new Rectangle(b.getX(), b.getY(), b.getW(), b.getH()), RGBColour.RED);
							
							frame.drawShapeFilled(new Ellipse(b.getX() + b.getW() / 2, b.getY() + b.getH() / 2, 5f, 5f, 0f), RGBColour.WHITE);
						}
					}
				}
				//frame.drawShape(analysingRegion, RGBColour.GREEN);
				frame.drawShape(new Rectangle(motionAnalyzer.getLeftRegion()), RGBColour.BLUE);
				frame.drawShape(new Rectangle(motionAnalyzer.getRightRegion()), RGBColour.BLUE);
				frame.drawShape(new Rectangle(motionAnalyzer.getMiddleRegion()), RGBColour.GREEN);
				
				frame.drawShape(new Rectangle(facePredictionRegion), RGBColour.RED);
				
				if(motionAnalyzer.isClapping()) {
					frame.drawShapeFilled(new Ellipse(50f,50f,50f,50f,0), RGBColour.ORANGE);
				} else if (motionAnalyzer.isCheering()) {
					frame.drawShapeFilled(new Ellipse(50f,50f,50f,50f,0), RGBColour.GREEN);
				}
				
				
				
			}

			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}
	
	
	/**
	 * Resize a frame if it has not the standard frame size 640x480 (higher performance)
	 * @param frame to resize
	 * @return resized frame
	 */
	private MBFImage resizeFrame(MBFImage frame) {
		if(frame.getWidth() == 640 && frame.getHeight() == 480) {
			return frame;
		}
		FImage band0 = ResizeProcessor.resample(frame.getBand(0), 640, 480);
		FImage band1 = ResizeProcessor.resample(frame.getBand(1), 640, 480);
		FImage band2 = ResizeProcessor.resample(frame.getBand(2), 640, 480);
		return new MBFImage(band0,band1,band2);
	}


	/**
	 * Main Loop of the analyzer thread
	 */
	@Override
	public void run() {
		// Display for Testpurposes
		displayCapture();
		
		int frameCount = 0;
		// MAIN LOOP
		while (true) {
			if(frameCount>100) {
				frameCount=0;
			}
			try {
				if (capture.hasNextFrame()) {
					MBFImage frame = capture.getNextFrame();
					
					//Resize the frame in case capture object was not able to set width and height to 640x480
					//frame = resizeFrame(frame);
					
					//Update Blobs each frame
					updateBlobs(frame);
					
					// update analysing Region by faces every 5 frames
					if(frameCount%5==0) {
						updateAnalysingArea(getFaces(frame));
					}
					//motionRegion = getRegionOfMotion(frame);
					
					//Analyze Motion and trigger events
					motionAnalyzer.analyzeMotion(bloblist);
					
					if(motionAnalyzer.isClapping()) {
						player.playClapping();
					} else if (motionAnalyzer.isCheering()) {
						player.playCheering();
					}
					
					//higher cycle count
					count++;
				}

				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
