import java.util.ArrayList;

import org.openimaj.math.geometry.shape.Rectangle;

public class VideoMotionAnalyzer {
	Rectangle left = new Rectangle();
	Rectangle middle = new Rectangle();
	Rectangle right = new Rectangle();
	
	/**
	 * Lower threshold helps to avoid fail cheering triggering when activating clapping. Arms must be raised over this threshold to trigger cheering.
	*/
	Rectangle lowerThreshold = new Rectangle();
	private static final float LOWERTHRESHOLDMULT = 2.5f;
	
	float centerWidth;
	int cycleCount = 0;
	int leftCount = 0;
	int leftLowerCount = 0;
	int rightCount = 0;
	int rightLowerCount = 0;
	int middleCount = 0;
	

	
	private boolean clapping = false;
	private boolean cheering = false;
	
	/**
	 * Constructor
	 * @param ar overall Region
	 * @param centerWidth width of middle Region (head size)
	 * @throws IllegalArgumentException if overall region rectangle is null or center width <=0
	 */
	public VideoMotionAnalyzer(Rectangle ar, float centerWidth) {
		if(ar == null) {
			throw new IllegalArgumentException("No Analysing Area.");
		}
		if(centerWidth <= 0) {
			throw new IllegalArgumentException("Center Region must have a width.");
		}
		setRegion(ar, centerWidth);
	}
	
	
	/**
	 * Set the regions, that has to be analyzed (left, middle, right)
	 * @param ar Complete analyzing region above head
	 * @param centerWidth width of middle Region (head size)
	 */
	public void setRegion(Rectangle ar, float centerWidth) {
		left.setBounds(ar.getTopLeft().getX(), ar.getTopLeft().getY(), ((float)ar.getWidth() / 2f) - (float)(centerWidth/2f), (float)ar.getHeight());
		right.setBounds( ar.getTopLeft().getX() + (float)(ar.getWidth() / 2f) + (float)centerWidth / 2f , ar.getTopLeft().getY(), ((float)ar.getWidth() / 2f) - (float)(centerWidth/2f), (float)ar.getHeight());
		middle.setBounds((float)ar.getTopLeft().getX() +  (float)(ar.getWidth() / 2f) - (float)(centerWidth / 2f), ar.getTopLeft().getY(), centerWidth, (float)ar.getHeight());
		lowerThreshold.setBounds(left.x, left.y + left.height - (LOWERTHRESHOLDMULT*centerWidth), left.width + middle.width + right.width, LOWERTHRESHOLDMULT*centerWidth);
	}
	
	
	/**
	 * Get left region
	 * @return Rectangle left region
	 */
	public Rectangle getLeftRegion() {
		return left;
	}
	
	/**
	 * Get right region
	 * @return Rectangle right region
	 */
	public Rectangle getRightRegion() {
		return right;
	}
	
	/**
	 * Get right region
	 * @return Rectangle right region
	 */
	public Rectangle getMiddleRegion() {
		return middle;
	}
	
	public Rectangle getLowerThreshold() {
		return lowerThreshold;
	}
	
	/**
	 * Check if cheering is triggered
	 * @return true, if cheering is triggered.
	 */
	public boolean isCheering() {
		return cheering;
	}
	
	
	/**
	 * Check if clapping is triggered
	 * @return true, if clapping is triggered.
	 */
	public boolean isClapping() {
		return clapping;
	}
	
	/**
	 * Reset all counts
	 */
	private void setCountsToZero() {
		cycleCount = 0;
		leftCount = 0;
		leftLowerCount = 0;
		rightCount = 0;
		rightLowerCount = 0;
		middleCount = 0;
	}
	
	
	/**
	 * Analyze and approximate the motion per cycle
	 * @param bloblist List of tracked blobs in frame
	 */
	public void analyzeMotion(ArrayList<TrackingBlob> bloblist) {
		if (cycleCount>100) {
			setCountsToZero();
		} else {
			int blobsNotInRegion = 0;
			for (TrackingBlob blob : bloblist) {
				if(left.isInside(blob.getCenter())) {
					leftCount++;
					if(lowerThreshold.isInside(blob.getCenter())) {
						leftLowerCount++;
					}
				} else if (right.isInside(blob.getCenter())) {
					rightCount++;
					if(lowerThreshold.isInside(blob.getCenter())) {
						rightLowerCount++;
					}
				} else if (middle.isInside(blob.getCenter())) {
					middleCount++;
				} else {
					blobsNotInRegion++;
				}
			}
			if(blobsNotInRegion == bloblist.size() && bloblist.size() > 0) {
				cheering = false;
				clapping = false;
				setCountsToZero();
			}
			
			if(leftCount<=1 && rightCount<=1 && middleCount <=1) {
				cheering = false;
				clapping = false;
			} else {
				
				if(leftCount>1 && middleCount <=1 || rightCount>1 && middleCount <=1) {
					if(leftLowerCount != leftCount) {
						cheering = true;
					}
					if(rightLowerCount != rightCount) {
						cheering = true;
					}
				}
				if(leftCount>1 && rightCount>1 && middleCount>1) {
					clapping = true;
				}
			}
		}
		cycleCount++;
	}
}
