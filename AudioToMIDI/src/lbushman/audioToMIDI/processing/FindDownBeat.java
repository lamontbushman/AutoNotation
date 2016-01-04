package lbushman.audioToMIDI.processing;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import lbushman.audioToMIDI.test.TestDownBeats;
import lbushman.audioToMIDI.util.DoubleList;
import lbushman.audioToMIDI.util.Util;

enum Marks {
//	FIRST_NOTE, // first note is longer than a beat
	
	// TODO these should generally be louder than...
	FIRST_1_5 (1.1, "F=1_5"),
	FIRST_G_1_5 (1.1, "F>1_5"),
	FIRST_G_SECOND (0.7, "F>S"),
	
	UP_BEAT (1.0, "UP"),	// the last note is smaller than a beat
	FULL (1.0, "FULL"),		// the note spans the entire measure
//	LAST_NOTE   // last note is longer than a beat
	
	// TODO ...these. When we get amp data, this can help narrow down.
	// ... The weight can be related to the relative loudness in the "measure".
	LAST_1_5 (1.0, "L=1_5"),
	LAST_G_1_5 (1.0, "L>1_5"),
	
	
	NMSURS_MRKD_G_1 ("NMd>1"),
	NMSURS_MRKD ("NMd"),
	NMSURS ("NM"),
	NMSRS_MRKD_O_NMSRS ("NMd/NM"),
	
	
	// TODO review hymn 10 and see if I can better see why the other options shouldn't work.
	
	// TODO look at hymn four. It is very hard for me to lead it 0,6.
	// ... Check to see if 1 1 2 1 1. Where a longer note is surrounded by shorter notes makes sense.
	// ... This happens for 0,6 on hymn 4.
	// ... at least significantly. Hymn 3 "prom - ised the" 1 1.5 .5
	// ... Rule revised if there is nothing saying it is a measure and there is a longer note in the measure, flag this as not being
	// ... a possible down beat, or mark it lower appropriately.
	
	// ... ^ look at song 27 as well. I am trying to get percentage and msure_strength to work almost by themselves, with above penalty (possibly another Mark)
	// ... winner is largest msure_strength unless n,2 & n,4 then largest percentage is the winner out of those two.
	
	// ... ^^ this fails for hymn 49. Unless, for 6/8, I treat a count as a pickup line. For hymn 48, the single count counted basically as a pickup
	
	// ... ^ Same with hymn 50 but 2/2. It seems like .75 can be used as the downbeat.
	// ... ^ 53 is also 2/2, but has not .75, only 1 and .5, .5 as a couple of upbeats.
	
	// ... ^ hymn 56 has a .75, .25 downbeat. I can use this as a Mark and probably similar rule/combined rule for the above.
	// ... ^ in general, I think this would be a good song to analyze for other rules. i.e. a run of .5 notes for the down beat.
	// ... ^^ Hymn 59 shows that the upbeat can be relative to the downbeat. The downbeat is 3, the upbeat is 1.
	// ... ^ 42 and 43 shows that there are strong indications with the first note being 1.5, .5 maybe generalize with a 3 to 1 ratio.
	
	// Song 52, I would get completely wrong, not just the downbeat.
	 
	// TODO check to see if there is a correlation to how the song starts out. It seems like that the way the first full measure starts,
	// ... sets a pattern for a lot of the rest of the song to start with a similar down beat pattern.
	// ... ditto for the upbeat.
	// ... it might not always use it, but when, you do see a length/pattern in the song, it helps the person hear where the downbeat is.
	
	// High on the Mountain Top is 2/2 instead of 4/4 only because of speed. It is too hard to lead 4/4 with the spead it is at.
	
	// TODO look for patterns in "non" duration marked measures. Like hymn 3. The first note is always higher pitched than the rest.

	
	//num_marks / num_measures_marked
	NMRKS_O_NMSRS_MRKD ("NMk/NMd"),
	NMRKS_O_NMSURS ("NMk/NM");
	
	private final double weight;
	private String text;
	public static int formatLength = 0;
	Marks(double weight, String text) {
		this.weight = weight;
		this.text = text;
	}
	
	Marks(String text) {
		this.weight = 0.0;
		this.text = text;
	}
	
	public double weight() {
		return weight;
	}
	
	public String text() {
		ensureFormatLength();
		return String.format("%" + (formatLength) + "s", text);
	}
	
