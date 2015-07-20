package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;

import lbushman.audioToMIDI.util.Util;

public class Peaks {
	private static int findBottom(List<Double> signal, int currentIndex, int end, RunningWindowStats first, RunningWindowStats second) {
		/*
		TODO idea. Maybe clear both first and second.
		TODO possibly calculate the size for first and second (i.e. the window length)
		 		As soon as I find the first index greater than the previous use (index - currentIndex) / SOME_CONSTANT or something similar as the windowLength
				Also maybe compute size of downStats accordingly. I probably need a minimum though. Probably make sample rate large enough or fft small enough 
		        to keep a reasonable downStats length.
		TODO what can I don in the eclipse console? Send it commands etc?
		*/
		
		int index = currentIndex;
		
		//TODO size maybe needs to be calculated.
		RunningWindowStats downStats = new RunningWindowStats(5);
		
		while(index < end) {
			if(index == -1 )
				System.err.println("index is negative!!");
			
			downStats.add(signal.get(index));
			
			if(second.isFull())
				first.add(second.peek());
			else
				first.add(signal.get(index));
			
			if(first.isFull())
				second.add(signal.get(index));
			
			if(index + 1 < end && signal.get(index).doubleValue() <= signal.get(index + 1).doubleValue()) {
				if(downStats.isFull()) {
					double zScore = downStats.zScore(signal.get(index + 1));

					//Found bottom as soon as it is a significant difference
					if(zScore >= 0.001) {//0.001 related to a p-value of 0.05
						return index;
					}
				}
			}
			index++;
		}
		return index;
	}
	
	private static int findSignRise(List<Double> signal, int currentIndex, int end, RunningWindowStats first, RunningWindowStats second) {
		if(!first.isFull() || !second.isFull()) {
			//Probably a sign that the window size is too big for the graph.
			System.err.println("Error hoping windows to be full in findSignRise.: " + first.size() + " : " + second.size());
			//System.exit(1);
		}

		/*
		 	pValues didn't work because they were significantly different but the differences weren't great enough no matter how probable they were different.
			double pValue = RunningWindowStats.pValue(first, second);
			while(currentIndex < end && (secondMean <= firstMean || pValue > 0.01)) {//0.05 //0.025
		*/
		double firstMean = first.mean();
		double secondMean = second.mean();

		// TODO pass in percentage instead of hard coded 0.41
		while(currentIndex < end  && (secondMean - firstMean) / secondMean < 0.41) { //Tested values between //0.23 and 0.45
			first.add(second.peek());
			second.add(signal.get(currentIndex));
			firstMean = first.mean();
			secondMean = second.mean();
			currentIndex++;
		}
		
		return currentIndex;
	}
	
	private static int findPeak(List<Double> signal, int curentIndex, int end, int windowLength) {
		//TODO possibly pass in the multiply of the window length
		int searchLen = (int) (windowLength * 2);
		int endSearch = curentIndex + searchLen;
		if(endSearch > end) {
			endSearch = end;
		}
		int index = Util.maxIndex(signal, curentIndex, endSearch);
		return (index == -1)? end: index;
	}
	
/*	public static List<Integer> findPeaks1(List<Double> signal, int firstPeak, int windowLength, double pSignificance) {
		List<Integer> peakIndexes = new ArrayList<Integer>();
		if(firstPeak < 0)
			peakIndexes.add(firstPeak); //assuming start is the first peak of concern
		
		return peakIndexes;
	}*/
	
	/**
	 * NOTE: pValue worked pretty great comparing two running pValues to find peaks.
	 *       Just found some false peaks and ranges of peaks.
	 *       Tried skipping consecutive peaks to overcome this second problem.
	 *       
	 * @param signal
	 * @param firstPeak set to -1 if wanting to search the whole graph. Otherwise firstPeak will be added to the
	 * return list and will start searching from here.
	 * @param windowLength
	 * @param pSignificance
	 * @return
	 */
	public static List<Integer> findPeaks(List<Double> signal, int firstPeak, int windowLength, double pSignificance) {
		List<Integer> peakIndexes = new ArrayList<Integer>();//TODO maybe use LinkedList
		int index;
		if(firstPeak < 0) {
			peakIndexes.add(firstPeak); //assuming start is the first peak of concern
			index = 0;
		} else {
			index = firstPeak;
		}
		peakIndexes.add(firstPeak); //assuming start is the first peak of concern
		
		int end = signal.size();

		
		RunningWindowStats firstWindow = new RunningWindowStats(windowLength);
		RunningWindowStats secondWindow = new RunningWindowStats(windowLength);
				
		while(index < end) {
			index = findBottom(signal, index, end, firstWindow, secondWindow);
			index = findSignRise(signal, index, end, firstWindow, secondWindow);
			index = findPeak(signal, index, end, windowLength);

			//In the case that it missed the peak by a few bins.
			// Search within a windows length from index. 
			int offset = (windowLength - 1) / 2;
			if(index <= end - offset) //TODO check this logic.
				index = Util.maxIndex(signal, index - offset, index + offset);

			//This assumes that the end is not a peak
			if(index == end) {		
				return peakIndexes;
			} else {
				peakIndexes.add(index);
			}
		}
		return peakIndexes;
	}
}
