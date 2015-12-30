package lbushman.audioToMIDI.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbushman.audioToMIDI.processing.FindDownBeat;
import lbushman.audioToMIDI.processing.PossibleDownBeat;
import lbushman.audioToMIDI.util.Util;

public class TestDownBeats {
	public static boolean test(int songNumber, List<String[]> data) {
		boolean passed = false;
		List<Double> lengths = ReadSongs.parseNoteLengths(data);
		List<Integer> dbInfo = ReadSongs.parseDownBeatInfo(data);
		PossibleDownBeat pdb = new PossibleDownBeat(dbInfo.get(0), dbInfo.get(1));
		FindDownBeat fdb = new FindDownBeat(lengths);
		System.out.println("Test song: " + songNumber + " numMeasures: " + Util.sum(lengths) / (double) dbInfo.get(1));
		List<PossibleDownBeat> dbeats = fdb.returnPossibleDownBeats();
		if(dbeats.size() == 1 && dbeats.get(0).equals(pdb)) {
			passed = true;
			System.out.println("Passed");
		} else {
			boolean contains = false;
			for(PossibleDownBeat pdb2 : dbeats) {
				if(pdb2.equals(pdb)) {
					contains = true;
					break;
				}
			}
			
			if(contains) {
				System.out.println("Contains correct down beat " + dbeats.size());
				System.out.println("Remaining DownBeats");
				fdb.showPossibleDownBeats();
				int maxI = -1;
				double maxValue = Double.MIN_VALUE;
				int index = 0;
				for(PossibleDownBeat pdb2 : dbeats) {
					if(pdb2.getScore() > maxValue) {
						maxValue = pdb2.getScore();
						maxI = index;
					}
					index++;
				}
				if(dbeats.get(maxI).equals(pdb)) {
					System.out.println("Passed anyways: " + pdb);
					passed = true;
				} else {
					System.out.println("\t\t\t\t\tFailed: " + pdb);
				}
			} else {
				System.out.println("\t\t\t\t\tDOESN'T contain correct down beat " + dbeats.size());
			}
		}
		System.out.println();
		return passed;
	}
	
	public static void testAll() {
		Map<Integer, List<String[]>> data = ReadSongs.readAll();
		int numTests = data.size();
		int numPassed = 0;
		for(Entry<Integer, List<String[]>> subData : data.entrySet()) {
			if(test(subData.getKey(), subData.getValue())) {
				numPassed++;
			}
		}
		
		System.out.println("Passed " + numPassed + "/" + numTests + ": " + (numPassed / (double) numTests));
	}
	
	public static void main(String args[]) {
		testAll();
		/*		List<String[]> data = ReadSongs.read(2);
		TestDownBeats.test(2, data);*/
	}
}