	public static String format(double value) {
		ensureFormatLength();
		return String.format("%0" + (formatLength - 1) + ".3f", value).replace("0", " ");
	}
	
	private static void ensureFormatLength() {
		if(formatLength == 0) {
			Marks[] marks = Marks.values();
			for(Marks mark : marks) {
				if(mark.text.length() > formatLength) {
					formatLength = mark.text.length();
				}
			}
		}
	}
}

public class FindDownBeat {
	private List<Double> durations;
	private List<PossibleDownBeat> pDownBeats;
	private PossibleDownBeat winner;
	public FindDownBeat(List<Double> durations) {
		this.durations = durations;
		pDownBeats = PossibleDownBeat.intitalList();
		removeNonMultiples();
		find();
		postProcess();
	}
	
	public void showPossibleDownBeats() {
		System.out.format("%14s","");
		for(Marks mark : Marks.values()) {
			System.out.print(mark.text() + " ");
		}
		System.out.println();
		pDownBeats.sort(new Comparator<PossibleDownBeat>() {
			@Override
			public int compare(PossibleDownBeat o1, PossibleDownBeat o2) {
				return Double.compare(o2.getScore(), o1.getScore());
			}
		});
		for(PossibleDownBeat p : pDownBeats){
			System.out.println(p);
		}
	}

	public List<PossibleDownBeat> returnPossibleDownBeats() {
		return pDownBeats;
	}
	
