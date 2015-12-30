package lbushman.audioToMIDI.processing;

import java.util.List;
import java.util.ListIterator;

import lbushman.audioToMIDI.util.DoubleList;
import lbushman.audioToMIDI.util.Util;

enum Marks {
//	FIRST_NOTE, // first note is longer than a beat
	FIRST_1_5 (1.1),
	FIRST_G_1_5 (1.1),
	
	UP_BEAT (1.0),	// the last note is smaller than a beat
	FULL (0.0),		// the note spans the entire measure
//	LAST_NOTE   // last note is longer than a beat
	LAST_1_5 (1.0),
	LAST_G_1_5 (1.0),
	
	
	MORE_THAN_ONE (0.0),
	NUM_MEASURES_MARKED (0.0);
	
	private final double weight;
	Marks(double weight) {
		this.weight = weight;
	}
	
	public double weight() {
		return weight;
	}
}

public class FindDownBeat {
	private List<Double> durations;
	private List<PossibleDownBeat> pDownBeats;
	public FindDownBeat(List<Double> durations) {
		this.durations = durations;
		pDownBeats = PossibleDownBeat.intitalList();
		removeNonMultiples();
		find();
		postProcess();
	}
	
	public void showPossibleDownBeats() {
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
		for(PossibleDownBeat pdb : pDownBeats) {
			double[] measureMarks = pdb.getMeasureMarks();
			double score = 0;
			for(Marks mark : Marks.values()) {
				score += measureMarks[mark.ordinal()] * mark.weight();
			}
			pdb.setScore(score);
		}

				
		int numPoss = pDownBeats.size();
		if((numPoss == 2 || numPoss == 3)) {
			boolean doTest = false;
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
						(double) pdb1.getMeasureMarks()[Marks.NUM_MEASURES_MARKED.ordinal()];
				//If there are "too many" full measures choose the higher measure
				if(fraction >= 2/9.0) { // 3/11, 4/9, 4/12
					// The highest possible score, effectively choosing it.
					// Want to keep the others around in pDownBeats for future possible analysis.
					pdb2.setScore(Integer.MAX_VALUE);
				}
			}
		}
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
    		int numMarked = 0;
    		
    		double msurLen = 0;
    		int marked = 0;
    		while(durationIter.hasNext()) {
    			Double next = durationIter.next();
    			// Beginning of measure
    			if(msurLen == 0) {
    				if(next > 1 && next != p.getLength()) {
    					if(next == 1.5) {
    						measureMarks[Marks.FIRST_1_5.ordinal()]++;
    					} else {
    						measureMarks[Marks.FIRST_G_1_5.ordinal()]++;
    					}
    					//measureMarks[Marks.FIRST_NOTE.ordinal()]++;
    					marked++;
    				}
    			}
    			
    			msurLen += next;
    			
    			//End of measure
    			if(msurLen == p.getLength()) {
    				msurLen = 0;
    				numMeasures++;
    				if(next < 1) {
    					measureMarks[Marks.UP_BEAT.ordinal()]++;
    					marked++;
    				} else if (next > 1) {
    					if(next == p.getLength()) {
    						
    						// Gives smaller lengths too much credit
    						measureMarks[Marks.FULL.ordinal()]++;
    						marked++;
    					} else {
        					if(next == 1.5) {
        						measureMarks[Marks.LAST_1_5.ordinal()]++;
        					} else {
        						measureMarks[Marks.LAST_G_1_5.ordinal()]++;
        					}
    						//measureMarks[Marks.LAST_NOTE.ordinal()]++;
    						marked++;
    					}
    				}
    				if(marked > 0) {
    					numMarked++;
    					if(marked > 1) {
    					//	measureMarks[Marks.MORE_THAN_ONE.ordinal()]++;
    					}
    					marked = 0;
    				}
    			}
    			Util.verify(msurLen < p.getLength(), "find: Shouldn't happen 2");
    		}
    		// TODO I can set based on the amount of beats per measure, how much the different measureMarks are worth.
    		// TODO maybe a ratio between measureMarks, maybe the highest count (no percentage)
    		
    		//p.setScore(measureMarks[Marks.MORE_THAN_ONE.ordinal()]);
    		p.setScore(Util.sum(new DoubleList(measureMarks)) - measureMarks[Marks.FULL.ordinal()] /* / (double) numMarked*/);
    		measureMarks[Marks.NUM_MEASURES_MARKED.ordinal()] = numMarked;
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
