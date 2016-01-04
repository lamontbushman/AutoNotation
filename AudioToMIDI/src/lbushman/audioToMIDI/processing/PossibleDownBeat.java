package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;

public class PossibleDownBeat {
	// Modified to the most probable not the possible. We are going to get some wrong. By allowing some songs to not be right with no chance,
	//   we are allowing more songs to be correct with a higher chance.
	final private static int[] POSSIBLE_BTS_PER_MSURE = {2,3,4,6/*,8*/};
	final private static int MAX_BTS_A_PICKUP_MSURE = 1;/*POSSIBLE_BTS_PER_MSURE[POSSIBLE_BTS_PER_MSURE.length - 1] - 1*/; // TODO There can be a half/partial beat pickup. I think the current beat detection works for this.

	private int offset;
	private int length; // measure length
	private double score; // "probability"
	private double[] measureMarks;

	public PossibleDownBeat(int offset, int length, double score) {
		this.offset = offset;
		this.length = length;
		this.score = score;
	}
	
	public PossibleDownBeat(int offset, int length) {
		this.offset = offset;
		this.length = length;
		this.score = 1;
	}
	
	public PossibleDownBeat(PossibleDownBeat pdb) {
		this.offset = pdb.offset;
		this.length = pdb.length;
		this.score = pdb.score;
	}
	
	public static List<PossibleDownBeat> intitalList() {
		List<PossibleDownBeat> possibleDownBeats = new ArrayList<PossibleDownBeat>();
		for(int offset = 0; offset <= MAX_BTS_A_PICKUP_MSURE; offset++) {
			for(int btsPerMsure : POSSIBLE_BTS_PER_MSURE) {
				possibleDownBeats.add(new PossibleDownBeat(offset, btsPerMsure));
			}
		}
		return possibleDownBeats;
	}
	
	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public double[] getMeasureMarks() {
		return measureMarks;
	}

	public void setMeasureMarks(double[] measureMarks) {
		this.measureMarks = measureMarks;
	}
	
	public String toString(double[] array) {
		if(array == null) {
			return "";
		}
		
		String out = "[";
		for(double v : array) {
			out += v + ", ";
		}
		return out.substring(0, out.length() - 2) + "]";
	}
	
	public String measureMarks() {
		if(measureMarks == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(double m : measureMarks) {
			sb.append(Marks.format(m) + ", ");
		}
		return sb.substring(0, sb.length() - 2) + "]";
	}

	public String toString() {
		return offsetNLength() + ":" + String.format("%-7.2e", score) + ": " + measureMarks();
	}
	
	public String offsetNLength() {
		return offset + ":" + length;
	}
	
	public boolean equals(PossibleDownBeat pdb) {
		return pdb.length == length && pdb.offset == offset;
	}
}
