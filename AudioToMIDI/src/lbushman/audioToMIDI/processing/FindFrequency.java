package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;

import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.util.Util;

public class FindFrequency {
	private final int fftLength;
	private final double sampleRate;
	// The number of cents between two different notes
	// (log (freq1 / freq2) / log(2)) * 1200
	public FindFrequency(int fftLength, double sampleRate) {
		this.fftLength = fftLength;
		this.sampleRate = sampleRate;
	}
	
	public static int getHarmonic(List<Double> fft, int nth) {
		int fundamental = findFundamentalBin(fft);
		if(fundamental == -1) {
			Util.logIfFails(false, "getHarmonic: fundamental == -1");
			return -1;
		}
		int harmonic = fundamental * nth;
		int searchLen = 5;
		Util.logIfFails(harmonic - searchLen > -1 && harmonic + searchLen + 1 < fft.size() , "Range correction in getHarmonic. nth: " + nth);
		harmonic = Util.maxIndex(fft, Math.max(0, harmonic - searchLen), Math.min(fft.size() - 1, harmonic + searchLen + 1));
		Util.verify(harmonic >= 0, "stopping");
		if (true)
		return harmonic;

		
		int secondHarmonic  = harmonic * 2;
		secondHarmonic = Util.maxIndex(fft, secondHarmonic - searchLen, secondHarmonic + searchLen + 1);
		if(false)
		return secondHarmonic;
		
		int thirdHarmonic  = secondHarmonic * 2;
		thirdHarmonic = Util.maxIndex(fft, thirdHarmonic - searchLen, thirdHarmonic + searchLen + 1);
		if(false)
		return thirdHarmonic;
		
		
		int fourthHarmonic  = thirdHarmonic * 2;
		fourthHarmonic = Util.maxIndex(fft, fourthHarmonic - searchLen, fourthHarmonic + searchLen + 1);

		return fourthHarmonic;
	}
	
	// This idea failed.
	public Note getNoteBasedOffFirstHarmonic(List<Double> fft) {
		int harmonic = getHarmonic(fft, 2);
		Note harmonicNote = FrequencyToNote.findNote(FundamentalFrequency.computeFrequency(harmonic, sampleRate, fftLength));
		
		// This made it moderately worse
		return harmonicNote.toLowerHarmonic();
		
		// This worked pretty well for one song, but probably would fail for others.
		// return /*(int) Math.floor(*/harmonic / 2/*.0)*/;
		
		// This made it extremely worse
		// return (int) Math.round(harmonic / 2.0);
	}
	
	public static Note computeNote(double bin, double sampleRate, int fftLength) {
		return FrequencyToNote.findNote(computeFrequency(bin, sampleRate, fftLength));
	}
	
    public static double computeFrequency(double bin, double sampleRate, int fftLength) {
    	return bin * sampleRate / fftLength;
    }
	
