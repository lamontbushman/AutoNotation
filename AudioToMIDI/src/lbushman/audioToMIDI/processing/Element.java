package lbushman.audioToMIDI.processing;

public class Element<V> {
	int index;
	V value;
	
	Element(int index, V value) {
		this.index = index;
		this.value = value;
	}
}
