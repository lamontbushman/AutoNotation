package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.TreeSet;

import lbushman.audioToMIDI.io.KeySignature;
import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.io.SheetData;
import lbushman.audioToMIDI.io.SheetNote;
import lbushman.audioToMIDI.io.TimeSignature;
import lbushman.audioToMIDI.util.DoubleList;
import lbushman.audioToMIDI.util.Util;

public class ProcessSignal {
	private final AudioData data;
	private final int wholeFinished; 
	private final int overlapIncrement;
	private final int fftLength;
	private final int[] signal;
	
	public ProcessSignal(AudioData audioData, double overlapPercentage, int fftLength) {
		data = audioData;
		data.setOverlapPercentage(overlapPercentage);
		data.setFftLength(fftLength);
		double sampleRate = audioData.getFormat().getSampleRate();
		int	numFFTsInOneSecond = (int) (((sampleRate / fftLength) / overlapPercentage)); //partial FFTs.
		
		
		
		// The number of indexes that have at least one index of an original FFT length.
		
		wholeFinished = (int) Math.floor(fftLength / overlapPercentage);
		overlapIncrement  = (int) (fftLength * overlapPercentage);
		this.fftLength = fftLength;
		signal = data.getOriginalSignal();
		
		
		// completeNumFFTsInOneSecond = numFFTsInOneSecond - 1/overlapPercentage;
		// sR = 16384;
		// fftLength = 2048
		// sR /fft = 8
		// oP = .25
		// num = 32
		// BPM = 120; 2 bts/sec
		// 1/2 sec/bt
		// fastestNote = 1/4 BPM
		// 1/8 sec/finest note
		// numFFtsin8thSec = 32/8 = 4
		// complete... = 22/8 = 3.5 = 3
		// notes
//	0				1				2				3				4				5				6				7				8
//	1	2	3	4	5	6	7	8	9	0	1	2	3	4	5	6	7	8	9	0	1	2	3	4	5	6	7	8	9	0	1	2	3
		data.setNumFftsInOneSecond(numFFTsInOneSecond);
		//numFFTsInOneSecond = (samplingRate / (newFFTLength / 2)) / overlap; // /2 because of doubling and padding
	}
	
	public Double toAmp(double[] preFFT) {
		return Util.average(preFFT);
	}
	
