package lbushman.audioToMIDI.processing;

import lbushman.audioToMIDI.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class FundamentalFrequency extends Thread {
	
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
	
	@Override
	public void run() {
		computeFrequencies();
		computeNormalizedFrequencies();
	}
	
	private void computeNormalizedFrequencies() {
		//TODO move to function or another appropriate place.
		List<Double> normalized = new ArrayList<Double>();
		for(Double d : data.getFrequencies()) {
			Double dd = FrequencyToNote.findFrequency(d);
			normalized.add(dd);
		}
		data.setNormalizedFrequencies(normalized);
	}
	
	/**
	 * Computes the onsets. Sets both the onsets and the spectral flux to audioData.
	 */
	private void computeFrequencies() {
		int base = 0;
		int fftLength = data.getFftLength();
		int halfFFTLength = fftLength / 2;
		List<Double> frequencies = new ArrayList<Double>(data.getNumFFT());
		
		int i = 0;
		while(base + fftLength <= ffts.size()) {
			List<Double> halfFFT = ffts.subList(base, base + halfFFTLength);
			base += fftLength;
			Util.println("Computing frequency: " + i );
			frequencies.add(computeFrequency(halfFFT));
			i++;
		}
		
		
/*		System.out.println("percentHeight");
		for(Double d : percentages) {
			System.out.println(d);
		}
		System.out.println("percentHeightEND");
		
		System.out.println("percentHeight2");
		for(Double d : percentages2) {
			System.out.println(d);
		}
		System.out.println("percentHeightEND2");*/
		
		
		data.setFrequencies(frequencies);
		
		
/*		for(int i = 0; i < frequencies.size(); i++) {
			Double frequency = frequencies.get(i);
			String note = FrequencyToNote.findNote(frequency);
			System.out.println(i + " " + frequency + " " + note);
		}*/
	}
	
	/*List<Double> percentages = new LinkedList<Double>();
	List<Double> percentages2 = new LinkedList<Double>();*/
	int myCount = 0;
	public Double computeFrequency(List<Double> halfFFT) {
		
    	int highestPeak = Util.maxIndex(halfFFT, 0, halfFFT.size());
    	
   /* 	if(true)
    		return computeFrequency(highestPeak);*/
    	
    	//Low pass filter.
/*    	if(highestPeak < 24)
    		return 0.0;
*/    	
    	
    	int half = highestPeak / 2;
    	int peak  = findPeak(halfFFT, half, 3);
    	
/*    	double greater = (half >= peak)? half : peak;//divide by double
		int lesser = (half < peak)? half : peak;
		double error = (Math.abs(greater - lesser) / greater);
		if(error >= 0.08)//.036
			peak = highestPeak;*/
//   	System.out.println("error[" + myCount + "]: " + error);
    	
    	double percentHeight = halfFFT.get(peak) / halfFFT.get(highestPeak);
    	//percentages.add(percentHeight);
    	
    	/*if(percentHeight == 1) {
    		return 0.0;
    	}*/
    	
    	if(!(percentHeight > 0.10 && percentHeight < 1.0)) {//.25 is good
    		peak = highestPeak;
    	}
    	
    	//If highestPeak is the base harmonic, 
    	// make it more accurate by finding the difference between the
    	// base and the next (if it exists)
    	if(peak == highestPeak) {
    		int secondPeakGuess = half + highestPeak;
    		if(secondPeakGuess < halfFFT.size()) {
	        	int secondPeak  = findPeak(halfFFT, secondPeakGuess, 3);
	        	double percentHeight2 = halfFFT.get(secondPeak) / halfFFT.get(highestPeak);
	        	if(percentHeight2 > 0.10 && percentHeight2 < 1.0) {
	        		peak = Math.abs(secondPeak - highestPeak);
	        	}
    		}
    	}
    	myCount++;
    	
    	/*
    	int second = half + highestPeak;
    	int peak2  = findPeak(halfFFT, second, 3);
    	double percentHeight2 = halfFFT.get(peak2) / halfFFT.get(highestPeak);
    	percentages2.add(percentHeight2);
    	*/

/*    	if(percentHeight2 > 0.20 && percentHeight < 1.0) {
    		peak = highestPeak / 2;
    	}
*/    	
    	
    		
    	
    	//System.out.println("percentHeight[" + myCount + "]: " + percentHeight);

    	
    /*	double greater = (half >= peak)? half : peak;//divide by double
		int lesser = (half < peak)? half : peak;
		double error = (Math.abs(greater - lesser) / greater);
		if(error >= 0.08)//.036
			peak = highestPeak;

    	myCount++;*/
    	
	//	int index1 = computeFundamentalIndex(halfFFT, highestPeak);
    	/*int index2 = computeFundamentalIndexMyCorrelation(halfFFT, highestPeak);
    	int averageIndex = (index1 + index2) / 2;*/
		double fundFreq = computeFrequency(peak); // Adding one I think it is biasing to the left a little.
		
		//Only for debug purposes
		//printOperations(highestPeak, index, fundFreq);
		
		return fundFreq;
	}
	
	//Only for debug purposes
	private int printI = 0;
	Double[] maxAmp = null;
	private void printOperations(int highestPeak, int fundIndex, double fundamentalFrequency) {
		//Calculate the amps at time periods
		
		/*if(maxAmp == null)
			maxAmp = ProcessSignal.computeAmp(data);*/
		
		Double freqF = computeFrequency(highestPeak);
		String noteF = FrequencyToNote.findNote(freqF).toString();
		
		String baseNote = FrequencyToNote.findNote(fundamentalFrequency).toString();
			
		/*int index = findMax(Arrays.copyOfRange(acAbsolute, start, end), 10);
		double freq = ProcessSignal.computeFrequency(index, audioData);
		String note = FrequencyToNote.findNote(freq);*/
		
		Util.println("FFT  i: " + printI + "Index: " + highestPeak + " frequency: " + freqF + " note: " + noteF + " maxAmp[i]: " /*+ maxAmp[printI]*/);
    	//System.out.println("AC   i: " + i + "Index: " + index + " frequency: " + freq + " note: " + note + " maxAmp[i]: " + maxAmp[i]);
		Util.println("Base i: " + printI + "Index: " + fundIndex + " frequency: " + fundamentalFrequency + " note: " + baseNote /*+ " maxAmp[i]: " + maxAmp[printI]*/);
		Util.println("");
    	printI++;
	}
	
    private double computeFrequency(int bin) {
    	return computeFrequency(bin, data);
    }
    
    public int computeBin(double frequency) {
    	return computeBin(frequency, data);
    }
    
    public static int computeBin(double frequency, AudioData data) {
    	double bin = frequency * data.getFftLength() / data.getFormat().getSampleRate();
    	return (int) bin;
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
    
    public static double computeFrequency(int bin, double sampleRate, int fftLength) {
    	return bin * sampleRate / fftLength;
    }
    
	private static int findPeak(List<Double> signal, int currentIndex, int leftRight) {
		//TODO possibly pass in the multiply of the window length
		/*int searchLen = (int) (leftRight * 2);*/
		int begSearch = currentIndex - leftRight;
		if(begSearch < 0) {
			begSearch = 0;
		}
		int endSearch = currentIndex + leftRight;
		if(endSearch > signal.size()) {
			endSearch = signal.size();
		}
		int index = Util.lastMaxIndex(signal, begSearch, endSearch);
		return (index == -1)? -1/* endSearch or signal.size() */: index;
	}
    
	private int computeFundamentalIndexMyCorrelation(List<Double> halfFFT, int highestPeak) {
/*		if(true)
			return highestPeak;*/
		
		final int MAX_NUM_PEAKS = 8;
		int windowLength = 7;
		int leftRight = (windowLength - 1) / 2;
		
		List<Double> errors = new LinkedList<Double>();
		List<Double> sumOfHeights = new LinkedList<Double>();
		int fundamentalIndex = highestPeak;
		
		while(fundamentalIndex > 0) {
			int nextPeak = fundamentalIndex + fundamentalIndex;
			double error = -1;
			int numPeaks = 0;
			double sumOfHeight = 0;
			double offset = 1.0;
			while(nextPeak < halfFFT.size() /*&& numPeaks < MAX_NUM_PEAKS*/) {
				int peak  = findPeak(halfFFT, nextPeak, leftRight);
				double greater = (nextPeak >= peak)? nextPeak : peak;//divide by double
				int lesser = (nextPeak < peak)? nextPeak : peak;
				error += (Math.abs(greater - lesser) / greater);// * offset; /*(MAX_NUM_PEAKS - numPeaks)*/;
				/*offset -= .2;
				if(offset < 1)
					offset = 1;*/
				numPeaks++;
				
				sumOfHeight += (halfFFT.get(peak) > 200000)? halfFFT.get(peak) : 0;
				
				
				
				
				nextPeak += fundamentalIndex;
			}
			if(error != -1)
				error++;
			else
				error = Double.MAX_VALUE;
			errors.add(error/*/numPeaks*/);
			
			sumOfHeights.add(sumOfHeight);
			
			fundamentalIndex--;
		} 
	
		int index = Util.minIndex(errors, 0, errors.size());
		
		if(index != 0 ) {
			Util.printErrorln("NSSSSS: " + index);
			/*index = errors.size() + 1;*/
		}

		index = errors.size() - index;
		int beg = index/2 - leftRight;
		if (beg < 0)
			beg = 0;
		int end = index/2 + leftRight;
		if(end > errors.size())
			end = errors.size();
		
		List<Double> subErrors = errors.subList(beg, end);
		int secondIndex = Util.minIndex(subErrors, 0, subErrors.size());
		secondIndex += beg;
		ListIterator<Double> it = sumOfHeights.listIterator();
		while(it.hasNext()) {
			Util.println(/*it.nextIndex() + */  " " + it.next());
		}

	
		return index;
	}    
	
	private int computeFundamentalIndex(List<Double> halfFFT, int highestPeak) {
		List<Integer> peaks  = Peaks.findPeaks(halfFFT, highestPeak, 7, .05);
    	
		List<Integer> diffInConsecutivePeaks = new LinkedList<Integer>();
		for(int i = 0; i < peaks.size() - 1; i++) {
			diffInConsecutivePeaks.add(peaks.get(i+1) - peaks.get(i));
		}

		List<Integer> modes = Util.mode(diffInConsecutivePeaks);
		if(modes.size() > 1) {
			Util.printErrorln("You have multiple modes at an FFT");
			//System.exit(1);
		}
		
/*		if(modes.size() > 1) {
			Iterator<Integer> it = modes.iterator();
			while(it.hasNext()) {
				Integer index = it.next();
				System.out.println(index);
				Double freq = computeFrequency(index);
				index = FrequencyToNote.findIndex(freq);
				System.out.println(index);
			}
		}*/
		
		//Return the average of the modes.
		int index = (modes.size() >= 1) ? Util.round(Util.average(modes))/*modes.get(0)*/ : highestPeak/*-1*/;
		
		//Another idea is to see if there are more than 2 or 3 peaks and then choose only the highest peak.
		//Also can see if index is divisible (or closely divisible) by highestPeak.
		/*if(index != -1) {*/
			//See if the index is marginally higher than the highest peak.
			//I don't think there will be ever a case where the fundamental should be 
			// (marginally) higher than the fundamental.
			if(index > highestPeak + 4) {
				//In the case where there are two prominent peaks.
				//Hoping that the difference between the two prominent peaks will
				// do better than the highest peak.
				int count = 0;
				double total = 0;
				for(double d : diffInConsecutivePeaks) {
					if(Math.abs(highestPeak - d) / highestPeak < .03) {
						total += d;
						count++;
					}
				}
				if(count > 0)
					return (int) Math.round(total / count);
				/*if(highestPeak != 0 && Math.abs(highestPeak - diffInConsecutivePeaks.get(0)) / highestPeak < .03) {
					return diffInConsecutivePeaks.get(0);
				}*/
				return highestPeak;
			}
		/*}*/
		
		return index;
	}
	
    private int findMax(List<Double> fftData) {
    	int start = 0;
    	//Only search half of the data.
    	int end = start + (data.getFftLength() / 2);
    	return Util.maxIndex(fftData, start, end);
    }
	
    
    
    
    
    
    
    /*
     
         private double smallestCloseToDivisible(double d1, double d2) {
    	double smaller;
    	double larger;
    	if(d1 <= d2) {
    		smaller = d1;
    		larger = d2;
    	} else {
    		smaller = d2;
    		larger = d1;    		
    	}
    }
     */
    
    
    
    
    
    
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
