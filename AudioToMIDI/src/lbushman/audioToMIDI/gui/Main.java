package lbushman.audioToMIDI.gui;

//Reddit forums
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import lbushman.audioToMIDI.io.CaptureAudio;
import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.io.PlayAudio;
import lbushman.audioToMIDI.io.ReadAudioFile;
import lbushman.audioToMIDI.io.WriteAudioFile;
import lbushman.audioToMIDI.processing.AudioData;
import lbushman.audioToMIDI.processing.Complex;
import lbushman.audioToMIDI.processing.DownBeatData;
import lbushman.audioToMIDI.processing.DownBeatDetection;
import lbushman.audioToMIDI.processing.FrequencyToNote;
import lbushman.audioToMIDI.processing.FundamentalFrequency;
import lbushman.audioToMIDI.processing.Pair;
import lbushman.audioToMIDI.processing.Peaks;
import lbushman.audioToMIDI.processing.PossibleDownBeat;
import lbushman.audioToMIDI.processing.ProcessSignal;
import lbushman.audioToMIDI.processing.RunningWindowStats;
import lbushman.audioToMIDI.util.Util;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
	CaptureAudio audio;
    Button captureButton;
    Button openButton;
    Button displayFFTButton;
    AudioData audioData;
//	private Graph acGraph;
    private Graph centerGraph;
    
	private Graph fftGraph;
		
	@Override public void start(Stage stage) {
		Util.setDebugMode(false);
		
		int index = FrequencyToNote.findIndex(261.625);
		Note n = FrequencyToNote.toNote(index);
		
        stage.setTitle("Signal Processing Senior Project");       
        
//        acGraph = new Graph("Autocorrelation", "Nth Sample", "Power");
        centerGraph = new Graph("Spectral Flux", "Nth sample", "Power");
        fftGraph = new Graph("FFT", "Nth Sample", "Power");
        
       	BorderPane border = new BorderPane();
    	HBox box = addHBox();
    	border.setTop(box);
//    	border.setCenter(acGraph);
    	border.setCenter(centerGraph);
    	border.setBottom(fftGraph);
    	
    	Scene scene = new Scene(border,1000,700);

    	box.requestFocus();
        stage.setScene(scene);
        stage.show();
/*        openButton.setDisable(false);
        openButton.fire();*/
    }
	        
    private void readFromFile(File file) {
		ReadAudioFile audio = new ReadAudioFile(file);
		audio.readFile();
		audioData = null;
		audioData = new AudioData(
				audio.getStream().toByteArray(),
				audio.getFormat());
		processSignal(audioData, null);
    }
    
    private void processSignal(AudioData audioData, File file) {		
		// Play the data
		playClip(audioData.getSampledData(), audioData.getFormat());
		
		// Save the data
		if(file != null) {
	    	writeToFile(audioData.getSampledData(), file, audioData.getFormat());
		}
		Util.println("Reset2");
		ProcessSignal ps = new ProcessSignal(audioData, 
				/*0.120*/ 0.25/*overlap of FFTs*/, 2048 /*original fftLength */); //8192

		
		//16384//.25, 4096 moderate both
		//20480//.20, 4096 better but slower
		//40960//.20, 8192 better fft, worse spectrum and slower
		//5120//.20, 1024 horrible fft, not horrible spectrum, slow
		//2048//.5,  1024 horrible fft, almost perfect spectrum
		
		
		//.5, 4096, great for fft
		//.25, 1024 really great for spectrum
		
		
		
		ps.process();
		
		//TODO why is audioData being passed in?
		this.audioData = audioData;
		
		//TODO ensure multiple reads resets the data appropriately. I think it is.
		
		
		//TODO use this to limit graph indexing, etc.
		//	data.getNumFFT();
													
		
		//TODO possibly show frequencies, normalizedFrequencies, and note names in a graph.
		
		//TODO show notes where consecutive duplicates are not shown.
		//ps.printNonConsecutiveNotes(false);
	
		List<Double> spectralFlux = audioData.getSpectralFlux();
if(false) {
		displayCenterGraph(spectralFlux);

		List<Double> normalized = new ArrayList<Double>();
		
//		Double[] amp = ProcessSignal.computeAmp(audioData);
		int i = 0;
		for(Double d : audioData.getFrequencies()) {
			Double dd = FrequencyToNote.findFrequency(d);
			normalized.add(dd);
			Util.println("[" + i + "] " + dd + "\t" + FrequencyToNote.findNote(dd) /*+ " " + amp[i]*/);
			i++;
		}
		audioData.setNormalizedFrequencies(normalized);
}		
		//Double fftAbsolute[] = audioData.getFftAbsolute();
		Double fftLowpass[] = audioData.getFftLowPassAbsolute();
		
		/*for(int i = 0; i < fftAbsolute.length;i++) {
			if(fftAbsolute[i] != fftLowpass[i]) {
				Util.println(i + " " + fftAbsolute[i] + "\t" + fftLowpass[i]);
			}
		}
		System.err.println("END OF DIFF!");*/
		
		
		//displayAC(0);
		displayFFt(0, Arrays.asList(fftLowpass));
		System.out.println("done with process signal");
    }
    
    private void updateGraph(Graph graph, Number[] data) {
    	graph.updateList(data);
    }
    
    private <T> void updateGraph(Graph graph, List<T> dataList) {
    	Number[] data = dataList.toArray(new Number[dataList.size()]);
    	graph.updateList(data);
    }
    
    private <T> void updateGraph(Graph graph, List<T> dataList, int index) {
    	Number[] data = dataList.toArray(new Number[dataList.size()]);
    	
    	int start = index * audioData.getFftLength();
    	int end = start + audioData.getFftLength();
    	Number[] range = Arrays.copyOfRange(data, start, end);
    	
    	graph.updateList(range);
    }
     
    private void playClip(byte[] signal, AudioFormat format) {
    	PlayAudio play = new PlayAudio(signal, format);
    	play.playClip();	
    }
    
    private void playClip(byte[] signal, AudioFormat format, int begin, int end) {
    	byte[] subSignal = Arrays.copyOfRange(signal, begin, end);
    	playClip(subSignal, audioData.getFormat());
    }
    
    private HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 12, 10, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        TextField captureField = new TextField();
        captureField.setPrefSize(125, 20);
        captureField.promptTextProperty().set(".wav");
        TextField openField = new TextField();
        openField.setPrefSize(125, 20);
        openField.promptTextProperty().set(".wav");
        captureButton = new Button("Capture");
        openButton = new Button("Open File");
        
        Label label1 = new Label("Save File:");

        
        captureField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if(newValue.length() < 5 || !newValue.endsWith(".wav")) {
					captureButton.setDisable(true);
				} else {
					captureButton.setDisable(false);
				}	
			}
         });
        
        captureButton.setDisable(true);
        captureButton.setPrefSize(100, 20);
        captureButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(captureButton.getText() == "Capture") {
					captureField.setDisable(true);
					//captureButton.setDisable(false);
					captureButton.setText("Stop");
					Thread myThread = new Thread() {
						@Override
						public void run() {
							//readData(false, new File(captureField.getText()));
							startCapture();
						}
					};
					myThread.start();
				} else {
					stopCapture(new File(captureField.getText()));
					captureButton.setText("Capture");
					captureField.setDisable(false);
				}	
			}
		});

        Label label2 = new Label("Open File:");
        openField.setText("maryDown.wav");
        
        openField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if(newValue.length() < 5 || !newValue.endsWith(".wav") || 
						!(new File(newValue).isFile())) {
					openButton.setDisable(true);
				} else {
					openButton.setDisable(false);
				}	
			}
         });
        
        openButton = new Button("Open File");
        openButton.setDisable(true);
        openButton.setPrefSize(100, 20);
        openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
