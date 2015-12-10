package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.loading.MLet;

import lbushman.audioToMIDI.util.Util;

public class FFT {
	final static double LOG2 = Math.log(2);
		
	public static void fftForward(Complex input[]) {
		fftOld(input, 1);
	}
	
	public static void fftInverse(Complex input[]) {
		fftOld(input, -1);
	}
	
	/**
	 * Doing this to speed up FFT
	 * @param input
	 */
	private static int numBits = 0;
	private static int length;
	public static void setNumBits(int length) {
		FFT.length = length;
		Util.timeDiff("LOG");
		int n = length;
		numBits = (int) (Math.log(n) / LOG2);
		
		if (Math.pow(2,numBits) != n) {
			System.err.println("Length for DFT must be a power of 2.\n"
					+ "Length: " + n + " numBits: " + numBits);
			System.exit(0);
		}
		Util.timeDiff("LOG");
	}
		
	private static int fftCount = 0;

	private static void fftOld(Complex input[], int direction) {
		int n = input.length;
		Util.verify(n == length, "Input length must be the same size as the class was constructed. n: " + n + " numBits: " + numBits);
numBits = (int) (Math.log(n) / LOG2);
		
		if (Math.pow(2,numBits) != n) {
			System.err.println("Length for DFT must be a power of 2.\n"
					+ "Length: " + n + " numBits: " + numBits);
			System.exit(0);
		}
		
		revBinaryPermuteOld(input);
		
		for(int ldm = 1; ldm <= numBits; ldm++) {
			int m = (int) Math.pow(2,ldm);
			int mh = m/2;
			
			//TODO make sure <= not <
			for(int r = 0; r <= n - m; r+=m) { // n/m iterations
				for(int j = 0; j < mh; j++) { // m/2 iterations
					//Util.totalTimeDiff("EXPO");			
					Complex e = Complex.expI(direction * 2.0*Math.PI*j/m);//check this
					//Util.totalTimeDiff("EXPO");
					Complex u = input[r + j];
					Complex v = Complex.mult(input[r + j + mh], e);
					input[r + j] = Complex.add(u,v);
					input[r + j + mh] = Complex.subt(u,v); //subtract wasn't tested
				}
			}
			//System.out.println((fftCount++) + "Inner for count: " +  count);
		}
	}
	
	private final int mNumBits;
	private final int mLength;
	private static final double TWO_PI = 2.0*Math.PI;
	private Complex[][] fftExpITable;
	private static FFT fft;
	private FFT(int length) {
		mLength = length;
		// precompute data for all ffts of given length 
		
		mNumBits = (int) (Math.log(length) / LOG2);
		
		if (Math.pow(2,numBits) != length) {
			System.err.println("Length for DFT must be a power of 2.\n"
					+ "Length: " + length + " mNumBits: " + mNumBits);
			System.exit(0);
		}
		
		fftExpITable = new Complex[mNumBits][];
		int mh = 1;
		int m = 2;
		for(int ldm = 0; ldm < mNumBits; ldm++) {
			fftExpITable[ldm] = new Complex[mh];
			for(int j = 0; j < mh; j++) {
				fftExpITable[ldm][j] = Complex.expI(TWO_PI*j/m);
			}
			m *= 2;
			mh *= 2;
		}
	}
	
	public static FFT getInstance(int length) {
		if(FFT.fft != null && FFT.fft.mLength == length) {
			return FFT.fft;
		} else {
			FFT fft = new FFT(length);
			FFT.fft = fft;
			return fft;
		}
	}
	
	
	public void fft(Complex input[]) {
		int n = input.length;
		//Util.totalTimeDiff("NOTHING");
		Util.verify(n == mLength, "Input length must be the same size as the class was constructed.");
		//Util.totalTimeDiff("NOTHING");
		//Util.totalTimeDiff("REV");
		revBinaryPermute(input);
		//Util.totalTimeDiff("REV");
		
		//Util.totalTimeDiff("NOTHING");
		//Util.totalTimeDiff("NOTHING");
		
		Complex[] subExpI;

		int mh = 1;
		int m = mh * 2;
		for(int ldm = 0; ldm < mNumBits; ldm++) {
			subExpI = fftExpITable[ldm];
			for(int r = 0; r <= n - m; r+=m) { // n/m iterations
				for(int j = 0; j < mh; j++) { // m/2 iterations
					Complex e = subExpI[j]; // Complex.expI(TWO_PI*j/m);//check this
					Complex u = input[r + j];
					Complex v = Complex.mult(input[r + j + mh], e);
					input[r + j] = Complex.add(u,v);
					input[r + j + mh] = Complex.subt(u,v); //subtract wasn't tested
				}
			}
			m *= 2;
			mh *= 2;
		}
	}
		
