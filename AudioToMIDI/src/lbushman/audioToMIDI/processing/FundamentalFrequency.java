package lbushman.audioToMIDI.processing;

import lbushman.audioToMIDI.util.Util;
import java.util.ArrayList;
import java.util.List;

public class FundamentalFrequency {
	public static int findBottom(List<Double> signal, int currentIndex, int end, RunningWindowStats first, RunningWindowStats second) {
		//probably clear both first and second every time this is called.
		//also the first time don't use an initialized size of either. Instead calculate the windowLength
		// As soon as I find the first index greater than the previous use (index - currentIndex) / SOME_CONSTANT or something similar as the windowLength
		// also maybe compute size of downStats accordingly. I probably need a minimum though. Probably make sample rate large enough or fft small enough to keep a reasonable downStats length.
		// maybe I should make the right sizes right now, but nahh.
		
		//TODO what can I don in the eclipse console? Send it commands etc?
		int index = currentIndex;
		RunningWindowStats downStats = new RunningWindowStats(5);
		//System.out.println("blah2");
		while(index < end) {
			if(index == -1 )
				System.out.println("index is negative!!");
			downStats.add(signal.get(index));
			//TODO important!!. fix this later. clear both and only add second once, first is full.
			if(second.isFull())
				first.add(second.peek());
			else
				first.add(signal.get(index));
			if(first.isFull())
				second.add(signal.get(index));
			
			if(signal.get(index).doubleValue() <= signal.get(index + 1).doubleValue()) {
				if(downStats.isFull()) {
					double zScore = downStats.zScore(signal.get(index + 1));
				//	System.out.println(index + " Z: " + zScore);
					if(zScore >= .001) {
						//System.out.println("Found bottom at: " + index);
						return index;
					}
				}
			}
			index++;
		}
		return index;
		
/*		while(index < end &&
				signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
			downStats.add(signal.get(index));
			first.add(second.peek());
			second.add(signal.get(index));
			index++;
		}
		
		return index;
*/	}
	
	/**
	 * TODO I am going to find out why the pValue is not working when it did before.
	 */
	public static int findSignRise(List<Double> signal, int currentIndex, int end, RunningWindowStats first, RunningWindowStats second) {
		if(!first.isFull() || !second.isFull()) {
			System.err.println("Errror expecting windows to be full in findSignRise.");
			//System.exit(1);r
		}
		//currentIndex = 195;
		double pValue = RunningWindowStats.pValue(first, second);
		
		/*if(second.mean() > first.mean() && pValue <= 0.05pSignificance) {//maybe this is all I needed at first.
			return currentIndex;
		}*/
		pValue = Double.POSITIVE_INFINITY;
		double firstMean = first.mean();
		double secondMean = second.mean();
//		while(currentIndex < end && (secondMean <= firstMean || pValue > 0.01)) {//0.05 //0.025
		while(currentIndex < end  && (second.mean() -first.mean()) / second.mean() < 0.41) { //0.23 works OK lets hope .25,.33, (.45 and 04 a little too high), 
			first.add(second.peek());
			second.add(signal.get(currentIndex));
			pValue = RunningWindowStats.pValue(first, second);
			firstMean = first.mean();
			secondMean = second.mean();
			currentIndex++;
		}
		
		return currentIndex;// - (first.size() + second.size());
	}
	
	public static int findPeak(List<Double> signal, int curentIndex, int end, int windowLength) {
		int searchLen = (int) (windowLength * 2 /* 2 */);
		int endSearch = curentIndex + searchLen;
		if(endSearch > end) {
			endSearch = end;
		}
		int index = Util.maxIndex(signal, curentIndex, endSearch);
		return (index == -1)? end: index;
	}
	
