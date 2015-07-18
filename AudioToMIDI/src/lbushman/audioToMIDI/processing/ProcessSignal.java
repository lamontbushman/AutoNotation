package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessSignal {
	private AudioData data;
	
	public ProcessSignal(AudioData audioData, double overlapPercentage, int fftLength) {
		data = audioData;
		data.setOverlapPercentage(overlapPercentage);
		data.setFftLength(fftLength);
	}
	
    public double computeFrequency(int bin) {
    	return computeFrequency(bin, data);
    }
    
    public static double computeFrequency(int bin, AudioData data) {
    	return bin * data.getFormat().getSampleRate() / data.getFftLength();
    }
    
    public void process() {
    	computeComplexAndOverlap(false/*doHann*/);
    	doProcess();
    	
    	OnsetDetection od = new OnsetDetection(data, Arrays.asList(data.getFftAbsolute()));
    	od.computeOnsets();
    	
    	OnsetDetection od2 = new OnsetDetection(data, Arrays.asList(data.getFftLowPassAbsolute()));
    	od2.computeOnsets();
    	
    	//fftAbsolute();
    	//fftCepstrum();

    //	setFrequencies();
   // 	setNotenames();
    //	setNormalizedFrequencies();
    }
            	
	private void computeComplexAndOverlap(boolean doHann) {
		int fftLength = data.getFftLength();

		double overlapPercentage = data.getOverlapPercentage();
		//TODO this is only working for 50% right now
		if (overlapPercentage != 0.5) {
			System.err.println("Another overlap is not working fully yet.");
			System.exit(1);
		}
		
		int[] signal = data.getOriginalSignal();
		
		// TODO check to see if this is a reasonable overlap
		int increment = (int) (fftLength * overlapPercentage);
		
		
		int len = signal.length;
		int newSize = (int) (Math.round((double)len / fftLength) * fftLength +
				Math.round((double)(len - increment) / fftLength) * fftLength);
		
		Complex[] complexData = new Complex[newSize];
		
		List<Double> weights = null;
		if(doHann) {
			weights = getHannWeights(fftLength);
			data.setDataHanned();
		}
		data.setDataWindowed();
		
		int windowIndex = 0;
		//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
		for(int i = 0; i <= signal.length - increment; i+= increment) {
			int hanIndex = 0;
			for(int j = i; j < i + fftLength; j++) {
				//System.out.println("w: " + windowIndex + " j:" + j);
				if(j < signal.length)
					if(data.isDataHanned())
						complexData[windowIndex] = new Complex(hann(hanIndex, signal[j], weights));
					else
						complexData[windowIndex] = new Complex(signal[j]);
				else
					complexData[windowIndex] = new Complex(0);
				windowIndex++;
				hanIndex++;
			}
		}
		data.setComplexData(complexData);
	}
	
	private static List<Double> getHannWeights(int fftLength) {
    	List<Double> weights = new ArrayList<Double>();
    	for(int i = 0; i < fftLength; i++) {
    		double weight = 
    				Math.pow(
    	    				Math.sin((Math.PI*i) / (fftLength -1)),
    	    				2);
    		weights.add(weight);
    	}
    	return weights;
	}
	
	private double hann(int index, double value, List<Double> weights) {
		return weights.get(index) * value;
	}
				
	/**
	 * Applies an RC low-pass filter to x 
	 * @param x array to apply filter to
	 * @param dt time interval
	 * @param RC time constant
	 * @return
	 */
	static List<Double> lowPass(final List<Double> x, double dt, double RC) {
		List<Double> y = new ArrayList<Double>(x.size());
		double alpha = dt / (RC + dt);
		y.add(x.get(0));
		
		for(int i = 1; i < x.size(); i++) {
//			y[i] = alpha * x[i] + (1-alpha) * y[i-1];
//			y[i] = y[i - 1] + alpha * (x[i] - y[i-1]);
			y.add(y.get(i - 1) + alpha * (x.get(i) - y.get(i - 1)));
		}
		return y;
	}
	
	static List<Double> lowPass(final List<Double> x) {
		return lowPass(x, 5, 20);//5,20 //1,2
	}
				
	/**
	 * Returns a copy of data with the size doubled and the
	 * second half set to zero.
	 * @param data
	 * @return
	 */
	private Complex[] doubleAndPad(Complex[] data) {
		Complex[] doubledData = Arrays.copyOf(data, data.length * 2);
		//Set the second half to zero
		for(int i = data.length; i < doubledData.length; i++) {
			doubledData[i] = new Complex(0);
		}
		return doubledData;
	}
	
	//TODO ensure complexData is divisible by the fftLength");
	private void doProcess() {
		Complex[] complexData = data.getComplexData();
		int origFFTLength = data.getFftLength();
		
		//Double the size of things
		data.setFftAbsolute(new Double[complexData.length * 2]);
		data.setFftLowPassAbsolute(new Double[complexData.length * 2]);

		int newFFTLength = origFFTLength * 2;
		//TODO Future self this may be the source of your errors.
		data.setFftLength(newFFTLength);
		
		Double[] fftAbsolute = data.getFftAbsolute();
		Double[] fftLowpass = data.getFftLowPassAbsolute();
		
		for(int i = 0; i < complexData.length; i+= origFFTLength) {
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + origFFTLength);
			//Double the length and pad for linear (instead of cyclic) autocorrelation.
			toFft = doubleAndPad(toFft);
			
			FFT.fftForward(toFft);
			
			//Low pass filter test
			List<Double> lp = new ArrayList<Double>(toFft.length);//double[toFft.length];
			for(int j = 0; j < toFft.length; j++) {
				double absolute = toFft[j].absolute();
				fftAbsolute[(i*2)+j] = absolute;
				lp.add(absolute);
			}
			
			lp = lowPass(lp);
			
			for(int j = 0; j < toFft.length; j++) {	
				fftLowpass[(i*2)+j] = lp.get(j);//lp[j];		//TODO WARNING this is because the FFT was doubled 
				//In case filter is not applied
				//fftAbsolute[(i*2)+j] = toFft[j].absolute();
			}
		}
	}
	
	public Double[] computeAmp() {
		Complex[] overlapData = data.getComplexData();
		
		Double[] maxAmp = new Double[data.getNumFFT()/*fftAbsolute.length*/];
		
		 //TODO this still needs to be fixed so, I don't have to divide by 2.
		int fftLength = data.getFftLength() / 2;
		
		
		double sum = 0;
		int maxI = 0;
		for(int i = 0; i < overlapData.length; i+= fftLength) {
			sum = 0;
			for(int j = 0; j < fftLength; j++)
				sum += overlapData[i+j].absolute();
			maxAmp[maxI] = sum/fftLength;
			maxI++;
		}
		
		return maxAmp;
	}
	
	public void printNonConsecutiveNotes(boolean showFrequencies) {
		String lastNote = "";
		String[] notes = data.getNoteNames();
		
		Double[] frequencies = null;
		if(showFrequencies)
			frequencies = data.getFrequencies();
		
		for(int i = 0; i < notes.length; i++) {
			if(showFrequencies)
				System.out.print(frequencies[i] + " " + notes[i] + " ");
			else
				System.out.print(notes[i] + " ");
			
			if (!lastNote.equals(notes[i])) {
				System.out.println();
			}
			lastNote = notes[i];
		}
	}
	
	/**
	 * Assumes complexData is the desired length of data to compute on.
	 * Warning: It will double the length to compute the FFT length. Thus,
	 *  the bins have to be accounted for this change.
	 *  
	 * The first part of the autocorrelation is undesired because obviously
	 * the signal is correlated with itself (i.e. no lag)
	 * 
	 * //TODO probably the problem is in the size of the sample rate.
	 * @param complexData
	 */
	private Double[] autoCorrelation(final Complex[] complexData) {
		//http://stackoverflow.com/questions/3949324/calculate-autocorrelation-using-fft-in-matlab#3950398
		Complex[] toFFT = doubleAndPad(complexData);
		Double[] autoCorrelationAbsolute = new Double[toFFT.length];
		
		//TODO set a variable in AudioData showing that the FFT was doubled
		//TODO move this out into the calling function
		data.setFftLength(toFFT.length * 2);
		
		FFT.fftForward(toFFT);
		
		for(int j = 0; j < toFFT.length; j++) {
			//Same as Complex.mult(toFFT[j], toFFT[j].conjugate()) but simpler
			toFFT[j] = new Complex(toFFT[j].absoluteSquare());
			//Someone said doing it twice will help
			//(I think it is supposed to emphasize the peaks)
			//toFFT[j] = new Complex(toFFT[j].absoluteSquare());
		}
		
		//Effective inverse FFT
		//FFT() = IFFT() for real numbers.
		FFT.fftForward(toFFT);
		
		for(int i = 0; i < toFFT.length; i++) {
			autoCorrelationAbsolute[i] = toFFT[i].absolute();
		}
		return autoCorrelationAbsolute;
	}
	
	/**
	 * Computes one cepstrum based on the entire complexData array.
	 * @param complexData
	 */
	private Double[] cepstrum(final Complex[] complexData) {
		Complex[] toFFT = Arrays.copyOf(complexData, complexData.length);
		FFT.fftForward(toFFT);
		for(int j = 0; j < complexData.length; j++) {
			//maybe hann first
			toFFT[j] = new Complex(Math.log(toFFT[j].absoluteSquare()));
		}
		FFT.fftInverse(toFFT);
		Double[] cepstrum = new Double[complexData.length];
		for(int j = 0; j < complexData.length; j++) {
			cepstrum[j] = toFFT[j].absoluteSquare();
		}
		return cepstrum;
	}
}


