package simulation;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.*;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeFilterFunction;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.resize.filters.BoxFilter;
//import org.openimaj.image.processing.resize.filters.Lanczos3Filter;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.Rectangle;


/**
 * Video analyzer class to analyze camera input and trigger events (clapping, cheering)
 * @author Philipp Nitsche
 *
 */
public class VideoAnalyzer implements Runnable {
	private Device captureDevice;
	private VideoCapture capture;
	private SamplePlayer player;
	private final int TARGETWIDTH = 640;
	private final int TARGETHEIGHT = 480;
//	private int posX;
//	private int posY;
//	private int count;
	
	/**
	 * List of all blobs (connected regions of the same color) in frame.
	 */
	private ArrayList<TrackingBlob> bloblist = new ArrayList<>();
	
	/**
	 * analysingRegion is the Region to analyse motion in (Later divided in 3 seperate parts by VideoMotionAnalyzer)
	 */
	private Rectangle analysingRegion = new Rectangle(0,0,100,100);
	
	/**
	 * Motion analyzer to recognize moves
	 */
	private VideoMotionAnalyzer motionAnalyzer = new VideoMotionAnalyzer(analysingRegion, 100);
	
	/**
	 * facePredictionRegion is used to predict the face position by the last known position. This can increase performance.
	 */
	private Rectangle facePredictionRegion = new Rectangle();

	/**
	 * Constructor
	 * 
	 * @param in Capture object (video stream)
	 * @throws IllegalArgumentException if the capture device or the sample player is not given.
	 */
	public VideoAnalyzer(Device captureDevice, SamplePlayer player) {
//		if(captureDevice == null || player == null) {
//			throw new IllegalArgumentException("Capture device and player have to be defined.");
//		}
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
		capture = new VideoCapture(TARGETWIDTH, TARGETHEIGHT, captureDevice);
		analysingRegion.setBounds(capture.getWidth()/4, 0, capture.getWidth() / 2, capture.getHeight()/2);
	}
	
	
	/** stops the capturing
	 * 
	 */
	public void stopCapture() {
		capture.stopCapture();
		capture.close();
	}
	

