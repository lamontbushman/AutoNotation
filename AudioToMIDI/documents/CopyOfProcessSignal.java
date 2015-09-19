package lbushman.audioToMIDI.processing;

import lbushman.audioToMIDI.io.CaptureAudio;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;


public class CopyofProcessSignal {
	private AudioData data;
	
	public CopyofProcessSignal(AudioData audioData, double overlapPercentage, int fftLength) {
		data = audioData;
		data.setOverlapPercentage(overlapPercentage);
		data.setFftLength(fftLength);
	}
	
    public double computeFrequency(int bin) {
    	//(bin_id * sampleRate/2) / (n/2)
		//frequency = j * sampleRate/n
    	//return bin * 16384/2048;
    	/*return bin * data.getFormat().getSampleRate() / 
    			data.getFftLength();*/
    	return computeFrequency(bin, data);
    }
    
    public static double computeFrequency(int bin, AudioData data) {
    	return bin * data.getFormat().getSampleRate() / 
    			(data.getFftLength() * 2);
    	//TODO remove * 2 later on after fixing everything
    }
    
    public void process() {
    	computeComplexAndOverlap(false/*doHann*/);
    	//testInverseFft();
    	//fftAbsolute();
    	//fftCepstrum();
    	autoCorrelation();
    //	setFrequencies();
   // 	setNotenames();
    //	setNormalizedFrequencies();
    }
    