	public static int findFundamentalBin(List<Double> fft) {
		boolean maxIsFundamental = true;
		
		// Find the max peak
		// TODO for current settings a G3 lowest possible note on violin. G3 index 12. Start at 10 to ignore noise.
		int maxI = Util.maxIndex(fft, 10, fft.size());
		double maxV = fft.get(maxI);

		int searchLen = 1; // TODO LDB maybe increase this and else if value below
		
		// A potential peak at the index half of the max peak
		int maxIHalf = (int) Math.round(maxI / 2.0); 
		int maxIPlusHalf = maxI + maxIHalf;
		maxIHalf = Util.maxIndex(fft, Math.max(0,maxIHalf - searchLen), Math.min(fft.size() - 1, maxIHalf + searchLen + 1));
		double maxIHalfV = fft.get(maxIHalf);
		
		// A potential peak whose index 1.5 times of the maximum peak.
		maxIPlusHalf = Util.maxIndex(fft, Math.max(0,maxIPlusHalf - searchLen), Math.min(fft.size() - 1, maxIPlusHalf + searchLen + 1));
		double maxIPlusHalfV = fft.get(maxIPlusHalf);
		
		// Assuming maxI will only ever be the fundamental frequency or the harmonic above.
		// If either of the above two peaks are significant, maxIHalf is the fundamental frequency bin.
		if(maxIHalfV / maxV > 0.09) //0.40
				maxIsFundamental = false;
		else if(maxIPlusHalfV / maxV > 0.09) // 1/11 LDB TODO maybe increase this and searchLen
				maxIsFundamental = false;
		
		int maxIDoubleI = maxI * 2;
		maxIDoubleI = Util.maxIndex(fft, Math.max(0,maxIDoubleI - searchLen), Math.min(fft.size() - 1, maxIDoubleI + searchLen + 1));
		if(maxIDoubleI == -1) {
			System.out.println("time: " + Util.getProperty("time", "null") + " maxIDoubleI was passed the array.");
			return -1; // Assumes there isn't a valid note being played currently.
		}
		double maxIDoubleV = fft.get(maxIDoubleI);
		
		// The first two harmonics are by far the loudest.
		if(maxIDoubleV / maxV > maxIHalfV / maxV) {
			maxIsFundamental = true;
		}
		
		if(maxIDoubleV > maxIPlusHalfV) {
			maxIsFundamental = true;
		}
		
		int maxITripleI = maxI * 3;
		maxITripleI = Util.maxIndex(fft, Math.max(0,maxITripleI - searchLen), Math.min(fft.size() - 1, maxITripleI + searchLen + 1));
		double maxITripleV = fft.get(maxITripleI);
		
		if(maxITripleV / maxIPlusHalfV > 0.50 || maxIDoubleV / maxIHalfV > 0.5) {
			maxIsFundamental = true;
		} else {
			maxIsFundamental = false;
		}
		
		if(Util.integerValue == 47 && false /*|| maxI == 55 || maxI == 54 || maxI == 56*/  /*&& maxNotFundamental == true*/) {
		//if(Util.integerValue == 22  /*&& maxI > 30*/ && maxIHalfV / maxV <= 0.4 && maxIPlusHalfV / maxV <= 0.11) {
			System.out.println("maxI: " + maxI);
			System.out.println("Util.integerValue == " + Util.integerValue);
			System.out.println("maxIHalfV / maxV <= 0.4 : " + maxIHalfV / maxV);
			System.out.println("maxIPlusHalfV / maxV <= 0.11 : " + maxIPlusHalfV / maxV);
			System.out.println("maxIDoubleV / maxV : " + maxIDoubleV / maxV);
			System.out.println("maxNotFundamental: " + maxIsFundamental);
			System.out.println(maxIHalfV + " : " + maxV + " : " + maxIPlusHalfV + " : " + maxIDoubleV);
		}
		
		
		
		
		
		
		// List<Integer> noteTrials = new ArrayList<Integer>();
		int fundamentalI;
		if(!maxIsFundamental) {
				fundamentalI = maxIHalf;
				// Trying to use other peaks to fix possible off by one or two positions. It seems that it didn't help.
				/*					
					int peak4 = Util.maxIndex(subList, (maxIhalf * 4) - searchLen, (maxIhalf * 4) + searchLen + 1);
					noteTrials.add(maxIHalf);
					noteTrials.add(maxIhalf);
					noteTrials.add((int) Math.round(maxIPlusHalf / 3.0));
					noteTrials.add((int) Math.round(peak4 / 4.0));
					
					//noteTrials = Util.mode(noteTrials);
					if(noteTrials.size() != 1) {
						System.out.println("multiple trials: " + i + " size: " + noteTrials.size());
					} 
	
					maxI = (int) Math.round(Util.average(noteTrials));
					
						// maxI = maxIHalf;				
					
					//posFundamentalValue / 
					//maxI = maxIhalf;
				*/
				 
		} else {
			fundamentalI = maxI;
		}
		
		// Same as above commented out code.
		/*
			for(int j = 1; j <= 1; j++) {
				int peak = Util.maxIndex(subList, (fundamentalI * j) - searchLen, (fundamentalI * j) + searchLen + 1);
				noteTrials.add((int) Math.round(peak / (double) j));
			}
			noteTrials = Util.mode(noteTrials);
			//if(noteTrials.size() != 1) {
				System.out.println("multiple trials: " + i + " noteTrials: " + noteTrials);
			//} 
			fundamentalI = (int) Math.round(Util.average(noteTrials));
		 */
		
		
		// Just in case we have off by one or two errors because the searchLen wasn't big enough above.
		// Above smaller so that it doesn't find a non peak and affect finding the fundamental peak.
		// This is assuming we have found the peak, but might be off slightly.
		searchLen = 2;
		fundamentalI = Util.maxIndex(fft, Math.max(0,fundamentalI - searchLen), Math.min(fft.size() - 1, fundamentalI + searchLen + 1));
		return fundamentalI;
	}
	
