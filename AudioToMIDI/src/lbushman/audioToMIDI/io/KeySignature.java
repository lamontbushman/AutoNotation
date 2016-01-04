package lbushman.audioToMIDI.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import lbushman.audioToMIDI.util.Util;

public class KeySignature {
	final private static Character[] major = {'F','C','G','D','A','E','B'}; 
	final private static Character[] minor = {'B','E','A','D','G','C','F'};
	
	final private Boolean majorMinor;
	final private int numSharpsOrFlats;
	
	public KeySignature(Boolean majorMinor, int numSharpsOrFlats) {
		this.majorMinor = majorMinor;
		this.numSharpsOrFlats = numSharpsOrFlats;
	}
	
	@Override
	public String toString() {
		if(majorMinor == null) {
			return "";
		}
		
		String output = (majorMinor)? "#" : "b";
		Character[] keyList = (majorMinor)? major : minor;
		for(int i = 0; i < numSharpsOrFlats; i++) {
			output += keyList[i];
		}
		
		return output;
	}
	
	/**
	 * Tests to see if a note should be sharp or flat or not (null).
	 * @param note
	 * @return
	 */
	public Boolean shouldBeSharpFlatOrNull(Note note) {
		if(majorMinor) {
			if(Arrays.asList(major).subList(0, numSharpsOrFlats).contains(note.getName())) {
				return true;
			} else {
				return null;
			}
		} else if (!majorMinor){
			if(Arrays.asList(minor).subList(0, numSharpsOrFlats).contains(note.getName())) {
				return false;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public Note conform(Note note) {
		Boolean sharpFlatOrNull = shouldBeSharpFlatOrNull(note);
		if(sharpFlatOrNull != note.getSharpFlatNull()) {
			return new Note(note.getName(), sharpFlatOrNull, note.getPosition());
		} else {
			return new Note(note);
		}
	}
	
	public boolean contains(Note note) {
		/*
 				Note is			KeySignature is
 				F#				F#...
majorMinor is the same
find F is it within position					TODO fix position within note
 				
 				F				F#...
majorMinor is not the same
 				
 				Fb				F#...
majorMinor is not the same
 				
 				F#				Fb...
majorMinor is not the same
 				
 				F				Fb...
majorMinor is not the same
 				
 				Fb				Fb...
majorMinor is the same
find F is it within position
 				
 				F#				...
not the same 				
 				F				...
is the same 				
 				Fb				...
is not the same


				D				F#...
majorMinor is not the same
		*/
		Boolean sharpFlatNull = note.getSharpFlatNull();

		//If it is a natural note and a natural key return true.
		if(sharpFlatNull == null && majorMinor == null) {
			return true;
		}
		//An accidental will never be in the natural key signature.
		if(sharpFlatNull != null && majorMinor == null) {
			return false;
		}
		
		//majorMinor != null is implied
		if(sharpFlatNull != null && sharpFlatNull != majorMinor)
				return false;
		/*		//loop to see if it is in the list
		} else {
			//loop to see if it is not in the list
		}*/
		
	
		
		//Otherwise, when majorMinor is not null
		Character[] keyList = null;
		char name = note.getName();
		
		if(name == Note.INVALID) {
			return false;
		}
		
		if(majorMinor) {
			keyList = major;
		} else {
			keyList = minor;
		}
		
		
		//MajorMinor is not null
		if(sharpFlatNull == null) {
			for(int i = 0; i < numSharpsOrFlats; i++) {
				if(keyList[i] == name) {
					return false;
				}
			}
			return true;
		} else { //else they are the same and not null
			for(int i = 0; i < numSharpsOrFlats; i++) {
				if(keyList[i] == name) {
					return true;
				}
			}
			return false;
		}

		
		
		
		
/*		//MajorMinor is not null
		if(sharpFlatNull == null || sharpFlatNull != majorMinor) {
			for(int i = 0; i < numSharpsOrFlats; i++) {
				if(keyList[i] == name) {
					return false;
				}
			}
			return true;
		} else { //else they are the same and not null
			for(int i = 0; i < numSharpsOrFlats; i++) {
				if(keyList[i] == name) {
					return true;
				}
			}
			return false;
		}
*//*		
		if(sharpFlatNull == null && majorMinor != null) {
			//loop through majorOrMinor and see if name is not in the list.
		}
		if(sharpFlatNull != null && majorMinor != null) {
			if(sharpFlatNull != majorMinor) {
				//loop through majorOrMinor and see if name is not in the list.
			}
			if(sharpFlatNull == majorMinor) {
				//loop through majorOrMinor and see if name IS in the list.
			}
		}
		*/
	}
	
	public static KeySignature deriveSignature(List<Note> notes) {
		//Counts of the accidentals in the list of notes.
		List<Integer> sharps = new ArrayList<Integer>(7);
		List<Integer> flats = new ArrayList<Integer>(7);
		List<Integer> naturals = new ArrayList<Integer>(7);

		int totalSharps = 0;
		int totalFlats = 0;

		//Initialize accidentals
		for(int i = 0; i < 7; i++) {
			flats.add(0);
			sharps.add(0);
			naturals.add(0);
		}
		
		//Loop through notes and increment usages of the accidentals.
		ListIterator<Note> noteIt = notes.listIterator();
		while(noteIt.hasNext()) {
			Note note = noteIt.next();
			char name = note.getName();
			if(name == Note.INVALID)
				continue;
			int index = name - 'A';
			switch(note.getSharpFlatEmpty()) {
				case Note.SHARP:
					sharps.set(index, sharps.get(index) + 1);
					totalSharps++;
					break;
				case Note.FLAT:
					flats.set(index, flats.get(index) + 1);
					totalFlats++;
					break;
				case Note.EMPTY:
					naturals.set(index, naturals.get(index) + 1);
					break;
			}
		}
		
		Character[] majorMinorList = null;
		List<Integer> thisList = null;
		List<Integer> thatList = null;
		
		Boolean majorMinor = null;
		int numSharpsOrFlats = -1;
		
		int totalAccidentals = totalSharps + totalFlats;
		
		if(totalAccidentals > 0) {
			majorMinor = (totalSharps >= totalFlats);
			if(majorMinor) {
				majorMinorList = major;
				thisList = sharps;
				thatList = flats;
			} else {
				majorMinorList = minor;
				thisList = flats;
				thatList = sharps;
			}
			
			//TODO there might be a better way to choose a key signature to decrease
			// the total amount of accidentals.

			int thisListDecreases = 0;
			int thatListIncreases = 0;
			int naturalListIncreases = 0;
			List<Integer> accidentalDecreases = new ArrayList<Integer>(7);
			for(int i = 0; i < majorMinorList.length; i++) {
				int index = majorMinorList[i] - 'A';
				thisListDecreases += thisList.get(index);
				thatListIncreases += thatList.get(index);
				naturalListIncreases += naturals.get(index);
				accidentalDecreases.add(thisListDecreases - (thatListIncreases + naturalListIncreases));
				/*if(otherList[i] != 0)
					thatListDecreases -= otherList[i];
				if(naturals[i] != 0)
					naturalListDecreases -=;*/
			}
			//Get the earliest max index.
			numSharpsOrFlats = 1 + Util.maxIndex(accidentalDecreases, 0, accidentalDecreases.size());
		}
		return new KeySignature(majorMinor, numSharpsOrFlats);
	}
	
	public static void main(String args[]) {
		KeySignature ks = new KeySignature(true,1);
		System.out.println(ks);
		Note n = new Note('E',true,4);
		System.out.println(ks.contains(n));
	}
}
