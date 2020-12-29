import java.util.ArrayList;

import org.openimaj.math.geometry.shape.Rectangle;

public class VideoMotionAnalyzer {
	Rectangle left = new Rectangle();
	Rectangle middle = new Rectangle();
	Rectangle right = new Rectangle();
	int cycleCount = 0;
	int leftCount = 0;
	int rightCount = 0;
	int middleCount = 0;
	
	private boolean clapping = false;
	private boolean cheering = false;
	
	public VideoMotionAnalyzer(Rectangle ar, float centerWidth) {
		setRegion(ar, centerWidth);
	}
	
	public void setRegion(Rectangle ar, float centerWidth) {
		left.setBounds(ar.getTopLeft().getX(), ar.getTopLeft().getY(), ((float)ar.getWidth() / 2f) - (float)(centerWidth/2f), (float)ar.getHeight());
		right.setBounds( ar.getTopLeft().getX() + (float)(ar.getWidth() / 2f) + (float)centerWidth / 2f , ar.getTopLeft().getY(), ((float)ar.getWidth() / 2f) - (float)(centerWidth/2f), (float)ar.getHeight());
		middle.setBounds((float)ar.getTopLeft().getX() +  (float)(ar.getWidth() / 2f) - (float)(centerWidth / 2f), ar.getTopLeft().getY(), centerWidth, (float)ar.getHeight());
	}
	
	public Rectangle getLeftRegion() {
		return left;
	}
	
	public Rectangle getRightRegion() {
		return right;
	}
	
	public Rectangle getMiddleRegion() {
		return middle;
	}
	
	public boolean isCheering() {
		return cheering;
	}
	
	public boolean isClapping() {
		return clapping;
	}
	
	public void analyzeMotion(ArrayList<TrackingBlob> bloblist) {
		if (cycleCount>100) {
			cycleCount = 0;
			leftCount = 0;
			rightCount = 0;
			middleCount = 0;
		} else {
			int blobsNotInRegion = 0;
			for (TrackingBlob blob : bloblist) {
				if(left.isInside(blob.getCenter())) {
					leftCount++;
				} else if (right.isInside(blob.getCenter())) {
					rightCount++;
				} else if (middle.isInside(blob.getCenter())) {
					middleCount++;
				} else {
					blobsNotInRegion++;
				}
			}
			if(blobsNotInRegion == bloblist.size() && bloblist.size() > 0) {
				cheering = false;
				clapping = false;
				cycleCount = 0;
				leftCount = 0;
				rightCount = 0;
				middleCount = 0;
			}
			
			if(leftCount<=1 && rightCount<=1 && middleCount <=1) {
				cheering = false;
				clapping = false;
			} else {
				
				if(leftCount>1 && middleCount <=1 || rightCount>1 && middleCount <=1) {
					cheering = true;
				}
				if(leftCount>1 && rightCount>1 && middleCount>1) {
					clapping = true;
				}
			}
		}
		cycleCount++;
	}
}