	/**
	 * Computes notes based off of bin ratios in comparison with the original fundamental bins.
	 * The bins supplied can be the fundamental bins or harmonics of the fundamental.
	 * @param bins The fundamental bins or multiples of the fundamental.
	 * @param notes Original notes based off fundamental bins.
	 * @return
	 */
	public List<Note> computeNotes(List<Integer> semitones, List<Note> originalNotes, List<Integer> semitonesFromNotes) {
		List<Note> notes = new ArrayList<Note>(originalNotes);
		notes.set(0, originalNotes.get(0));
		Util.verify(semitones != null && semitones.size() == semitonesFromNotes.size(), "computeNotes: lists are not the same size.");
		for(int i = 0; i < semitones.size(); i++) {
			int currSemitone = FrequencyToNote.semitonesBetween(notes.get(i), notes.get(i + 1));
			int offset = semitones.get(i) - semitonesFromNotes.get(i);
			offset = semitones.get(i) - currSemitone;

			// I believe FrequencyToNote.findFrequency() should guarantee that this doesn't fail.
			// Or maybe this might be because the fft bins are not precise enough.
			Util.logIfFails(Math.abs(offset) < 2, "You broke music!! Semitones are more than one apart.");
			//if(Math.abs(offset) < 2) {
				Note note = FrequencyToNote.toNewNote(originalNotes.get(i + 1), offset);
				notes.set(i + 1, note);
			//}
		}
		return notes;
	}
	
	public List<Note> computeNotesFromBins(List<Integer> bins, List<Note> originalNotes, List<Integer> semitonesFromNotes) {
		Util.verify(bins.size() - 1 == semitonesFromNotes.size() && bins.size() == originalNotes.size(), "computeNotes: bins, originalNotes, semitonesFromNotes sizes are not correct.");	
		List<Integer> semitones = computeSemitones(bins, false, true);
		return computeNotes(semitones, originalNotes, semitones);
	}
	
	public List<Integer> computeSemitones(List<Integer> bins, boolean noteNotRatio, boolean justNotEqual) {
		List<Integer> semitones = new ArrayList<Integer>();
		Util.verify(bins != null && bins.size() > 0, "computeNotes: bins is null or empty.");
		double lastFreq = computeFrequency(bins.get(0), sampleRate, fftLength);
		if(noteNotRatio) {
			lastFreq = FrequencyToNote.findFrequency(lastFreq);
		}		
		for(int i = 1; i < bins.size(); i++) {
			double freq = computeFrequency(bins.get(i), sampleRate, fftLength); // Probably don't need this, ratio is a ratio.
			if(noteNotRatio) {
				freq = FrequencyToNote.findFrequency(freq);
			}
			int nJustSemitones = FrequencyToNote.numSemitonesBetween(freq, lastFreq, noteNotRatio, justNotEqual);
			semitones.add(nJustSemitones);
			lastFreq = freq;
		}
		Util.verify(semitones.size() + 1 == bins.size(), "computeSemitones: sizes didn't end up as should be: semitones: " + semitones.size() + " bins: " + bins.size());
		return semitones;
	}

