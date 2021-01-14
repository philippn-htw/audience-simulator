
public class BeatAnalyzerMainTest {
	public static void main(String[] args) {
		BeatAnalyzer ba = new BeatAnalyzer(0, null);
		Thread t= new Thread(ba);
		t.start();
		
//		try {
//			Thread.sleep(20000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		t.interrupt();
	}
}
