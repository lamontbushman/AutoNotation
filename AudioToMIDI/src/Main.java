import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
	CaptureAudio audio;
    Button captureButton;
    Button openButton;
    Button displayFFTButton;
    Double[] absolute;
    AudioData audioData;
	private int numFFT;
	private int fftLength;
	private int fftArrayLength;
	private byte[] capturedAudio;
	private Graph acGraph;
	private Graph fftGraph;
		
  //  @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override public void start(Stage stage) {
        stage.setTitle("Signal Processing Senior Project");       
        
        acGraph = new Graph("Autocorrelation", "Nth Sample", "Power");
        fftGraph = new Graph("FFT", "Nth Sample", "Power");
        
       	BorderPane border = new BorderPane();
    	HBox box = addHBox();
    	border.setTop(box);
    	border.setCenter(acGraph);
    	border.setBottom(fftGraph);
    	
    	Scene scene = new Scene(border,800,600);
        
        //Scene scene  = new Scene(lineChart,800,600);
    	box.requestFocus();
        stage.setScene(scene);
        stage.show();
    }
	
	public List<Integer> findPeaks(Number[] signal, int windowLength, int start, int end, int firstPeak, double zAllowance) {
		List<Integer> peakIndexes = new ArrayList<Integer>();
		//TODO firstPeak - start
		peakIndexes.add(firstPeak - start); //assuming start is the first peak of concern
		RunningWindowStats rws = new RunningWindowStats(windowLength);
		end = start + (end - start) / 2;
		int index = firstPeak;
		
		int findPeakWindow = -1;
		
	//	System.out.println("Start: " + (firstPeak - start));
	//	System.out.println("Window Length : " + windowLength);
		boolean newPeakFound = false;
		
		while(index+1 < end) {
			//Find bottom of peak
			while(index+1 < end &&
					signal[index].doubleValue() > signal[index + 1].doubleValue()) {
				index++;
			}
//			System.out.println("Bottom of peak at index: " + index);
				
			//Fill up window (may be full from previous peak)
			//TODO see if this needs to be removed and not worry about filling the window
			// maybe a modified zAllowance based off rws.size()
			for(; index < end && !rws.isFull(); index++) {
/*				if(rws.isFull()) {
					System.out.println("Window is full at index: " + index);
					break;
				} else {
					rws.add(signal[index]);
				}*/
				rws.add(signal[index]);
//				System.out.println("Window filling: " + index);
			}
			
			//Find beginning of next peak
			for(; index + 3< end; index++) {
				double zValue = Math.abs(rws.zScore(signal[index+3]));
//				System.out.println("i: " + index + " zV: " + zValue);
				if(zValue <= zAllowance) {
					rws.add(signal[index]);
				} else {
					newPeakFound = true;
//					System.out.println("Beg of peak at index: " + index);
					break;
				}
			}
			
			if(!newPeakFound) {
//				System.out.println("No new peak found");
				
				//break;
			}
			
			/*//Find top of peak  (may have to adjust zAllowance to get above local peaks)
			while(index+1 < end &&
					signal[index].doubleValue() < signal[index + 1].doubleValue()) {
				index++;
			}*/
			
			//Find top of peak
			if(findPeakWindow == -1) {
				findPeakWindow = (index - start) / 4;
			}
			int maxEnd = index + findPeakWindow;
			if(maxEnd >= end) {
				maxEnd = end;
			}
			
			if(newPeakFound) {
				index = Util.maxIndex(signal, index, maxEnd);
			
				//TODO The top of the peak may depend on hitting end of signal
				peakIndexes.add((index - start));
				//System.out.println("Peak at index: " + (index - start));
			} else {
				//System.out.println("No new peak found");
				break;
			}
			newPeakFound = false;
		}
		return peakIndexes;
	}
        
    private void readFromFile(File file) {
		ReadAudioFile audio = new ReadAudioFile(file);
		audio.readFile();
		AudioData audioData = new AudioData(
				audio.getStream().toByteArray(),
				audio.getFormat());
		processSignal(audioData, null);
    }
    
    private void processSignal(AudioData audioData, File file) {
    	// Display Original Signal
		//updateGraph(audioData.getOriginalSignal());
		
		// Play the data
		playClip(audioData.getSampledData(), audioData.getFormat());
		
		// Save the data
		if(file != null) {
	    	writeToFile(audioData.getSampledData(), file, audioData.getFormat());
		}
		
		double overlap = 0.5;
		int fftLength = 1024;//8192;
				
		
		ProcessSignal ps = new ProcessSignal(audioData, overlap, fftLength);
		ps.process();
		//absolute = audioData.getFftAbsolute();
		//absolute = audioData.getFftCepstrum();
		absolute = audioData.getAutoCorrelationAbsolute();
		this.audioData = audioData;
		
		byte[] data = audioData.getSampledData();
		
		if(capturedAudio == null)
			capturedAudio = audioData.getSampledData();
/*		else {
			byte[] data1 = audioData.getSampledData();
			if(data1.length != capturedAudio.length)
				System.out.println("Not the same length");
			else {
				for (int i = 0; i < capturedAudio.length; i++) {
					if(data1[i] != capturedAudio[i])
						System.out.println("different");
					System.out.println(data1[i] + " " + capturedAudio[i]);
				}
			}
			System.exit(0);
		}
*/		
		
		
/*		System.out.println("Print first");
		Complex[] complex = audioData.getComplexData();
		
		int[] complexData = new int[complex.length];
		for(int i = 0; i < complex.length; i++) {
			complexData[i] = (int) complex[i].absolute();
		}
		
		updateGraph(complexData);*/
	/*	
		Double[] dbls = audioData.getFftInverseTest();
		int[] invtest = new int[dbls.length];
		for(int i = 0; i < dbls.length; i++) {
			invtest[i] = dbls[i].intValue();
		}
		
		updateGraph(invtest);
*/		
		/*
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Print next");
		updateGraph(audioData.getFftInverseTest());
		*/
/*		if(true)
			return;
*/		
		
		
		
	//	numFFT = audioData.getFftAbsolute().length / audioData.getFftLength();
		//fftArrayLength = audioData.getFftAbsolute().length;
		//fftArrayLength = audioData.getFftCepstrum().length;
		fftArrayLength = audioData.getAutoCorrelationAbsolute().length;
		this.fftLength = audioData.getFftLength() * 2;
		

/*		
		for(int i = 0; i < numFFT; i++) {
			updateGraph(Arrays.copyOfRange(absolute, fftStart, fftEnd));
			fftStart += fftLength;
			fftEnd += fftLength;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/		
														/*Double[] frequencies = audioData.getFrequencies();
														System.out.println("HI" + frequencies.length);
														updateGraph(audioData.getFrequencies());*/
													
		//updateGraph(audioData.getNormalizedFrequencies());
		/*for(Double d : frequencies) {
			System.out.println("F:" + d);
		}*/
		
		
	//	String[] notes = audioData.getNoteNames(); 
/*		for(String note : audioData.getNoteNames()) {
			System.out.print(note + " ");
		}
*/  
													/*		String lastNote = "";
															String[] notes = audioData.getNoteNames();
															for(int i = 0; i < frequencies.length; i++) {
																System.out.print(frequencies[i] + " " + notes[i] + " ");
																if (!lastNote.equals(notes[i])) {
																	System.out.println();
																}
																lastNote = notes[i];
															}
*/		

		Complex[] overlapData = audioData.getComplexData();
		Double[] maxAmp = new Double[absolute.length];
		double sum = 0;
		int maxI = 0;
		for(int i = 0; i < overlapData.length; i+= fftLength) {
			sum = 0;
			for(int j = 0; j < fftLength; j++)
				sum += overlapData[i+j].absolute();
			maxAmp[maxI] = sum/fftLength;
			maxI++;
		}
		
		Double fftAbsolute[] = audioData.getFftAbsolute();
		
		
		for(int i = 0; i < absolute.length/this.fftLength; i++) {
			int start = i * this.fftLength;
			int end = start + this.fftLength;
			int index = findMax(Arrays.copyOfRange(absolute, start, end), 10);
			double freq = ProcessSignal.computeFrequency(index, audioData);
			String note = FrequencyToNote.findNote(freq);
			
			int indexF = findMax(Arrays.copyOfRange(fftAbsolute, start, end), 0);
			double freqF = ProcessSignal.computeFrequency(indexF, audioData);
			String noteF = FrequencyToNote.findNote(freqF);
			
			int baseFI = calculateBaseFrequencyIndex(audioData.getFftAbsolute(), i, 0);
			double baseF = ProcessSignal.computeFrequency(baseFI, audioData);
			String baseNote = FrequencyToNote.findNote(baseF);
			
        	System.out.println("FFT  i: " + i + "Index: " + indexF + " frequency: " + freqF + " note: " + noteF + " maxAmp[i]: " + maxAmp[i]);
        	System.out.println("AC   i: " + i + "Index: " + index + " frequency: " + freq + " note: " + note + " maxAmp[i]: " + maxAmp[i]);
        	System.out.println("Base i: " + i + "Index: " + baseFI + " frequency: " + baseF + " note: " + baseNote + " maxAmp[i]: " + maxAmp[i]);
        	System.out.println();
		}
		//displayAC(0);
		displayFFt(0);
    }
    
    private String getFreqAndNote(int index) {
    	double frequency = ProcessSignal.computeFrequency(index, audioData);
    	String closestNote = FrequencyToNote.findNote(frequency);
    	return frequency + "Hz " + closestNote;
    }
    
    private void updateGraph(Graph graph, Number[] data) {
    	graph.updateList(data);
    }
    
    private void updateGraph(Graph graph, Number[] data, int index) {
    	System.out.println(fftArrayLength/fftLength);	
    	int start = index * fftLength;
    	int end = start + fftLength;
    	Number[] range = Arrays.copyOfRange(data, start, end);
    	
    	//System.out.println("Index: " + index + " frequency: " + ProcessSignal.computeFrequency(index, audioData));
    	//updateGraph(fft);
    	
/*		if(fft[i] == null)
			System.out.println(i + " is null");
		else
			;//fft[i] = Math.log(fft[i]);*/
    	graph.updateList(range);
    }
    
    private int findMax(Number[] data, final Integer dataStart, final Integer dataEnd, int ignoreFirstN) {
    	int start = dataStart + ignoreFirstN;//114688
    	//Only search half of the data.
    	int end = dataStart + (dataEnd - dataStart) / 2;//116736
    	return Util.maxIndex(data, start, end);
    }
        
    private int findMax(Number[] fft, int ignoreFirstN) {
    	double max = -1;
    	int maxIndex = -1;
    	int start = ignoreFirstN;//toFft.length/4 - 2048; // ignore DC 
    	int end = fft.length/2;// - 2048- fudge;
    	
    	for(int j = start; j <  end; j++) {
    		if(fft == null)
    			System.out.println("it is null");
    		else if(fft[j] == null)
    			System.out.println("it is null!!");
    		double test = fft[j].doubleValue();
    		if(test > max) {
    			max = test;
    			maxIndex = j;
    		}
    	}
    	//maximums[i/fftLength] = max;
    	//4949
    	//System.out.println("!!!!!!!Max Index: " + maxIndex + "  " + toFft[maxIndex] + " " + toFft[maxIndex].absolute());

    	if(maxIndex == start || maxIndex == end)
    		System.out.println("ALERT!!! At the beg/end: " + maxIndex);
    		
		/*if(indexes.size() == -1 || indexes.size() == 0 || prevIndex != indexes.get(indexes.size()-1));
			indexes.add(maxIndex);
		prevIndex = maxIndex;*/
    	
    	return maxIndex;
    }
     
    private void playClip(byte[] signal, AudioFormat format) {
    	PlayAudio play = new PlayAudio(signal, format);
    	play.playClip();	
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

        Button nthAutoCorrelation = new Button("Display AC[N]");
        nthAutoCorrelation.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				displayAC(Integer.parseInt(nthField.getText()));
			}
		});

        Button nthFFT = new Button("Display FFT[N]");
        nthFFT.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				displayFFt(Integer.parseInt(nthField.getText()));
			}
		});
        
        TextField freqIndex = new TextField();
        freqIndex.promptTextProperty().set("Note index");
        
        TextField noteAndFreqField = new TextField("Freq and Note");
        noteAndFreqField.setEditable(false);
        
        Button noteAndFreqButton = new Button("Show Note");
        noteAndFreqButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String text = getFreqAndNote(Integer.parseInt(freqIndex.getText()));
				noteAndFreqField.setText(text);
			}
		});
        
        hbox.getChildren().addAll(label1, captureField, captureButton, 
        		label2, openField, openButton, frequencyButton, nthField, 
        		nthAutoCorrelation, nthFFT, freqIndex, noteAndFreqButton,
        		noteAndFreqField);
        
        return hbox;
    }
    
    private void displayFrequencies() {
    	//TODO get its own graph
		updateGraph(fftGraph, audioData.getFrequencies());
    }
    
    private void displayFFt(int n) {
		updateGraph(fftGraph, audioData.getFftAbsolute(), n);
		//Number[] range = Arrays.copyOfRange(audioData.getFftAbsolute(), start, end);
		calculateBaseFrequencyIndex(audioData.getFftAbsolute(), n, 0);
    }
    
    private int calculateBaseFrequencyIndex(Number[] fftData, int nth, int startOffset) {
    	int start = nth * this.fftLength;
    	int end = start + this.fftLength;
    	int maxI = findMax(fftData, start, end, 0);
		List<Integer> peaks = findPeaks(fftData, 25,/*(int) audioData.getFormat().getSampleRate() / audioData.getFftLength(),*/ // A good guess for window size
				start, end, maxI,0.000013); // 1 std deviation. Really no backing to this
		
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
		return (modes.size() > 1) ? modes.get(0) : -1;
    }
    
    private void displayAC(int n) {
    	Number[] data = audioData.getAutoCorrelationAbsolute();
    	Number[] logData = new Number[data.length];
    	for(int i = 0; i < data.length; i++) {
    		logData[i] = Math.log10(data[i].doubleValue());
    	}
		updateGraph(acGraph, logData, n);
    }
    
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

