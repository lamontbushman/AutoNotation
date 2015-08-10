package lbushman.audioToMIDI.processing;

import java.util.List;

import javax.sound.sampled.AudioFormat;

public class AudioData {
	private AudioFormat format;
	private byte[] sampledData;
	
	//TODO get better name for this
	private int[] originalSignal;
	private Complex[] complexData;
	private double overlapPercent;
	private int fftLength;
	private boolean dataWindowed;
	private Complex[] fft;
	private List<Double> fftAbsolute;
	private Double[] fftLowPassAbsolute;
//	private Double[] fftCepstrum;
	private List<Double> frequencies;
	private String[] noteNames;
	private List<Double> normalizedFrequencies;
	private Double[] fftInverseTest;
	private List<Double> autoCorrelationAbsolute;
	private Integer numFFT;
	
	private boolean dataHanned;
	private List<Double> spectralFlux;
	private List<Integer> onsets;
	private Double[] amp;
	private int numFFTsInOneSecond;
	private List<Integer> beats;
	private List<Integer> beats2;
	private List<Double> beatsPercent;
	
	public AudioData(byte[] samples, AudioFormat audioFormat) {
		format = audioFormat;
		//Data in bytes.
		sampledData = samples;
		//Format first needs to be set before toIntArray() called.
		originalSignal = toIntArray(samples);
		numFFT = null;
	}
	
	private int[] toIntArray(byte[] bites) {
		//TODO Check format is set
		if(format.getSampleSizeInBits() == 16) {
			int[] array = new int[bites.length / 2];
			int arrayIndex = 0;
			int first = 0 ;
			int second = 1;
			if(!format.isBigEndian()) {
				first = 1;
				second = 0;
			} 
			for(int i = 0; i < bites.length - 1; i+=2) {
				array[arrayIndex] = (bites[i + first] << 8) | (bites[i + second] & 0xFF);
				//System.out.print(Integer.toHexString(array[arrayIndex]) + " ");
				arrayIndex++;
			}
			return array;
		} else if(format.getSampleSizeInBits() == 8){
			int[] array = new int[bites.length];
			for(int i = 0; i < bites.length; i++) {
				array[i] = bites[i];
//				System.out.println(array[i] + "  " + bites[i]);
			}
			return array;
		} else {
			System.err.println("Unexpected sample size in bits.");
			return null;
		}
	}
	
	public int getNumFFT() {
		if(numFFT == null) {
			if(getFftAbsolute() != null) {
				//TODO getFFTLength() better have the updated FFT length
				numFFT = getFftAbsolute().size() / getFftLength();
			} else {
				return 0;
			}
		}
		return numFFT;
	}
	
	/**
	 * only should be called for testing purposes.
	 * @param numFFT
	 */
	public void setNumFFT(int numFFT) {
		this.numFFT = numFFT;
	}
	
	public AudioFormat getFormat() {
		return format;
	}

	public byte[] getSampledData() {
		return sampledData;
	}
	
	public int[] getOriginalSignal() {
		return originalSignal;
	}
	
	public Complex[] getComplexData() {
		return complexData;
	}

	public void setComplexData(Complex[] complexData) {
		this.complexData = complexData;
	}

	public double getOverlapPercentage() {
		return overlapPercent;
	}

	public void setOverlapPercentage(double overlapPercentage) {
		overlapPercent = overlapPercentage;
	}
	
	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}
	
	public boolean isDataWindowed() {
		return dataWindowed;
	}

	public void setDataWindowed() {
		this.dataWindowed = true;
	}
	
	public List<Double> getFftAbsolute() {
		return fftAbsolute;
	}

	public void setFftAbsolute(List<Double> fftAbsolute) {
		this.fftAbsolute = fftAbsolute;
	}

	public List<Double> getFrequencies() {
		return frequencies;
	}

	public void setFrequencies(List<Double> frequencies) {
		this.frequencies = frequencies;
	}

	public String[] getNoteNames() {
		return noteNames;
	}

	public void setNoteNames(String[] noteNames) {
		this.noteNames = noteNames;
	}

	public List<Double> getNormalizedFrequencies() {
		return normalizedFrequencies;
	}

	public void setNormalizedFrequencies(List<Double> normalizedFrequencies) {
		this.normalizedFrequencies = normalizedFrequencies;
	}

	public void setFftInverseTest(Double[] fftData) {
		fftInverseTest = fftData;
		// TODO Auto-generated method stub
	}
	
	public Double[] getFftInverseTest() {
		return fftInverseTest;
	}

	public Complex[] getFft() {
		return fft;
	}

	public void setFft(Complex[] fft) {
		this.fft = fft;
	}

	public List<Double> getAutoCorrelationAbsolute() {
		return autoCorrelationAbsolute;
	}

	public void setAutoCorrelationAbsolute(List<Double> autoCorrelationAbsolute) {
		this.autoCorrelationAbsolute = autoCorrelationAbsolute;
	}

	public boolean isDataHanned() {
		return dataHanned;
	}

	public void setDataHanned() {
		this.dataHanned = true;
	}

	public Double[] getFftLowPassAbsolute() {
		return fftLowPassAbsolute;
	}

	public void setFftLowPassAbsolute(Double[] fftLowPassAbsolute) {
		this.fftLowPassAbsolute = fftLowPassAbsolute;
	}
	
	public List<Double> getSpectralFlux() {
		return spectralFlux;
	}

	public void setSpecralFlux(List<Double> spectralFlux) {
		// TODO Auto-generated method stub
		this.spectralFlux = spectralFlux;
	}
	
	public List<Integer> getOnsets() {
		return onsets;
	}

	public void setOnsets(List<Integer> onsets) {
		this.onsets = onsets;
	}

	public Double[] getAmp() {
		return this.amp;
	}
	
	public void setAmp(Double[] amp) {
		this.amp = amp;
	}
	
	public int getNumFftsInOneSecond() {
		return numFFTsInOneSecond;
	}

	public void setNumFftsInOneSecond(int numFFTsInOneSecond) {
		this.numFFTsInOneSecond = numFFTsInOneSecond;
	}
	
	public List<Integer> getBeats() {
		return beats;
	}

	public void setBeats(List<Integer> beats) {
		this.beats = beats;
	}
	
	public List<Integer> getBeats2() {
		return beats2;
	}

	public void setBeats2(List<Integer> beats2) {
		this.beats2 = beats2;
	}
	
	public List<Double> getBeatsPercent() {
		return beatsPercent;
	}

	public void setBeatsPercent(List<Double> beatPercent) {
		this.beatsPercent = beatPercent;
	}
}
