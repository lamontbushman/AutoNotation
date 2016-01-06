package lbushman.audioToMIDI.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lbushman.audioToMIDI.processing.PossibleDownBeat;
import lbushman.audioToMIDI.util.Util;

public class SheetNote {
	private Note note;
	private double duration;
	private Boolean isDownBeat;
	
	public SheetNote(Note note, double duration, Boolean isDownBeat) {
		this.note = note;
		this.duration = duration;
		this.isDownBeat = isDownBeat;
	}
	
	public Note getNote() {
		return note;
	}
	
	public double getDuration() {
		return duration;
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	public Boolean getIsDownBeat() {
		return isDownBeat;
	}

	public void setIsDownBeat(Boolean isDownBeat) {
		this.isDownBeat = isDownBeat;
	}
	
	public String toString(KeySignature ks) {
		StringBuilder sb = new StringBuilder();
		
		if(isDownBeat == null)
			System.err.println("Downbeat not initialized.");
		
		//Show beginning of measure.
		if(isDownBeat != null && isDownBeat)
			sb.append("|");
		
		//Display how long the note is.
		//(1.5 is 1.5 beats long.)
		//if(duration != 1.0)
		//	sb.append(duration);
		sb.append(prettyDuration(duration));
		
		//Display note.
		//Don't display accidental if it is in the key signature.
		sb.append(note.toString(!ks.contains(note)));
		
		return sb.toString();
	}
	
	private static String[] prettyDurationStrs = {"\u00BC", "\u00BD", "\u00BE", "", "1\u00BD", "2" , "3"};
	private static Double[] durations = {0.25,0.5,0.75,1.0,1.5,2.0,3.0};
	public static String prettyDuration(double duration) {
		int index = Arrays.asList(durations).indexOf(duration);
		if(index != -1)
			return prettyDurationStrs[index];
		else
			return duration + "";
	}

	public static List<SheetNote> createList(List<Note> notes, List<Double> noteDurations, PossibleDownBeat pdb) {
		Util.verify(notes != null && noteDurations != null && pdb != null && notes.size() == noteDurations.size(),
				"Can't create SheetNote list");
		List<SheetNote> list = new ArrayList<SheetNote>();
		int offset = pdb.getOffset();
		int msrLen = pdb.getLength();
		int size = notes.size();
		double msrDuration = 0;
		int i = 0;
		while(i < size && msrDuration != offset) {
			double duration = noteDurations.get(i);
			list.add(new SheetNote(notes.get(i), duration, false));
			msrDuration += duration;
			i++;
			// If I allow partial upBeats, then offset has to be made a double
			if (msrDuration > offset){
				Util.verify(false, "SheetNote.createList: Offsets are off");
			}
		}
		msrDuration = 0;
		for(; i < size; i++) {
			double duration = noteDurations.get(i);
			list.add(new SheetNote(notes.get(i), duration, msrDuration == 0));
			msrDuration += duration;
			if(msrDuration == msrLen) {
				msrDuration = 0;
			} else if (msrDuration > msrLen){
				// This won't apply, if I allow notes to pass over the measure. The above logic will have to change slightly.
				Util.verify(false, "SheetNote.createList: Note durations or PossibleDownBeat is off. Durations are not adding up");
			}
		}
		return list;
	}
}