    private void setFrequencies() {
    	Double[] absolute = null;
    	if((absolute = data.getFftAbsolute()) == null &&
    	   (absolute = data.getFftCepstrum()) == null &&
    	   (absolute = data.getAutoCorrelationAbsolute()) == null)
    	{
    		System.err.println("Absolute data is null");
			System.exit(0);
    	}
    	
/*
  		Double[] absolute = data.getFftAbsolute();   	
  		if(absolute == null) {
    		absolute = data.getFftCepstrum();
    		if(absolute == null) {
    			absolute = data.getAutoCorrelationAbsolute();
    			if(absolute == null) {
    				System.err.println("FFT Absolute is null");
    				System.exit(0);
    			}   			
    		}
    	}
*/
    	
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
    
    private Double findMax(Double[] absolutes, int index, int fftLength) {
    	double max = 0;
    	int maxIndex = -1;
    	int end = 0;
    	if(data.getAutoCorrelationAbsolute() != null) {
    		end = fftLength;
    	} else {
    		end = fftLength / 2;
    	}
    	
    	//TODO TODO TODO remove index + 1 and absolutes[index] = 0;
    	if(data.getFftCepstrum() != null) {
    		//index++;
    		
	    	absolutes[index] = 0.0;
	    	absolutes[index+1] = 0.0;
	    	absolutes[index+2] = 0.0;
	    	absolutes[index+3] = 0.0;
			absolutes[index+4] = 0.0;
			absolutes[index+5] = 0.0;
			absolutes[index+6] = 0.0;
			absolutes[index+7] = 0.0;
			absolutes[index+8] = 0.0;
			
			absolutes[index+fftLength-8] = 0.0;
			absolutes[index+fftLength-7] = 0.0;
			absolutes[index+fftLength-6] = 0.0;
			absolutes[index+fftLength-5] = 0.0;
	    	absolutes[index+fftLength-4] = 0.0;
	    	absolutes[index+fftLength-3] = 0.0;
	    	absolutes[index+fftLength-2] = 0.0;
	    	absolutes[index+fftLength-1] = 0.0; 	
    	}
    	
    	for(int i = index; i < index + end /*fftLength*/; i++) {
    		if(absolutes[i] > max) {
    			max = absolutes[i];
    			maxIndex = i;
    		}
    	}
    	return computeFrequency(maxIndex % fftLength);
    }
    
    public static Double findMax(Double[] absolutes, int fftLength, AudioData data) {
    	double max = 0;
    	int maxIndex = -1;
    	int halfFFT = fftLength / 2;
    	//int halfFFT = fftLength;
    	for(int i = 0; i < halfFFT /*fftLength*/; i++) {
    		if(absolutes[i] > max) {
    			max = absolutes[i];
    			maxIndex = i;
    		}
    	}
    	return computeFrequency(maxIndex % fftLength, data);
    }
    
    /**
     * Based on single FFT
     */
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
	
	private void computeComplexAndOverlap(boolean doHann) {
		//TODO remove hard-coded percentage
		//overlapPercentage = .5;
		int fftLength = data.getFftLength();
		double overlapPercentage = data.getOverlapPercentage();
		int[] signal = data.getOriginalSignal();
		// TODO check to see if this is a reasonable overlap
		int increment = (int) (fftLength * overlapPercentage);
		// fftLength = 9 
		// percent = .33333
		// increment = 3
		
		//Compute the length of the new data array after overlapping
/*		//tempSize = length * (1 + (1 - p))
		double temp = signal.length * (2.0 - overlapPercentage);
		int newSize = (int)temp;
		if(newSize != temp) {
			newSize += fftLength;
		}*/
		
/*		int len = signal.length;
		int delta = len - increment;
		int newSize = ((fftLength - (delta % fftLength)) % fftLength) + 
				delta + ((len / fftLength) * fftLength);
*/		

/*		int newSize = (int) (Math.floorDiv(len, fftLength) * fftLength +
				Math.ceil((len - increment) / fftLength) * fftLength);
*/


//		System.out.println(Math.round(double)len / fftLength);
//		System.out.println((Math.round((double)len / fftLength) * fftLength));
//		System.out.println(Math.round((len - increment) / fftLength) * fftLength);
		

		
	//	System.out.println("increment:" + increment + " newSize: " + newSize + " fftLength: " + fftLength + " length: " + signal.length);
		
/*		int length = (signal.length / fftLength) * fftLength;
		
		if(length % fftLength != 0) {
			System.out.println(length + " " + fftLength);
			System.exit(0);
		}
		
		Complex[] complexData = new Complex[length];
		List<Double> weights = getHannWeights(fftLength);
		data.setDataWindowed();
		int hanIndex = 0;
		for(int i = 0; i < length; i++) {
			complexData[i] = new Complex(hann(hanIndex, signal[i], weights));
			hanIndex = (hanIndex + 1) % fftLength;
		}
*/
		
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
		
		
/*		int windowIndex = 0;
		for(int i = 0; i < signal.length - increment; i+= increment) {
			//TODO I am right here.
			for(int j = i; j < i + fftLength; j++) {
				System.out.println("w: " + windowIndex + " j:" + j);
				if(j < signal.length)
					complexData[windowIndex] = new Complex(signal[j]);
				else
					complexData[windowIndex] = new Complex(0);
				windowIndex++;
			}
		}
*/		
		data.setComplexData(complexData);
		
/*		
		//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
    	int start = signal.length / 2;
    	int end = start + 2048; //let's start with hearing at least 880HZ (well probably little under that).
    	Complex data[] = new Complex[2048];
    	int count = 0;
    	List<Double> weights = new ArrayList<Double>();
    	for(int i = 0; i < 2048; i++) {
    		double weight = 
    				Math.pow(
    	    				Math.sin((Math.PI*i) / (2048 -1)),
    	    				2);
    		weights.add(weight);
    	}
    	
    	
    	
    	for(int i = start; i < end; i++) {
    		data[count] = new Complex(weights.get(count)*signal[i]);
    		count++;
    	}
    	*/
    	//fft(data);
		
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
		
		/*Complex[] complexData = data.getComplexData();
		int fftLength = data.getFftLength();
		if(complexData != null) {
			data.setDataWindowed();
			int dataPointer = 0;
			int fftPointer = 0;
			while(dataPointer < complexData.length) {
				for(int i = 0; i < fftLength; i++) {
					complexData[count] = new Complex(weights.get(count)*signal[i]);
		    		count++;d
				}
			}
			while()
			for(int i = 0; i < complexData.length - data.getFftLength(); i+=fftLength) {
				
			}
		}*/
	}
	
/*    public void fftTest(Complex signal[]) {
    	FFT.fft(signal);
    	Double data[] = new Double[signal.length];
    	for(int i = 0; i < signal.length; i++) {
    		data[i] = signal[i].absolute();
    	}
    	updateGraph(data);
    	
		int maxI = 0;
		double max = 0;
    	for(int i = 0; i < data.length/2; i++) {
    		if(data[i] > max) {
    			max = data[i];
    			maxI = i;
    		}
    		if(data[i] > 800) {
    			System.out.println("Harmonic: " + i + " " + data[i] + " frequency: " + computeFrequency(i));
    		}
    	}
    	System.out.println("MAX: " + maxI);
    	//System.out.println(Collections.max(Arrays.asList(data)));
    	
    }*/
	
	/**
	 * FFT and absolute
	 */
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
	
/*	private void autoCorrelation() {
		Complex[] complexData = data.getComplexData();
		Double[] autoCorrelationAbsolute = new Double[complexData.length];
		int fftLength = data.getFftLength();
		double[] maximums = new double[complexData.length/fftLength];
		int prevIndex = 0;
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(int i = 0; i < complexData.length; i+= fftLength) {
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
			//Double the length for linear (instead of cyclic) autocorrelation.
			//http://stackoverflow.com/questions/3949324/calculate-autocorrelation-using-fft-in-matlab#3950398
			//The second half is null
			toFft = Arrays.copyOfRange(toFft, 0, toFft.length * 2);
			for(int j = toFft.length/2; j < toFft.length; j++) {
				toFft[j] = new Complex(0);
			}
			
//			FFT.fftForward(toFft);
			
			Vector<point> points = new Vector<point>();
			for(int j = 0; j < toFft.length; j++) {
				points.add(toFft[j].toPoint());
			}
			
			FFT.fftForward(toFft);
			dft fft = new dft(points);
			
			for(int j = 0; j < toFft.length; j++) {
				if(!toFft[j].isEqual(fft.dftPoint(j)))
					System.out.println("(FFT) These are not \"equal\": " + toFft[j] + " " + fft.dftPoint(j));
			}			

			//TODO if it doesn't work try it on their data.
			//TODO probably the problem is in the size of the sample rate.
			for(int j = 0; j < toFft.lengthfftLength; j++) {
				toFft[j] = new Complex(toFft[j].absoluteSquare());
				//The amazing thing is that these are exactly the same!!!!
				//toFft[j] = new Complex(Math.pow(toFft[j].absolute(), 2));
				//toFft[j] = Complex.mult(toFft[j], toFft[j].conjugate());
			}
			
			//fft = ifft for real numbers.
			FFT.fftForward(toFft);
			
			Complex[] copy = Arrays.copyOfRange(toFft, 0, toFft.length);
			Vector<Complex> vfft = new Vector<Complex>(Arrays.asList(copy));
			FFT.fftInverse(toFft);
			idft inv = new idft(new Vector<Complex>(vfft));
			
			for(int j = 0; j < toFft.length; j++) {
				System.out.println(toFft[j] + " " + inv.iDftPoint(j));
			}
			
			
	    	double max = -1;
	    	int maxIndex = -1;
	    	int fudge = toFft.length/4 - 2048; // ignore DC 
	    	int end = toFft.length/2 - 2048- fudge;
	    	
	    	for(int j = fudge; j <  end fftLength; j++) {
	    		double test = toFft[j].absolute();
	    		if(test > max) {
	    			max = test;
	    			maxIndex = j;
	    		}
	    	}
	    	maximums[i/fftLength] = max;
	    	if(maxIndex == fudge || maxIndex == end)
	    		System.out.println("ALERT!!! At the beg/end: " + maxIndex);
	    	else if(maxIndex != 4949)
	    		System.out.println("!!!!!!!Max Index: " + maxIndex + "  " + toFft[maxIndex] + " " + toFft[maxIndex].absolute());
	    	else if(maxIndex > 0)
	    		System.out.println("Max Index: " + maxIndex + "  " + toFft[maxIndex] + " " + toFft[maxIndex].absolute());
	    	else
	    		System.out.println("ALERT!!! Max index is at zero: " + maxIndex);
			
			
		//	System.out.println("hi");
			
			
			// TODO maybe store toFfft in a big array if needed for another algorithm.
			for(int j = 0; j < fftLength; j++) {
				autoCorrelationAbsolute[i+j] = toFft[j].absolute();
			}
			
			if(indexes.size() == -1 || indexes.size() == 0 || prevIndex != indexes.get(indexes.size()-1));
				indexes.add(maxIndex);
			prevIndex = maxIndex;
		}
		
		
		
		data.setAutoCorrelationAbsolute(autoCorrelationAbsolute);
	}*/
	
	
	 // Return RC low-pass filter output samples, given input samples,
	 // time interval dt, and time constant RC
	private double[] lowPass(final double x[], double dt, double RC) {
		double[] y = new double[x.length];
		double alpha = dt / (RC + dt);
		y[0] = x[0];
		for(int i = 1; i < x.length; i++) {
//			y[i] = alpha * x[i] + (1-alpha) * y[i-1];
			y[i] = y[i - 1] + alpha * (x[i] - y[i-1]);
		}
		return y;
	}
	/*
	 function lowpass(real[0..n] x, real dt, real RC)
	   var real[0..n] y
	   var real α := dt / (RC + dt)
	   y[0] := x[0]
	   for i from 1 to n
	       y[i] := α * x[i] + (1-α) * y[i-1]
	   return y
	The loop that calculates each of the n outputs can be refactored into the equivalent:

	   for i from 1 to n
	       y[i] := y[i-1] + α * (x[i] - y[i-1])*/
	
	private void autoCorrelation() {
		Complex[] complexData = data.getComplexData();
		Double[] autoCorrelationAbsolute = new Double[complexData.length * 2];
		int fftLength = data.getFftLength();
		double[] maximums = new double[complexData.length/fftLength];
		int prevIndex = 0;
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		data.setFftAbsolute(new Double[complexData.length * 2]);
		Double[] fftAbsolute = data.getFftAbsolute();
		for(int i = 0; i < complexData.length; i+= fftLength) {
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
			
			//Double the length for linear (instead of cyclic) autocorrelation.
			toFft = Arrays.copyOfRange(toFft, 0, toFft.length * 2);
			
			//Set the second half to zero
			for(int j = toFft.length/2; j < toFft.length; j++) {
				toFft[j] = new Complex(0);
			}
			
			FFT.fftForward(toFft);
			
			//Low pass filter test
			double[] lp = new double[toFft.length];
			for(int j = 0; j < toFft.length; j++) {	
				lp[j] = toFft[j].absolute();
			}
			
			lp = lowPass(lp, 5, 20);//5,20 //1,2
			
			for(int j = 0; j < toFft.length; j++) {	
				fftAbsolute[(i*2)+j] = lp[j];
			}
/*			
			//TODO uncomment if filter is removed
			for(int j = 0; j < toFft.length; j++) {	
				fftAbsolute[(i*2)+j] = toFft[j].absolute();
			}
*/			
			
			//TODO probably the problem is in the size of the sample rate.

			for(int j = 0; j < toFft.length; j++) {
				//Same as Complex.mult(toFft[j], toFft[j].conjugate()) but simpler
				toFft[j] = new Complex(toFft[j].absoluteSquare());
			//	toFft[j] = new Complex(toFft[j].absoluteSquare());//Somewhere they said to do it twice.
			}
			
			//fft = ifft for real numbers.
			FFT.fftForward(toFft);
			
			// TODO maybe store toFfft in a big array if needed for another algorithm.
			for(int j = 0; j < fftLength * 2; j++) {
				autoCorrelationAbsolute[(i*2)+j] = toFft[j].absolute();
			}


			
/*	    	double max = -1;
	    	int maxIndex = -1;
	    	int fudge = toFft.length/4 - 2048; // ignore DC 
	    	int end = toFft.length/2 - 2048- fudge;
	    	
	    	for(int j = fudge; j <  end fftLength; j++) {
	    		double test = toFft[j].absolute();
	    		if(test > max) {
	    			max = test;
	    			maxIndex = j;
	    		}
	    	}
	    	maximums[i/fftLength] = max;
	    	//4949
	    	//System.out.println("!!!!!!!Max Index: " + maxIndex + "  " + toFft[maxIndex] + " " + toFft[maxIndex].absolute());

	    	if(maxIndex == fudge || maxIndex == end)
	    		System.out.println("ALERT!!! At the beg/end: " + maxIndex);
	    		
			if(indexes.size() == -1 || indexes.size() == 0 || prevIndex != indexes.get(indexes.size()-1));
				indexes.add(maxIndex);
			prevIndex = maxIndex;

*/			
		}
		
		
		
		data.setAutoCorrelationAbsolute(autoCorrelationAbsolute);
	}
	
	private void testInverseFft() {
		Complex[] complexData = data.getComplexData();
		Double[] fftData = new Double[complexData.length];
		int fftLength = data.getFftLength();
		
		List<Double> weights = getHannWeights(fftLength);
		for(int i = 0; i < complexData.length; i+= fftLength) {
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
			FFT.fftForward(toFft);
			
			for(int j = 0; j < fftLength; j++) {
				toFft[j] = new Complex(hann(j, toFft[j].absolute(), weights));
			}
			FFT.fftInverse(toFft);
			
			// TODO maybe store toFfft in a big array if needed for another algorithm.
			for(int j = 0; j < fftLength; j++) {
				fftData[i+j] = toFft[j].absolute();
			}
		}
		data.setFftInverseTest(fftData);
	}
	
	private void fftCepstrum() {
		Complex[] complexData = data.getComplexData();
		Double[] fftData = new Double[complexData.length];
		int fftLength = data.getFftLength();
		List<Double> weights = getHannWeights(fftLength);
		for(int i = 0; i < complexData.length; i+= fftLength) {
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
			FFT.fftForward(toFft);
			
			// TODO maybe store toFfft in a big array if needed for another algorithm.
			for(int j = 0; j < fftLength; j++) {
				toFft[j] = 
				new Complex(Math.log(toFft[j].absoluteSquare()));
//				new Complex(hann(j, Math.log(toFft[j].absoluteSquare()), weights));
			}
			
			FFT.fftInverse(toFft);
			for(int j = 0; j < fftLength; j++) {
				fftData[i+j] = toFft[j].absoluteSquare();
			}
		}
		data.setFftCepstrum(fftData);
	}
    
    public static void main(String args[]) {
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
	    	//ps.computeComplexAndOverlap(/*.50, 4*/);
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

    	System.exit(0);
    	
    	Complex[] c = new Complex[4];
    	Complex c1 = new Complex(1);
    	c[0] = c1;
    	c[1] = c1;
    	c[0].add(new Complex(2));
    	System.out.println(c[0]);
    	System.out.println(c[1]);
    	//c[0] == c[1]
    }
}

