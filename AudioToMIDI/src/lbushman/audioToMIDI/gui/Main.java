package lbushman.audioToMIDI.gui;

//Reddit forums
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import lbushman.audioToMIDI.io.CaptureAudio;
import lbushman.audioToMIDI.io.PlayAudio;
import lbushman.audioToMIDI.io.ReadAudioFile;
import lbushman.audioToMIDI.io.WriteAudioFile;
import lbushman.audioToMIDI.processing.AudioData;
import lbushman.audioToMIDI.processing.Complex;
import lbushman.audioToMIDI.processing.DownBeatData;
import lbushman.audioToMIDI.processing.DownBeatDetection;
import lbushman.audioToMIDI.processing.FrequencyToNote;
import lbushman.audioToMIDI.processing.FundamentalFrequency;
import lbushman.audioToMIDI.processing.Peaks;
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
    
    private void displayFrequencies() {
    	//TODO get its own graph
//		updateGraph(fftGraph, audioData.getFrequencies());
		updateGraph(fftGraph, audioData.getNormalizedFrequencies());
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
		
		List<Integer> onsets = new ArrayList<Integer>(audioData.getBeats());
		
/*		onsets.remove(new Integer(951));
		onsets.remove(new Integer(947));
		onsets.add(new Integer(949));
		onsets.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }amps
		});*/
		int beatDifference = 16;
		List<Integer> trackedBeats = ProcessSignal.beatTracker(onsets, beatDifference);
		List<Double> onsetAmps = ProcessSignal.onsetAmps(onsets, Arrays.asList(audioData.getAmp()), 200);
		
		DownBeatData data = new DownBeatData();
		data.avgBeatLength = beatDifference;
		data.beats = trackedBeats;
		data.onsetAmps = onsetAmps;
		//data.kSignature;
		data.onsets = onsets;
		
		DownBeatDetection dbDetection = new DownBeatDetection(data);
		
		dbDetection.detect();
		
		
		if(false) {
			fftGraph.updateList(secondBeats);
		} else if (true){
			
			
			fftGraph.updateList(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,800.0));
			
			
		} else if(true) {
			fftGraph.updateList(onsetAmps.toArray(new Double[onsetAmps.size()]));
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
		if(true) {
			fftGraph.update2List(preparePositionsForDisplay(onsets, 3500));
		} else if(true) {
			fftGraph.update2List(beats);
		} else {
			fftGraph.clearData2();
		}
		
		if(false) {
			fftGraph.update3List(secondBeats /*beatPerc*/);			
		} else if (true) {
			Double[] amps = audioData.getAmp();
			Double[] ampsAmped = new Double[amps.length];
			for(int i = 0; i < ampsAmped.length; i++) {
				if(i < 130)
					ampsAmped[i] = amps[i] * 70;//28;
				else
					ampsAmped[i] = amps[i] * 70;
			}
			
			fftGraph.update3List(ampsAmped);
		} else if (false) {
			//trackedBeats.removeAll(audioData.getBeats());
			fftGraph.update3List(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,1200.0));
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