	public void queues(boolean computeAmps, boolean oneArray) {
		//TODO maybe initialize with rough final size;
    	//Util.timeDiff("QUEUES");
    	FFT fftInstance = FFT.getInstance(data.getFftLength());
    	
    	IOQueue<double[], Double> toAmp = new IOQueue<double[], Double>(32) {
			@Override
			protected Double process(double[] preFFT) {
				//System.out.println("toAmp " + Thread.currentThread().getId());
				return toAmp(preFFT);
			}
		};
		toAmp.start();
		if(!computeAmps) {
			toAmp.signalFinished();
		}
		
    	IOQueue<Complex[], double[]> toFft = new IOQueue<Complex[], double[]>(32) {
			@Override
			protected double[] process(Complex[] preFFT) {
				//System.out.println("computeFFT " + Thread.currentThread().getId());
				return computeFFT(preFFT, fftInstance);
			}
		};
		toFft.start();
		
		//System.out.println("before computeComplexAndOverlap3");
		if(computeAmps) {
			computeComplexAndOverlap3(true, toAmp, toFft);
			toAmp.signalFinished();
		} else {
			computeComplexAndOverlap3(true, null, toFft);
		}
		//Finished adding to queues.
		
		toFft.signalFinished();
		
		
		try {
			//Wait for the queues to be processed
			//System.out.println("before amp join");
			toAmp.join();
			//System.out.println("before toFft join");
			toFft.join();
			//System.out.println("after toFft join");
			Util.verify(toAmp.isFinished(), ProcessSignal.this + " toAmp is not finished?");
			Util.verify(toFft.isFinished(), ProcessSignal.this + " toFft is not finished?");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Util.timeDiff("Copying");
		data.setAmp(toAmp.processedList().toArray(new Double[toAmp.maxSize()]));
		toAmp = null;
		if(oneArray) {
			List<Double> absolutes = new ArrayList<Double>(toFft.maxSize() * data.getFftLength());
			for(double[] dArray : toFft.processedList()) {
				for(double d : dArray) {
					absolutes.add(d);
				}
			}
			data.setFftAbsolute(absolutes);
		} else {
			// Older code works with one array of all the ffts in one. While this works with a list of ffts.
			data.setAbsolute(toFft.processedList());
		}
		toFft = null;
		
//		Util.timeDiff("Copying");	
//		Util.timeDiff("QUEUES");
	}
	
	public void oldPath() {
    	Util.timeDiff("oldpath");  	
    	
//    	Util.timeDiff("OVERLAP");
    	computeComplexAndOverlap2(true);
    	data.clearOriginalSignal(); // Last time we're using it. Save memory.
//    	Util.timeDiff("OVERLAP");

    	
    	
//    	Util.timeDiff("FFTS");
    	computeFFtsAndFilter();
    	data.setComplexData(null); // Last time we're using it. Save memory.
 //   	Util.timeDiff("FFTS");
    	
    	
//    	Util.timeDiff("AMP");
    	OnsetDetection.computeAmp(data);
    	data.setOverlappedData(null); // Last time we're using it. Save memory.
//    	Util.timeDiff("AMP");
    	Util.timeDiff("oldpath");
	}
    
    public void process() {
    	//computeComplexAndOverlap(false/*doHann*/);
    	
/*    	SignalQueue<Integer> intlist = new SignalQueue<Integer>();
    	List<Integer> signal = Arrays.asList(data.getOriginalSignal());
    	*/
    	
    	queues(true, true);
    	//oldPath();
    	
    	//System.out.println("begin");
    	
    	/*
    	computeAutoCorrelation();
    	computeFrequenciesFromAutocorrelation();
    	
    	List<Double> lowPassAbsolute = new LinkedList<Double>(); 	
    	List<Double> fftAbsolute = data.getFftAbsolute();	
    	
    	for(int i = 0; i + data.getFftLength() <= data.getFftAbsolute().size(); i+= data.getFftLength()) {
    		lowPassAbsolute.addAll(
    				lowPass(fftAbsolute.subList(i, i+ data.getFftLength())));
    		i+= data.getFftLength();
    	}
    	data.setFftLowPassAbsolute(fftAbsolute.toArray(new Double[fftAbsolute.size()]));*/
    	  	
 	

if(false) {    	
    	FundamentalFrequency ff = new FundamentalFrequency(data, 
    			/*Arrays.asList(data.getFftLowPassAbsolute())*/
    			data.getFftAbsolute()
    			);
    	ff.start();
   
    	OnsetDetection od = new OnsetDetection(data, data.getFftAbsolute());
    	od.start();
    	
    	
    	
    	//od.computeOnsets();
     	
    	try {
			ff.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	try {
			od.join();	    	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	BeatDetection bt = new BeatDetection(data, data.getFftAbsolute(), 4);
    	bt.start();
    	try {
			bt.join();
		} catch (InterruptedException e) {
			System.err.println("Beat detection was interrupted");
			e.printStackTrace();
		}
    	System.err.println("Beats:");
    	
    	data.setBeats(bt.getBeats());
    	for(Integer i : bt.getBeats()) { 
    		System.out.print(i + " ");
    	}
    	
    	

 
    	/*		//Add the last offset
		for(int j = amps.length - 1; j > 0; j--) {
			if(amps[j] >= 800) {
				data.getOnsets().add(j/2);
				break;
			}
		}*/
    	
    	List<Integer> onsets = bt.getBeats();
    	data.setBtClass(bt);
    	List<Integer> differences = new ArrayList<Integer>(onsets.size() / 2);
    	
    	int last = onsets.get(0);
		for(int i = 1; i < onsets.size(); i++) {
			differences.add(onsets.get(i) - last);
			last = onsets.get(i);
		}
		int average = (int) Math.round(Util.average(differences));
    	
    	
    	List<Integer> remOnsets = new ArrayList<Integer>();
    	List<Double> fftAbs = data.getFftAbsolute();
    	for(int onset : onsets) {
    		if(onset - 1 > 0 && fftAbs.get(onset-1) > fftAbs.get(onset)) {
    			remOnsets.add(onset);
    		}
    	}
    	
    	//onsets.removeAll(remOnsets);
    	
    	//TODO put this back if needed
		//bt.combineBeats(average);
		//onsets = bt.mergedBeats();
		
		data.setBeats(onsets);
		
		
		
		//STARTED WORKING HERE
    	int length = data.getAmp().length;
    	Double amps[] = data.getAmp();
    	double averageAmp = Util.averageD(Arrays.asList(amps));
    	double tolerance = averageAmp * 0.25;
    	
    	List<Integer> positionsRemoved = new ArrayList<Integer>();
    	List<Integer> positionsAdded = new ArrayList<Integer>();
    	
    	List<Integer> removeTheseOnsets = new ArrayList<Integer>();
    	
    	int firstValidOnset = 0;
    	int runningCountAboveAverage = 0;
    	int runningCountBelowAverage = 0;
    	final int START_RUN_LENGTH = 5;
    	final int BUFFER = 4;
     	for(int i = 0; i < data.getAmp().length; i++) {
     		if(amps[i] >= tolerance) {
     			runningCountAboveAverage++;
     			if(runningCountAboveAverage == START_RUN_LENGTH) {
     				if(firstValidOnset == 0) {
     					firstValidOnset = i - (START_RUN_LENGTH + BUFFER);
     					runningCountAboveAverage = 0;
     				} else {
     					break;	
     				}
     			}
     		} else {
     			runningCountAboveAverage = 0;
     			//If a run of below average amps are reached again before another
     			// run of above average amps are reached, start over again.
     			if(firstValidOnset != 0) {
     				runningCountBelowAverage++;
     				if(runningCountBelowAverage > START_RUN_LENGTH) {
     					runningCountBelowAverage = 0;
     					firstValidOnset = 0;
     				}
     			}
     		}
     		
     	}
     	
     	for(Integer i : onsets) {
     		if(i < firstValidOnset) {
     			removeTheseOnsets.add(i);
     		}
     	}
     	
     	onsets.removeAll(removeTheseOnsets);
     	
    	
     	
     	
     	
    	for(int i = 0; i < data.getAmp().length; i++) {
/*    		if(onsets.contains(i)) {
    			int lower = i;
    			int higher = i + 4;
    			boolean doRemove = true;
    			for( ;lower < higher; lower++) {
        			if(amps[lower] >= tolerance) {
        				doRemove = false;
        				break;
        			} 				
    			}
    			if(doRemove) {
    				removeTheseOnsets.add(i);
    			}
    		}*/
    		
    		
    		if(amps[i] <= tolerance) {
    			positionsRemoved.add(i);
    		} else {
    			positionsAdded.add(i);
    		}
    	}
		
/*		ListIterator<Integer> lit = onsets.listIterator();
		int prev = 0;
		while(lit.hasNext()) {
			int curr = lit.next();
			if(curr == prev)
				lit.remove();
			prev = curr;
		}*/
		
		//ENDED WORKING HERE TODO move into function
		
		
		List<Integer> halfedOnsets = Util.halfList(onsets);
		
		
		ListIterator<Integer> lit2 = halfedOnsets.listIterator();
		int prev = 0;
		while(lit2.hasNext()) {
			int curr = lit2.next();
			if(curr == prev)
				lit2.remove();
			prev = curr;
		}
		
		
    	/*ListIterator<Integer> it = onsets.listIterator();
    	Set<Integer> list = new TreeSet<Integer>();
    	while(it.hasNext()) {
    		list.add(it.next() / 2);
    	}
    	onsets.clear();
    	onsets.addAll(list);*/
    	
    	
    	
//    	List<Integer> onsets = data.getOnsets();
    	//TODO Beat difference can probably be validated or improved upon with the BeatDetection
    	//TODO beadDifference might need to be the smallest beat difference (within reason) in case most frequent doesn't pertain to the down beat.
    	int beatDifference = processOnsets(halfedOnsets);
    	System.out.println("LDB: bD: " + beatDifference);
    	//TODO time signature happens above.
    	/*
    	List<Integer> trackedBeats = beatTracker(onsets, beatDifference / 2);
    	ListIterator<Integer> it2 = trackedBeats.listIterator();
    	Set<Integer> list2 = new TreeSet<Integer>();
    	while(it2.hasNext()) {
    		list2.add(it2.next() * 2);
    	}
    	trackedBeats.clear();
    	trackedBeats.addAll(list2);//[90, 102, 112, 124, 134, 144, 156, 178, 190, 200, 220, 232, 242, 264, 274, 286, 296, 306, 318, 328, 338, 348, 358, 370, 380, 390, 464]
    	data.setTrackedBeats(trackedBeats);
    	*/
		

		
    	//TODO use both BeatDetection and onsets to get the first onset. We can validate the two to find better onsets!!!!!! This can improve accuracy a ton!!!
    	TimeSignature ts = bt.findTimeSignature(onsets.get(0)/*halfedOnsets.get(0) * 2*/, beatDifference);//TODO LDB Am I sure the *2 is what I want
    	data.setBeats2(bt.getSecondBeats());
    	data.setBeatsPercent(bt.getBeatPercent());
    	
    	bt.combineBeats(beatDifference);
    	bt.getCombinedBeats();
    	
    	TimeSignature ts2 = bt.findTimeSignature2(beatDifference);
    	
    	System.out.println("\nTime signature: " + ts.numerator + " / " + ts.denominator);
    	
    	System.out.println("Time signature: " + ts2.numerator + " / " + ts2.denominator);
    	

    	
    	
    	/*
    	OnsetDetection od2 = new OnsetDetection(data, Arrays.asList(data.getFftLowPassAbsolute()));
    	od2.computeOnsets();*/
    	
    	//fftAbsolute();
    	//fftCepstrum();

    //	setFrequencies();
   // 	setNotenames();
    //	setNormalizedFrequencies();
    	data.setBeats2(removeTheseOnsets);
}   	
    }

    /**
     * Matches up the highest amp with an onset
     * Returns a list of amps by beat index
     */
    public static List<Double> onsetAmps(List<Integer> onsets, List<Double> amps, double multiplier) {
    	final double PERCENTAGE = 0.80;
    	final int DEFAULT_SEARCH_LENGTH = 3;
    	int searchLen = 0;
    	int onsetAmpsListSize = onsets.get(onsets.size() - 1) + 1;
    	int onsetListSize = onsets.size();
    	
    	List<Double> onsetAmps = new ArrayList<Double>();
    	for(int i = 0; i < onsetAmpsListSize; i++) {
    		onsetAmps.add(0.0);
    	}
    	

    	for(int i = 0; i < onsetListSize; i++) {
    		int onset = onsets.get(i);
    		int nextOnset;
    		if(i + 1 < onsetListSize) {
    			nextOnset = onsets.get(i + 1);
    			int diff = nextOnset - onset;
    			searchLen = (int) Math.round(diff * PERCENTAGE);
    		} else if (searchLen == 0){
    			searchLen = DEFAULT_SEARCH_LENGTH;
    		} // else  Use previous searchLen
    		
    		int searchEnd = onset + searchLen;
    		searchEnd = (searchEnd > amps.size()) ? onsetAmpsListSize : searchEnd;
    		
    		int maxI = Util.maxIndex(amps, onset, searchEnd);
    		double max = amps.get(maxI) * multiplier;
    		/*if(maxI < onsetAmpsListSize)
    			onsetAmps.set(onset maxI, max); //match up the onset with the max amp. Not the initial amp.
    		else*/
    			onsetAmps.set(onset , max); //match up the onset with the max amp. Not the initial amp.
    	}
    	
    	return onsetAmps;
    }

    /**
     * Assumes that the first onset is a whole note. Fails horribly if it isn't.
     * @param onsets
     * @param beatDifference
     * @return
     */
	public static List<Integer> beatTracker(List<Integer> onsets, int beatDifference) {
		final double PERCENT_OFF_ALLOWANCE = 0.201;//0.28572;//0.19;//1.125;
		
		if(onsets.isEmpty())
			return null;
		
		Set<Integer> beats = new HashSet<Integer>();
		//beats.add(onsets.get(0));
		
		double runningDifference = beatDifference;
		boolean previousAdded = false;
		int candidate = 0;
		int onset = 0;
		double error = 0;
		if(onsets.size() > 1)
		for(int i = 0; i + 1 < onsets.size(); i++) {
			candidate = onsets.get(i) + beatDifference;
			onset = onsets.get(i + 1);
			error = (Math.abs(candidate - onset) / runningDifference); 
			
			if(error < PERCENT_OFF_ALLOWANCE) {
				beats.add(onsets.get(i));
				beats.add(onsets.get(i+1));
				System.out.println(onset + " " + error);
				previousAdded = true;
			} else {
				previousAdded = false;
/*				if(previousAdded) {
					beats.add(onsets.get(i - 1));
				}
				previousAdded = false;
*/			}
		}
		
		List<Integer> sortedBeats = new ArrayList<Integer>(beats);
		sortedBeats.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }
		});
		
		List<Integer> missingBeats = new ArrayList<Integer>();
		
		TreeSet<Integer> remainingOnsets = new TreeSet<Integer>(onsets);
		remainingOnsets.removeAll(sortedBeats);
		
		
		final double PERCENT_OFF_ALLOWANCE2 = 2 - PERCENT_OFF_ALLOWANCE;//0.28572;//0.19;//1.125;
		for(int i = 0; i + 1 < sortedBeats.size(); i++) {
			int next = sortedBeats.get(i + 1);
			int current = sortedBeats.get(i);
			int difference = next - current;
			double err2 = difference / runningDifference;//If within twice the size of a beat or greater.
			if(err2 >= PERCENT_OFF_ALLOWANCE2) {
				double avgNumBeats = difference / runningDifference;
				double numBeats = (int) Math.round(avgNumBeats);
				double distance = difference / numBeats;
				int floorDist = (int) Math.floor(distance);
				int ceilDist = (int) Math.ceil(distance);
				int avgDist = (int) Math.round(distance);
				
				// with an error of .125 it will take 8 notes to be off one whole beat.
				// with using the ceil or floor, the note is likely to be off.
				
				//This one doesn't make sense here!!!! because err can be greater than 1.2
				if(err2 <= 0.125) {
					floorDist = ceilDist = avgDist;
				}
				
				if(numBeats == 2) {//make sure correct
					ceilDist = avgDist;
				}
				
				NavigableSet<Integer> subSet = remainingOnsets.subSet(current, false, next, false);
				
				int currentPos = current;
				int totalOnsetsAdded = 0;
				for(int beat = 0; beat < numBeats - 1; beat++) {
					candidate = currentPos + avgDist;
					int added = 0;
					for (Integer subOnset : subSet) {
						double err3 = Math.abs(candidate - subOnset) / runningDifference;
						if(err3 < PERCENT_OFF_ALLOWANCE) { // maybe make this smaller in this instance
							if(added == 0) {
								missingBeats.add(subOnset); //Not likely to get here twice in this loop, but I am curious if it ever does.
								currentPos = subOnset; // maybe not
								added++;
								totalOnsetsAdded++;// this logic kind of fails if more than one was added this inner for loop.
							} else {
								System.out.println("ERROR: " + added + " onsets. Candidate: " + candidate + " Onset: " + onset);								
							}
						}
					}
					
					if(added == 0) {
						if((beat + totalOnsetsAdded) % 2 == 0) {
							currentPos += ceilDist;
						} else {
							currentPos += floorDist;
						}
						missingBeats.add(currentPos);
					}
				}
			}
		}
		
		sortedBeats.addAll(missingBeats);
		
		sortedBeats.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }
		});
		
		missingBeats.clear();
		
		final int lastOnset = onsets.get(onsets.size() - 1);
		int lastBeat = sortedBeats.get(sortedBeats.size() - 1);
		double totalBeatDifference = sortedBeats.get(sortedBeats.size() - 1) - sortedBeats.get(0);
		double newBeatAvg= totalBeatDifference / (sortedBeats.size() - 1);
		
		TreeSet<Integer> onsetSet = new TreeSet<Integer>(onsets);
		Integer nextOnset = onsetSet.higher(lastBeat);
		
		
		while(lastOnset > lastBeat && nextOnset != null) {
			//maybe get new average for beat differences.
			int diff = (nextOnset - lastBeat);
			double numBeats = diff / newBeatAvg;
			double rNumBeats = Math.round(numBeats);
			double err = Math.abs(numBeats - rNumBeats);
			
			if(err < PERCENT_OFF_ALLOWANCE) { //Is nextOnset a "power" of a beat length?
				double beatDiff = diff / rNumBeats;
				int rBD = (int) Math.round(beatDiff);
				int fBD = (int) Math.floor(beatDiff);
				int cBD = (int) Math.ceil(beatDiff); 
				
				// with an error of .125 it will take 8 notes to be off one whole beat.
				// with using the ceil or floor, the note is likely to be off.
				if(err <= 0.125) {
					fBD = cBD = rBD;
				}
				
				if(rNumBeats == 2) {
					cBD = rBD;
				}
				
				for(int beat = 0; beat < rNumBeats - 1; beat++) {
					if(beat % 2 == 0) {
						lastBeat += cBD;
					} else {
						lastBeat += fBD;
					}
					missingBeats.add(lastBeat);
				}
				missingBeats.add(nextOnset);
				lastBeat = nextOnset;			
			} else {
				//lastBeat = lastBeat;
			}
			nextOnset = onsetSet.higher(nextOnset);
		}
		
		if(lastBeat < lastOnset) {
			while(lastBeat < (lastOnset + (2 * newBeatAvg))) {
				lastBeat += newBeatAvg;
				missingBeats.add(lastBeat);
			}
		}
		
		sortedBeats.addAll(missingBeats);
		
		sortedBeats.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }
		});
		
		missingBeats.clear();