	public void revBinaryPermute(Complex input[]) {
		for(int i = 0; i < input.length; i++) {
			int r = revbin(i,input.length);
			if(r > i) {
				Complex temp = input[i];
				input[i] = input[r];
				input[r] = temp;
			}
		}
	}
	
	public int revbin(int toReverse, int dataLength) {
		int reverse = 0;
		int numBits = mNumBits;
		// int numBits = (int) (Math.log(dataLength) / LOG2);
		while(numBits > 0) {
			reverse <<= 1;
			reverse += toReverse & 1;
			toReverse >>= 1;
			numBits--;
		}
		return reverse;
	}
	
	public static void revBinaryPermuteOld(Complex input[]) {
		for(int i = 0; i < input.length; i++) {
			int r = revbinOld(i,input.length);
			if(r > i) {
				Complex temp = input[i];
				input[i] = input[r];
				input[r] = temp;
			}
		}
	}

	public static int revbinOld(int toReverse, int dataLength) {
		int reverse = 0;
		//int numBits = FFT.numBits;
		int numBits = (int) (Math.log(dataLength) / LOG2);
		while(numBits > 0) {
			reverse <<= 1;
			reverse += toReverse & 1;
			toReverse >>= 1;
			numBits--;
		}
		return reverse;
	}
	
