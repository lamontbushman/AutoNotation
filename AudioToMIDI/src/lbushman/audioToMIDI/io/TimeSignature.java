package lbushman.audioToMIDI.io;

public class TimeSignature {
	public int numerator;
	public int denominator;
	
	@Deprecated
	public TimeSignature() {}
	
	public TimeSignature(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	//Number of beats from beginning of first full measure
	//public int beatOffset;
	
	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}
}
