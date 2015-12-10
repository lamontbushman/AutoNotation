package lbushman.audioToMIDI.processing;

import java.util.List;

public class ListElement<V> {
	int index;
	List<V> value;
	
	ListElement(int index, List<V> value) {
		this.index = index;
		this.value = value;
	}
}