//		int firstOnset = onsets.get(0);
		int firstBeat = sortedBeats.get(0);
		
		//maybe don't do this again
		totalBeatDifference = sortedBeats.get(sortedBeats.size() - 1) - sortedBeats.get(0);
		newBeatAvg= totalBeatDifference / (sortedBeats.size() - 1);
		
		onsetSet = new TreeSet<Integer>(onsets);
		Integer previousOnset = onsetSet.lower(firstBeat);
		
		while(previousOnset != null) {//check this logic throughly
			int diff = firstBeat - previousOnset;
			double numBeats = diff / newBeatAvg;
			double rNumBeats = Math.round(numBeats);
			double err = Math.abs(numBeats - rNumBeats);
			
			if(err < PERCENT_OFF_ALLOWANCE) { //Is nextOnset a "power" of a beat length?
				double beatDiff = diff / rNumBeats;
				int rBD = (int) Math.round(beatDiff);
				int fBD = (int) Math.floor(beatDiff);
				int cBD = (int) Math.ceil(beatDiff); 
				
				// with an error of .125 it will take 8 notes to be off one whole beat.
				// with using the ceil or floor, the note is likely to be off.
				if(err <= 0.125) {
					fBD = cBD = rBD;
				}
				
				if(rNumBeats == 2) {
					cBD = rBD;
				}
				
				for(int beat = 0; beat < rNumBeats - 1; beat++) {
					if(beat % 2 == 0) {
						firstBeat -= cBD;
					} else {
						firstBeat -= fBD;
					}
					missingBeats.add(firstBeat);
				}
				missingBeats.add(previousOnset);
				firstBeat = previousOnset;	
			} else {
				//firstBeat = firstBeat
			}
			previousOnset = onsetSet.lower(previousOnset);
		}
		
		//int firstOnset = onsets.get(0);
		while(onsetSet.lower(firstBeat) != null) {
			firstBeat -= newBeatAvg;
			missingBeats.add(firstBeat);
		}
		
		sortedBeats.addAll(missingBeats);
		
		sortedBeats.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }
		});
		
		return sortedBeats;
	}

	   /**
     * Assumes that the first onset is a whole note. FainextOnset = onsetSet.higher(nextOnset);ls horribly if it isn't.
     * @param onsets
     * @param beatDifference
     * @return
     */
	public static List<Integer> beatTrackerBack(List<Integer> onsets, int beatDifference) {
		final double PERCENT_OFF_ALLOWANCE = 0.15;//0.19;//0.28572;//0.19;//1.125;
		//0.4;
		
		if(onsets.isEmpty())
			return null;
		
		List<Integer> beats = new ArrayList<Integer>();
		List<Integer> candidateBeats = new ArrayList<Integer>();
		
		int last = onsets.get(onsets.size() - 1) + 1;
		int first = onsets.get(0);
/*		for(int i = first; i < last; i+= beatDifference) {
			beats.add(i);
		}
*/		
		beats.add(onsets.get(0));
		
		System.out.println("Errors:");
		double runningDifference = beatDifference;
		boolean addedOnset = true;
		if(onsets.size() > 1)
		for(int i = 1; i < onsets.size(); i++) {
			int candidate = 0;
			if(candidateBeats.isEmpty()) {
				candidate = beats.get(beats.size() - 1) + beatDifference;
			} else {
				candidate = candidateBeats.get(candidateBeats.size() - 1) + beatDifference;
			}
				
			int onset = onsets.get(i);
			double error = (Math.abs(candidate - onset) / runningDifference); 
			
		/*	if(addedOnset)
				beats.remove(beats.size() - 1);
*/
			if(error <= (candidateBeats.size() + 1) * PERCENT_OFF_ALLOWANCE) {
				if(!candidateBeats.isEmpty()) {
					beats.addAll(candidateBeats);
					candidateBeats.clear();
					if(error > PERCENT_OFF_ALLOWANCE) {
						System.out.println("Saved from extra allowance.");
					}					
				}
				/*else {
					if(candidateBeats.isEmpty()) {
						System.out.println("E: Houston we have a problem!");
					}
				}*/
				
				beats.add(onset);
				addedOnset = true;
				System.out.println(onset + " " + error);
			} else {
				candidateBeats.add(candidate);
				
				// Don't skip over / give up on this onset yet
				if(onset > candidate)
					i--;
				
				
				addedOnset = false;
				System.out.println(candidate + " E: " + error);
			}			
		}
		
		/*if(addedOnset)
			beats.remove(beats.size() - 1);*/
		
		if(!candidateBeats.isEmpty()) {
			beats.addAll(candidateBeats);
		}
		
//		beats.removeAll(onsets);
		
		return beats;
	}
	
	private double getAverageFrequency(List<Double> frequencies, int start, int end) {
    	List<Double> subFreq = frequencies.subList(start, end);
		List<Double> modes = Util.mode(subFreq);
		if(modes.size() != 1) {
			Util.printErrorln("Expecting only one mode between onsets");
			//System.exit(1);
		}
		if(modes.size() == 0)
			System.err.println("Zero modes between onsets.");
		return modes.get(0);
		
		//Previous ideas for getting frequency.
		/*int middle = (previous + (current - previous) / 2) * 2;
		double freqMode = frequencies.get(middle);*/
		/*int last = current * 2;
   		freqMode = frequencies.get(last);*/
    }
    
	private int processOnsets(List<Integer> onsets) {
		Iterator<Integer> onsetIt = onsets.iterator();
    	
		List<Note> notes = new LinkedList<Note>();
    	List<Double> frequencies = data.getNormalizedFrequencies();
    	List<Integer> timeBetweenNotes = new LinkedList<Integer>();
    	int totalDiff = 0;
    	
    	Integer previous = null;
    	Integer current = null;
    	
    	if(onsetIt.hasNext())
    		previous = onsetIt.next() * 2;
    	
    	//TODO find when the last note ends!!! Or guess later based off of music info.
    	
    	
    	while(onsetIt.hasNext()) {
    		current = onsetIt.next() * 2;
    		double freqMode = getAverageFrequency(frequencies, previous, current);
    		Note note = FrequencyToNote.findNote(freqMode);
    		
    		if(note.getName() == Note.INVALID) {
    			System.err.println("Removed invalid note at: " + current);
    			onsetIt.remove();
    			continue;
    		}
    		
    		notes.add(note);
    		
    		int difference = current - previous;
    		timeBetweenNotes.add(difference);
    		totalDiff += difference;
    		
    		//Only for display purposes
    		System.out.println("[" + previous/2 + " - " + current/2 + "] " + freqMode + "\t" + note);
    		
    		previous = current;
    	}
  
		double freqMode = getAverageFrequency(frequencies, previous, frequencies.size());
		int index = frequencies.lastIndexOf(freqMode);//+ (current * 2);
		Note note = FrequencyToNote.findNote(freqMode);
		
		if(note.getName() == Note.INVALID) {
			System.err.println("Removed invalid note at last: " + current);
		} else {
			notes.add(note);
			//Only for display purposes
			System.out.println("[" + previous/2 + " - " + index/2 + "] " + freqMode + "\t" + note);

			int difference = index - previous;
			timeBetweenNotes.add(difference);
			totalDiff += difference;
		}
		
		
		//TODO this is betting on that there exists an exact mode, this can be disastrous if assumption is wrong.
		List<Integer> modes = Util.mode(timeBetweenNotes);
		if(modes.size() != 1) {
			Util.printErrorln("Expecting only one mode for differences between onsets.");
			//System.exit(1);
		}
		//Casting to double for division
		double mode = modes.get(0);
		
		//Round to nearest note durations.
		List<SheetNote> sheetNotes = new ArrayList<SheetNote>(timeBetweenNotes.size());
		double numBeats = 0;
		ListIterator<Integer> diffIt = timeBetweenNotes.listIterator();
		ListIterator<Note> noteIt = notes.listIterator();
		Util.println("Refresh");
		while(diffIt.hasNext()) {
			//beat duration as small as an eighth note (2).
			//TODO Somehow make this dynamic.
			double next = diffIt.next();
			double noteDuration = next / mode;//Util.fractionCeil(next / mode, 2);
			SheetNote sn = new SheetNote(noteIt.next(), noteDuration, false); // TODO set last argument some time.
			sheetNotes.add(sn);
			numBeats += noteDuration;
		}
		
		double averageNoteDuration = totalDiff / numBeats;
		//TODO add reasoning behind the divide by 4.
		double samplesPerNote = averageNoteDuration * data.getFftLength() / 2;
		double beatsPerSecond = data.getFormat().getSampleRate() / samplesPerNote;
		beatsPerSecond /= data.getOverlapPercentage(); 
		int beatsPerMinute = (int) Math.round(beatsPerSecond * 60);
		 
		SheetData sd = new SheetData();
		sd.setBeatsPerMinute(beatsPerMinute);
		sd.setKeySignature(KeySignature.deriveSignature(notes));
		
		
		//I think this is not the best, because, this averages the individual beat for the whole song.
		// Including longer notes. The longer notes are less precise.
		
/*		ListIterator<SheetNote> sIt = sheetNotes.listIterator();
		//Store in notes the fractional and the integer (timeBetweenNotes) durations.
		
		ListIterator<Integer> dIt = timeBetweenNotes.listIterator();
		
		while(sIt.hasNext()) {// && dit.hasNext();
			if(!dIt.hasNext()) {
				System.err.println("timeBetweenNotes and sheetNotes are not the same!!");
			}
			SheetNote sn = sIt.next();
			double noteDuration = Util.fractionCeil(dIt.next() / averageNoteDuration, 2);
			sn.setDuration(noteDuration);
		}
		*/
		
		sd.setNotes(sheetNotes);
		
		//TODO write significant time signatures.
		sd.setTimeSignatureNumerator(0);
		sd.setTimeSignatureDenominator(0);
		
		System.out.println(sd);
		
		return (int) mode;
	}
	
	private double convert(int[] signal, int sIndex, Double[] weights, int wIndex, int sLength) {
/*		long threadId = Thread.currentThread().getId();
        System.out.println("Thread # " + threadId + " is doing this task");*/
		if(sIndex < sLength)
			if(weights != null) {
				return  weights[wIndex] * signal[sIndex];
			}
			else {
				return signal[sIndex];
			}
		return 0;
	}
	
