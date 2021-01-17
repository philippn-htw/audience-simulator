import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class is needed to use Minim outside of processing
 *
 */
public class MinimFileSystemHandler {
	 public String sketchPath( String fileName ) {
		 return System.getProperty("user.dir") + fileName;
	 }
	 
	 public InputStream createInput( String fileName ) {
		 try {
			return new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}
	 }
}
