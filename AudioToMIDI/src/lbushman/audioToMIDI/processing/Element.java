package lbushman.audioToMIDI.processing;

import java.util.List;

public class Element<V> {
	int index;
	List<V> value;
	
	Element(int index, List<V> value) {
		this.index = index;
		this.value = value;
	}
}