/*	private void enqueue(double value, IOQueue<List<Double>,Double> toAmp,
			IOQueue<List<Complex>, List<Double>> toComplex) {
		toAmp.add(value);
		to
	}*/
	
	/**
	 * Finds the original index of a signal based on the fftLength and the overlapPercentage.
	 * Make sure returned value is within the range of the original signal before indexing into it.
	 */
	private int findOriginalIndex(int index) {		
		// The index into an original FFT length sized portion
		int baseIndex = index / wholeFinished;
		baseIndex = baseIndex * fftLength;
		
		int remainder = index % wholeFinished;
		
		int overlapBase = remainder / fftLength;
		overlapBase = overlapBase * overlapIncrement;
		
		int overlapIndex = remainder % fftLength;
		
		return baseIndex + overlapBase + overlapIndex;
	}
	
	public double calculateFrequencyFromOriginalSignal(int overlapFromIndex, int overlapToIndex) {
		Double[] fft = compute1FftOnOriginalSignal(overlapFromIndex, overlapToIndex);
		//int bin =  Util.maxIndex(fft, 0, fft.length / 2);
		
		int bin = FindFrequency.findFundamentalBin(Arrays.asList(fft).subList(0, fft.length / 2));
		
		return FundamentalFrequency.computeFrequency(bin, data.getFormat().getSampleRate(), fft.length);
	};
	
	/**
	 * The idea is to find a more accurate frequency by recomputing the ffts for this object 
	 *      with a larger fft length than the original between fftIndex1 and fftIndex2. 
	 *      With a smaller ratio sampleRate / fftLength, there is more precision.
	 * 
	 * @param fftIndex1 inclusive
	 * @param fftIndex2 exclusive
	 * @return
	 */
	public Note calculateNote(int fftIndex1, int fftIndex2, List<Integer> continualBins) {
		int fromIndex = findOriginalIndex(fftIndex1 * fftLength);
		int toIndex = findOriginalIndex(fftIndex2 * fftLength);
		
		int[] signal = Arrays.copyOfRange(data.getOriginalSignal(), fromIndex, toIndex);
		// Warning don't change Format.
		AudioData audioData = new AudioData(data.getFormat(), signal);
		ProcessSignal ps = new ProcessSignal(audioData, 0.25, fftLength * 2);
		ps.queues(false, false);
		List<Integer> bins = new ArrayList<Integer>();
		for(double[] fft : audioData.getAbsolute()) {
			bins.add(FindFrequency.findFundamentalBin(new DoubleList(fft).subList(0, fft.length / 2)));
		}
		List<Integer> binModes = Util.mode(bins);
		Util.logIfFails(binModes.size() == 1, "caculateNote: More than one mode. " + bins + " modes : " + binModes);
		Util.logIfFails(!(binModes.size() == 2 && Math.abs(binModes.get(0) - binModes.get(1)) > 2), "caculateNote: Two modes. Picking the lower. " + bins + " modes : " + binModes);
		Util.verify(binModes.size() < 3, "caculateNote: More than two modes. " + bins + " modes : " + binModes);
		int bin = binModes.get(0);
		if(binModes.size() == 2 && Math.abs(bin - binModes.get(1)) > 2) {
			bin = Math.min(bin, binModes.get(1)); // Assuming that they are harmonics, picking the lower.
		}
		continualBins.add(bin);
		
		Note note = FindFrequency.computeNote(bin, audioData.getFormat().getSampleRate(), audioData.getFftLength());
		System.out.println(count + " " + note + " bin: " + bin  + " modes: " + binModes + " bins: " + bins);
		count = (count + 1) % 113;
		return note;
	}
	public static int count = 0;
	/**
	 * Indexes are an index into overlapped data. i.e. The indexes pertaining to toAmp, or toComplex which
	 * are passed to / added into by computeComplexAndOverlap3
	 * @param overlapFromIndex inclusive
	 * @param overlapToIndex exclusive
	 */
	public Double[] compute1FftOnOriginalSignal(int overlapFromIndex, int overlapToIndex) {
		System.out.println(data.getOriginalSignal().length);
		System.out.println(signal.length);
		int fromIndex = findOriginalIndex(overlapFromIndex * fftLength);
		int toIndex = findOriginalIndex(overlapToIndex * fftLength);
		int newfftLength = toIndex - fromIndex;
		// Am I sure that I want to do the ceiling. I think I want to do the floor.
		newfftLength = Util.floorBase2(newfftLength);
		System.out.println("[" + overlapFromIndex + " - " + overlapToIndex + "] + [" + fromIndex + " - " + toIndex + "], diff: " + (toIndex - fromIndex) + " fftLen: " + newfftLength);
		FFT instance = FFT.getInstance(newfftLength);
		Complex[] toFft = new Complex[newfftLength];
		Double[] weightsThenData = getHannWeights(newfftLength);
		int hannIndex = 0;
		for(int i = fromIndex; i < fromIndex + newfftLength; i++) {
			toFft[hannIndex] = new Complex(weightsThenData[hannIndex] * signal[i]);
			hannIndex++;
		}
		instance.fft(toFft);
		for(int i = 0; i < newfftLength; i++) {
			weightsThenData[i] = toFft[i].absolute();
		}
		return weightsThenData;
	}
	
	private void computeComplexAndOverlap3(boolean doHann, IOQueue<double[],Double> toAmp, IOQueue<Complex[], double[]> toComplex) {
		int fftLength = data.getFftLength();
		double overlapPercentage = data.getOverlapPercentage();
		int[] signal = data.getOriginalSignal();
		int increment = (int) (fftLength * overlapPercentage);
		//Rough estimate of new size.
		//List<Complex> complexData = new ArrayList<Complex>((int) ((1/overlapPercentage) * signal.length));
		//List<Double> overlapedData = new ArrayList<Double>();
		
		final Double[] weights = (doHann)? getHannWeights(fftLength) : null;
		if(weights != null) {
			data.setDataHanned();
		}
		data.setDataWindowed();
		double value;
		
		//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
		
		for(int i = 0; i <= signal.length - increment; i+= increment) {
			// System.out.println(i + " " + signal.length);
			int hanIndex = 0;
			double[] abs = new double[fftLength];
			Complex[] complex = new Complex[fftLength];
			
			for(int j = i; j < i + fftLength; j++) {
/*				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				
				// System.out.println(j);
				//System.out.println("w: " + windowIndex + " j:" + j);
				if(j < signal.length)
					if(data.isDataHanned()) {
						value = weights[hanIndex] * signal[j];
					}
					else {
						value = signal[j];
					}
				else
					value = 0;
				
				abs[hanIndex] = Math.abs(value);
				complex[hanIndex] = new Complex(value);
				hanIndex++;
			}
			if(toAmp != null) {
				toAmp.add(abs);
			}
			toComplex.add(complex);
			
			//Util.totalTimeDiff("wostreams");
			
			
			
			
/*			
			Util.totalTimeDiff("streams");
			final int base = i;
			List<Double> preFFt2 = IntStream.range(i, i + fftLength).parallel()
					.mapToDouble(v -> convert(signal, v, weights, v - base, signal.length))
					.boxed().collect(Collectors.toList());
			
			// mapToDouble(d -> Math.abs(d)).boxed()
			//System.out.println(preFFt2.parallelStream().isParallel() + " is parallel");
			List<Double> abs2 = preFFt2.parallelStream().map(Math::abs).collect(Collectors.toList());
			toAmp.add(abs2);
			List<Complex> complex2 =  preFFt2.parallelStream().map(Complex::new).collect(Collectors.toList());
			//toComplex.add(complex2);
			Util.totalTimeDiff("streams");
*/
			
			/*IntStream.range(0, list.size())
				.flatMap(i -> DoubleStream.of(t))
			
	        	.filter(i -> names[i].length() <= i)
	         .mapToObj(i -> names[i])
	         .collect(Collectors.toList());*/
			
			/*			OfInt spliterator = Arrays.spliterator(signal, i, i + fftLength);
			OfInt sp2 = spliterator.trySplit();
			
			OfInt sp3 = spliterator.trySplit();*/
		}
		//Util.totalTime("streams");
		//Util.totalTime("wostreams");
		
		
		/*Util.timeDiff("overlapArray");
		data.setOverlappedData(Arrays.asList(overlapedData.toArray(new Double[overlapedData.size()])));
		Util.timeDiff("overlapArray");
		Util.timeDiff("complexArray");
		data.setComplexData(complexData.toArray(new Complex[complexData.size()]));
		Util.timeDiff("complexArray");*/

	}
	
	private void computeComplexAndOverlap2(boolean doHann) {
		int fftLength = data.getFftLength();
		double overlapPercentage = data.getOverlapPercentage();
		int[] signal = data.getOriginalSignal();
		int increment = (int) (fftLength * overlapPercentage);
		//Rough estimate of new size.
		List<Complex> complexData = new ArrayList<Complex>((int) ((1/overlapPercentage) * signal.length));
		List<Double> overlapedData = new ArrayList<Double>();
		
		Double[] weights = null;
		if(doHann) {
			weights = getHannWeights(fftLength);
			data.setDataHanned();
		}
		data.setDataWindowed();
		double value;
		
		//int windowIndex = 0;
		//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
		for(int i = 0; i <= signal.length - increment; i+= increment) {
			// System.out.println(i + " " + signal.length);
			int hanIndex = 0;
			for(int j = i; j < i + fftLength; j++) {
				// System.out.println(j);
				//System.out.println("w: " + windowIndex + " j:" + j);
				if(j < signal.length)
					if(data.isDataHanned()) {
						value = weights[hanIndex] * signal[j];
					}
					else {
						value = signal[j];
					}
				else
					value = 0;
				
				overlapedData.add(Math.abs(value));
				complexData.add(new Complex(value));
				
				//windowIndex++;
				hanIndex++;
			}
		}
		Util.timeDiff("overlapArray");
		data.setOverlappedData(Arrays.asList(overlapedData.toArray(new Double[overlapedData.size()])));
		Util.timeDiff("overlapArray");
		Util.timeDiff("complexArray");
		data.setComplexData(complexData.toArray(new Complex[complexData.size()]));
		Util.timeDiff("complexArray");

	}
            	
	private void computeComplexAndOverlap(boolean doHann) {
/*		int fftLength = data.getFftLength();

		double overlapPercentage = data.getOverlapPercentage();
		//TODO this is only working for 50% right now
		if (overlapPercentage != 0.5) {
			System.err.println("Another overlap is not working fully yet.");
			System.exit(1);
		}
		
		int[] signal = data.getOriginalSignal();
		
		// TODO check to see if this is a reasonable overlap
		int increment = (int) (fftLength * overlapPercentage);
		
		
		int len = signal.length;
		int newSize = (int) (Math.round((double)len / fftLength) * fftLength +
				Math.round((double)(len - increment) / fftLength) * fftLength);
		
		Complex[] complexData = new Complex[newSize];
		
		List<Double> weights = null;
		if(doHann) {
			weights = getHannWeights(fftLength);
			data.setDataHanned();
		}
		data.setDataWindowed();
		
		int windowIndex = 0;
		//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
		for(int i = 0; i <= signal.length - increment; i+= increment) {
			int hanIndex = 0;
			for(int j = i; j < i + fftLength; j++) {
				//System.out.println("w: " + windowIndex + " j:" + j);
				if(j < signal.length)
					if(data.isDataHanned())
						complexData[windowIndex] = new Complex(hann(hanIndex, signal[j], weights));
					else
						complexData[windowIndex] = new Complex(signal[j]);
				else
					complexData[windowIndex] = new Complex(0);
				windowIndex++;
				hanIndex++;
			}
		}
		data.setComplexData(complexData);
		
	*/
	}
	
	private static Double[] getHannWeights(int fftLength) {
		Double[] weights = new Double[fftLength];
    	for(int i = 0; i < fftLength; i++) {
    		double weight = 
    				Math.pow(
    	    				Math.sin((Math.PI*i) / (fftLength -1)),
    	    				2);
    		weights[i] = weight;
    	}
    	return weights;
	}
	
				
	/**
	 * Applies an RC low-pass filter to x 
	 * @param x array to apply filter to
	 * @param dt time interval
	 * @param RC time constant
	 * @return
	 */
	static List<Double> lowPass(final List<Double> x, double dt, double RC) {
		List<Double> y = new ArrayList<Double>(x.size());
		double alpha = dt / (RC + dt);
		y.add(x.get(0));
		
		for(int i = 1; i < x.size(); i++) {
//			y[i] = alpha * x[i] + (1-alpha) * y[i-1];
//			y[i] = y[i - 1] + alpha * (x[i] - y[i-1]);
			y.add(y.get(i - 1) + alpha * (x.get(i) - y.get(i - 1)));
		}
		return y;
	}
	
	static List<Double> lowPass(final List<Double> x) {
		return lowPass(x, 5, 20);//5,20 //1,2
	}
				
	/**
	 * Returns a copy of data with the size doubled and the
	 * second half set to zero.
	 * @param data
	 * @return
	 */
	private Complex[] doubleAndPad(Complex[] data) {
		Complex[] doubledData = Arrays.copyOf(data, data.length * 2);
		//Set the second half to zero
		for(int i = data.length; i < doubledData.length; i++) {
			doubledData[i] = new Complex(0);
		}
		return doubledData;
	}
	
	private void addToFFT(double d, boolean doDouble) {
		List<Double> fftAbsolute = data.getFftAbsolute();
		if(fftAbsolute == null) {
			if(doDouble)
				data.setFftAbsolute(new ArrayList<Double>(data.getComplexData().length * 2));
			else 
				data.setFftAbsolute(new ArrayList<Double>(data.getComplexData().length));
			fftAbsolute = data.getFftAbsolute();
		}
		
		fftAbsolute.add(d);
	}

	//TODO ensure complexData is divisible by the fftLength");
	private double[] computeFFT(Complex[] complexData, FFT fftInstance) {
		// FFT.setNumBits(origFFTLength);
		int fftLen = complexData.length;
		double[] fftAbsolute = new double[fftLen];
		
		// I shouldn't have to copy complexData we are throwing it away right after this anyways.
		//Util.verify(complexData instanceof JArray, "1. You didn't pass me an efficient array");
		//Util.verify(complexData instanceof JArray<?>, "2. You didn't pass me an efficient array");
		Util.totalTimeDiff("FORWARD");
		// This should be the original backing array.
		Complex[] toFft = complexData;
		fftInstance.fft(toFft);
		Util.totalTimeDiff("FORWARD");
		
		
		Util.totalTimeDiff("ABSOLUTE");
		for(int j = 0; j < toFft.length; j++) {
			fftAbsolute[j] = toFft[j].absolute();
		}
		Util.totalTimeDiff("ABSOLUTE");
		return fftAbsolute;
	}	
	
	//TODO ensure complexData is divisible by the fftLength");
	private void computeFFtsAndFilter() {
		int multiply = 1;
		
		Complex[] complexData = data.getComplexData();
		int origFFTLength = data.getFftLength();
		
		//Double the size of things
		data.setFftAbsolute(new ArrayList<Double>(complexData.length * multiply));
		//data.setFftLowPassAbsolute(new Double[complexData.length * multiply]);

		int newFFTLength = origFFTLength * multiply;
		//TODO Future self this may be the source of your errors.
		data.setFftLength(newFFTLength);
		
		List<Double> fftAbsolute = data.getFftAbsolute();
		//Double[] fftLowpass = data.getFftLowPassAbsolute();
		
		FFT.setNumBits(origFFTLength * multiply);
		FFT fft = FFT.getInstance(origFFTLength * multiply);
		
		
		for(int i = 0; i < complexData.length; i+= origFFTLength) {
			Util.totalTimeDiff("COPY");
			Complex[] toFft = Arrays.copyOfRange(complexData, i, i + origFFTLength); // TODO don't need to copy if in a List trashing data after this.
			Util.totalTimeDiff("COPY");
			
			//Double the length and pad for linear (instead of cyclic) autocorrelation.
			Util.totalTimeDiff("DOUBLE");
			if(multiply > 1)
				toFft = doubleAndPad(toFft);
			Util.totalTimeDiff("DOUBLE");
			
			Util.totalTimeDiff("FORWARD");
			//FFT.fftForward(toFft);
			fft.fft(toFft);
			Util.totalTimeDiff("FORWARD");
			
			//Low pass filter test
			Util.totalTimeDiff("ABSOLUTE");
			//List<Double> lp = new ArrayList<Double>(toFft.length);//double[toFft.length];
			for(int j = 0; j < toFft.length; j++) {
				double absolute = toFft[j].absolute();
				fftAbsolute.add(absolute);
				
				//fftAbsolute[(i*2)+j] = absolute;
	//			fftLowpass[(i*multiply)+j] = absolute;		//TODO this is temporarily removing the low pass effectively!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!	
			//	lp.add(absolute);					//TODO removed this
			}
			Util.totalTimeDiff("ABSOLUTE");
			
			//TODO this is temporary !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!111
/*			
			lp = lowPass(lp);
			// DO twice maybe
			
			for(int j = 0; j < toFft.length; j++) {	
				fftLowpass[(i*2)+j] = lp.get(j);//lp[j];		//TODO WARNING this is because the FFT was doubled 
				//In case filter is not applied
				//fftAbsolute[(i*2)+j] = toFft[j].absolute();
			}*/
		}
		Util.totalTime("COPY");
		Util.totalTime("DOUBLE");
		Util.totalTime("FORWARD");
		Util.totalTime("ABSOLUTE");
		Util.totalTime("REV");
		Util.totalTime("EXPO");
		Util.totalTime("NULLCHECK");
		Util.totalTime("NOTHING");
	}
	
	public void printNonConsecutiveNotes(boolean showFrequencies) {
		String lastNote = "";
		String[] notes = data.getNoteNames();
		
		List<Double> frequencies = null;
		if(showFrequencies)
			frequencies = data.getFrequencies();
		
		for(int i = 0; i < notes.length; i++) {
			if(showFrequencies)
				System.out.print(frequencies.get(i) + " " + notes[i] + " ");
			else
				System.out.print(notes[i] + " ");
			
			if (!lastNote.equals(notes[i])) {
				System.out.println();
			}
			lastNote = notes[i];
		}
	}
	
	private void computeAutoCorrelation() {
		List<Complex> complex = Arrays.asList(data.getComplexData());
		data.setAutoCorrelationAbsolute(new LinkedList<Double>());
		List<Double> absolutes = data.getAutoCorrelationAbsolute();
		
		int fftLength = data.getFftLength();
		int position = 0;
		while(position + fftLength <= complex.size()) {
			absolutes.addAll(Arrays.asList(
					autoCorrelation(complex.subList(position, position + fftLength).toArray(new Complex[fftLength]))
					));
			position += fftLength;
		}
		
		//TODO set a variable in AudioData showing that the FFT was doubled
		data.setFftLength(fftLength * 2);
	}
	
    private void computeFrequenciesFromAutocorrelation() {
		
	}
	
	/**
	 * Assumes complexData is the desired length of data to compute on.
	 * Warning: It will double the length to compute the FFT length. Thus,
	 *  the bins have to be accounted for this change.
	 *  
	 * The first part of the autocorrelation is undesired because obviously
	 * the signal is correlated with itself (i.e. no lag)
	 * 
	 * //TODO probably the problem is in the size of the sample rate.
	 * @param complexData
	 */
	private Double[] autoCorrelation(final Complex[] complexData) {
		//http://stackoverflow.com/questions/3949324/calculate-autocorrelation-using-fft-in-matlab#3950398
		Complex[] toFFT = doubleAndPad(complexData);
		
		Double[] autoCorrelationAbsolute = new Double[toFFT.length];
				
		FFT.fftForward(toFFT);
		
		for(int j = 0; j < toFFT.length; j++) {
			//Same as Complex.mult(toFFT[j], toFFT[j].conjugate()) but simpler
			double square = toFFT[j].absoluteSquare();
			addToFFT(Math.sqrt(square), true);
			toFFT[j] = new Complex(square);
			//Someone said doing it twice will help
			//(I think it is supposed to emphasize the peaks)
			//toFFT[j] = new Complex(toFFT[j].absoluteSquare());
		}
		
		//Effective inverse FFT
		//FFT() = IFFT() for real numbers.
		FFT.fftForward(toFFT);
		
		for(int i = 0; i < toFFT.length; i++) {
			autoCorrelationAbsolute[i] = toFFT[i].absolute();
		}
		return autoCorrelationAbsolute;
	}
		
	/**
	 * Computes one cepstrum based on the entire complexData array.
	 * @param complexData
	 */
	private Double[] cepstrum(final Complex[] complexData) {
		Complex[] toFFT = Arrays.copyOf(complexData, complexData.length);
		FFT.fftForward(toFFT);
		for(int j = 0; j < complexData.length; j++) {
			//maybe hann first
			toFFT[j] = new Complex(Math.log(toFFT[j].absoluteSquare()));
		}
		FFT.fftInverse(toFFT);
		Double[] cepstrum = new Double[complexData.length];
		for(int j = 0; j < complexData.length; j++) {
			cepstrum[j] = toFFT[j].absoluteSquare();
		}
		return cepstrum;
	}
}


