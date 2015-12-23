package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.util.Util;

public class AutoTuneNotes {
	private final Map<Integer, NoteNFrequency> map;
	private final int fftSize;
	private final double sampleRate;
	private int lowestBin, highestBin;

	/**
	 * @param map
	 */
	public AutoTuneNotes(int fftSize, double sampleRate) {
		this.fftSize = fftSize;
		this.sampleRate = sampleRate;
		map = new HashMap<Integer, NoteNFrequency>();
	}
	
	public Note getNote(int bin) {
		NoteNFrequency nNf = map.get(bin);
		if(nNf == null) {
			nNf = new NoteNFrequency(bin);
			map.put(bin, nNf);
			lowestBin = Math.min(bin, lowestBin);
			highestBin = Math.max(bin, highestBin);
		}
		nNf.increment();
		return nNf.note;
	}
	
	public void autoTune() {
		int lower = findLowerNoteBaseBin(lowestBin);
		int upper = findHigherNoteCeilBin(highestBin);
		List<Pair<Integer, NoteNFrequency>> allNotes = getAllNotes(lower, upper);
		List<Pair<Integer, Integer>> binsNWidths = getBinsNWidths(allNotes);
		System.out.println("bin note count frequency");
		for(Pair<Integer, NoteNFrequency> note : allNotes) {
			NoteNFrequency nnf = map.get(note.left);
			int count = 0;
			if(nnf != null) {
				count = nnf.getNumberOfNotes();
			}
			System.out.format("%3d %4s %2d %8.4f%n",note.left, note.right.note, count, note.right.frequency);
		}
	}
	
	private ArrayList<Pair<Integer, Integer>> getBinsNWidths(List<Pair<Integer, NoteNFrequency>> allNotes) {
		ArrayList<Pair<Integer, Integer>> binsNWidths = new ArrayList<Pair<Integer, Integer>>();
		int baseBin = 0;
		Note lastNote = allNotes.get(0).right.note;
		int width = 1;
		for(int i = 1; i < allNotes.size(); i++) {
			Note note = allNotes.get(i).right.note;
			if(lastNote.equals(note)) {
				width++;
			} else {
				binsNWidths.add(new Pair<Integer,Integer>(baseBin, width));
				baseBin = i;
				lastNote = note;
				width = 1;
			}
		}
		return binsNWidths;
	}
	
	private int findLowerNoteBaseBin(int lower) {
		// Find lowest bin of the note below lower
		Note note = computeNote(lower);
		while(lower >= 0 && computeNote(lower).equals(note)) lower--;
		note = computeNote(lower);
		while(lower >= 0 && computeNote(lower).equals(note)) lower--;
		lower++;
		return lower;
	}
	
	private int findHigherNoteCeilBin(int upper) {
		// Find the highest bin of the note higher than upper
		Note note = computeNote(upper);
		while(note == computeNote(upper)) upper++;
		note = computeNote(upper);
		while(note == computeNote(upper)) upper++;
		upper--;
		return upper;
	}
	
	private List<Pair<Integer, NoteNFrequency>> getAllNotes(int lower, int upper) {
		List<Pair<Integer, NoteNFrequency>> map = new ArrayList<Pair<Integer, NoteNFrequency>>();
		for(int i = lower; i <= upper; i++) {
			map.add(new Pair<Integer, NoteNFrequency>(i, new NoteNFrequency(i)));
		}
		return map;
	}
	
	private double computeFrequency(int bin) {
		return FundamentalFrequency.computeFrequency(bin, sampleRate, fftSize);
	}
	
	private Note getNote(double frequency) {
		return FrequencyToNote.findNote(frequency);
	}
	
	private Note computeNote(int bin) {
		return getNote(computeFrequency(bin));
	}
	
	private class NoteNFrequency {
		public Note note;
		public double frequency;
		private int numberOfNotes;
		public NoteNFrequency(int bin) {
			frequency = computeFrequency(bin);
			note = getNote(frequency);			
			numberOfNotes = 0;
		}

		public void increment() {
			numberOfNotes++;
		}
		public int getNumberOfNotes() {
			return numberOfNotes;
		}
	}
}