	public static List<Integer> semitones = new ArrayList<Integer>();
	public List<Note> computeNotes(List<Integer> fundamentalBins) {
		semitones.clear();
		Util.verify(fundamentalBins != null && fundamentalBins.size() > 0, "computeNotes: fundamentalBins is null or empty.");
		double[] frequencies = new double[fundamentalBins.size()];
			frequencies[0] = computeFrequency(fundamentalBins.get(0), sampleRate, fftLength);
		double[] temperedFrequencies = new double[fundamentalBins.size()];
			temperedFrequencies[0] = FrequencyToNote.findFrequency(frequencies[0]);
		List<Note> notes = new ArrayList<Note>(fundamentalBins.size());
			notes.add(FrequencyToNote.findNoteExact(temperedFrequencies[0]));
		
		
		/*
		// [5, 2, 0, 2, -2, -2, 1, -2, -2, -2, 2, -2, -3, -1, 3, 5, 4, -2, -7, 2, 9, -1, -2, -2, -1, 1, -5, 5, 2, 0, 2, -2, -2, 0, -1, -2, -2, 2, -2, -3, -1, 3, 5, 5, -2, -7, 2, 8, -1, -2, -2, -1, 1, -5, 0, -3, 3, 0, -3, 3, 5, 4, -2, -2, -1, -3, -2, 2, 2, -4, 5, 2, 2, -7, 1, 1, 5, -1, -2, 0, 0, 2, -5, 2, 2, -8, 9, -1, -2, 2, -2, -2, -1, -2, -2, 2, 2, -4, 5, 2, 2, -2, -2, -2, -1, 8, 0, -2, -2, -1, 0, 1]
		for(int i = 0; i < fundamentalBins.size(); i++) {
			frequencies[i] = computeFrequency(fundamentalBins.get(i), sampleRate, fftLength);
		}
		double[] ratios = new double[fundamentalBins.size() - 1];
		int[] nSemitones = new int[fundamentalBins.size() - 1];
		double lastFreq = frequencies[0];
		for(int i = 1; i < fundamentalBins.size(); i++) {
			ratios[i] = Math.max(frequencies[i],lastFreq) / Math.min(frequencies[i],lastFreq);
			nSemitones[i] = FrequencyToNote.numSemitonesBetween(frequencies[i], lastFreq, false);
			lastFreq = frequencies[i];
		}
		*/

		
		
		
		
		
		
		
		
		for(int i = 1; i < fundamentalBins.size(); i++) {

			frequencies[i] = computeFrequency(fundamentalBins.get(i), sampleRate, fftLength);
			int nJustSemitones = FrequencyToNote.numSemitonesBetween(frequencies[i], frequencies[i - 1], false, true);
			semitones.add(nJustSemitones);
			temperedFrequencies[i] = FrequencyToNote.findFrequencyExactOffset(temperedFrequencies[i - 1], nJustSemitones);
			notes.add(FrequencyToNote.findNoteExact(temperedFrequencies[i]));
			
			
			/*			
			frequencies[i] = computeFrequency(fundamentalBins.get(i), sampleRate, fftLength);
			int nJustSemitones = FrequencyToNote.numSemitonesBetween(frequencies[i], frequencies[i - 1], false);
			double temperedFrequency = FrequencyToNote.findFrequency(frequencies[i]);
			int nTemperedSemitones = FrequencyToNote.numSemitonesBetween(temperedFrequency, temperedFrequencies[i - 1], true);
			int offset = nJustSemitones - nTemperedSemitones;
			// I believe FrequencyToNote.findFrequency() should guarantee that this doesn't fail.
			// Or maybe this might be because the fft bins are not precise enough.
			Util.verify(Math.abs(offset) < 2, "You broke music!! Semitones are more than one apart.");
			temperedFrequency = FrequencyToNote.findFrequencyExactOffset(temperedFrequency, offset);
			temperedFrequencies[i] = temperedFrequency;
			notes.add(FrequencyToNote.findNoteExact(temperedFrequency));
			// TODO ensure that I am not drifting going up and down semitones. 
			// I need to ensure that I validate a note against what it is saying it is and the difference with the last note.
			*/
			
		}
		System.out.println();
		return notes;
	}
}
