package lbushman.audioToMIDI.util;

public class TimeData {
	static long timeBegin = -1;
	long before;
	long after;
	String key;
	
	public TimeData(String key) {
		before = System.currentTimeMillis();
		if(timeBegin == -1) {
			timeBegin = before;
		}
		this.key = key;
		
		after = 0;
	}
	
	public long stop() {
		after = System.currentTimeMillis();
		return after - before;
	}
	
	@Override
	public String toString() {
		if(after == 0 && before != 0) {
			return String.format("%1$10d", before -timeBegin) + " B: " + key; 
		} else if (after != 0 && before != 0) {
			return String.format("%1$10d", after -timeBegin) + " D: " + key + " " + (after - before);
		} else {
			return "Bad Time";
		}
	}
}
