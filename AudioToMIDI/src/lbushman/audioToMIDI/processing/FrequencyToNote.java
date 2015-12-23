package lbushman.audioToMIDI.processing;

import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.util.Util;


public class FrequencyToNote {
	private final static int midiOffset = 12;
	// Equal temperament frequencies
	private final static double[] frequencies = {
		15.43,//Below range
		16.35,17.32,18.35,19.45,20.6,21.83,23.12,24.5,25.96,27.5,29.14,
		30.87,32.7,34.65,36.71,38.89,41.2,43.65,46.25,49,51.91,55,58.27,
		61.74,65.41,69.3,73.42,77.78,82.41,87.31,92.5,98,103.83,110,116.54,
		123.47,130.81,138.59,146.83,155.56,164.81,174.61,185,196,207.65,
		220,233.08,246.94,261.63,277.18,293.66,311.13,329.63,349.23,369.99,
		392,415.3,440,466.16,493.88,523.25,554.37,587.33,622.25,659.25,
		698.46,739.99,783.99,830.61,880,932.33,987.77,1046.5,1108.73,
		1174.66,1244.51,1318.51,1396.91,1479.98,1567.98,1661.22,1760,
		1864.66,1975.53,2093,2217.46,2349.32,2489.02,2637.02,2793.83,
		2959.96,3135.96,3322.44,3520,3729.31,3951.07,4186.01,4434.92,
		4698.63,4978.03,5274.04,5587.65,5919.91,6271.93,6644.88,7040,
		7458.62,7902.13,
		8370.9 // Above range
	};
	private final static String[] noteNames = {
			"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"
	};
	/**
	 * 	Index 0, ratio between a note and itself
	 *  Index 1, ratio between a note and a half step above
	 *  Index 2, whole step
	 */
	private final static double[] justScaleRatios = {
		1.0, 25/24.0, 9/8.0, 6/5.0, 5/4.0, 4/3.0, 45/32.0, 3/2.0, 8/5.0, 5/3.0, 9/5.0, 15/8.0, 2.0
	};
	
	private final static double[] equalTemperamentScaleRatios = {
		1.0000, 1.05946, 1.12246, 1.18921, 1.25992, 1.33483, 1.41421, 1.49831, 1.58740, 1.68179, 1.78180, 1.88775, 2.0000		
	};
	

	private FrequencyToNote() {}
	
	private static int findNoteNamesIndex(String name) {
		int i = -1;
		for(i = 0; i < noteNames.length; i++) {
			if(noteNames[i].compareTo(name) == 0) {
				break;
			}
		}
		return (i < noteNames.length)? i : -1;
	}
	
	public static int semitonesBetween(Note note1, Note note2) {
		String name1 = note1.getName() + note1.getSharpFlatEmpty();
		String name2 = note2.getName() + note2.getSharpFlatEmpty();
		int index1 = findNoteNamesIndex(name1);
		int index2 = findNoteNamesIndex(name2);
		int pos1 = note1.getPosition();
		int pos2 = note2.getPosition();
		int value1 = (pos1 * noteNames.length) + index1;
		int value2 = (pos2 * noteNames.length) + index2;
		return value2 - value1;
/*		
		int semitones = 0;
		if((pos2 * noteNames.length) + index2 > (pos1 * noteNames.length) + index1) {
		//if (pos2 > pos1 || index2 > index1) {
			while(index1 != index2 || pos1 != pos2) {
				index1++;
				nSemitones++;
				if(index1 == noteNames.length) {
					index1 = 0;
					pos1++;
				}
			}
		} else {
			while(index1 != index2 || pos1 != pos2) {
				index1--;
				nSemitones--;
				if(index1 == -1) {
					index1 = noteNames.length - 1;
					pos1--;
				}
			}			
		}
		return nSemitones;*/
	}
	
	public static Note toNote(int index) {
		if(index == 0 || index == frequencies.length - 1)
			return new Note();
		else {
			String nameAndKey = noteNames[(index - 1) % noteNames.length];
			Character name = null;
			Boolean sharpFlatNull = null;
			if(nameAndKey.length() > 1) {
				if(nameAndKey.charAt(1) == '#') {
					sharpFlatNull = true;
				} else {
					sharpFlatNull = false;
				}
			}
			name = nameAndKey.charAt(0);
			Note note = new Note(name, sharpFlatNull, (index -1) / noteNames.length);
			return note;
		}
		//return noteNames[(index - 1) % noteNames.length] + ((index -1) / noteNames.length);
	}
	
	public static int toMidiNote(int index) {
		return index - 1 + midiOffset;
	}
	