	public static void main(String args[]) {
		Integer values[] = new Integer[]{62, 76, 90, 103, 115, 116, 129, 143, 170, 184, 198, 225, 238, 251, 280, 291, 304, 316, 327, 328, 340, 354, 366, 378, 390, 402, 414, 426};
		List<Integer> differences = new ArrayList<Integer>(values.length / 2);
		
		//List<Integer> newValues = Arrays.asList(62, 76, 90, 103, 115, 116, 129, 143, 170, 184, 198, 225, 238, 251, 280, 291, 304, 316, 327, 328, 340, 354, 366, 378, 390, 402, 414, 426);
		
		
		int last = values[0];
		for(int i = 1; i < values.length; i++) {
			differences.add(values[i] - last);
			last = values[i];
		}
		
		
		List<Integer> differencesSorted = new ArrayList<Integer>(differences);
		
		differencesSorted.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		
		
		RunningWindowStats rws = new RunningWindowStats(5);
		List<Double> averages = new ArrayList<Double>();
				
		List<Group> groups = new ArrayList<Group>();
		
		int count = 0;
		for(int i : differencesSorted) {
			rws.add(i);
			averages.add(rws.mean());
			
			if(i > 1.3 * averages.get(count)) {
				System.out.println(i + " is above average.");
				Group group = new Group();
				group.upperBound = count;
				groups.add(group);
				rws.clear();
			}
			else
				System.out.println(i + " " + averages.get(count));
			
			count++;
		}
		
		Group lastGroup = new Group();
		lastGroup.upperBound = differencesSorted.size();
		groups.add(lastGroup);
		
		int lower = 0;
		for(Group group : groups) {
			group.lowerBound = lower;
			System.out.println(lower + " : " + group.upperBound);
			List<Integer> subSortedDiff = differencesSorted.subList(group.lowerBound, group.upperBound);
			group.average = Util.average(subSortedDiff);
			group.min = Collections.min(subSortedDiff);
			group.max = Collections.max(subSortedDiff);
			lower = group.upperBound;
		}
		
		System.out.println("Groups");
		for(Group group : groups) {
			System.out.println(group.average);
		}
		
		
		
		int modeIndex = -1;
		int greatestDifference = Integer.MIN_VALUE;
		for(int i = 0; i < groups.size(); i++) {
			int difference = groups.get(i).size();
			if(difference > greatestDifference) {
				modeIndex = i;
				greatestDifference = difference;
			}
		}
		
		System.out.println("The most frequent is: " + greatestDifference + " at " + modeIndex + "\n");
		
		
		for(Integer difference : differences) {
			for(Group group : groups) {
				if(group.contains(difference)) {
					System.out.println(group.value(groups.get(modeIndex)));
				}
			}
		}
		
/*		
		double mode = groups.get(modeIndex);
		
		for(Integer difference : differences) {
			Double fraction = difference / mode;
			Double ceil = Util.fractionCeil(fraction, 2);
			Double floor = Util.fractionFloor(fraction, 2);
			Double rounded = Util.fractionRound(fraction, 2);
			
			System.out.println(difference);
			System.out.println(fraction);
			System.out.println(ceil);
			System.out.println(floor);
			System.out.println(rounded);
			System.out.println();
		}
*/		
		
		//Then the base note has to be somewhat frequent. i.e a certain percentage of the whole song. ahh it probably doesn't matter.
		
		
		if(true)
			return;
		
		
		
		List<Complex> numbers = new ArrayList<Complex>();
		
		
		
		
		
		for(Integer i : values) {
			numbers.add(new Complex(i,0));
		}
		
		double value = Math.ceil(Math.log(values.length) / Math.log(2));
		double paddTo = Math.pow(2, value);
		
		for(int i = values.length; i < paddTo; i++) {
			numbers.add(new Complex());
		}
		
		Complex[] data = numbers.toArray(new Complex[numbers.size()]);
		
		FFT.fftForward(data);
		
		for(Complex c : data) {
			System.out.println(c.absolute());
		}
		
		if(true)
			return;
		
/*		Complex[] complexes = {
				new Complex(0), //0
				new Complex(.5),
				new Complex(1), //2
				new Complex(.5),
				new Complex(0),  //4
				new Complex(-.5),
				new Complex(-1),  //6
				new Complex(-.5),
				new Complex(0),  //8
				
				
				new Complex(.5),
				new Complex(1),  //10
				new Complex(.5),
				new Complex(0),  //12
				new Complex(-.5),
				new Complex(-1),  //14
				new Complex(-.5),
				new Complex(0),  //16
				new Complex(.5),
				new Complex(1),  //18
				new Complex(.5),
				new Complex(0),  //20
				new Complex(-.5),
				new Complex(-1), //22
				new Complex(-.5),
				new Complex(0),   //22
		};*/
		
		Complex[] complexes = new Complex[(int) Math.pow(2, 5)];//32
		for(int i = 0; i < complexes.length; i++) {
			switch(i%8) {
			case 0:
				complexes[i] = new Complex(0);
				break;
			case 1:
				complexes[i] = new Complex(.5);
				break;
			case 2:
				complexes[i] = new Complex(1);
				break;	
			case 3:
				complexes[i] = new Complex(.5);
				break;
			case 4:
				complexes[i] = new Complex(0);
				break;
			case 5:
				complexes[i] = new Complex(-.5);
				break;
			case 6:
				complexes[i] = new Complex(-1);
				break;
			case 7:
				complexes[i] = new Complex(-.5);
				break;
/*			case 8:
				complexes[i] = new Complex(0);				
				System.out.println(0 + " " + i%9);
				break;*/
			}
		}
		
		for(int i = 0; i < complexes.length; i++) {
			System.out.println(complexes[i]);
		}
		
		System.out.println("Before forward");		
		fftForward(complexes);
		System.out.println("After forward");
		
		for(int i = 0; i < complexes.length; i++) {
			System.out.println(complexes[i].absolute());
		}
		
		System.out.println("Before inverse");
		fftForward(complexes);
		//fftForward(complexes);
		System.out.println("After inverse");
		
		for(int i = 0; i < complexes.length; i++) {
			System.out.println(complexes[i].absolute());
		}
		/*Complex[] complexes = {
				new Complex(0),
				new Complex(1),
				new Complex(2),
				new Complex(3),
				new Complex(4),
				new Complex(5),
				new Complex(6),
				new Complex(7)
		};*/
	/*	for(Complex c : complexes) {
			System.out.println(c);
		}
		revBinaryPermute(complexes);
		for(Complex c : complexes) {
			System.out.println(c);
		}*/
	}
}

/*	private static void swap(Complex complex1, Complex complex2) {
Complex temp = complex1;
complex1 = complex2;
complex2 = temp;
System.out.println(complex1 + " " + complex2);
}*/