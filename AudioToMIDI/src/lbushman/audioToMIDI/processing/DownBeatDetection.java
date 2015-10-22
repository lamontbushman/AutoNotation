package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import lbushman.audioToMIDI.util.Util;

public class DownBeatDetection {
	private DownBeatData data;
	private final int MAX_BTS_PER_MSURE = 8;
	private final int MAX_BTS_A_PICKUP_MSURE = 
			MAX_BTS_PER_MSURE - 1;
	
	public DownBeatDetection(DownBeatData data) {
		this.data = data;
		
		calculateNoteDurations(data.beats);
		List<Double> noteDurations = new ArrayList<Double>();
		if(data.onsets.size() > 1) { 
			int firstOnset = data.onsets.get(0);
			int secondOnset;
			for(int i = 1; i < data.onsets.size(); i++) {
				secondOnset = data.onsets.get(i);
				int difference = secondOnset - firstOnset;
				double duration = difference / (double) data.avgBeatLength;
				noteDurations.add(duration);
				firstOnset = secondOnset;
			}
		}
		data.noteDurations = noteDurations;

		for(Double duration : noteDurations) {
			System.out.print(Util.fractionCeil(duration, 2) + " ");
		}
		System.out.println();

		for(Double duration : noteDurations) {
			System.out.print(Util.fractionFloor(duration, 2) + " ");
		}
		System.out.println();

		for(Double duration : noteDurations) {
			System.out.print(Util.fractionRound(duration, 2) + " ");
		}
		System.out.println();
		for(Double duration : noteDurations) {
			System.out.print(duration + " ");
		}
		System.out.println();
	}
	
	private void calculateNoteDurations(List<Integer> beats) {
		List<Double> noteDurations = new ArrayList<Double>();
		TreeSet<Integer> onsetSet = new TreeSet<Integer>(data.onsets);
		TreeSet<Integer> beatSet = new TreeSet<Integer>(beats);
		NavigableSet<Integer> onsetsBetween = null;
		int currentOnset = onsetSet.first();
		int lastOnset = onsetSet.last();
		Integer currentBeat = beatSet.first();
		
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
				int numBeats = onsetsBetween.size() + 1 // wrong should be; beatSet.subSet(currentOnset, false, currentBeat, false).size(); //however I want to save the subset.
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
				}*/
				
				
			}
		}
		
		return;
		
		
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