/*
 *    public static void main(String args[]) {
    	for(byte length = 7; length < 22; length++)
    	{
	    	//byte length = 4;//4
	    	byte[] bites = new byte[length];
	    	for (byte i = 0; i < length; i++) {
	    		bites[i] = i;
	    	}
	    	//byte[] bites = {0,1,2,3,4,5,6,7,8,9,10};//,11};//,9,10,11,12,13,14,15,16,17};
	    	AudioFormat format = CaptureAudio.getDefaultFormat();
	    	AudioData ad = new AudioData(bites, format);
	
	    	
	    	ProcessSignal ps = new ProcessSignal(ad,.5,4);
	    	//ps.computeComplexAndOverlap(/*.50, 4*);
	    	ps.process();
	    	
	    	System.out.println("Complex Data");
	    	int i = 0;
	    	for(Complex c : ad.getComplexData()) {
	    		System.out.println("i: " + i + "\t" + c.absolute() + "\t" + c);
	    		i++;
	    	}
	    	
	    	Double[] fftAbsolute = ad.getFftAbsolute();
	    	for(i = 0; i < fftAbsolute.length; i++) {
	    		System.out.println(ps.computeFrequency(i) + " " + fftAbsolute[i]);
	    	}
    	}
    }
 */

/**
 * FFT and absolute
 *
private void fftAbsolute() {
	Complex[] complexData = data.getComplexData();
	Double[] fftData = new Double[complexData.length];
	int fftIndex = 0;
	int fftLength = data.getFftLength();
	for(int i = 0; i < complexData.length; i+= fftLength) {
		Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
		FFT.fftForward(toFft);
		
		// TODO maybe store toFfft in a big array if needed for another algorithm.
		for(int j = 0; j < fftLength; j++) {
			fftData[i+j] = toFft[j].absolute();
		}
	}
	data.setFftAbsolute(fftData);
}

private void fft() {
	Complex[] complexData = data.getComplexData();
	Complex[] fftData = new Complex[complexData.length];
	int fftLength = data.getFftLength();
	for(int i = 0; i < complexData.length; i+= fftLength) {
		Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
		FFT.fftForward(toFft);
		
		// TODO maybe store toFfft in a big array if needed for another algorithm.
		for(int j = 0; j < fftLength; j++) {
			fftData[i+j] = toFft[j];
		}
	}
	data.setFft(fftData);
}
*/