/*
private void readData(boolean readFile, File file) {
	ByteArrayOutputStream stream;
	byte[] signalBites;
	
	if(readFile) { 
		ReadAudioFile audio = new ReadAudioFile(file);
		audio.readFile();
		stream = audio.getStream();
		signalBites = stream.toByteArray();
		format = audio.getFormat();
	} else {
    	initializeFormat();
    	//ByteArrayOutputStream stream = runReadAudio();
    	startCapture();
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	stopCapture(file);
    	
    	//TODO study about Thread.getStackTrace()
    	stream = audio.getStream();
    	signalBites = stream.toByteArray();
    	writeToFile(signalBites, file);
	}
	
	currentSignal = signalBites;
}
*/

/*    private void clear() {
acGraph.clearData();
fftGraph.clearData();
}
*/      
/*    private void updateGraph(Number[] signal) {
clear();
acGraph.updateList(signal);
*/  	
/*        //populating the series with data
int start = 0;// signal.length / 2;
int end = signal.length;//start + 300;//(bites.length * 17) / 32;
for(int i = start; i < end; i++) {
	//fix for 32 bit
    series.getData().add(new XYChart.Data((i - start), signal[i]));
    if(i % 100 == 0)
    	System.out.println((i - start)*2 + " " + bites.length);
}
}*/

