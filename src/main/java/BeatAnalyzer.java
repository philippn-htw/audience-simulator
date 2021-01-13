
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.sound.*;

/**
 * Analyzes audio input streams and calculates bpm, triggers clapping on Beat.
 * This Beat analyzer works best on tracks with heavy and straight percussion.
 * In real life scenarios a percussion only mix should feed this analyzer.
 * 
 * This classe uses algorithms from Sound2Light Copyright (c) 2016 Electronic
 * Theatre Controls, Inc., http://www.etcconnect.com and "Evaluation of the
 * Audio Beat Tracking System BeatRoot" by Simon Dixon
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

	float[] last_spectrum = new float[bands];

	// Define a smoothing factor which determines how much the spectrums of
	// consecutive
	// points in time should be combined to create a smoother visualisation of the
	// spectrum.
	// A smoothing factor of 1.0 means no smoothing (only the data from the newest
	// analysis
	// is rendered), decrease the factor down towards 0.0 to have the visualisation
	// update
	// more slowly, which is easier on the eye.
	float smoothingFactor = 0.4f;

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
	
	private static final int sampleCount=1000;

	List<Integer> bpms;
	List<Integer> beatsInPeriod;
	double[] fluxValues = new double[sampleCount];
	List<Boolean> onsetValues;
	double[] fluxValuesNormalized = new double[sampleCount];

	int deviceIndex = 0;

	SoundFile sample;

	/**
	 * Constructor
	 * 
	 * @param amplitude Amplitude Analyzer to check if a new song started or not
	 */
	public BeatAnalyzer(int deviceIndex, AmplitudeAnalyzer amplitude) {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		onsetValues = new ArrayList<>();
		frameCount = 0;
		this.amplitude = amplitude;
		this.deviceIndex = deviceIndex;
	}

	public BeatAnalyzer(int deviceIndex) {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		onsetValues = new ArrayList<>();
		frameCount = 0;
		this.deviceIndex = deviceIndex;
	}

	public BeatAnalyzer() {
		bpms = new ArrayList<>();
		beatsInPeriod = new ArrayList<>();
		onsetValues = new ArrayList<>();
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
		// surface.setVisible(false);
		sample = new SoundFile(this,
				"D:\\Studium\\Programme\\FS3\\Multimedia\\PublikumsSimulator\\AudienceSimGithub\\audience-simulator\\src\\main\\resources\\sounds\\stadium\\clapping_1.mp3");
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
	 * Detect a beat in current frame
	 * @param sEnergy
	 */
	private void detectBeat() {
		//Perform the analysis
		fft.analyze();

		double sEnergy = 0.0;

		for (int i = 0; i < bands; i++) {
			// Smooth the FFT spectrum data by smoothing factor
			sum[i] += (fft.spectrum[i] - sum[i]) * smoothingFactor;

			if (i > 1) {
				sEnergy += abs(sum[i] * (log((float) i / (float) bands) / -5));
			} else {
				sEnergy += abs(sum[i] * (log((float) 1 / (float) bands) / -5));
			}
		}
		
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
	 * processing native
	 */
	public void draw() {
		updateSpectralFluxes(fft.analyze());

		if (frameCount >= sampleCount - 1) {
			frameCount = 0;
			updateOnset();
			calculateBPM();
		}
		if(bpm>0) {
			if((frameCount-60)%(int)(100/((float)bpm/60f))==0) {
				//System.out.println("BEAT");			
				sample.play();
			}
		}

		frameCount++;
	}

	/**
	 * Spectrual Fluxes are the sum of only increases in every frequency band. Loop
	 * is divided by two for better performance. Have to be called every frame
	 */
	private void updateSpectralFluxes(float[] spec) {
		//smooth bands
		for (int i = 0; i < bands; i++) {
			spec[i] += (spec[i] - sum[i]) * smoothingFactor;
			if (i > 1) {
				sum[i] = Math.abs(sum[i] * (log((float) i / (float) bands) / -5));
			} else {
				sum[i] = Math.abs(sum[i] * (log((float) 1 / (float) bands) / -5));
			}
		}
		
		double flux = 0.0;
		for (int i = 0; i < bands / 2; i++) {
			if (spec[i] > last_spectrum[i]) {
				flux += spec[i] - last_spectrum[i];
			}
			if (spec[i + bands / 2] > last_spectrum[i + bands / 2]) {
				flux += spec[i + bands / 2] - last_spectrum[i + bands / 2];
			}
			
			last_spectrum[i] = spec[i];
		}

		fluxValues[frameCount] = flux;


	}

	/**
	 * updates the list of onsets.
	 */
	private void updateOnset() {
		// reset all values on onsetValues
		onsetValues.clear();

		for(int i = 0;i<sampleCount;i++) {
			fluxValuesNormalized[i] = 0;
		}
		

		// normalize spectrum to average of 0 and standart derivation of 1
		float average = 0.0f;
		float variance = 0.0f;
		for (int i = 0; i < sampleCount; i++) {
			average += fluxValues[i];
			variance += fluxValues[i] * fluxValues[i];
		}
		average = average / bands;

		double stDeviation;
		if (variance > 0) {
			stDeviation = Math.sqrt(variance);
		} else {
			stDeviation = 1;
		}

		// set the minimum standart deviation to 20 if it is lower
		stDeviation = Math.max(stDeviation, 20.0);

		for (int i = 0; i < sampleCount; i++) {
			fluxValuesNormalized[i]=((fluxValues[i] - average) / stDeviation);
		}

		// detect onsets
		int w = 20; // window for local maximum detection (current frame +- w)
		int m = 3; // multiplier to increase range before onset
		float pastThresholdWeight = 0.84f;
		float averageThresholdDelta = 0.01f;

		double pastThreshold = fluxValuesNormalized[m * w - 1];

		for (int i = m * w; i < sampleCount-w; i++) {
			// ------------------------------- 1. Past Threshold
			// -----------------------------
			// Calculate the past threshold recursively, as the maximum between a weighted
			// average between
			// the last threshold and the last sample, and the last sample itself
			double pastThresholdNew = Math.max(fluxValuesNormalized[i - 1],
					pastThresholdWeight * pastThreshold + (1 - pastThresholdWeight) * fluxValuesNormalized[i - 1]);
			pastThreshold = pastThresholdNew;

			// Continue if the sample does not meet the past threshold
			if (fluxValuesNormalized[i] < pastThreshold) {
				//System.out.println("NOT OVER PAST THRESHOLD");
				onsetValues.add(false);
				continue;
			}

			// -------------------------------- 2. Local Maximum
			// -----------------------------
			// Compare to the releavant surronding samples
			boolean localMaximum = true;
			for (int k = i - w; k <= i + w; ++k) {
				if (fluxValuesNormalized[i] < fluxValuesNormalized[k]) {
					localMaximum = false;
					break;
				}
			}

			// Continue of the sample does no meet the local neighbour criterium
			if (!localMaximum) {
				onsetValues.add(false);
				//System.out.println("NOT LOCAL MAXIMUM");
				continue;
			}

			// ---------------------------- 3. Average Threshold
			// -----------------------------
			// Sum the surounding samples, then divide by their number and add the delta
			float averageThreshold = 0.0f;
			for (int k = i - w * m; k < i + w; ++k) {
				averageThreshold += fluxValuesNormalized[k];
			}
			averageThreshold /= (m * w + w + 1);
			averageThreshold += averageThresholdDelta;

			// Continue if the sample does not meet the average threshold
			if (fluxValuesNormalized[i] < averageThreshold) {
				onsetValues.add(false);
				//System.out.println("NOT OVER AVERAGE THRESHOLD");
				continue;
			}

			// Set the sample to be an onset if it has met all the criteria
			onsetValues.add(true);
			
		}

	}
	
	
	/**
	 * calculate the bpm by searching for most likely intervall between beats.
	 */
	private void calculateBPM() {
		int intervall = 0;
		List<Integer> intervallList = new ArrayList<>();
		int n=0;
		for(int i=0;i<onsetValues.size();i++) {
			intervall++;
			if(onsetValues.get(i)==true) {
				intervallList.add(intervall);
				//System.out.println(intervall);
				intervall=0;
				n++;
			}
		}
		
		// find the main intervall
		int mainIntervall = 0;
		int mainIntervallCount = 0;
		for(int i=0; i<intervallList.size();i++) {
			if(Math.abs(intervallList.get(i)-mainIntervall) > 8) {
				int currentIntervallCount = 0;
				for(int k=i; k<intervallList.size();k++) {
					if(Math.abs(intervallList.get(i)-intervallList.get(k))<8) {
						currentIntervallCount++;
					}
				}
				if(currentIntervallCount > mainIntervallCount) {
					mainIntervall = intervallList.get(i);
					mainIntervallCount = currentIntervallCount;
				}
			}
		}
		
		
		
		// calculate average of main intervalls
		int mainIntervallSum=0;
		int mainIntervallElements=0;
		for(int i=0; i<intervallList.size();i++) {
			if(Math.abs(intervallList.get(i)-mainIntervall) < 8) {
				mainIntervallSum += intervallList.get(i);
				mainIntervallElements++;
			}
		}
		
		if(mainIntervallElements>0) {
			int mainIntervallAverage = mainIntervallSum / mainIntervallElements;
		
		
			
			if(intervallList.size() > 0) {
			
				bpm = (sampleCount / mainIntervallAverage) * (60/(sampleCount/100));
				
				if(bpm>120) {
					bpm=bpm/2;
				}
			
				bpms.add(bpm);
			
				//Calculate average of existing bpm values
				int bpmAverage = 0;
				for (int i = 0; i < bpms.size(); i++) {
	
					bpmAverage += bpms.get(i);
				}
	
				bpm = bpmAverage / bpms.size();
			
				System.out.println(bpm);
			}
		}
	}

	
	/**
	 * Checks if a song is in progress. Otherwise resets everything.
	 */
	private void checkSongInProgress() {
		if (!amplitude.isSongInProgress()) {
			bpms.clear();
			beatsInPeriod.clear();
			prev = 0;
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