//				readData(true, new File(openField.getText()));
				openButton.setDisable(true);
				openField.setDisable(true);
				readFromFile(new File(openField.getText()));
				openButton.setDisable(false);
				openField.setDisable(false);
			}
		});
        
        Button frequencyButton = new Button("Show Frequencies");
        frequencyButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				displayFrequencies();
			}
		});
        
        TextField nthField = new TextField();
        nthField.setPrefSize(50, 20);
        nthField.promptTextProperty().set("Nth Graph");

/*        Button nthAutoCorrelation = new Button("Display AC[N]");
        nthAutoCorrelation.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				displayAC(Integer.parseInt(nthField.getText()));
			}
		});*/

        Button nthFFT = new Button("Display FFT[N]");
        nthFFT.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
//				displayFFt(Integer.parseInt(nthField.getText()), audioData.getAutoCorrelationAbsolute());
				displayFFt(Integer.parseInt(nthField.getText()), 
						Arrays.asList(audioData.getFftLowPassAbsolute()));
			}
		});
        
        TextField freqIndex = new TextField();
        freqIndex.promptTextProperty().set("Note index");
        freqIndex.setPrefSize(50, 20);
        
        TextField noteAndFreqField = new TextField("Freq and Note");
        noteAndFreqField.setEditable(false);
        noteAndFreqField.setPrefSize(50, 20);
        
        Button noteAndFreqButton = new Button("Show Note");
        noteAndFreqButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String text = getFreqAndNote(Integer.parseInt(freqIndex.getText()));
				noteAndFreqField.setText(text);
			}
		});
        
        TextField playBeg = new TextField();
        playBeg.promptTextProperty().set("Beg");
        playBeg.setPrefSize(50, 20);
        
        TextField playEnd = new TextField();
        playEnd.promptTextProperty().set("End");
        playEnd.setPrefSize(50, 20);
        
        Button playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				int begin = Integer.parseInt(playBeg.getText());
				int end = Integer.parseInt(playEnd.getText());
				
				//int originalLen = audioData.getFftLength() / 2;
				//int overLapLen = originalLen / 2;
				int len = audioData.getFftLength();
				
				double percentage = audioData.getOverlapPercentage() * 2;
				len *= percentage;
				
				begin = begin * len;
				end = end * len;
				
		    	playClip(audioData.getSampledData(), audioData.getFormat(), begin, end);
			}
		});
        
        hbox.getChildren().addAll(label1, captureField, captureButton, 
        		label2, openField, openButton, frequencyButton, nthField, 
        		/*nthAutoCorrelation,*/ nthFFT, freqIndex, noteAndFreqButton,
        		noteAndFreqField, playBeg, playEnd, playButton);
        
        return hbox;
    }
    
    private String getFreqAndNote(int index) {
    	double frequency = FundamentalFrequency.computeFrequency(index, audioData);
    	String closestNote = FrequencyToNote.findNote(frequency).toString();
    	return frequency + "Hz " + closestNote;
    }
    
    public int avgOnsetDiff(List<Integer> differences) {
		List<Integer> modes = Util.mode(differences);
		// TODO There is a chance this may fail. Seek further implementation.
		Util.logIfFails(modes.size() == 1, "More than one mode for differences between onsets. modes: " + modes);
		return (int) Math.round(Util.average(modes));
    }
    
    public List<Integer> onsetDiff(List<Integer> onsets) {
    	Util.verify(onsets.size() >= 2, "There are less than two onsets.");
    	ListIterator<Integer> it = onsets.listIterator();
    	int previous = it.next();
    	int current = 0;
    	List<Integer> differences = new ArrayList<Integer>();
    	while(it.hasNext()) {
    		current = it.next();
    		differences.add(current - previous);
    		previous = current;
    	}
    	return differences;
    }
    
    private void displayFrequencies() {
    	List<Double> ffts = audioData.getFftAbsolute();
    	int fftLen = audioData.getFftLength();
    	int halfFFtLen = fftLen / 2;
    	// Create function getHalfFft(num); / getFft(num)
    	
    	List<Double> corrValuesPerc = new ArrayList<Double>();
    	List<Double> corrValues = new ArrayList<Double>();
    	
    	List<Double> freqs = new ArrayList<Double>();
    	for(int time = 0; time < audioData.getNumFFT(); time++) {
    		int fromI = time * fftLen;
	    	int toI = fromI + halfFFtLen;
	    	List<Double> halfFft = ffts.subList(fromI, toI);
	    	
	    	List<Double> correlations = new ArrayList<Double>();
	    	int from = 1; // be careful if you change from 1; logic has to change for position in list.
	    	int to = halfFFtLen / 4;
	    	//TODO maybe create a width.
	    	for(int corrI = from; corrI < to; corrI++) {
	    		double ampTotal = 0;
	    		int position = corrI;
	    		int numAmps = 0;
	    		while(position < halfFFtLen) {
	    			ampTotal += halfFft.get(position);
	    			position += corrI;
	    			numAmps++;
	    		}
	    		correlations.add(ampTotal / numAmps);
	    	}
	    	int correlation = Util.maxIndex(correlations, 0, correlations.size());
	    	Double sum  = Util.sum(correlations);
	    	corrValuesPerc.add(correlations.get(correlation) / sum);
	    	corrValues.add(correlations.get(correlation));
	    	double frequency = FundamentalFrequency.computeFrequency(correlation, audioData);
	    	frequency = FrequencyToNote.findFrequency(frequency);
	    	freqs.add(frequency);
    	}
    	
    	List<Integer> rollOnsets = new ArrayList<Integer>();
    	List<Double> ampsL = corrValuesPerc;// Arrays.asList(audioData.getAmp());
    	List<Double> onsetAmps = new ArrayList<Double>(); 
		boolean peakAddedSinceLastFall = false;
		double lastHeight = ampsL.get(0);
		for(int i = 1; i < ampsL.size(); i++) {
			
			if(ampsL.get(i) > lastHeight && (ampsL.get(i) - lastHeight) / lastHeight  > 1.1) {
				//peaks.add(0.0);
				peakAddedSinceLastFall = false;
			} else {
				if(!peakAddedSinceLastFall) {
					rollOnsets.add((i - 1) /*+1*/);
					onsetAmps.add(ampsL.get(i));
					/*int peak = i - 1;
					double total = 0;
					double maxValue = lastHeight;
					final int PEAK_HALF = 2;
					for(int j = 1; j <= PEAK_HALF; j++) {
						total += maxValue / Math.max(bts.get(peak - j), 0.1);
						total += maxValue / Math.max(bts.get(peak + j), 0.1);
					}
					peaks.add(Math.min((total/maxValue)*1000, 0.000001));*/
					
					
					
					peakAddedSinceLastFall = true;
				} else {
					//peaks.add(0.0);
				}
			}
			lastHeight = ampsL.get(i);
		}
		
/*		final int rollBack = 3;
		ListIterator<Integer> lit = rollOnsets.listIterator();
		while(lit.hasNext()) {
			int onset = lit.next();
			int upperExc = Math.min(onset + 1, ampsL.size() - 1);
			int lowerInc = Math.max(onset - rollBack, 0);
			onset = Util.minIndex(ampsL, lowerInc, upperExc);
			lit.set(onset);
		}*/
    	
		
		
		
    	
		//TODO I don't ever care about the beginning or end logic so I am lazy!
    	int sameCount = 1;
    	int samePass = 3;
    	ArrayList<Integer> code = new ArrayList<Integer>();
    	double previous = -1;
    	for(int i = 0; i < freqs.size(); i++) {
    		double freq = freqs.get(i);
    		if(freq == previous) {
    			sameCount++;
    		} else {
    			sameCount = 1;
    		}
    		code.add(sameCount);
    		previous = freq;
    	}
    	
    	Util.verify(code.size() == freqs.size(), "code.size() != freqs.size()");
    	
    	Double[] smoothedFreqs = new Double[freqs.size()];
    	for(int i = freqs.size() - 1; i >= 0; i--) {
    		if(code.get(i) >= samePass) {
    			int numSame = code.get(i);
    			for(int j = 0; j < numSame; j++) {
    				smoothedFreqs[i] = freqs.get(i);
    				i--;
    			}
    		} else {
    			int numSame = code.get(i);
    			for(int j = 0; j < numSame; j++) {
    				smoothedFreqs[i] = 0.0;
    				i--;
    			}   			
    		}
    		i++;
    	}
    	
    	/*
		RunningWindowStats rws = new RunningWindowStats(3);
		List<Double> smoothedFreqs = new ArrayList<Double>();
		for(int i = 0; i < smoothedFreqs.size(); i++) {
			rws.add(smoothedFreqs.get(i));
			if(rws.isFull()) {
				rws.
			}
		}*/
		
    	ArrayList<Integer> onsets = new ArrayList<Integer>();
    	for(int i = 0; i < corrValuesPerc.size(); i++) {
    		double perc = corrValuesPerc.get(i);
    		if(perc > .000  && perc < .0157) {
    			onsets.add(i);
    		}
    	}

    	ListIterator<Integer> pOLIter = onsets.listIterator();
    	// I like when things blow up. It lets me know something is wrong.
    	int lastOnset = pOLIter.next();
    	while(pOLIter.hasNext()) {
    		int onset = onsets.get(pOLIter.nextIndex());
    		if(onset - lastOnset == 1) {
    			pOLIter.remove();
    		}
    		pOLIter.next();
    		lastOnset = onset;
    	}
    	
    	// Validate onsets. Generally for either ends of the song.
    	int validWindow = 5;
    	int vWMax = corrValuesPerc.size() - 1;
    	
    	pOLIter = onsets.listIterator();
    	while(pOLIter.hasNext()) {
    		int onset = pOLIter.next();
    		int length = Math.min(onset + validWindow, vWMax);
    		int doRemove = onset;
    		
    		for(; doRemove < length; doRemove++) {
    			if(corrValuesPerc.get(doRemove) > 0.02) {//.0152056
    				doRemove = 0;
    				break;
    			}
    		}
    		if(doRemove != 0) {
    			pOLIter.remove(); // TODO log the onset being removed.
    		}
    	}
    	
    	
    	// add last offset to onsets temporarily just for elongating trackedBeats.
    	// TODO This probably won't be even close to being accurate, unless I stop the string from vibrating at the right time.
    	int findOffIterator = onsets.get(onsets.size() - 1);
    	int window = 5;
    	int lastS = Math.min(freqs.size() - 1, findOffIterator + window);
    	List<Double> lastMode = Util.mode(freqs.subList(findOffIterator, lastS));
    	Util.verify(lastMode.size() == 1, "Last note is not clear.");
    	double freq = lastMode.get(0);
    	int numBeforeFail = 3;
    	int lastOffset = findOffIterator;
    	while(findOffIterator < freqs.size() && numBeforeFail > 0) {
    		
    		if(freqs.get(findOffIterator) != freq) {
    			numBeforeFail--;
    		} else {
    			lastOffset = findOffIterator;
    		}
    		findOffIterator++;
    	}
    	// onsets.add(lastOffset); // Will be removing lastOffset later.
    	
    	
    	
    	
    	// We might want to change this to the distance that divides other numbers the best.
    	// i.e. Praise to the man. Maybe what we have here is what we want, but later account for
    	// what a beat actually is not the average difference between onsets. 
    	int btdifference = avgOnsetDiff(onsetDiff(onsets));
    	List<Integer> trackedBeats = ProcessSignal.beatTracker(onsets, btdifference);
    	
    	// Here I removed lastOffset.
    	// This doesn't work onsets.remove(lastOffset);
    	// TODO make sure I am not making similar stupid situations.
    	// onsets.remove(onsets.size() - 1);
    	
    	List<Integer> notesOnBeat = new ArrayList<Integer>(trackedBeats);
    	List<Double> notesOnBeatD = new ArrayList<Double>();
    	for(int i = 0; i < freqs.size(); i++) { //TODO replace all arrays so that they are the same length. Except for onset ones. replace freqs.size() with that value.
    		notesOnBeatD.add(0.0);
    	}
    	
    	ListIterator<Integer> nOBIter = notesOnBeat.listIterator();
    	while(nOBIter.hasNext()) {
    		int onset = nOBIter.next();
    		if(!onsets.contains(onset)) {
    			nOBIter.set(null);
    			notesOnBeatD.set(onset, 0.5);
    		} else {
    			notesOnBeatD.add(onset, 1.0);
    		}
    	}
    	
    	System.out.println(notesOnBeat);
    	
    	
    	
    	
    	
    	
    	
    	
		List<Double> onsetAmps2 = ProcessSignal.onsetAmps(onsets, Arrays.asList(audioData.getAmp()), 200);
		
		DownBeatData data = new DownBeatData();
		data.avgBeatLength = btdifference;
		data.beats = trackedBeats;
		data.onsetAmps = onsetAmps2;
		//data.kSignature;
		data.onsets = onsets;		
		DownBeatDetection dbDetection = new DownBeatDetection(data);
		
		dbDetection.detect();
    	
		System.out.println("Note Durations:");
    	for(double i : data.noteDurations) {
    		System.out.print(i + " ");
    	}
    	System.out.println();
    	
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
    	}*/
    	
    	List<PossibleDownBeat> pDownBeats = PossibleDownBeat.intitalList();
    	/***********************************************************************************************************/
    	// Simplest and most accurate (non) down beat filter
    	// A note (at least in hymns never crosses a measure). In other words, an onset must occur on the down beat.
    	// For High on the Mountain Top, this removed all but 5 of the 40 possible offsetNMeasureLengths.
    	/**********************************************************************************************************/
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
    				p.setScore(numFirstLarger/* / (double) numMeasures*/);
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
    		}*/    		
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
    	
    	
    	
    	//TODO get its own graph
