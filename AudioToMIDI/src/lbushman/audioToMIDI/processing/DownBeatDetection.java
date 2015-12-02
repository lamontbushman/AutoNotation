package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import lbushman.audioToMIDI.util.Util;

public class DownBeatDetection {
	private DownBeatData data;
	private final int MAX_BTS_PER_MSURE = 8;
	private final int MAX_BTS_A_PICKUP_MSURE = 
			MAX_BTS_PER_MSURE - 1;
	private double lastNoteError;
	private double possibleNoteLengths[] = {1/4.0, /*3/8,*/ 1/2.0, 3/4.0, 1, 1.5, 2, 3, 4/*added four for come come ye saints. If I change beat length differently this is not required, 5, 6, 7, 8*/};
	
	public DownBeatDetection(DownBeatData data) {
		this.data = data;
		
//Builders//		System.out.println("1.5 0.5 1.0 1.0 1.0 1.0 1.0 1.0 1.5 0.5 1.0 1.0 1.0 1.0 2.0 1.5 0.5 1.0 1.0 1.5 0.5 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 2.0");
		double[] list = {1,1,1,1,1,3,1,1,/*1,*/ .5,.5,   1,1,3,1,1,1,1,1,2,2,1,1,1,1,3,1,1,1,1,1,3,1,1,1,1,1,3,1,2,2,1,1,1,1,2,2,3}; // high.wav
		for(double d : list) {
			System.out.print(d + " ");
		}
		System.out.println();
		
		
		List<Double> noteDurations = calculateNoteDurations(data.beats, data.avgBeatLength);
		data.noteDurations = noteDurations;
		for(Double duration : noteDurations) {
			System.out.print(duration + " ");
		}
		System.out.println("w: ");
		
		List<Double> noteDurations2 = new ArrayList<Double>();
		if(data.onsets.size() > 1) { 
			int firstOnset = data.onsets.get(0);
			int secondOnset;
			for(int i = 1; i < data.onsets.size(); i++) {
				secondOnset = data.onsets.get(i);
				int difference = secondOnset - firstOnset;
				
				System.out.print(calculateNoteDuration(difference, data.avgBeatLength) + " ");
				
				double duration = difference / (double) data.avgBeatLength;
				noteDurations2.add(duration);
				firstOnset = secondOnset;
			}
		}
		System.out.println();
		//data.noteDurations = noteDurations2;
		

		for(Double duration : noteDurations2) {
			System.out.print(Util.fractionCeil(duration, 2) + " ");
		}
		System.out.println();

		for(Double duration : noteDurations2) {
			System.out.print(Util.fractionFloor(duration, 2) + " ");
		}
		System.out.println();

		for(Double duration : noteDurations2) {
			System.out.print(Util.fractionRound(duration, 2) + " ");
		}
		System.out.println();
		for(Double duration : noteDurations2) {
			System.out.print(duration + " ");
		}
		System.out.println();
	}
	
	private double calculateNoteDuration(int length, int beatLength) {
		double fraction = length / (double) beatLength; // test division
		double error = 0;
		double leastError = Double.MAX_VALUE;
		double bestCandidate = -1;
		for(double candidate : possibleNoteLengths) {
			error = Math.abs(candidate - fraction) / candidate; // beatLength;
			if (error < leastError) {
				leastError = error;
				bestCandidate = candidate;
			} else {
				// In case of a length being between two others exactly, 
				//   this will give favor to the first.
				break; 
			}
		}
		Util.verify(bestCandidate != -1, "calculateNoteDuration() unexpected state,");
		lastNoteError = leastError;
		return bestCandidate;
	}
	
