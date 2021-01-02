import processing.core.PApplet;
import processing.sound.*;

public class AmplitudeAnalyzer extends PApplet implements Runnable  {

    private Amplitude amplitude=new Amplitude(this);
    private AudioIn stream;
    private boolean SongInProgress=false;
    public AmplitudeAnalyzer(AudioIn in, SamplePlayer player)  {
        stream=in;
    }

    private void checkSongInProgress() throws InterruptedException {
        if(SongInProgress)  {
            if(amplitude.analyze()==0)  {
                wait(1000);
                if(amplitude.analyze()==0)  {
                    SongInProgress=false;
                }
            }
        } else if(amplitude.analyze()>0)
            SongInProgress=true;
    }

    @Override
    public void run() {
        stream.start();
        amplitude.input(stream);
        if(amplitude.analyze()>0)
            SongInProgress=true;
        while(true) {
            try {
                checkSongInProgress();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
