import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;


public class ProcessSignal {
	AudioData data;
	
	public ProcessSignal(AudioData audioData) {
		data = audioData;
	}
	
    private double computeFrequency(int bin) {
    	//(bin_id * sampleRate/2) / (n/2)
		//frequency = j * sampleRate/n
    	//return bin * 16384/2048;
    	return bin * data.getFormat().getSampleRate() / 
    			data.getFftLength();
    }
	
	public void computeComplexAndOverlap(double overlapPercentage, int fftLength) {
		//TODO remove hard-coded percentage
		overlapPercentage = .5;
		data.setFftLength(fftLength);
		data.setOverlapPercentage(overlapPercentage);
		int[] signal = data.getOriginalSignal();
		// TODO check to see if this is a reasonable overlap
		int increment = (int) (fftLength * overlapPercentage);
		// fftLength = 9 
		// percent = .33333
		// increment = 3
		
		//Compute the length of the new data array after overlapping
		//tempSize = length * (1 + (1 - p))
		double temp = signal.length * (2.0 - overlapPercentage);
		int newSize = (int)temp;
		if(newSize != temp) {
			newSize += fftLength;
		}
		Complex[] complexData = new Complex[newSize];
		
		int windowIndex = 0;
		for(int i = 0; i < signal.length - increment; i+= increment) {
			//TODO I am right here.
		}
		
		
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
    	//fft(data);
		
	}
	
	public void hannWindow() {
		
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
    
    public static void main(String args[]) {
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