	public static int findIndex(double frequency) {//440/466.16    450
		int index = 0;
		while(index + 1 < frequencies.length &&
				 frequencies[index+1] <= frequency) {
			index++;
		}
		double first = frequency - frequencies[index];
		double second;
		if(index + 1 < frequencies.length)
			second = frequencies[index + 1] - frequency;
		else {
			return index;
		}
		
		double diff = second - first;
		double devi = Math.abs(diff) / frequencies[index + 1];
		
		if(devi < .01) {
			//TODO
			Util.printErrorln("Found a frequency within 5% of the center between two other frequencies."
					+ " deviation: " + devi + "frequency: " + frequency + 
					" lower: " + frequencies[index] + " upper: " + frequencies[index + 1]);
		}
		
		/*if (first / frequencies[index+1] == second / frequencies[index+1]) {
			System.err.println("Found a frequency in the dead center!!!! Perhaps change algorithm.");
		}*/
		
/*		System.out.println(first + " " + second);*/
		
		if(diff >= 0) {//(first <= second) {
			return index;
		} else {
			return index + 1;
		}
	}
	
	/**
	 * 
	 * @param frequency
	 * @return The closest Note on the equal tempered scale to {@code frequency}
	 */
	public static Note findNote(double frequency) {
		return toNote(findIndex(frequency)) /*+ " " + findIndex(frequency) + " " + frequencies[findIndex(frequency)]*/;
		//return frequencies[findIndex(frequency)] +"";
	}
	
	/**
	 * Returns the closest frequency on the equal tempered scale.
	 * @param frequency
	 * @return
	 */
	public static double findFrequency(double frequency) {
		return frequencies[findIndex(frequency)]; /*+ " " + findIndex(frequency) + " " + frequencies[findIndex(frequency)]*/
		//return frequencies[findIndex(frequency)] +"";
	}
	
	public static int findMidiNote(double frequency) {
		return toMidiNote(findIndex(frequency));
	}
	
	private static int findIndexExact(double equalTemperamentFrequency) {
		int index = 0;
		while(index < frequencies.length && frequencies[index] != equalTemperamentFrequency) {
			index++;
		}
		Util.verify(index < frequencies.length, "findIndexExact was supplied without an \"equal tempered frequency\"");
		return index;
	}
	
	public static Note findNoteExact(double equalTemperamentFrequency) {
		return toNote(findIndexExact(equalTemperamentFrequency));
	}
	
	public static double findFrequencyExactOffset(double equalTemperamentFrequency, int offset) {
		int index = findIndexExact(equalTemperamentFrequency);
		index += offset;
		Util.verify(index < frequencies.length && index > -1, 
				"Offset into findFrequencyExactOffset is out of bounds. Index: " + index + " size: " + frequencies.length + " offset: " + offset
				+ " frequency: " + equalTemperamentFrequency);
		if(index < 0)
			index = 0;
		else if(index >= frequencies.length)
			index = frequencies.length - 1;
		return frequencies[index];
	}
	
	/**
	 *
	 * @param frequency1
	 * @param frequency2
	 * @param equalTemperament The supplied frequencies are the frequencies supplied by 
	 * 							{@link FrequencyToNote#findFrequency(double) findFrequency()}
	 * @return freq1 - freq2 in half notes
	 */
	public static int numSemitonesBetween(double frequency1, double frequency2, boolean equalTemperament) {
		if(equalTemperament) {
			return findIndexExact(frequency1) - findIndexExact(frequency2);
		} else {
			int nSemitones = Integer.MIN_VALUE;
			double higher, lower;
			int upDown;
			if(frequency1 >= frequency2) {
				higher = frequency1;
				lower = frequency2;
				upDown = 1;
			} else {
				higher = frequency2;
				lower = frequency1;
				upDown = -1;
			}
			// I can make more efficient, but I need to be efficient!!!
			double ratio = higher / lower;
			double lowestError = Double.MAX_VALUE;
			for(int i = 0; i < justScaleRatios.length; i++) {
				double error = (Math.abs(ratio - justScaleRatios[i])) / justScaleRatios[i];
				if(error < lowestError) {
				/*	Util.logIfFails(Math.abs(error - lowestError) / lowestError > 0.2, 
							"Hard to tell whether: " + nSemitones + " or " + (nSemitones + 1) + " semitones: " + (Math.abs(error - lowestError) / lowestError) + " : " +
							error + " : " + lowestError);*/
					lowestError = error;
					nSemitones = i;
				}
			}
			System.out.format(" %3d ",nSemitones * upDown);
			// System.out.println(ratio + " " + equalTemperamentScaleRatios[nSemitones]);
			return nSemitones * upDown;	
		}
	}
	
	public static void main(String args[]) {
		double frequency = 10000;
		System.out.println(FrequencyToNote.findNote(frequency));//8136.515 // 16.835
		System.out.println(FrequencyToNote.findMidiNote(frequency));
	}

}
