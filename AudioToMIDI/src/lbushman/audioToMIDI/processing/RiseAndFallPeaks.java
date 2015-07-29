package lbushman.audioToMIDI.processing;

import java.util.LinkedList;
import java.util.List;

import lbushman.audioToMIDI.util.Util;

public class RiseAndFallPeaks {
	private static int climbDown(List<Double> signal, int currentIndex, int end) {
		int index = currentIndex;
		while(index < end) {
			if(index == -1 )
				System.err.println("index is negative!!");
			
			double difference = 0;
			double percentage = 0;
			if(index + 1 < end) {
				double second = signal.get(index + 1);
				difference = second - signal.get(index);
				percentage = Math.abs(difference / second);
			}
			if(index + 1 < end && difference > 0 && percentage > 0.3 /*signal.get(index) < signal.get(index + 1) */) {
				Util.println("cI: " + currentIndex + " PERCENTAGE: " + percentage);
				return index;
			}
			index++;
		}
		return index;
	}
	
	private static int climbUp(List<Double> signal, int currentIndex, int end) {
		int index = currentIndex;
		while(index < end) {
			if(index == -1 )
				System.err.println("index is negative!!");

			double difference = 0;
			double percentage = 0;
			if(index + 1 < end) {
				double second = signal.get(index + 1);
				difference = second - signal.get(index);
				percentage = Math.abs(difference / second);
			}
			if(index + 1 < end && difference < 0 /*&& percentage > 0.025*/ /*signal.get(index) < signal.get(index + 1) */) {
				Util.println("climbUp cI: " + currentIndex + " PERCENTAGE: " + percentage);
				return index;
			}

		/*	if(index + 1 < end && signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
				return index;
			}*/
			
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
	public static List<Integer> findPeaks(List<Double> signal/*, int windowLength, double pSignificance*/) {
		List<Integer> peakIndexes = new LinkedList<Integer>();//TODO maybe use LinkedList
		int index = 0;		
		int end = signal.size();

/*		
		RunningWindowStats firstWindow = new RunningWindowStats(windowLength);
		RunningWindowStats secondWindow = new RunningWindowStats(windowLength);
*/				
		while(index < end) {
			index = climbUp(signal, index, end);
			peakIndexes.add(index);
			index = climbDown(signal, index, end);
//			index = findBottom(signal, index, end/*, firstWindow, secondWindow*/);
//			index = findSsignalignRise(signal, index, end/*, firstWindow, secondWindow*/);
//			index = findPeak(signal, index, end, windowLength);

			//In the case that it missed the peak by a few bins.
			// Search within a windows length from index. 
/*			int offset = (windowLength - 1) / 2;
			if(index <= end - offset) //TODO check this logic.
				index = Util.maxIndex(signal, index - offset, index + offset);
*/
			//This assumes that the end is not a peak
/*			if(index == end) {		
				return peakIndexes;
			} else {
				peakIndexes.add(index);
			}
*/		}
		return peakIndexes;
	}
}