	public static List<Integer> displayVariance(List<Double> signal, int firstPeak, int windowLength, double pSignificance) {
		//windowLength = 6;//7, 10
		windowLength = 7;
		
		List<Integer> peakIndexes = new ArrayList<Integer>();
		peakIndexes.add(firstPeak); //assuming start is the first peak of concern
		
		RunningWindowStats firstWindow = new RunningWindowStats(windowLength);
		RunningWindowStats secondWindow = new RunningWindowStats(windowLength);
		
		int index = firstPeak;	
		//Only search the first half of the FFT.
		//Because of this I am not worrying about hitting the end of the array
		//Wishy washiness is allowed.
		int end = signal.size() / 2;
		
		//Hopefully this length reaches the peak, but not the next peak.
		int searchPeakBefore = (windowLength * 2);
		int searchPeakLen = (int) (windowLength * 1.5);
		boolean newPeakFounds = false;
		
/*		//Find bottom of first peak
		while(index < end &&
				signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
			index++;
		}*/
	//	index = findBottom(signal, index, end, firstWindow, secondWindow);
		
		
		while(index < end) {
			//Find bottom of first peak
/*			while(index < end &&
					signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
				index++;
			}*/

/*			//Find bottom of next peak
			for(; index < end; index++) {
				double pValue = RunningWindowStats.pValue(firstWindow, secondWindow);
//				System.out.println("finding bottom:" + index + "," + pValue + "," + ((pValue <= 0.05)?"T":"F") + "," + ((pValue <= 0.1)?"T":"F"));
				
						//				System.out.println("zScore[" + (index - windowLength) + "-" + index + "] = " + zScore);
				
				
		//		if(index != 109 && index != 165 && index != 218 && index != 274 && index != 328/*zScore <= zAllowance*) {
		//		index != 110 && index != 165 && index != 220 && index != 275 && index != 330 && index != 385
				int varCount = 0;
				if(pValue > pSignificance) {
					if(varCount < 8) {
						varCount++;
					} else {
						// bottom of peak found
						//newPeakFound = true;						
					}
					break;
				} else {
					firstWindow.add(secondWindow.peek());
					secondWindow.add(signal.get(index));
				}
			}			
*/			
			//TODO see if this needs to be removed and not worry about filling the window
			//maybe a modified zAllowance based off rws.size()

//TODO maybe replace these
			//Fill up windows (may be full from previous peak)		
/*			while(!firstWindow.isFull()) {
				firstWindow.add(signal.get(index));
				index++;
				//for(; !firstWindow.isFull() && index < end; index++) {
			}
*/			
/*			while(!secondWindow.isFull()) {
				secondWindow.add(signal.get(index));
				index++;
			}
*/			
			index = findBottom(signal, index, end, firstWindow, secondWindow);
			index = findSignRise(signal, index, end, firstWindow, secondWindow);
			index = findPeak(signal, index, end, windowLength);
			//Sanity check just in case missed peak by a few bins
			
			// TODO surround a window length 
			if(index <= end - 3)
				index = Util.maxIndex(signal, index - 3, index + 3);

			if(index == end) {		
				return peakIndexes;
			} else {
				peakIndexes.add(index);
			}
		}
		if(true)
			return peakIndexes;
		
		
		while(index < end) {
			int varCount = 0;//non-consecutive peaks
			//Find beginning of next peak
			for(; index < end; index++) {
				double pValue = RunningWindowStats.pValue(firstWindow, secondWindow);
//				System.out.println(index + "," + pValue + "," + ((pValue <= 0.05)?"T":"F") + "," + ((pValue <= 0.1)?"T":"F"));
				
						//				System.out.println("zScore[" + (index - windowLength) + "-" + index + "] = " + zScore);
				
				
		//		if(index != 109 && index != 165 && index != 218 && index != 274 && index != 328/*zScore <= zAllowance*/) {
		//		index != 110 && index != 165 && index != 220 && index != 275 && index != 330 && index != 385
				
				
				if(pValue > pSignificance) {
					System.out.println(index);
				} else {
					int indexSave = index;
					System.out.println(index + "," + pValue + "," + ((pValue <= 0.05)?"T":"F") + "," + ((pValue <= 0.1)?"T":"F"));
					
					int searchBeg = index - searchPeakBefore; 
					int searchEnd = searchBeg + searchPeakLen;
					//probably not needed
					if(searchEnd >= end) {
						searchEnd = end;
					}
					
//					System.out.println("Searching from " + searchBeg + " to " + searchEnd);
					index = Util.maxIndex(signal, searchBeg, searchEnd);
					System.out.println("Found peak at: " + index);
					
					/*while(index < end &&
							signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
						firstWindow.add(secondWindow.peek());
						secondWindow.add(signal.get(index));
						index++;
					}*/
					
					index = findBottom(signal, index, searchEnd, firstWindow, secondWindow);
					
					
					if(index < indexSave) {
						System.out.println("!!!last index after botom!!!!");
						index = indexSave;
					}
					
					
				}
					firstWindow.add(secondWindow.peek());
					secondWindow.add(signal.get(index));
					
					
	/*			} else if (varCount < 8){
					varCount++;
				} else {
					newPeakFound = true;
					break;
				}*/
			}
			
			/*if(newPeakFound) {
				//Search for peak within a range
				//Start at the beginning of the second window
				int searchBeg = index - searchPeakBefore; 
				int searchEnd = searchBeg + searchPeakLen;
				//probably not needed
				if(searchEnd >= end) {
					searchEnd = end;
				}
				
//				System.out.println("Searching from " + searchBeg + " to " + searchEnd);
				index = Util.maxIndex(signal, searchBeg, searchEnd);
				
				//Clear both windows, since the data is probably not accurate
				// with the advent of the new peak.
				//However, doing so, I am forced to assume that the next peak
				// will not start within two windowLengths;
	//			firstWindow.clear();
	//			secondWindow.clear();
				
			
				//TODO The top of the peak may depend on hitting end of signal
				peakIndexes.add(index);
				//index = searchEnd;
				
//				System.out.println("Peak at index: " + index);
			} else {
				//System.out.println("No new peak found");
				break;
			}
			newPeakFound = false;*/
		}
		return peakIndexes;
	}
	
	
	
