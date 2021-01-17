package simulation;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import processing.core.PApplet;

public class SamplePlayer extends PApplet {
    public int bpm; //evtl unnötig
    private SimLocation location;   // evtl unnötig
    private Minim minim=new Minim(new MinimFileSystemHandler());
    private AudioPlayer ambiencePlayer;
    private AudioSample clappingPlayer;
    private AudioSample cheeringPlayer;
    private boolean shouldBeClapping; //die hier soll der VideoAnalyzer verändern.
    private boolean hasCheered; // true, wenn die cheer-methode vor kurzem aufgerufen wurde


    public void setShouldBeClapping(boolean sbc) {
        shouldBeClapping = sbc;
        Thread t=new Thread(() -> {
            try {
                Thread.sleep((long)(Math.random()*10000+10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            shouldBeClapping=false;
        });
        t.start();
    }

    /**
     * weist den Playern files zu und spielt Ambience (nicht wirklich MVC-Konform aber was solls)
     * @param location
     */
    public SamplePlayer(SimLocation location)   {
        this.location=location;
        ambiencePlayer=minim.loadFile(location.getPath()+"ambience.mp3");
        clappingPlayer=minim.loadSample(location.getPath()+"clapping_1.mp3");
        cheeringPlayer=minim.loadSample(location.getPath()+"cheering_1.mp3");
        this.playAmbience();
    }

    /**
     * spielt die ganze Zeit ambience im Loop (auf voller Lautstärke).
     */
    public void playAmbience()  {
        ambiencePlayer.loop();
    }

    /**
     * spielt clapping, wenn shouldBeClapping true ist.
     */
    public void playClapping()  {
        if(shouldBeClapping)    {
            clappingPlayer.trigger();
        }
    }

    /**
     * spielt cheering, wenn nicht vor weniger als 0.5 Sekunden Cheering gespielt wurde
     */
    public void playCheering()  {
        if(!hasCheered) {
            cheeringPlayer.trigger();
            hasCheered=true;
            Thread t=new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hasCheered=false;
            });
            t.start();
        }

    }

    public void closePlayers()  {
        ambiencePlayer.close();
        clappingPlayer.close();
        cheeringPlayer.close();
        minim.stop();
    }

    /**
     * macht im Endeffekt das gleiche wie der Konstruktor
     * @param location
     */
    public void switchLocation(SimLocation location)    {
        this.location=location;
        ambiencePlayer=minim.loadFile(location.getPath()+"ambience.mp3");
        clappingPlayer=minim.loadSample(location.getPath()+"clap.mp3");
        cheeringPlayer=minim.loadSample(location.getPath()+"cheer.mp3");
        this.playAmbience();
    }
}