//		updateGraph(fftGraph, audioData.getFrequencies());
    	if(true) {
    		//updateGraph(fftGraph, Arrays.asList(prepareValuesForDisplay(audioData.getNormalizedFrequencies(), 70)));
    	//	updateGraph(fftGraph, Arrays.asList(prepareValuesForDisplay(freqs/*audioData.getNormalizedFrequencies()*/, 100)));
    	//	fftGraph.update2List(prepareValuesForDisplay(onsetAmps2/*freqs*/, 5));
    		//fftGraph.update3List(prepareValuesForDisplay(Arrays.asList(smoothedFreqs),20));
    		
		    		//fftGraph.updateList(prepareValuesForDisplay(notesOnBeatD, 27000));
		    		fftGraph.update2List(preparePositionsForDisplay(onsets, 40000/*25000*/));
		    		//fftGraph.update3List(preparePositionsForDisplay(trackedBeats, 19000));
    		//fftGraph.update3List(preparePositionsForDisplay(onsets, 19000));
    		
    		fftGraph.update3List(prepareValuesForDisplay(Arrays.asList(audioData.getAmp()), 940));
		    		//fftGraph.update3List(prepareValuesForDisplay(onsetAmps2, 4));
      		
    	//	updateGraph(fftGraph, Arrays.asList(preparePositionsForDisplay(rollOnsets, 19000)));
   // 		updateGraph(fftGraph, Arrays.asList(preparePositionsForDisplay(onsets, 19000)));
   // 		updateGraph(fftGraph, prepareValuesForDisplay(corrValues, 500));
//			fftGraph.update2List(prepareValuesForDisplay(audioData.getNormalizedFrequencies(), 15));
    		//fftGraph.update3List(prepareValuesForDisplay(corrValuesPerc, 5/*150000*/));
			//fftGraph.update3List(prepareValuesForDisplay(corrValuesPerc, 15000000));
    	} else {
    		fftGraph.clearData();
    		fftGraph.clearData2();
    		fftGraph.clearData3();
    	}
    	//fftGraph.clearData();
    	
		Double[] beats = new Double[audioData.getNumFFT()];
		Double[] secondBeats = new Double[audioData.getNumFFT()]; 
		Double[] beatPerc = new Double[audioData.getNumFFT()];
		for(int i = 0; i < audioData.getNumFFT()/*audioData.getNormalizedFrequencies().size()*/; i++) {
			beats[i] = 0.0;
			beatPerc[i] = 0.0;
			secondBeats[i] = 0.0;
		}

		
		int count = 0; //classify as agogic or dynamic or both: An accent of length is called an agogic accent. An accent of loudness is called a dynamic accent.
		for(Integer i : audioData.getBeats()) {
			beats[i] = 3500.0;
			
		//	beatPerc[i] = (audioData.getBeatsPercent().get(count) - 1.2) * 2500;
			/*if(beatPerc[i] < 0 ) {
				beatPerc[i] = 0.0;
			}*/
			count++;
		}
		
		for(Integer i : audioData.getBeats2()) {
			secondBeats[i] = 800.0;
		}
		

		List<Integer> beatsList = audioData.getBeats();
		int positionSum = beatsList.get(0);
		int last = positionSum;
		double percentSum = audioData.getBeatsPercent().get(0);
		int percPosition = 0;
		int sumCount = 1;
		
		for(int i = 1; i < beatsList.size(); i++) {
			int onset = beatsList.get(i);
			if((onset - last) / 14.0 < .30) {//.30
				//TODO if percent is above a threshold. Probably not (well that is how I can adjust threshold)
				percentSum += audioData.getBeatsPercent().get(percPosition /*i*/);
				positionSum += onset;
				sumCount++;
			} else {

/*				if(positionSum == 0) {
					beatPerc[i] = percentSum / sumCount;
				} else {
					
*/				
				int index = (int) Math.round(positionSum/ ((double) sumCount)); 
				if(index == -1)
					index = positionSum;
				beatPerc[index] = percentSum /*/ sumCount*/;
//	}

				percentSum = audioData.getBeatsPercent().get(i /*i*/);
				positionSum = onset;
				sumCount = 1;
			}
			last = onset;
			percPosition++;
		}
		
		for(int i = 0; i < beatPerc.length; i++) {
			beatPerc[i] = (beatPerc[i] - 1.2) * 700/*2500*/;
			if(beatPerc[i] < 0 ) {
				beatPerc[i] = 0.0;
			}
		}
		
		
		
