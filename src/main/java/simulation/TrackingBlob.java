package simulation;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Blob-Class Blobs are used to identify connected regions of the same color
 * 
 * @author Philipp Nitsche
 *
 */
public class TrackingBlob {
	int x;
	int y;
	int w;
	int h;

	/**
	 * Blob Constructor
	 * 
	 * @param x X-Coordinate of Blob
	 * @param y Y-Coordinate of Blob
	 */
	public TrackingBlob(int x, int y) {
		if(x<0 || y<0) {
			throw new IllegalArgumentException("illegal blob coordinate");
		}
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}
	
	/**
	 * Get the Center of the blob
	 * @return
	 */
	public Point2d getCenter() {
		double centerX = x + (w/2.0);
		double centerY = y + (h/2.0);
		return new Point2dImpl(centerX,centerY);
	}

	/**
	 * Add a new Pixel to the Blob
	 * 
	 * @param x position of the pixel
	 * @param y position of the pixel
	 */
	protected void addToBlob(int x, int y) {
		if(x<0 || y<0) {
			throw new IllegalArgumentException("Illegal blob point");
		}

		if (x < this.x) {
			w += this.x - x;
			this.x = x;
		} else if (x > this.x && x - this.x > w) {
			w = x - this.x;
		}

		if (y < this.y) {
			h += this.y - y;
			this.y = y;
		} else if (y > this.y && y - this.y > h) {
			h = y - this.y;
		}

	}

	/**
	 * Checks if the pixel is close to an existing Blob
	 * 
	 * @param x    X-Coordinate of the pixel
	 * @param y    Y-Coordinate of the pixel
	 * @param dist Distance threshold which is close to a pixel
	 * @return true, if the pixel is close to the blob
	 */
	protected boolean isNear(int x, int y, int dist) {
		if(x<0 || y<0 || dist < 0) {
			throw new IllegalArgumentException("Illegal value");
		}
		
		if (x > this.x - dist && x < this.x + w + dist && y > this.y - dist && y < this.y + h + dist) {
			return true;
		}
		return false;
	}
}
