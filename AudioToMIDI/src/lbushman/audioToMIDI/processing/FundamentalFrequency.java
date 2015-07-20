package lbushman.audioToMIDI.processing;

import lbushman.audioToMIDI.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FundamentalFrequency {
	
	//TODO I don't know if this should be final. I am weak on my knowledge of final on objects.
	private final AudioData data;
	private final List<Double> ffts;
	
	/**
	 * This class will compute the fundamental frequencies based off ffts. This is independent of any ffts in 
	 * audioData. This is so that multiple versions of an fft can be compared (i.e. with filtering).
	 * @param audioData
	 * @param ffts
	 */
	public FundamentalFrequency(final AudioData audioData, final List<Double> ffts) {
		data = audioData;
		this.ffts = ffts;
		if(data == null || ffts == null) {
			System.err.println("FundamentalFrequency - One or both are null, ffts : " + ffts + " data: " + data);
			System.exit(1);
		}
		if(data.getFftLength() < 1) {
			System.err.println("FundamentalFrequency - fftLength is below expected value : " + data.getFftLength());
			System.exit(1);
		}
	}
	
	/**
	 * Computes the onsets. Sets both the onsets and the spectral flux to audioData.
	 */
	public void computeFrequencies() {
		int base = 0;
		int fftLength = data.getFftLength();
		int halfFFTLength = fftLength / 2;
		List<Double> frequencies = new ArrayList<Double>(data.getNumFFT());
		
		while(base + fftLength <= ffts.size()) {
			List<Double> halfFFT = ffts.subList(base, base + halfFFTLength);
			base += fftLength;
			frequencies.add(computeFrequency(halfFFT));
		}
		
		data.setFrequencies(frequencies);
		
		for(int i = 0; i < frequencies.size(); i++) {
			Double frequency = frequencies.get(i);
			String note = FrequencyToNote.findNote(frequency);
			System.out.println(i + " " + frequency + " " + note);
		}
	}
	
	private Double computeFrequency(List<Double> fft) {
		int highestPeak = findMax(fft);
		int index = computeFundamentalIndex(fft, highestPeak);
		double fundFreq = computeFrequency(index);
		
		//Only for debug purposes
		//printOperations(highestPeak, index, fundFreq);
		
		return fundFreq;
	}
	
	//Only for debug purposes
	private int printI = 0;
	Double[] maxAmp = null;
	private void printOperations(int highestPeak, int fundIndex, double fundamentalFrequency) {
		//Calculate the amps at time periods
		if(maxAmp == null)
			maxAmp = ProcessSignal.computeAmp(data);
		
		Double freqF = computeFrequency(highestPeak);
		String noteF = FrequencyToNote.findNote(freqF);
		
		String baseNote = FrequencyToNote.findNote(fundamentalFrequency);
			
		/*int index = findMax(Arrays.copyOfRange(acAbsolute, start, end), 10);
		double freq = ProcessSignal.computeFrequency(index, audioData);
		String note = FrequencyToNote.findNote(freq);*/
		
		System.out.println("FFT  i: " + printI + "Index: " + highestPeak + " frequency: " + freqF + " note: " + noteF + " maxAmp[i]: " + maxAmp[printI]);
    	//System.out.println("AC   i: " + i + "Index: " + index + " frequency: " + freq + " note: " + note + " maxAmp[i]: " + maxAmp[i]);
    	System.out.println("Base i: " + printI + "Index: " + fundIndex + " frequency: " + fundamentalFrequency + " note: " + baseNote + " maxAmp[i]: " + maxAmp[printI]);
    	System.out.println();
    	printI++;
	}
	
    private double computeFrequency(int bin) {
    	return computeFrequency(bin, data);
    }	
	
    /**
     * Probably don't need this
     * @param bin
     * @param data
     * @return
     */
    public static double computeFrequency(int bin, AudioData data) {
    	return bin * data.getFormat().getSampleRate() / data.getFftLength();
    }
	
	private int computeFundamentalIndex(List<Double> fft, int highestPeak) {
		List<Integer> peaks = Peaks.findPeaks(fft, highestPeak, 7, .05);
    	
		List<Integer> peakDiff = new LinkedList<Integer>();
		for(int i = 0; i < peaks.size() - 1; i++) {
			peakDiff.add(peaks.get(i+1) - peaks.get(i));
		}

		List<Integer> modes = Util.mode(peakDiff);
		if(modes.size() > 1) {
			System.err.println("You have multiple modes at an FFT");
			//System.exit(1);
		}
		return (modes.size() >= 1) ? Util.round(Util.average(modes))/*modes.get(0)*/ : -1;
	}
	
    private int findMax(List<Double> fftData) {
    	int start = 0;
    	//Only search half of the data.
    	int end = start + (data.getFftLength() / 2);
    	return Util.maxIndex(fftData, start, end);
    }
	
/*    private int calculateBaseFrequencyIndex(Double[] fftData, int nth, int startOffset) {
    	int start = nth * data.getFftLength();
    	//Only look at half of the FFT.
    	int end = start + (data.getFftLength() / 2);
    	int maxI = findMax(fftData, start, end, 0);
    	
    	int subMaxI = maxI - start;
    	
    	List<Double> data = Arrays.asList(fftData).subList(start, end);
    	List<Integer> peaks = Peaks.findPeaks(data, subMaxI, 7, .05);
    	    	
		List<Integer> peakDiff = new LinkedList<Integer>();
		for(int i = 0; i < peaks.size() - 1; i++) {
			peakDiff.add(peaks.get(i+1) - peaks.get(i));
		}
		//This all will not work if the peaks are not exactly correct.
		List<Integer> modes = Util.mode(peakDiff);
		if(modes.size() > 1) {
			System.err.println("You have multiple modes at FFT number: " + nth);
			//System.exit(1);
		}
		return (modes.size() >= 1) ? Util.round(Util.average(modes))modes.get(0) : -1;
    }*/
}