	/**
	 * get the Distance between two RGB color vectors.
	 * 
	 * @param r Red channel
	 * @param g Green channel
	 * @param b Blue channel
	 * @return distance between the custom color vector and green
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

	
//	/**
//	 * Updates the captured position (DEPRECATED)
//	 */
//	private void updatePosition(MBFImage frame) {
//		count = 0;
//		posX = 0;
//		posY = 0;
//		
//		for (int y = 0; y < frame.getHeight(); y++) {
//			for (int x = 0; x < frame.getWidth(); x++) {
//				float currentRed = frame.getBand(0).pixels[y][x];
//				float currentGreen = frame.getBand(1).pixels[y][x];
//				float currentBlue = frame.getBand(2).pixels[y][x];
//
//				double dist = colorDist(currentRed, currentGreen, currentBlue);
//
//				if (dist < 0.5) {
//					posX += x;
//					posY += y;
//					count++;
//				}
//			}
//		}
//	}
	
	
	/**
	 * Get the average of the four surrounding pixels to reduce jittering
	 * @param frame
	 * @param x x-position of the pixel
	 * @param y y-position of the pixel
	 * @param band color channel to average.
	 * @return average pixel value
	 * @throws IllegalArgumentException if pixel coordinate is not inside the frame or if the band does not exists.
	 */
	private float pixelAverage(MBFImage frame, int x, int y, int band) {
		if(x < 0 || y < 0 || x > frame.getWidth() || y > frame.getHeight()) {
			throw new IllegalArgumentException("invalid pixel coordinate");
		}
		
		if(band<0 || band > 2) {
			throw new IllegalArgumentException("invalid band");
		}
		
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
	public VideoDisplay<MBFImage> displayCapture() {
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(capture);
		

		display.addVideoListener(new VideoDisplayListener<MBFImage>() {
			public void beforeUpdate(MBFImage frame) {
				
				//Resize frame if not happened already
				MBFImage resizedFrame = resizeFrame(frame);
				
				if(frame.getWidth() != TARGETWIDTH || frame.getHeight() != TARGETHEIGHT) {
					for (int y = 0; y < frame.getHeight(); y++) {
						for (int x = 0; x < frame.getWidth(); x++) {
							if(x<TARGETWIDTH && y<TARGETHEIGHT) {
								frame.getBand(0).pixels[y][x] = resizedFrame.getBand(0).pixels[y][x];
								frame.getBand(1).pixels[y][x] = resizedFrame.getBand(1).pixels[y][x];
								frame.getBand(2).pixels[y][x] = resizedFrame.getBand(2).pixels[y][x];
							} else {
								frame.getBand(0).pixels[y][x] = 0;
								frame.getBand(1).pixels[y][x] = 0;
								frame.getBand(2).pixels[y][x] = 0;
							}
						}
					}
				}
				
				//Draw Guides into the video
				synchronized (bloblist) {
					if (bloblist.size() > 0) {
						for (TrackingBlob b : bloblist) {
							
							frame.drawShapeFilled(new Ellipse(b.getX() + b.getW() / 2, b.getY() + b.getH() / 2, 5f, 5f, 0f), RGBColour.WHITE);
						}
					}
				}
				
				frame.drawShape(new Rectangle(motionAnalyzer.getLeftRegion()), RGBColour.BLUE);
				frame.drawShape(new Rectangle(motionAnalyzer.getRightRegion()), RGBColour.BLUE);
				frame.drawShape(new Rectangle(motionAnalyzer.getMiddleRegion()), RGBColour.GREEN);
				frame.drawShape(new Rectangle(motionAnalyzer.getLowerThreshold()), RGBColour.CYAN);
				
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
		
		return display;
	}
	
	
	/**
	 * Resize a frame if it has not the standard frame size 640x480 (higher performance)
	 * @param frame to resize
	 * @return resized frame
	 */
	private MBFImage resizeFrame(MBFImage frame) {
		if(frame.getWidth() == TARGETWIDTH && frame.getHeight() == TARGETHEIGHT) {
			return frame;
		}
		ResizeFilterFunction filter= new BoxFilter();
		//Create a new MBFImage cause ResizeProcessor side effects the original
		MBFImage toResize = new MBFImage(frame.getBand(0).clone(),frame.getBand(1).clone(),frame.getBand(2).clone());
		
		FImage band0 = ResizeProcessor.resample(toResize.getBand(0), TARGETWIDTH, TARGETHEIGHT, false, filter);
		FImage band1 = ResizeProcessor.resample(toResize.getBand(1), TARGETWIDTH, TARGETHEIGHT, false, filter);
		FImage band2 = ResizeProcessor.resample(toResize.getBand(2), TARGETWIDTH, TARGETHEIGHT, false, filter);
		return new MBFImage(band0,band1,band2);
	}


	/**
	 * Main Loop of the analyzer thread
	 */
	@Override
	public void run() {
		// Display for Test purposes
		VideoDisplay<MBFImage> display = displayCapture();
		
		int frameCount = 0;
		//MBFImage frame;
		// MAIN LOOP
		while (true) {
			
			if(Thread.interrupted()) {
				display.close();
				break;
			}
			
			if(frameCount>100) {
				frameCount=0;
			}
			try {
				if (capture.hasNextFrame()) {
					MBFImage frame = capture.getNextFrame();
					
					//Resize the frame in case capture object was not able to set width and height to 640x480
					MBFImage frameResized = resizeFrame(frame);
					
					//Update Blobs each frame
					updateBlobs(frameResized);
					
					// update analyzing Region by faces every 5 frames
					if(frameCount%3==0) {
						updateAnalysingArea(getFaces(frameResized));
					}
					
					//safe isClapping value before analyze motion to check if clapping was currently activated or if it was already activated.
					boolean previousClapping = motionAnalyzer.isClapping();
					
					//Analyze Motion and trigger events
					motionAnalyzer.analyzeMotion(bloblist);
					
					if(player!=null) {
						if(motionAnalyzer.isClapping() && previousClapping==false) {
							player.setShouldBeClapping(true);
						} else if (motionAnalyzer.isCheering()) {
							player.playCheering();
						}
					}
					
					//higher cycle count
					frameCount++;
				}
				
				if(Thread.interrupted()) {
					display.close();
				}

				Thread.sleep(1);
			} catch (InterruptedException e) {
				display.close();
				break;
			}
		}

	}

}
