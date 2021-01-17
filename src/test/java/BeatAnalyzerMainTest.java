import processing.sound.AudioIn;
import processing.sound.Sound;
import simulation.BeatAnalyzer;
import simulation.MainApplet;

public class BeatAnalyzerMainTest {
	public static void main(String[] args) {
		System.out.println(Sound.list());
		
		MainApplet applet = new MainApplet();
		AudioIn in = new AudioIn(applet,0);
		BeatAnalyzer ba = new BeatAnalyzer(in, null);
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