/*  private void updateGraph(int[] signal) {
series.getData().clear();

//populating the series with data
int start = signal.length / 2;
int end = start + 300;//(bites.length * 17) / 32;
for(int i = start; i < end; i++) {
	//fix for 32 bit
    series.getData().add(new XYChart.Data((i - start), signal[i]));
    if(i % 100 == 0)
    	System.out.println((i - start)*2 + " " + bites.length);
}
}*/


/*    private void displayFFt(int fftNumber, int offStart, int offEnd, int maxQuery) {
System.out.println(fftArrayLength/fftLength);
int fftStart = fftNumber * fftLength;
int fftEnd = fftStart + fftLength;//fftStart + 250;//fftStart + fftLength;
fftStart += offStart;
fftEnd -= offEnd;

System.out.println("Displaying FFT start: " + fftStart + " end: " + fftEnd);

//fftEnd -= 2 * (fftLength / 3);
if(fftStart < fftArrayLength) {
	Double[] fft = Arrays.copyOfRange(absolute, fftStart, fftEnd);
	for(int i = 0; i < fft.length; i++)

	
	System.out.println("Max at " + maxQuery + " is " + fft[maxQuery]);
	
	int index = findMax(fft, 10);
	System.out.println("Index: " + index + " frequency: " + ProcessSignal.computeFrequency(index, audioData));
	updateGraph(fft);
	
//		System.out.println("This won't be accurate for modified lengths. Frequency: " + ProcessSignal.findMax(fft, /*fftLength*fftEnd - fftStart, audioData));
	fftStart += fftLength;
	fftEnd += fftLength;
}
}
*/