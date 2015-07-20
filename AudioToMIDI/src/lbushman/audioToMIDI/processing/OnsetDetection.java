package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//TODO study modifiers in c, final, etc
/**
 * Development time:
 * 	Spectral Flux: 1.5 hours
 *  Peak Detection: 
 * @author Lamont Bushman
 *
 */
public class OnsetDetection {	
	//TODO I don't know if this should be final. I am weak on my knowledge of final on objects.
	private final AudioData data;
	private final List<Number> ffts;
	
	/**
	 * This class will compute the onsets based off ffts. This is independent of any ffts in 
	 * audioData. This is so that multiple versions of an fft can be compared (i.e. with filtering).
	 * @param audioData
	 * @param ffts
	 */
	public OnsetDetection(final AudioData audioData, final List<Number> ffts) {
		data = audioData;
		this.ffts = ffts;
		if(data == null || ffts == null) {
			System.err.println("OnsetDetection - One or both are null, ffts : " + ffts + " data: " + data);
			System.exit(1);
		}
		if(data.getFftLength() < 2) {
			System.err.println("OnsetDetection - fftLength is below expected value : " + data.getFftLength());
			System.exit(1);
		}
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
		flux = ProcessSignal.lowPass(flux);
		data.setSpecralFlux(flux);
		
		peaks = Peaks.findPeaks(flux, 0, 7, .05);
		
		data.setOnsets(peaks);
	}
	
	private List<Double> spectralFlux() {
		int base = 0;
//		int n = 0;
		int fftLength = data.getFftLength();
		List<Double> flux = new ArrayList<Double>(data.getNumFFT());

		while(base + fftLength < ffts.size()) {
			List<Number> fft1 = ffts.subList(base, base + fftLength);
			base += fftLength;
			List<Number> fft2 = ffts.subList(base, base + fftLength);
			base += fftLength;
			flux.add(spectralFlux(fft1, fft2));//n
			//n++;
		}
		
		return flux;
	}
		
	/**
	 * @param fft1
	 * @param fft2
	 * @return
	 */
	private double spectralFlux(final List<Number> fft1, final List<Number> fft2) {
		assertFFTs(fft1, fft2);
		
		double positiveFlux = 0;
		Iterator<Number> it1 = fft1.iterator();
		Iterator<Number> it2 = fft2.iterator();
		
		while(it1.hasNext()) {
			double diff = it2.next().doubleValue() - it1.next().doubleValue(); 
			positiveFlux += (diff > 0)? diff : 0;  
		}
		
		return positiveFlux;
	}
	
	private void assertFFTs(final List<Number> fft1, final List<Number> fft2) {
		if(fft1.size() != fft2.size()) {
			System.err.println("Specral Flux- fft1 and fft2 are not the same size: " + fft1.size() + " " + fft2.size());
			System.exit(1);
		} else if(fft1.size() < 2) {
			System.err.println("Specral Flux- unexpected size of ffts: " + fft1.size());
			System.exit(1);
		}
	}
}