	private List<Double> calculateNoteDurations(List<Integer> beats, int avgBeatLength) {
		List<Double> noteDurations = new ArrayList<Double>();
		TreeSet<Integer> onsetSet = new TreeSet<Integer>(data.onsets);
		TreeSet<Integer> beatSet = new TreeSet<Integer>(beats);
		NavigableSet<Integer> onsetsBetween = null;
		int currentOnset = onsetSet.first();
		int lastOnset = onsetSet.last();
		Integer currentBeat = beatSet.first();


/*		Iterator<Integer> onsetIt = data.onsets.iterator();
		Iterator<Integer>  beatIt = beats.iterator();
		Integer currOnset;
		Integer currBeat;
		while(beatIt.hasNext()) {
			currBeat = beatIt.next();
			while(onsetIt.hasNext()) {
				currOnset = onsetIt.next();
				if(currOnset )
			}
		}*/

		//1. Find first matching onset and beat (first). Assume that the sum of any onsets before is less than a beat length.
		int onset1 = onsetSet.first();
		Integer onBeatOnset1 = onset1;
		if(!beatSet.contains(onset1)) {
			onBeatOnset1 = beatSet.higher(onset1);
			while(onBeatOnset1 != null && !onsetSet.contains(onBeatOnset1)) {
				onBeatOnset1 = beatSet.higher(onBeatOnset1);
			}
			if(onBeatOnset1 == null) {
				System.out.println("Error: No beat and onset match");
			}
		}
		
		//2. If there are beats before
		NavigableSet<Integer> oneBeatsOnsetsSet = onsetSet.headSet(onBeatOnset1, true); // guaranteed to have at least one element.	
		if(oneBeatsOnsetsSet.size() > 1) {
			// a. Make sure that the sum of any onsets before is less than a beat length.
			// b. Calculate the onset lengths
			List<Double> lengths = new ArrayList<Double>();
			Integer previous = null;
			double durationSum = 0;
			for(Integer onset : oneBeatsOnsetsSet) {
				if(previous != null) {
					int length = onset - previous;
					//  i. Divide
					// ii. "Round" to the nearest stored fraction.
					double duration = calculateNoteDuration(length, avgBeatLength);
					lengths.add(duration);
					durationSum += duration;
				}
				previous = onset;
			}
			// iii. Make sure total fractions are less than a beat length (add breakpoint for failure testing! Later an assert)
			if(durationSum >= 1) {
				System.err.println("Error: Beat matching is probably off. A beat wasn't detected near the beginning of the song.");
			}
			// iv. Add lengths to noteDurations
			noteDurations.addAll(lengths);	
		}
		
		
		while(true) {
			//3. Find next matching onset and beat (second). -- calculate the number of beats between first and second!! nBeats
			//int onset1 = onsetSet.first();
			int nBeats = 0;
			Integer onBeatOnset2 = beatSet.higher(onBeatOnset1);
			if(onBeatOnset2 == null) {
				break;
			} else {
				nBeats = 1;
			}
			
			while(onBeatOnset2 != null && !onsetSet.contains(onBeatOnset2)) {
				onBeatOnset2 = beatSet.higher(onBeatOnset2);
				nBeats = nBeats + 1;
			}
			if(onBeatOnset2 == null) { 
				break; // This should never be reached unless the last "offset" is added, correct?
			}
			
			//a. Calculate the onset lengths
			oneBeatsOnsetsSet = onsetSet.subSet(onBeatOnset1, true, onBeatOnset2, true);
			int diffBetweenBeats = onBeatOnset2 - onBeatOnset1;
			int currAvgBeatLen = (int) Math.round(diffBetweenBeats / (double) nBeats);
			
			List<Double> lengths = new ArrayList<Double>();
			Integer previous = null;
			double durationSum = 0;
			for(Integer onset : oneBeatsOnsetsSet) {
				if(previous != null) {
					int length = onset - previous;
					//  i. Divide
					// ii. "Round" to the nearest stored fraction.
					double duration = calculateNoteDuration(length, currAvgBeatLen);
					lengths.add(duration);
					durationSum += duration;
				}
				previous = onset;
			}
			// iii. Make sure total fractions are reasonable within nBeats
			//				1. (add breakpoint for failure testing!)
			//					a. This probably is a failure in beat detection 
			//					b. Also this may be evidence of a fermata. 
			//						Probably not. Fermatas will probably be detected on measure boundaries.
			//						Fermatas elongate notes not beats
			//					c. It could also be problems in not having enough precision (need a faster sampling rate)
			//				2. If not exactly nBeats
			//					a. Fudge the numbers maybe (test later). or test to see if this is reasonable.
			//						i. Add breakpoint/assert in case
			if(durationSum != nBeats) {
				System.out.println("Error: Notes don't add to: " + nBeats + " sum: " + durationSum);
			}
			// iv. Add lengths to noteDurations
			noteDurations.addAll(lengths);	
			
			//4. Set first = second
			onBeatOnset1 = onBeatOnset2;
			//5. Repeat to 3 till there are no more beats above current onset
		}
		// anything left to do with nBeats?
		//6. Outside of loop: If there are any more onsets after current beat
		//		a. Set an alarm breakpoint/assert
		if(onsetSet.higher(onBeatOnset1) != null) {
			System.out.println("Implement this just like step 2 (calculating lengths before)");
		}
		
		//7. Calculate length of last onset? based on loudness?
		//		-- then later when doing measures. Adjust this value. (Need to factor in the first measure to find the last measure length)
		
		//8. Based on steps 6 and 7 calculate the tail end
				
		return noteDurations;
/*		
		if(currentOnset != currentBeat) {
			System.out.println("The first two notes are not the same.");
			System.exit(0);
		}
		
		while(currentOnset != lastOnset) {
			currentBeat = beatSet.higher(currentOnset);
			while(currentBeat != null && !onsetSet.contains(currentBeat)) {
				currentBeat = beatSet.higher(currentBeat);
			}
			if(currentBeat == null) {
				//probably found end of the beats.
				//break;
				//return noteDurations;
			}
			onsetsBetween = onsetSet.subSet(currentOnset, false, currentBeat, false);
			if(onsetsBetween.size() == 0) {
				int beatsBetween = beatSet.subSet(currentOnset, false, currentBeat, false).size();
				noteDurations.add(beatsBetween + 1.0);
				currentOnset = currentBeat;
				//continue; update currentBeat etc.
			} else {
				//do complicated math or maybe easy. Or do another call to beatTracker now with half the size and then recursive call this function.
				int difference = currentBeat - currentOnset;
				int numBeats = onsetsBetween.size() + 1; // wrong should be; beatSet.subSet(currentOnset, false, currentBeat, false).size(); //however I want to save the subset.
				int avgSubBeatLen = (int) Math.round((difference / ((double)numBeats)) / 2.0);
				
				
				onsetsBetween.add(currentBeat);
				onsetsBetween.add(currentOnset);
				List<Integer> onsetsBetweenList = new ArrayList<Integer>(onsetsBetween);//make sure this is sorted.
				List<Integer> subBeats = ProcessSignal.beatTracker(onsetsBetweenList, avgSubBeatLen);
				
				
				
				/*int difference = currentBeat - currentOnset;
				int numBeats = onsetsBetween.size() + 1;
				
				onsetsBetween.add(currentBeat);
				onsetsBetween.add(currentOnset);
				List<Double> subDurations = new ArrayList<Double>();
				List<Double> errors = new ArrayList<Double>();
				double sum = 0;
				
				int O1 = currentOnset;
				int O2;
				for(int i = 1; i < onsetsBetween.size(); i++) {
					O2 = onsetsBetween.first();
					int diff = O2 - O1;
					double duration = diff / ((double) difference);
					double rDuration = Util.fractionRound(duration, 4);
					double error = rDuration - duration;
					errors.add(error);
					subDurations.add(rDuration);
					sum += rDuration;
					O1 = O2;
				}
				
				while(sum != numBeats) {
					if(sum > numBeats) {
						//Round down a note
						//Find the lowest positive error
						TreeSet<Double> errorSet = new TreeSet<Double>(errors);
						Double lowestPositive = errorSet.ceiling(0.0);
						int index = errors.indexOf(lowestPositive);
						
					} else {
						
					}
				}**
				
				
			}
		}
		
		return noteDurations;
		*/
		
		/*onsetsBetween = onsetSet.subSet(currentOnset, false, currentBeat, false);
		if(onsetSet.contains(currentBeat) && onsetsBetween.size() == 0) {
			noteDurations.add(1.0);
		} else if(!onsetSet.contains(currentBeat)) {
			while(on)
		}*/
		
	}

	public void detect() {
		for(int offset = 0; offset < MAX_BTS_A_PICKUP_MSURE && offset < data.beats.size(); offset++) {
			for(int beatIndex = offset; beatIndex < data.beats.size(); beatIndex++) {
				//System.out.println();
			}
		}
	}
}
