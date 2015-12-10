package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import lbushman.audioToMIDI.util.Util;


//TODO study modifiers in c, final, etc
/**
 * Development time:
 * 	Spectral Flux: 1.5 hours
 *  Peak Detection: 
 * @author Lamont Bushman
 *
 */
public class OnsetDetection extends Thread {	
	//TODO I don't know if this should be final. I am weak on my knowledge of final on objects.
	private final AudioData data;
	private final List<Double> ffts;
	
	/**
	 * This class will compute the onsets based off ffts. This is independent of any ffts in 
	 * audioData. This is so that multiple versions of an fft can be compared (i.e. with filtering).
	 * @param audioData
	 * @param list
	 */
	public OnsetDetection(final AudioData audioData, final List<Double> list) {
		data = audioData;
		this.ffts = list;
		if(data == null || list == null) {
			System.err.println("OnsetDetection - One or both are null, ffts : " + list + " data: " + data);
			System.exit(1);
		}
		if(data.getFftLength() < 2) {
			System.err.println("OnsetDetection - fftLength is below expected value : " + data.getFftLength());
			System.exit(1);
		}
	}
	
	@Override public void run() {
		CountDownLatch latch = new CountDownLatch(1);
		new Thread() {
			@Override
			public void run() {
				computeAmp(latch);
			}
		}.start();
		
		computeOnsets();
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		validateOnsets(data.getAmp());
	}
	
	public void computeAmp(CountDownLatch latch) {
		computeAmp(data);
		latch.countDown();
	}
	
	public static void computeAmp(AudioData data) {
		List<Double> overlapData = data.getOverlappedData();
		
		Double[] maxAmps = new Double[data.getNumFFT()/*fftAbsolute.length*/];
		
		 //TODO this still needs to be fixed so, I don't have to divide by 2.
		int fftLength = data.getFftLength() /*/ 2*/ ;
		
		
		double sum = 0;
		int maxI = 0;
		for(int i = 0; i < overlapData.size(); i+= fftLength) {
			//if(overlapData.get(i+512) < 0);
				//System.out.println(overlapData.get(i+512));
			List<Double> sub = overlapData.subList(i, i + fftLength);
			double avg = Util.averageD(sub);
			maxAmps[maxI] = avg;
//			System.out.println("size: " + sub.size() + " " + avg + " " + i);
			maxI++;
			
/*			sum = 0;
			for(int j = 0; j < fftLength; j++)
				sum += overlapData.get(i+j);
			maxAmps[maxI] = sum/fftLength;
			maxI++;*/
		}
		data.setAmp(maxAmps);
	}
	
	/**
	 * Computes the onsets. Sets both the onsets and the spectral flux to audioData.
	 */
	public void computeOnsets() {
		List<Double> flux = spectralFlux();
		data.setSpecralFlux(flux);
		
		List<Integer> peaks = new LinkedList<Integer>();
		// TODO find peaks and add to peaks.
		
		//double[] array = flux.stream().mapToDouble(d -> d).toArray();

		//TODO Do I want this to modify data's spectral flux. It is not doing it here, I think.
		
/*		ListIterator<Double> it = flux.listIterator();
		while(it.hasNext()) {
			Double next = it.next();
			if(next < 0.1E7 1.9E7 2.5E7)
				it.set(0.0);
		}*/
		
		//flux = ProcessSignal.lowPass(flux,200,10);
		//flux = ProcessSignal.lowPass(flux,5,22);
		//flux = ProcessSignal.lowPass(flux);
		
		data.setSpecralFlux(flux);
		
		peaks = Peaks.findPeaks(flux, 0, 3, .05);
		
		peaks = RiseAndFallPeaks.findPeaks(flux);
		
		data.setOnsets(peaks);
	}
	
	private List<Double> spectralFlux() {
		int base = 0;
//		int n = 0;
		int fftLength = data.getFftLength();
		List<Double> flux = new ArrayList<Double>(data.getNumFFT());

		while(base + fftLength < ffts.size()) {
			List<Double> fft1 = ffts.subList(base, base + fftLength);
			base += fftLength;
			List<Double> fft2 = ffts.subList(base, base + fftLength);
			base += fftLength;
			flux.add(spectralFlux(fft1, fft2));//n
			//n++;
		}
//		return flux;
		return filterFlux(flux);
	}
	