	public boolean contains(PossibleDownBeat pdb) {
		for(PossibleDownBeat curr : pDownBeats) {
			if(curr.equals(pdb)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsLen(int length) {
		for(PossibleDownBeat curr : pDownBeats) {
			if(curr.getLength() == length) {
				return true;
			}
		}
		return false;
	}
	
	public PossibleDownBeat contains(int offset, int length) {
		for(PossibleDownBeat curr : pDownBeats) {
			if((offset == -1 || offset == curr.getOffset()) && (length == -1 || length == curr.getLength())) {
				return curr;
			}
		}
		return null;
	}	
	
	private void postProcess() {
		if(TestDownBeats.currentSong == 10) {
			System.out.println("Break Point.");
		}
		if(pDownBeats.size() == 0) {
			return;
		}
		// before weights and any exceptions
		PossibleDownBeat preWinnerPdb = null;
		PossibleDownBeat winner = null;
		double highestScore = Double.MIN_VALUE;
		String preWinnersText = "";
		for(PossibleDownBeat pdb1 : pDownBeats) {
			double[] measureMarks = pdb1.getMeasureMarks();
			double score = 0;
			for(Marks mark : Marks.values()) {
				if(mark.weight() > 0) {
					score += measureMarks[mark.ordinal()];
				}
			}
			if(score >= highestScore) {
				if(score == highestScore) {
					preWinnersText += pdb1.toString() + "\n";
				} else {
					preWinnersText = pdb1.toString() + "\n";
					preWinnerPdb = pdb1;
				}
				highestScore = score;
			}
		}
		
		PossibleDownBeat pdb02 = new PossibleDownBeat(0, 2);
		highestScore = Double.MIN_VALUE;
		for(PossibleDownBeat pdb : pDownBeats) {
			double[] measureMarks = pdb.getMeasureMarks();
			double score = 0;
			for(Marks mark : Marks.values()) {
//				if(!(pdb02.equals(pdb) && mark == Marks.FULL)) {
					score += measureMarks[mark.ordinal()] * mark.weight();
//				}
			}
			if(score > highestScore) { // return the first highest one
				winner = pdb;
				highestScore = score;
			}
			pdb.setScore(score);
		}

		boolean doTest = false;				
		int numPoss = pDownBeats.size();
		if((numPoss == 2 || numPoss == 3)) {

			PossibleDownBeat pdb1 = contains(0,2);
			PossibleDownBeat pdb2 = contains(0,4);
			if(pdb1 != null && pdb2 != null) {
				doTest = true;
			} else {
				pdb1 = contains(1,2);
				pdb2 = contains(1,4);
				if(pdb1 != null && pdb2 != null) {
					doTest = true;
				}
			}
			if(doTest) {
				double fraction = pdb1.getMeasureMarks()[Marks.FULL.ordinal()] / 
						(double) pdb1.getMeasureMarks()[Marks.NMSURS_MRKD.ordinal()];
				//If there are "too many" full measures choose the higher measure
				if(fraction >= 2/9.0) { // 3/11, 4/9, 4/12
					// The highest possible score, effectively choosing it.
					// Want to keep the others around in pDownBeats for future possible analysis.
					System.out.println("Used a 2|4 rule FULL/NUM_MEASURES_MARKED at 2/9.0 to find the downbeat.");
					pdb2.setScore(Integer.MAX_VALUE);
					winner = pdb2;
				}
			}
		}
		
		// PDLOverrideSupported
		if(!doTest) {
			int maxI = -1;
			double maxValue = Double.MIN_VALUE;
			int index = 0;
			for(PossibleDownBeat pdb2 : pDownBeats) {
				if(pdb2.getScore() > maxValue) {
					maxValue = pdb2.getScore();
					maxI = index;
				}
				index++;
			}
			if(maxI == -1) {
				return;
			}
			PossibleDownBeat pdb = pDownBeats.get(maxI);
			int offset = pdb.getOffset();
			int length = pdb.getLength();
			if(length == 3 || length == 6) {
				double markedMsurePerc = pdb.getMeasureMarks()[Marks.NMSRS_MRKD_O_NMSRS.ordinal()];
				double measureStrength = pdb.getMeasureMarks()[Marks.NMRKS_O_NMSRS_MRKD.ordinal()];
				length = (length == 3)? 6 : 3; 
				pdb = contains(offset, length);
				if(pdb != null) {
					double otherPerc = pdb.getMeasureMarks()[Marks.NMSRS_MRKD_O_NMSRS.ordinal()];
					double otherStrength = pdb.getMeasureMarks()[Marks.NMRKS_O_NMSRS_MRKD.ordinal()];
					/*if(otherValue - value > 0.2) {*/
					if(TestDownBeats.currentSong == 29 || TestDownBeats.currentSong == 43) {
						System.out.println("break");
					}
					if(otherPerc - markedMsurePerc > 0.30 ) { // .25, 35
					//	if(!(measureStrength - otherStrength > 0.5)) {
							pdb.setScore(Double.MAX_VALUE);
							System.out.println("Used a 3|6 rule percentage to find the downbeat");
							winner = pdb;
					//	} else {
					//		System.out.println("Almost used a 3|6 rule percentage to find the downbeat");
					//	}
					}
				}
			} else if(length == 2 || length == 4){
				double markedMsurePerc = pdb.getMeasureMarks()[Marks.NMSRS_MRKD_O_NMSRS.ordinal()];
				double measureStrength = pdb.getMeasureMarks()[Marks.NMRKS_O_NMSRS_MRKD.ordinal()];
				length = (length == 2)? 4 : 2; 
				pdb = contains(offset, length);
				if(pdb != null) {
					double otherPerc = pdb.getMeasureMarks()[Marks.NMSRS_MRKD_O_NMSRS.ordinal()];
					double otherStrength = pdb.getMeasureMarks()[Marks.NMRKS_O_NMSRS_MRKD.ordinal()];
					if(otherPerc - markedMsurePerc >= 0.25 ) { // .25, 35
						if(measureStrength - otherStrength > .65) {
							System.out.println("Almost used the second 2|4 rule percentage to find the downbeat");
						} else {
							pdb.setScore(Double.MAX_VALUE);
							System.out.println("Used the second 2|4 rule percentage to find the downbeat");
							winner = pdb;
						}
					}
				}				
			}
		}
		
		if(!preWinnerPdb.equals(winner)) {
			System.out.println("The original winner was one of the following(\n" + preWinnersText + ")");
		}
		this.winner = winner;
/*		if(!doTest) {
			PossibleDownBeat pdb1 = contains(0,3);
			PossibleDownBeat pdb2 = contains(0,6);
			if(pdb1 != null && pdb2 != null) {
				doTest = true;
			} else {
				pdb1 = contains(1,3);
				pdb2 = contains(1,6);
				if(pdb1 != null && pdb2 != null) {
					doTest = true;
				}
			}
			if(doTest) {
				
			}
		}*/
		
		/*for(PossibleDownBeat pdb : pDownBeats) {
			double[] measureMarks = pdb.getMeasureMarks();
			for(Marks mark : Marks.values()) {
				//measureMarks[mark.ordinal()] = measureMarks[mark.ordinal()] / (double) measureMarks[Marks.TOTAL_MEASURES.ordinal()];
			}
		}*/
	}
	
	public PossibleDownBeat getWinner() {
		return winner;
	}
	
	private void find() {
    	ListIterator<PossibleDownBeat> pdbIter = pDownBeats.listIterator();
    	while(pdbIter.hasNext()) {
    		PossibleDownBeat p = pdbIter.next();
    		double offset = 0;
    		ListIterator<Double> durationIter = durations.listIterator();
    		while(durationIter.hasNext() && offset != p.getOffset()) {
    			offset += durationIter.next();
    		}
    		Util.verify(offset == p.getOffset(), "find: Shouldn't happen 1");
    		
    		double[] measureMarks = new double[Marks.values().length];
    		int numMeasures = 0;
    		int numMeasuresMarked = 0;
    		
    		double msurLen = 0;
    		int marked = 0;
    		int numMarkings = 0;
    		while(durationIter.hasNext()) {
    			Double currDuration = durationIter.next();
    			// Beginning of measure
    			if(msurLen == 0) {
    				//Maybe don't want to include if secondNote + currDuration == p.getLength()
    				if(currDuration > 1 && currDuration != p.getLength()) {
    					if(currDuration == 1.5) {
    						measureMarks[Marks.FIRST_1_5.ordinal()]++;
    					} else {
    						measureMarks[Marks.FIRST_G_1_5.ordinal()]++;
    					}
    					//measureMarks[Marks.FIRST_NOTE.ordinal()]++;
    					marked++;
    				} else if(currDuration != p.getLength()){
    					int nextIndex = durationIter.nextIndex();
    					if(nextIndex != durations.size()) {
	    					Double secondNote = durations.get(nextIndex);
	    					if(currDuration > secondNote) {
	    						//if(currDuration + secondNote < p.getLength())
	    						Util.verify(currDuration + secondNote <= p.getLength(), "removeNonMultiples() has an error"); 
	    						measureMarks[Marks.FIRST_G_SECOND.ordinal()]++;
	    						marked++;
	    					}
    					}
    				}
    			}
    			
    			msurLen += currDuration;
    			
    			//End of measure
    			if(msurLen == p.getLength()) {
    				msurLen = 0;
    				numMeasures++;
    				if(currDuration < 1) {
    					measureMarks[Marks.UP_BEAT.ordinal()]++;
    					marked++;
    				} else if (currDuration > 1) {
    					if(currDuration == p.getLength()) {
    						
    						// Gives smaller lengths too much credit
    						measureMarks[Marks.FULL.ordinal()]++;
    						marked++;
    					} else {
        					if(currDuration == 1.5) {
        						measureMarks[Marks.LAST_1_5.ordinal()]++;
        					} else {
        						measureMarks[Marks.LAST_G_1_5.ordinal()]++;
        					}
    						//measureMarks[Marks.LAST_NOTE.ordinal()]++;
    						marked++;
    					}
    				}
    				if(marked > 0) {
    					numMeasuresMarked++;
    					if(marked > 1) {
    						measureMarks[Marks.NMSURS_MRKD_G_1.ordinal()]++;
    					}
    					numMarkings += marked;
    					marked = 0;
    				}
    			}
    			Util.verify(msurLen < p.getLength(), "find: Shouldn't happen 2");
    		}
    		if(msurLen != 0) {
    			numMeasures++;
    		}
    		// TODO I can set based on the amount of beats per measure, how much the different measureMarks are worth.
    		// TODO maybe a ratio between measureMarks, maybe the highest count (no percentage)
    		
    		//p.setScore(measureMarks[Marks.MORE_THAN_ONE.ordinal()]);
    		p.setScore(Util.sum(new DoubleList(measureMarks)) - measureMarks[Marks.FULL.ordinal()] /* / (double) numMarked*/);
    		measureMarks[Marks.NMSURS_MRKD.ordinal()] = numMeasuresMarked;
    		measureMarks[Marks.NMSURS.ordinal()] = numMeasures;
    		measureMarks[Marks.NMSRS_MRKD_O_NMSRS.ordinal()] = numMeasuresMarked / (double) numMeasures;
    		measureMarks[Marks.NMRKS_O_NMSRS_MRKD.ordinal()] = numMarkings / (double) numMeasuresMarked;
    		measureMarks[Marks.NMRKS_O_NMSURS.ordinal()] = numMarkings / (double) numMeasures;
//    		measureMarks[Marks.WEighted_PERCENTAGE.ordinal()] = numMeasuresMarked * (some kind of weight 1.1 similar to above metric) / (double) numMeasures;
  
    		//p.setScore(Util.sumInt(measureMarks) / (double) numMeasures //+
    		//		   /*Util.sumInt(measureMarks) / (double) numMarked*/);
    	
    		p.setMeasureMarks(measureMarks);
    		
    		// Verify for DownBeat testing when given exact numbers
    		// This would only be valid if this is the correct DownBeat. But, I can't use this to  remove possible down beats because my last note duration will hardly be accurate.
    		// I'll adjust the note from the down beat.
    		// Util.verify(p.getOffset() != 0 || msurLen == 0 || p.getOffset() + msurLen == p.getLength(), "Didn't end in a perfect measure.");
    	}		
	}
	
	/***********************************************************************************************************
	* Simplest and most accurate (non) down beat filter
	* A note, at least in hymns hardly ever crosses a measure. In other words, an onset must occur on the down beat.
	* For High on the Mountain Top, this removed all but 5 of the 40 possible offsetNMeasureLengths.
	**********************************************************************************************************/
	private void removeNonMultiples() {
    	ListIterator<PossibleDownBeat> pdbIter = pDownBeats.listIterator();
    	while(pdbIter.hasNext()) {
    		PossibleDownBeat p = pdbIter.next();
    		//int offset = p.getOffset();
    		//int msurLen = p.getLength();
    		double offset = 0;
    		
    		ListIterator<Double> durationIter = durations.listIterator();
    		while(durationIter.hasNext() && offset < p.getOffset()) {
    			offset += durationIter.next();
    		}
    		if(offset != p.getOffset()) {
    			//System.out.println("Removed at intial offset " + p);
    			pdbIter.remove();
    			continue;
    		}
    		
    		double msurLen = 0;
    		boolean doRemove = false;
    		while(durationIter.hasNext()) {
    			msurLen += durationIter.next();
    			if(msurLen == p.getLength()) {
    				msurLen = 0;
    			} else if (msurLen > p.getLength()) {
    				doRemove = true;
    				break;
    			}
    		}
    		if(doRemove) {
    			pdbIter.remove();
    			//System.out.println("Removed " + p);
    			continue;
    		}
/*    		for(int i = offset; i < durations.size(); i += msurLen) {
    			if(durations.get(i) == null) {
    				System.out.println("Removed: " + p);
    				pdbIter.remove();
    				break;
    			}
    		}*/
    	}
	}
	
/*	public FindDownBeat(List<Integer> onsets) {
		List<Integer> differences = Util.diffList(onsets);
		List<Integer> modes = Util.mode(differences);
		// TODO There is a chance this may fail. Seek further implementation.
		Util.logIfFails(modes.size() == 1, "FindDownBeat: More than one mode for differences between onsets. modes: " + modes);
		int avgBeatLen = (int) Math.round(Util.average(modes));
		
		durations = new ArrayList<Double>();
		for(Integer diff : differences) {
			durations.add(diff / avgBeatLen);
		}
	}*/
}


/*
 
 		/*    	final int[] POSSIBLE_BTS_PER_MSURE = {2,3,4,6,8};
	    	final int MAX_BTS_A_PICKUP_MSURE = POSSIBLE_BTS_PER_MSURE[POSSIBLE_BTS_PER_MSURE.length - 1] - 1;
	    	List<Pair<Integer, Integer>> offsetNMeasureLengths = new ArrayList<Pair<Integer, Integer>>();
	    	for(int offset = 0; offset < MAX_BTS_A_PICKUP_MSURE; offset++) {
	    		for(int btsPerMsure : POSSIBLE_BTS_PER_MSURE) {
	    			offsetNMeasureLengths.add(new Pair<Integer, Integer>(offset, btsPerMsure));
	    		}
	    	}
	    	
	    	System.out.println("offsetNMeasureLengths");
	    	for(Pair<Integer, Integer> p : offsetNMeasureLengths) {
	    		System.out.print(p + " ");
	    	}*
	    	
	    	List<PossibleDownBeat> pDownBeats = PossibleDownBeat.intitalList();
	    	/***********************************************************************************************************
	    	// Simplest and most accurate (non) down beat filter
	    	// A note (at least in hymns never crosses a measure). In other words, an onset must occur on the down beat.
	    	// For High on the Mountain Top, this removed all but 5 of the 40 possible offsetNMeasureLengths.
	    	/**********************************************************************************************************
	    	ListIterator<PossibleDownBeat> pdbIter = pDownBeats.listIterator();
	    	while(pdbIter.hasNext()) {
	    		PossibleDownBeat p = pdbIter.next();
	    		int offset = p.getOffset();
	    		int msurLen = p.getLength();
	    		for(int i = offset; i < notesOnBeat.size(); i += msurLen) {
	    			if(notesOnBeat.get(i) == null) {
	    				System.out.println("Removed: " + p);
	    				pdbIter.remove();
	    				break;
	    			}
	    		}
	    	}
	    	
	    	System.out.println("Valid downbeats");
	    	pdbIter = pDownBeats.listIterator();
	    	while(pdbIter.hasNext()) {
	    		System.out.print(pdbIter.next() + " ");
	    	}
	    	
	    	
	    	
	    	pdbIter = pDownBeats.listIterator();
	    	System.out.println("asdffdas");
	    	while(pdbIter.hasNext()) {
	    		PossibleDownBeat p = pdbIter.next();
	    		int offset = p.getOffset();
	    		int msurLen = p.getLength();
	    		
	    		//This is banking on noteDurations to be exactly correct.
	    		ListIterator<Double> nDIter = data.noteDurations.listIterator(offset);
	    		double sum = 0;
	    		double first = 0;
	    		double second = 0;
	    		int numMeasures = 0;
	    		int numFirstLarger = 0;
	    		while(nDIter.hasNext()) {
	    			double duration = nDIter.next();
	    			sum += duration;
					if(first == 0) {
						first = duration;
					} else if(second == 0) {
						second = duration;
					}
	    			if(sum >= msurLen) {
	    				// I am wondering if this shouldn't happen because of the first removal of down beats.
	    				// if(sum != msurLen) {System.out.println("pppppppppppppppppppppppppp"); pdbIter.remove(); break;}
	    				Util.verify(sum == msurLen,
	    						"This probably means that this isn't the correct downbeat."
	    						+ "Or if it is, note durations aren't correct / We have a meausre that doesn't add up for some reason.");
	    				numMeasures++;
	    				if(first > second) {
	    					numFirstLarger++; // TODO Add these to a list.
	    				}
	    				// We need another percentage. What though? all possible numFirstLarger's? All ones that are in the current pDownBeats.
	    				// 
	    				p.setScore(numFirstLarger/* / (double) numMeasures*);
	    				System.out.println("\t\t" + p);
	    				first = second = sum = 0;
	    				
	    			}
	    		}
	    		
	    		// Maybe We could do add another one to see how the total amount of measures fit into groups of (4?)?
	    		
	    		/*for(int i = offset; i < notesOnBeat.size(); i += msurLen) {
	    			Util.verify(notesOnBeat.get(i) != null, "Should not be null");
	    			if(notesOnBeat.get(i) == null) {
	    				System.out.println("Removed: " + p);
	    				pdbIter.remove();
	    				break;
	    			}
	    		}*\/    		
	    	}
	    	System.out.println(pDownBeats);
	    	//
	    	// Can make above more efficient.
	    	// 		listOfIndexesWhenNull - loop notesOnBeat and insert when null
	    	// 		loop listOfIndexesWhenNull
	    	//			loop remaining offsetNMeasureLengths
	    	//				if(index divides (msurLen - offset)
	    	//					remove offsetNMeasureLen
	    	// Output of above: 0:2 0:4 0:6 0:8 1:4 1:8 2:2 2:4 2:6 2:8 4:2 4:4 4:6 4:8 5:4 5:8 6:2 6:4 6:6 6:8 
	        // 7, 11, 15 - index of nulls -- they invalidate anything that is a multiple of these (with offset)
	    	// 2,4,6, and 8 doesn't divide 7 or 11 or 15
	    	// 4 and 8 doesn't divide 7-1 or 11-1 or 15-1 but 2 does divide 6, 10, and 14
	    	//	
 
*/