/*
 *    public static void main(String args[]) {
    	for(byte length = 7; length < 22; length++)
    	{
	    	//byte length = 4;//4
	    	byte[] bites = new byte[length];
	    	for (byte i = 0; i < length; i++) {
	    		bites[i] = i;
	    	}
	    	//byte[] bites = {0,1,2,3,4,5,6,7,8,9,10};//,11};//,9,10,11,12,13,14,15,16,17};
	    	AudioFormat format = CaptureAudio.getDefaultFormat();
	    	AudioData ad = new AudioData(bites, format);
	
	    	
	    	ProcessSignal ps = new ProcessSignal(ad,.5,4);
	    	//ps.computeComplexAndOverlap(/*.50, 4*);
	    	ps.process();
	    	
	    	System.out.println("Complex Data");
	    	int i = 0;
	    	for(Complex c : ad.getComplexData()) {
	    		System.out.println("i: " + i + "\t" + c.absolute() + "\t" + c);
	    		i++;
	    	}
	    	
	    	Double[] fftAbsolute = ad.getFftAbsolute();
	    	for(i = 0; i < fftAbsolute.length; i++) {
	    		System.out.println(ps.computeFrequency(i) + " " + fftAbsolute[i]);
	    	}
    	}
    }
 */

/**
 * FFT and absolute
 *
private void fftAbsolute() {
	Complex[] complexData = data.getComplexData();
	Double[] fftData = new Double[complexData.length];
	int fftIndex = 0;
	int fftLength = data.getFftLength();
	for(int i = 0; i < complexData.length; i+= fftLength) {
		Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
		FFT.fftForward(toFft);
		
		// TODO maybe store toFfft in a big array if needed for another algorithm.
		for(int j = 0; j < fftLength; j++) {
			fftData[i+j] = toFft[j].absolute();
		}
	}
	data.setFftAbsolute(fftData);
}

private void fft() {
	Complex[] complexData = data.getComplexData();
	Complex[] fftData = new Complex[complexData.length];
	int fftLength = data.getFftLength();
	for(int i = 0; i < complexData.length; i+= fftLength) {
		Complex[] toFft = Arrays.copyOfRange(complexData, i, i + fftLength);
		FFT.fftForward(toFft);
		
		// TODO maybe store toFfft in a big array if needed for another algorithm.
		for(int j = 0; j < fftLength; j++) {
			fftData[i+j] = toFft[j];
		}
	}
	data.setFft(fftData);
}
*/

