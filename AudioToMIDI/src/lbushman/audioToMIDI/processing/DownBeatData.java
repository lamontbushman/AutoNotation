package lbushman.audioToMIDI.processing;

import java.util.List;

import lbushman.audioToMIDI.io.KeySignature;

public class DownBeatData {
	public List<Integer> onsets;
	public List<Integer> beats;
	public List<Double> onsetAmps;
	public KeySignature kSignature;
	public int avgBeatLength;
	//List<Notes>
	public List<Double> noteDurations;
}