/*		onsets.remove(new Integer(951));
		onsets.remove(new Integer(947));
		onsets.add(new Integer(949));
		onsets.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }amps
		});*/
		
		
		if(false) {
			fftGraph.updateList(secondBeats);
		} else if (true){
			
			
//			fftGraph.updateList(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,800.0));
			
			
		} else if(false) {
//			fftGraph.updateList(onsetAmps.toArray(new Double[onsetAmps.size()]));
		} else {
			fftGraph.clearData();
		}
		
/*		
		int values[] = {46, 66, 78, 84, 114, 124, 134, 144, 154, 166, 206, 218, 226, 280, 292, 300, 354, 372, 386, 392, 416, 426, 436, 446, 456, 466, 502, 514, 522, 558, 570, 578, 614, 624, 634, 644, 656, 676, 686, 696, 706, 718, 738, 748, 758, 770, 780, 802, 812, 822, 830, 844, 876, 888, 900, 910, 938, 946, 950, 960, 972, 1012, 1024, 1032, 1072, 1086, 1096};
		int output[] = new int[values[values.length - 1] + 1];
		
		for(int i = 0; i < values.length; i++) {
			output[values[i] - 1] = values[i];
		}
		
		System.out.println("Output");
		for(int i = 0; i < output.length; i++) {
			System.out.println(output[i]);
		}
*/		
		
   // 	Number[] data = beats.toArray(new Number[dataList.size()]);
		if(false) {
			audioData.getBtClass().detect();
			fftGraph.clearData2();
			fftGraph.update2List(prepareValuesForDisplay(audioData.getBtClass().percentages, 1));
			
			List<Double> bts = audioData.getBtClass().percentages;
			ArrayList<Double> peaks = new ArrayList<Double>();
			peakAddedSinceLastFall = false;
			lastHeight = bts.get(0);
			for(int i = 1; i < bts.size(); i++) {
				if(bts.get(i) >= lastHeight) {
					//peaks.add(0.0);
					peakAddedSinceLastFall = false;
				} else {
					if(!peakAddedSinceLastFall) {
						peaks.add((double) (i - 1));
						
						/*int peak = i - 1;
						double total = 0;
						double maxValue = lastHeight;
						final int PEAK_HALF = 2;
						for(int j = 1; j <= PEAK_HALF; j++) {
							total += maxValue / Math.max(bts.get(peak - j), 0.1);
							total += maxValue / Math.max(bts.get(peak + j), 0.1);
						}
						peaks.add(Math.min((total/maxValue)*1000, 0.000001));*/
						
						
						
						peakAddedSinceLastFall = true;
					} else {
						//peaks.add(0.0);
					}
				}
				lastHeight = bts.get(i);
			}
			//peaks.add(0.0);
			fftGraph.update3List(prepareValuesForDisplay(peaks, 1));
			
		}
		else if(false) {
			fftGraph.update2List(preparePositionsForDisplay(onsets, 3500));
		} else if(false) {
			fftGraph.update2List(beats);
		} else if(false) {
			fftGraph.clearData2();
		}
		
		if(false) {
			fftGraph.update3List(secondBeats /*beatPerc*/);			
		} else if (false) {
			Double[] amps = audioData.getAmp();
			Double[] ampsAmped = new Double[amps.length];
			for(int i = 0; i < ampsAmped.length; i++) {
				if(i < 130)
					ampsAmped[i] = amps[i] * 240000;//290;//28;
				else
					ampsAmped[i] = amps[i] * 240000;
			}
			
			fftGraph.update3List(ampsAmped);
		} else if (true) {
			//trackedBeats.removeAll(audioData.getBeats());
//			fftGraph.update3List(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,1200.0));
		} else if(false) {
			Integer actualBeats[] = {46, 66, 81, 86, 116, 126, 136, 146, 156, 166, 206, 221, 226, 266, 281, 286, 326, 346, 361, 366, 396, 406, 416, 426, 436, 446, 486, 501, 506, 546, 561, 566, 586, 596, 606, 616, 626, 646, 656, 666, 676, 686, 706, 716, 726, 736, 746, 766, 776, 786, 796, 806, 821, 831, 841, 851, 866, 876, 886, 896, 936, 951, 956, 996, 1011, 1016};
		    Double[] numbers = preparePositionsForDisplay(Arrays.asList(actualBeats),1200);
		    fftGraph.update3List(numbers);
		} else {
			fftGraph.clearData3();
		}

		
		
		/*Double[] beats2 = new Double[audioData.getNumFFT()];
		for(int i = 0; i < audioData.getNumFFT()audioData.getNormalizedFrequencies().size(); i++) {
			beats2[i] = 0.0;
		}
		
		for(Integer i : audioData.getBeats2()) {
			beats2[i] = 1000.0;
		}
		*/
		
		//fftGraph.update3List(beats2);
    }
    
    private Double[] prepareValuesForDisplay(List<Double> values, double height) {
    	if(values.isEmpty()) {
    		return null;
    	}
		Double[] graph = new Double[values.size()];
		for(int i = 0; i < graph.length; i++) {
			graph[i] = values.get(i) * height;
		}	

		return graph;
    }
    
    private Double[] preparePositionsForDisplay(List<Integer> positions, double height) {
    	if(positions.isEmpty()) {
    		return null;
    	}
		Double[] graph = new Double[positions.get(positions.size() - 1) + 1];
		for(int i = 0; i < graph.length; i++) {
			graph[i] = 0.0;
		}	
		for(Integer i : positions) {
			graph[i] = height;
		}
		return graph;
    }
    
    private void displayFFt(int n, final List<Double> absoluteData) {
		//updateGraph(fftGraph, Arrays.asList(absoluteData), n);
    	updateGraph(fftGraph, absoluteData, n);
    	
		int start = n * audioData.getFftLength();
    	int end = start + (audioData.getFftLength() / 2);
/*    	List<Double> subList = Arrays.asList(absoluteData).subList(start, end);*/
    	List<Double> subList = absoluteData.subList(start, end);
    	
    	
		FundamentalFrequency ff = new FundamentalFrequency(audioData, subList);
		ff.computeFrequency(subList);
		
		
//		Util.println("Fundamental Frequency Index: " + audioData.getFrequencies().get(n));
    }

	//Number[] range = Arrays.copyOfRange(audioData.getFftAbsolute(), start, end);
	//List<Double> data = Arrays.asList(fftData).subList(start, end);
    
    
    private <T> void displayCenterGraph(List<T> data) {
    	updateGraph(centerGraph, data);
    }
    
    private <T> void displayCenterGraph(int n, List<T> data) {
		updateGraph(centerGraph, data, n);
    }
    
