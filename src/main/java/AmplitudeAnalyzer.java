import processing.core.PApplet;
import processing.sound.*;

public class AmplitudeAnalyzer extends PApplet implements Runnable  {

    private Amplitude amplitude=new Amplitude(this);
    private AudioIn stream;
    private boolean SongInProgress=false;
    private SamplePlayer sPlayer;
    
    public AmplitudeAnalyzer(AudioIn in, SamplePlayer player)  {
        stream=in;
        sPlayer=player;
    }

    private void checkSongInProgress() throws InterruptedException {
        if(SongInProgress)  {
            if(amplitude.analyze()<=0.2)  {
                wait(1000);
                if(amplitude.analyze()<=0.2)  {
                    SongInProgress=false;
                    sPlayer.playCheering();
                }
            }
        } else if(amplitude.analyze()>=0.2)    {
            SongInProgress=true;
            sPlayer.playCheering();
        }
    }
    
    public boolean isSongInProgress() {
    	return SongInProgress;
    }

    @Override
    public void run() {
        stream.start();
        amplitude.input(stream);
        if(amplitude.analyze()>0)
            SongInProgress=true;
        while(!Thread.interrupted()) {
            try {
                checkSongInProgress();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