/*
private void setNotenames() {
	Double[] frequencies = data.getFrequencies();
	String[] noteNames = new String[frequencies.length];
	for(int i = 0; i < frequencies.length; i++) {
		noteNames[i] = FrequencyToNote.findNote(frequencies[i]);
	}
	data.setNoteNames(noteNames);
}

private void setNormalizedFrequencies() {
	Double[] frequencies = data.getFrequencies();
	Double[] normalizedFrequencies = new Double[frequencies.length];
	for(int i = 0; i < frequencies.length; i++) {
		normalizedFrequencies[i] = FrequencyToNote.findFrequency(frequencies[i]);
	}
	data.setNormalizedFrequencies(normalizedFrequencies);
}
*/

/**
public static Double findMax(Double[] absolutes, int fftLength, AudioData data) {
	double max = 0;
	int maxIndex = -1;
	int halfFFT = fftLength / 2;
	//int halfFFT = fftLength;
	for(int i = 0; i < halfFFT /*fftLength*; i++) {
		if(absolutes[i] > max) {
			max = absolutes[i];
			maxIndex = i;
		}
	}
	return computeFrequency(maxIndex % fftLength, data);
}
*/

//I am sure this is garbage
/*
     private void setFrequencies() {
    	Double[] absolute = null;
    	if((absolute = data.getFftAbsolute()) == null &&
    	   (absolute = data.getFftCepstrum()) == null &&
    	   (absolute = data.getAutoCorrelationAbsolute()) == null)
    	{
    		System.err.println("Absolute data is null");
			System.exit(0);
    	}
    	
    	int fftLength = data.getFftLength();
    	int frequencyLength = absolute.length / fftLength;
    	
    	System.out.println("absolute.length: " + absolute.length + " fftLength:" + fftLength + " fL: " + frequencyLength);
    	
    	if(frequencyLength * fftLength != absolute.length) {
    		System.out.println("The array is not divisible by the fftLength");
    		System.exit(0);
    	}
    	Double[] frequencies = new Double[frequencyLength];
    	
    	int freqIndex = 0;
    	for(int i = 0; i < absolute.length; i+= fftLength) {
    		frequencies[freqIndex] = findMax(absolute, i, fftLength);
    		freqIndex++;
    	}
    	
    	data.setFrequencies(frequencies);
    }
 */