/*    private void displayAC(int n) {
    	Number[] data = audioData.getAutoCorrelationAbsolute();
    	Number[] logData = new Number[data.length];
    	for(int i = 0; i < data.length; i++) {
    		logData[i] = Math.log10(data[i].doubleValue());
    	}
		updateGraph(acGraph, logData, n);
    }*/
    
	private void writeToFile(byte[] signalBites, File file, AudioFormat format) {
    	WriteAudioFile toFile = new WriteAudioFile(signalBites, format, file);
    	toFile.start();
	}
    
    public void startCapture() {
    	if(audio == null || audio.isStopped()) {
	    	try {
				audio = new CaptureAudio(
						new LineListener() {
							@Override
							public void update(LineEvent event) {
								Type type = event.getType();
								if (type == Type.CLOSE || type == Type.STOP) {
									if(!audio.isStopped()) {
										audio.stopCapture();
										System.err.println("Audio capture was stopped unexpectedly.");
									}
								}
							}
						});
				System.out.println("STARTED");
				audio.start();
				captureButton.setText("Stop");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	} else {
    		System.err.println("Capture is already in process");
    	}
    }
    
    public void stopCapture(File file) {
    	if(audio != null && !audio.isStopped()) {
    		audio.stopCapture();
    		System.out.println("STOPPED");
    		
    		
	    	//TODO study about Thread.getStackTrace()
	  //  	currentSignal = audio.getStream().toByteArray();
    	//	int[] signal = toIntArray(currentSignal);
	//		updateGraph(signal);

    		
    		AudioData audioData = new AudioData(
    				audio.getStream().toByteArray(),
    				audio.getFormat());
    		processSignal(audioData, file);
    		
/*			captureButton.setDisable(false);
			captureButton.setText("Capture");*/
    	}
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}



/*private int calculateBaseFrequencyIndex(Double[] fftData, int nth, int startOffset) {
	int start = nth * audioData.getFftLength();
	//Only look at half of the FFT.
	int end = start + (audioData.getFftLength() / 2);
	int maxI = findMax(fftData, start, end, 0);
	
	int subMaxI = maxI - start;
	

	List<Integer> peaks = Peaks.findPeaks(data, subMaxI, 7, .05);
	    	
	List<Integer> peakDiff = new LinkedList<Integer>();
	for(int i = 0; i < peaks.size() - 1; i++) {
		peakDiff.add(peaks.get(i+1) - peaks.get(i));
	}
	//This all will not work if the peaks are not exactly correct.
	List<Integer> modes = Util.mode(peakDiff);
	if(modes.size() > 1) {
		System.err.println("You have multiple modes at FFT number: " + nth);
		//System.exit(1);
	}
	return (modes.size() >= 1) ? Util.round(Util.average(modes))modes.get(0) : -1;
}
*/

/*for(int i = 0; i < audioData.getNumFFT(); i++) {
	//TODO needs to be the updated FFT length.
	int start = i * audioData.getFftLength();
	int end = start + audioData.getFftLength();
	

	
	int indexF = findMax(Arrays.copyOfRange(fftLowpass, start, end), 0);
	double freqF = FundamentalFrequency.computeFrequency(indexF, audioData);
	String noteF = FrequencyToNote.findNote(freqF);
	
	int baseFI = calculateBaseFrequencyIndex(fftLowpass, i, 0);
	double baseF = FundamentalFrequency.computeFrequency(baseFI, audioData);
	String baseNote = FrequencyToNote.findNote(baseF);

	System.out.println(i + " " + baseF + " " + baseNote);
	
//	System.out.println("FFT  i: " + i + "Index: " + indexF + " frequency: " + freqF + " note: " + noteF + " maxAmp[i]: "/* + maxAmp[i]*);
	//System.out.println("AC   i: " + i + "Index: " + index + " frequency: " + freq + " note: " + note + " maxAmp[i]: " + maxAmp[i]);
//  	System.out.println("Base i: " + i + "Index: " + baseFI + " frequency: " + baseF + " note: " + baseNote + " maxAmp[i]: " /*+ maxAmp[i]*);
//      	System.out.println();
}
*/