/*
private void setNotenames() {
	Double[] frequencies = data.getFrequencies();
	String[] noteNames = new String[frequencies.length];
	for(int i = 0; i < frequencies.length; i++) {
		noteNames[i] = FrequencyToNote.findNote(frequencies[i]);
	}
	data.setNoteNames(noteNames);
}

private void setNormalizedFrequencies() {
	Double[] frequencies = data.getFrequencies();
	Double[] normalizedFrequencies = new Double[frequencies.length];
	for(int i = 0; i < frequencies.length; i++) {
		normalizedFrequencies[i] = FrequencyToNote.findFrequency(frequencies[i]);
	}
	data.setNormalizedFrequencies(normalizedFrequencies);
}
*/

/**
public static Double findMax(Double[] absolutes, int fftLength, AudioData data) {
	double max = 0;
	int maxIndex = -1;
	int halfFFT = fftLength / 2;
	//int halfFFT = fftLength;
	for(int i = 0; i < halfFFT /*fftLength*; i++) {
		if(absolutes[i] > max) {
			max = absolutes[i];
			maxIndex = i;
		}
	}
	return computeFrequency(maxIndex % fftLength, data);
}
*/

//I am sure this is garbage
/*
     private void setFrequencies() {
    	Double[] absolute = null;
    	if((absolute = data.getFftAbsolute()) == null &&
    	   (absolute = data.getFftCepstrum()) == null &&
    	   (absolute = data.getAutoCorrelationAbsolute()) == null)
    	{
    		System.err.println("Absolute data is null");
			System.exit(0);
    	}
    	
    	int fftLength = data.getFftLength();
    	int frequencyLength = absolute.length / fftLength;
    	
    	System.out.println("absolute.length: " + absolute.length + " fftLength:" + fftLength + " fL: " + frequencyLength);
    	
    	if(frequencyLength * fftLength != absolute.length) {
    		System.out.println("The array is not divisible by the fftLength");
    		System.exit(0);
    	}
    	Double[] frequencies = new Double[frequencyLength];
    	
    	int freqIndex = 0;
    	for(int i = 0; i < absolute.length; i+= fftLength) {
    		frequencies[freqIndex] = findMax(absolute, i, fftLength);
    		freqIndex++;
    	}
    	
    	data.setFrequencies(frequencies);
    }
 */