	public static List<Integer> findPeaks(List<Double> signal, int firstPeak, int windowLength, double pSignificance) {
		List<Integer> peakIndexes = new ArrayList<Integer>();
		peakIndexes.add(firstPeak); //assuming start is the first peak of concern
		
/*		if (true)
			return peakIndexes;*/
		peakIndexes = displayVariance(signal, firstPeak, windowLength, pSignificance);
		if(peakIndexes != null)
			return peakIndexes;
		
		
		
		
		
		
		//windowLength = 6;//7, 10
	/*	
		List<Integer> peakIndexes = new ArrayList<Integer>();
		peakIndexes.add(firstPeak); //assuming start is the first peak of concern
*/		
		RunningWindowStats firstWindow = new RunningWindowStats(windowLength);
		RunningWindowStats secondWindow = new RunningWindowStats(windowLength);
		
		int index = firstPeak;	
		//Only search the first half of the FFT.
		//Because of this I am not worrying about hitting the end of the array
		//Wishy washiness is allowed.
		int end = signal.size() / 2;
		
		//Hopefully this length reaches the peak, but not the next peak.
		int searchPeakBefore = (windowLength * 2);
		int searchPeakLen = (int) (windowLength * 1.5);
		boolean newPeakFound = false;
		
		//Find bottom of first peak
		while(index < end &&
				signal.get(index).doubleValue() > signal.get(index + 1).doubleValue()) {
			index++;
		}
		
		
		while(index < end) {
			//Find bottom of next peak
			for(; index < end; index++) {
				double pValue = RunningWindowStats.pValue(firstWindow, secondWindow);
//				System.out.println("finding bottom:" + index + "," + pValue + "," + ((pValue <= 0.05)?"T":"F") + "," + ((pValue <= 0.1)?"T":"F"));
				
						//				System.out.println("zScore[" + (index - windowLength) + "-" + index + "] = " + zScore);
				
				
		//		if(index != 109 && index != 165 && index != 218 && index != 274 && index != 328/*zScore <= zAllowance*/) {
		//		index != 110 && index != 165 && index != 220 && index != 275 && index != 330 && index != 385
				int varCount = 0;
				if(pValue > pSignificance) {
					if(varCount < 8) {
						varCount++;
					} else {
						// bottom of peak found
						//newPeakFound = true;						
					}
					break;
				} else {
					firstWindow.add(secondWindow.peek());
					secondWindow.add(signal.get(index));
				}
			}			
			
			//TODO see if this needs to be removed and not worry about filling the window
			//maybe a modified zAllowance based off rws.size()
			
			//Fill up windows (may be full from previous peak)		
			while(!firstWindow.isFull()) {
				firstWindow.add(signal.get(index));
				index++;
				//for(; !firstWindow.isFull() && index < end; index++) {
			}
			
			while(!secondWindow.isFull()) {
				secondWindow.add(signal.get(index));
				index++;
			}
			
			int varCount = 0;//non-consecutive peaks
			//Find beginning of next peak
			for(; index < end; index++) {
				double pValue = RunningWindowStats.pValue(firstWindow, secondWindow);
//				System.out.println(index + "," + pValue + "," + ((pValue <= 0.05)?"T":"F") + "," + ((pValue <= 0.1)?"T":"F"));
				
						//				System.out.println("zScore[" + (index - windowLength) + "-" + index + "] = " + zScore);
				
				
		//		if(index != 109 && index != 165 && index != 218 && index != 274 && index != 328/*zScore <= zAllowance*/) {
		//		index != 110 && index != 165 && index != 220 && index != 275 && index != 330 && index != 385
				if(pValue > pSignificance) {
					firstWindow.add(secondWindow.peek());
					secondWindow.add(signal.get(index));
				} else if (varCount < 8){
					varCount++;
				} else {
					newPeakFound = true;
					break;
				}
			}
			
			if(newPeakFound) {
				//Search for peak within a range
				//Start at the beginning of the second window
				int searchBeg = index - searchPeakBefore; 
				int searchEnd = searchBeg + searchPeakLen;
				//probably not needed
				if(searchEnd >= end) {
					searchEnd = end;
				}
				
//				System.out.println("Searching from " + searchBeg + " to " + searchEnd);
				index = Util.maxIndex(signal, searchBeg, searchEnd);
				
				//Clear both windows, since the data is probably not accurate
				// with the advent of the new peak.
				//However, doing so, I am forced to assume that the next peak
				// will not start within two windowLengths;
	//			firstWindow.clear();
	//			secondWindow.clear();
				
			
				//TODO The top of the peak may depend on hitting end of signal
				peakIndexes.add(index);
				//index = searchEnd;
				
//				System.out.println("Peak at index: " + index);
			} else {
				//System.out.println("No new peak found");
				break;
			}
			newPeakFound = false;
		}
		return peakIndexes;
	}
}
