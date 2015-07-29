package lbushman.audioToMIDI.io;

//import java.awt.peer.ListPeer;
import java.util.List;

public class SheetData {
	//ListPeer huh;
	List<SheetNote> notes;
	double beatsPerMinute;
	int timeSignatureNumerator;
	int timeSignatureDenominator;
	KeySignature keySignature;
	
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
	
	//TODO display naturals if off of the key signature.
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Beats/Minute: " + beatsPerMinute + "\n")
		.append("Key Signature: " + keySignature + "\n")
		.append("Time Signuature : " +  timeSignatureNumerator + "/" + timeSignatureDenominator + "\n");
		
		for(SheetNote note : notes) {
			sb.append(note.toString(keySignature) + ", ");
		}
		
		return sb.toString();
	}
}
