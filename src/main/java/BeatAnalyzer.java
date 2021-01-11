
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.sound.*;

/**
 * Analyzes audio input streams and calculates bpm, triggers clapping on Beat.
 * This Beat analyzer works best on tracks with heavy and straight percussion.
 * In real life scenarios a dercussion only mix should feed this analyzer.
 * 
 * @author
 *
 */
public class BeatAnalyzer extends PApplet implements Runnable {
	// Declare the sound source and FFT analyzer variables
	AudioIn in;
	FFT fft;
	
	AmplitudeAnalyzer amplitude;
	

	// Define how many FFT bands to use (this needs to be a power of two)
	int bands = 128;

	// Define a smoothing factor which determines how much the spectrums of
	// consecutive
	// points in time should be combined to create a smoother visualisation of the
	// spectrum.
	// A smoothing factor of 1.0 means no smoothing (only the data from the newest
	// analysis
	// is rendered), decrease the factor down towards 0.0 to have the visualisation
	// update
	// more slowly, which is easier on the eye.
	float smoothingFactor = 0.2f;

	// Create a vector to store the smoothed spectrum data in
	float[] sum = new float[bands];

	// Variables for drawing the spectrum:
	// Declare a scaling factor for adjusting the height of the rectangles
	int scale = 5;
	// Declare a drawing variable for calculating the width of the
	float barWidth;

	// Sound Energy Value of previous frame
	double prev;
	// padding intervall between beat signals
	int padding;

	int frameCount;
	
	int bpm;

	List<Integer> bpms;
	List<Integer> beatsInPeriod;
	
	int deviceIndex=0;
	
	SoundFile sample;
	
	
	
	/**
	 * Constructor
	 * @param amplitude Amplitude Analyzer to check if a new song started or not
	 */
	public BeatAnalyzer(int deviceIndex, AmplitudeAnalyzer amplitude) {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		frameCount = 0;
		this.amplitude = amplitude;
		this.deviceIndex = deviceIndex;
	}
	
	public BeatAnalyzer(int deviceIndex) {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		frameCount = 0;
		this.deviceIndex = deviceIndex;
	}
	
	public BeatAnalyzer() {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		frameCount = 0;
	}
	
	
	/**
	 * Processing Native. Set Window Size
	 */
	public void settings() {
		size(640, 360);
	}

	/**
	 * Processing Native. Pre conditions.
	 */
	public void setup() {
		//surface.setVisible(false);
		sample = new SoundFile(this, "D:\\Studium\\Programme\\FS3\\Multimedia\\PublikumsSimulator\\AudienceSimGithub\\audience-simulator\\src\\main\\resources\\sounds\\stadium\\clapping_1.mp3");
		background(255);

		// Calculate the width of the rects depending on how many bands we have
		barWidth = width / (float) (bands);

		// Load and play a soundfile and loop it.
		in = new AudioIn(this, deviceIndex);
		// in.play();

		// Create the FFT analyzer and connect the playing soundfile to it.
		fft = new FFT(this, bands);
		fft.input(in);
		prev = 0;
		padding = 0;
		frameRate(100);
	}
	
	
	/**
	 * Processing Native. Loop with 100 fps and detect beat/calculate bpm
	 */
	public void draw() {

		// Perform the analysis
		fft.analyze();

		double sEnergy = 0;

		for (int i = 0; i < bands; i++) {
			// Smooth the FFT spectrum data by smoothing factor
			sum[i] += (fft.spectrum[i] - sum[i]) * smoothingFactor;

			if (i > 1) {
				sEnergy += abs(sum[i] * (log((float) i / (float) bands) / -5));
			} else {
				sEnergy += abs(sum[i] * (log((float) 1 / (float) bands) / -5));
			}
		}
		
		if(amplitude!=null)  {
			checkSongInProgress();
		}
		
		detectBeat(sEnergy);

		if (frameCount > 500) {
			frameCount = 0;
			calculateBpm();
		}
		
		if(bpm>0) {
			if((frameCount-60)%(int)(100/((float)bpm/60f))==0) {
				//System.out.println("BEAT");
				
				sample.play();
			}
		}

		padding++;
		frameCount++;

		prev = sEnergy;
	}

	
	/**
	 * Calculate the BPM in the current period by average intervall of beats
	 */
	private void calculateBpm() {
		int average = 0;
		int elements = 0;

		for (int i = 0; i < beatsInPeriod.size(); i++) {
			if (i > 0) {
				average += beatsInPeriod.get(i) - beatsInPeriod.get(i - 1);
				elements++;
			}
		}
		
		int averagePeriod = average/elements;
		if (averagePeriod > 0) {
			bpm = (500 / averagePeriod) * 12;
		} else {
			bpm = 0;
		}
		
		bpms.add(bpm);
		
		//Calculate average of existing bpm values
		int bpmAverage = 0;
		for (int i = 0; i < bpms.size(); i++) {

			bpmAverage += bpms.get(i);
		}
		//bpmAverage += bpm;
		

		//Set new bpm
//		if(bpms.size() < 10) {		
//			bpm = bpmAverage / (bpms.size()+1);
//			bpms.add(bpm);
//		} else {
//			if(Math.abs((bpmAverage /bpms.size())-bpm)<10) {
//				bpm = bpmAverage / (bpms.size()+1);
//				bpms.add(bpm);
//			}
//		}
		bpm = bpmAverage / bpms.size();
		beatsInPeriod.clear();
		
		SamplePlayer.bpm=bpm;
		
		System.out.println(bpm);
	}
	
	
	/**
	 * Detect a beat in current frame
	 * @param sEnergy
	 */
	private void detectBeat(double sEnergy) {
		if (prev > 0) {
			if (sEnergy / prev > 1.08) {
				if (padding > 45) {
					beatsInPeriod.add(frameCount);
					padding = 0;
					triggerClap();
				}
			}
		}
	}
	
	
	/**
	 * Checks if a song is in progress. Otherwise resets everything.
	 */
	private void checkSongInProgress() {
		if(!amplitude.isSongInProgress()) {
			bpms.clear();
			beatsInPeriod.clear();
			prev=0;
		}
	}
	
	
	/**
	 * trigger single Clap in SamplePlayer
	 */
	private void triggerClap() {

	}

	@Override
	public void run() {
		System.setProperty("java.version", "13.0.0");
		PApplet.main("BeatAnalyzer");
	}

}
