package simulation;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCaptureException;
import processing.sound.AudioIn;

public class Simulation {

    SamplePlayer sPlayer;
    VideoAnalyzer vAnalyzer;
    AmplitudeAnalyzer aAnalyzer;
    BeatAnalyzer bAnalyzer;
    Thread vaThread;
    Thread aaThread;
    Thread baThread;

    /**
     * initialisiert alle Analyzer
     * @param audioIn
     * @param videoDevice
     * @param location
     */
    public Simulation(AudioIn audioIn, Device videoDevice, SimLocation location)    {
        sPlayer=new SamplePlayer(location);
        vAnalyzer=new VideoAnalyzer(videoDevice,sPlayer);
        aAnalyzer=new AmplitudeAnalyzer(audioIn, sPlayer);
        bAnalyzer=new BeatAnalyzer(audioIn, aAnalyzer, sPlayer);
    }

    /**
     * startet alle Threads mit run-methoden der Analyzer
     * @throws VideoCaptureException wird geworfen wenn?
     */
    public void start() throws VideoCaptureException {
        vAnalyzer.startCapture();
        vaThread = new Thread(vAnalyzer);
        vaThread.start();
        aaThread = new Thread(aAnalyzer);
        aaThread.start();
        baThread=new Thread (bAnalyzer);
        baThread.start();
    }

    /**
     * interrupted alle threads und schließt alle player
     */
    public void stop()  {
        vaThread.interrupt();
        aaThread.interrupt();
        baThread.interrupt();
        sPlayer.closePlayers();
    }

    public void switchLocation(SimLocation location)    {
        sPlayer.switchLocation(location);
    }
}
