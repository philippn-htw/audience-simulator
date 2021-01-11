
public class BeatAnalyzerMainTest {
	public static void main(String[] args) {
		BeatAnalyzer ba = new BeatAnalyzer(0);
		Thread t= new Thread(ba);
		t.start();
	}
}
