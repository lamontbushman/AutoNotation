package lbushman.audioToMIDI.io;

//import java.awt.peer.ListPeer;
import java.util.List;

public class SheetData {
	//ListPeer huh;
	List<SheetNote> notes;
	double beatsPerMinute;
	@Deprecated
	int timeSignatureNumerator;
	@Deprecated
	int timeSignatureDenominator;
	TimeSignature timeSignature;
	KeySignature keySignature;
	
	public SheetData(List<SheetNote> notes, double beatsPerMinute,
			TimeSignature timeSignature, KeySignature keySignature) {
		this.notes = notes;
		this.beatsPerMinute = beatsPerMinute;
		this.timeSignature = timeSignature;
		this.keySignature = keySignature;
	}

	@Deprecated
	public SheetData() {	
	}
	
	public List<SheetNote> getNotes() {
		return notes;
	}

	public void setNotes(List<SheetNote> notes) {
		this.notes = notes;
	}

	public double getBeatsPerMinute() {
		return beatsPerMinute;
	}

	public void setBeatsPerMinute(double beatsPerMinute) {
		this.beatsPerMinute = beatsPerMinute;
	}
	
	public TimeSignature getTimeSignature() {
		return timeSignature;
	}
	
	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}

	public int getTimeSignatureNumerator() {
		return timeSignatureNumerator;
	}

	public void setTimeSignatureNumerator(int timeSignatureNumerator) {
		this.timeSignatureNumerator = timeSignatureNumerator;
	}

	public int getTimeSignatureDenominator() {
		return timeSignatureDenominator;
	}

	public void setTimeSignatureDenominator(int timeSignatureDenominator) {
		this.timeSignatureDenominator = timeSignatureDenominator;
	}

	public KeySignature getKeySignature() {
		return keySignature;
	}

	public void setKeySignature(KeySignature keySignature) {
		this.keySignature = keySignature;
	}
	
	//TODO Double check naturals are printed if not in the key signature.
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Beats/Minute: " + beatsPerMinute + "\n")
		.append("Key Signature: " + keySignature + "\n");
		if(timeSignature != null) {
			sb.append("Time Signuature : " + timeSignature + "\n");
		} else {
			sb.append("Time Signuature : " +  timeSignatureNumerator + "/" + timeSignatureDenominator + "\n");
		}
		for(SheetNote note : notes) {
			sb.append(note.toString(keySignature) + ", ");
		}
		
		return sb.toString();
	}
}
