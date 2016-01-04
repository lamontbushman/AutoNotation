package lbushman.audioToMIDI;

import lbushman.audioToMIDI.processing.Complex;
import lbushman.audioToMIDI.processing.IOQueue;
import java.util.*;

public class TestOverlap {
	public static List<Double> overlap(double[] signal, double overlapPercentage, int fftLength) {
		int increment = (int) (fftLength * overlapPercentage);
		System.out.println(increment + " " + overlapPercentage + " " +  fftLength);
		//Rough estimate of new size.
		//List<Complex> complexData = new ArrayList<Complex>((int) ((1/overlapPercentage) * signal.length));
		//List<Double> overlapedData = new ArrayList<Double>();
		List<Double> overlappedData = new ArrayList<Double>();
		
		double value = 0;
		
		for(int i = 0; i <= signal.length - increment; i+= increment) {
			// System.out.println(i + " " + signal.length);
			
			for(int j = i; j < i + fftLength; j++) {
				if(j < signal.length)
					value = signal[j];
				else
					value = 0;
				overlappedData.add(value);
			}
		}
		return overlappedData;
	}
	
	public static int findOriginalIndex(int index, int fftLength, double overlapPercentage) {
		
		// The number of indexes that have at least one index of an original FFT length.
		int wholeFinished = (int) Math.floor(fftLength / overlapPercentage);
		
		int overlapIncrement  = (int) (fftLength * overlapPercentage);
		
		// The index into an original FFT length sized portion
		int baseIndex = index / wholeFinished;
		baseIndex = baseIndex * fftLength;
		
		int remainder = index % wholeFinished;
		
		int overlapBase = remainder / fftLength;
		overlapBase = overlapBase * overlapIncrement;
		
		int overlapIndex = remainder % fftLength;
		
		return baseIndex + overlapBase + overlapIndex;
	}
	
	public static void main(String args[]) {
		double[] signal = new double[64];
		List<Double> reconstruct = new ArrayList<Double>();
		for(int i = 0; i < signal.length; i++) {
			signal[i] = i + 1;
		}
		for(int i = 0; i < signal.length; i++) {
			System.out.print(signal[i] + ", ");
		}
		System.out.println();
		double overlapPercentage = 0.25;
		int fftLength = 8;
		
		List<Double> list = TestOverlap.overlap(signal, overlapPercentage, fftLength);
		System.out.println(list);

		
		List<Double> intList = new ArrayList<Double>();
		for(int i = 0; i < list.size(); i++) {
			intList.add((double) i);
		}
		System.out.println(intList);
		
		
		Scanner in = new Scanner(System.in);
		int num;
		while((num = in.nextInt()) != -1) {
			System.out.println(findOriginalIndex(num, fftLength, overlapPercentage));
		}
		in.close();
		
		/*
		for(int i = 0; i < list.size(); i++) {
			int index = findOriginalIndex(i, fftLength, overlapPercentage);
			System.out.print((double)index + ", ");
			if (index < signal.length)
				reconstruct.add(signal[index]);
			else
				reconstruct.add(0.0);
		}
		System.out.println();
		System.out.println(reconstruct);
		*/
	}
}
