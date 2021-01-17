package simulation;
import processing.core.PApplet;
import processing.sound.*;

public class AmplitudeAnalyzer extends PApplet implements Runnable  {

    private final Amplitude amplitude=new Amplitude(this);
    private final AudioIn stream;
    private boolean SongInProgress=false;
    private final SamplePlayer sPlayer;
    private boolean blocker=false;//ist true, wenn checkSongInProgress am machen ist und verhindert andere aufrufe
    boolean ausTimer;
    
    public AmplitudeAnalyzer(AudioIn in, SamplePlayer player)  {
        stream=in;
        sPlayer=player;
    }

    /**
     * checkt, ob sich der Status des Songs (an, aus) geändert hat und spielt jubeln falls ja. beendet den block, wenn durch
     * @throws InterruptedException
     */
    private void checkSongInProgress() throws InterruptedException {
        if(SongInProgress) {
            if (amplitude.analyze() <= 0.2) {
                ausTimer = true;
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ausTimer = false;
                });
                t.start();
                boolean ausCheck = true; //ist true, wenn es kein Anzeichen gibt, dass der Song weitergeht
                while (ausTimer) {
                    Thread.sleep(1);
                    if (amplitude.analyze() >= 0.2) {
                        ausCheck = false;
                    }
                }
                if (ausCheck)   {
                    SongInProgress = false;
                    sPlayer.playCheering();
                }
            }
        } else if(amplitude.analyze()>=0.2)    {
            SongInProgress=true;
            sPlayer.playCheering();
        }
        blocker=false;
    }
    
    public boolean isSongInProgress() {
    	return SongInProgress;
    }

    @Override
    public void run() {
        stream.start();
        amplitude.input(stream);
        if(amplitude.analyze()>=0.2)
            SongInProgress=true;
        while(!Thread.interrupted()) {
            try {
                if(!blocker)    {
                    blocker=true;
                    checkSongInProgress();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