	private List<Double> filterFlux(List<Double> flux) {
		//search around window * 2 + 1
		final int WINDOW_SIZE = 4;
		final double MULTIPLIER = 1.5;
		//5,1.5 works but it is cutting it close I think
		
		//6,1.6
		
		//4,1.5 with     0.25/*overlap of FFTs*/, 2048 /*original fftLength */); //8192
		
		//6,3.5 with     0.10/*overlap of FFTs*/, 2048 /*original fftLength */); //8192
		
		List<Double> thresholds = new LinkedList<Double>();
		ListIterator<Double> it = flux.listIterator();
		
		while(it.hasNext()) {
			int start = Math.max(0, it.nextIndex() - WINDOW_SIZE);
			int end = Math.min(flux.size() /*- 1*/, it.nextIndex() + WINDOW_SIZE + 1);
			
			List<Double> subList = flux.subList(start, end);
			ListIterator<Double> itSub = subList.listIterator();
			double mean = 0;
			while(itSub.hasNext()) {
				mean += itSub.next();
			}
			mean /= (end - start);
			thresholds.add(mean * MULTIPLIER);
			it.next();
		}
	
		ListIterator<Double> tIt = thresholds.listIterator();
		it = flux.listIterator();
		while(tIt.hasNext()) {
			Double threshold = tIt.next();
			Double value = it.next();
			if(threshold <= value) {
				it.set(value - threshold);
			} else {
				it.set(0.0);
			}
		}
		
		return flux;
	}
		
	/**
	 * @param fft1
	 * @param fft2
	 * @return
	 */
	private double spectralFlux(final List<Double> fft1, final List<Double> fft2) {
		assertFFTs(fft1, fft2);
		
		double positiveFlux = 0;
		Iterator<Double> it1 = fft1.iterator();
		Iterator<Double> it2 = fft2.iterator();
		
		while(it1.hasNext()) {
			double diff = it2.next().doubleValue() - it1.next().doubleValue(); 
			positiveFlux += (diff > 0)? diff : 0;  
		}
		
		return positiveFlux;
	}
	
	private void assertFFTs(final List<Double> fft1, final List<Double> fft2) {
		if(fft1.size() != fft2.size()) {
			System.err.println("Specral Flux- fft1 and fft2 are not the same size: " + fft1.size() + " " + fft2.size());
			System.exit(1);
		} else if(fft1.size() < 2) {
			System.err.println("Specral Flux- unexpected size of ffts: " + fft1.size());
			System.exit(1);
		}
	}

	public void validateOnsets(Double amps[]) {		
		List<Integer> onsets = data.getOnsets();
		
		//Make sure the first onset is loud enough compared to the second onset.
		//This assumes that the second is a real onset.
		//This requires that the first note to be no less than 1/10 as loud as the second.
		if(onsets.size() > 1) {
			Integer first = onsets.get(0);
			double percentage = (amps[first*2] / amps[onsets.get(1)*2]);
			if(percentage < 1/10.0) {	
				System.err.println("!!! Removed first onset: " + first + " " + amps[first*2 + 2]);
				onsets.remove(0);
			}

		}
		
		ListIterator<Integer> it = onsets.listIterator();
		Integer i = null;
		
		double mean = 0;
		int position = 0;
		while(it.hasNext()) {
			position = it.next();
			System.err.println("onset: " + position + " " + amps[position * 2 + 2]);
			mean += amps[position * 2 + 2];
		}
		mean /= onsets.size();
		
		it = onsets.listIterator();
		while(it.hasNext()) {
			position = it.next();
			if(amps[position * 2 + 2] / mean < 0.10) {
				System.err.println("Removed onset: " + position + " " + amps[position * 2 + 2] / mean + " mean: " + mean);
				it.remove();
			}
		}
	
		
/*		while(it.hasNext()) {
			i = it.next();
			// * 2 because of onsets are the comparison between all FFT's.
			// amps is the amp for each FFT.
			// TODO average of surrounding points might be accurate. Hopefully, this is good
			// enough.
			System.err.println("!!! Want to removed onset: " + i + " " + amps[i*2 + 2]);
			if((i*2 + 2) < amps.length && amps[i*2 + 2] < 200) {
				System.err.println("!!! Want to removed onset: " + i + " " + amps[i*2 + 2]);
				//it.remove();
			}
		}*/
		
		
		
		
		
	}
}
