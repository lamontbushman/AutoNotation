package lbushman.audioToMIDI.io;

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
			sb.append(duration + ",");
		
		//Display note.
		//Don't display accidental if it is in the key signature.
		sb.append(note.toString(!ks.contains(note)));
		
		return sb.toString();
	}
}
