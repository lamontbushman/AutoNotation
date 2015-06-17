import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
	AudioFormat format;
	CaptureAudio audio;
	XYChart.Series series;
    Button captureButton;
    Button openButton;
    byte[] currentSignal;
	
	private int[] toIntArray(byte[] bites) {
		if(format.getSampleSizeInBits() == 16) {
			int[] array = new int[bites.length / 2];
			int arrayIndex = 0;
			int first = 0 ;
			int second = 1;
			if(!format.isBigEndian()) {
				first = 1;
				second = 0;
			} 
			for(int i = 0; i < bites.length - 1; i+=2) {
				array[arrayIndex] = (bites[i + first] << 8) | (bites[i + second] & 0xFF);
				//System.out.print(Integer.toHexString(array[arrayIndex]) + " ");
				arrayIndex++;
			}
			return array;
		} else {
			int[] array = new int[bites.length];
			for(int i = 0; i < bites.length; i++) {
				array[i] = bites[i];
				System.out.println(array[i] + "  " + bites[i]);
			}
			return array;
		}
	}
	
	private void initializeFormat() {
		// Initialize AudioFormat
		//4186.01 highest piano key
		//8372.02 twice that
		//16384 multiple of two 
		//log2(16384) = 14;
		float sampleRate = 16384;//16000;
		int sampleSizeInBits = 8;//16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		format = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}
	
	
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override public void start(Stage stage) {
        stage.setTitle("Signal Processing Senior Project");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Frames");
        yAxis.setLabel("Amplitude");
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
                
        lineChart.setTitle("Audio Signal");
        //defining a series
        series = new XYChart.Series();
        series.setName("Original Line");
        lineChart.getData().add(series);
        
       	BorderPane border = new BorderPane();
    	HBox box = addHBox();
    	border.setTop(box);
    	border.setCenter(lineChart);
    	
    	Scene scene = new Scene(border,800,600);
        
        //Scene scene  = new Scene(lineChart,800,600);
    	box.requestFocus();
        stage.setScene(scene);
        stage.show();
    }
    
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
    
    private void readFromFile(File file) {
		ReadAudioFile audio = new ReadAudioFile(file);
		audio.readFile();
		currentSignal = audio.getStream().toByteArray();
		format = audio.getFormat();
		
		int[] signal = toIntArray(currentSignal);
	
		
		updateGraph(signal);
		playClip(currentSignal);
    }
    
    private void fft(Complex signal[]) {
    	FFT.fft(signal);
    	Double data[] = new Double[signal.length];//maybe int/long
    	for(int i = 0; i < signal.length; i++) {
    		data[i] = signal[i].absolute();
    	}
    	updateGraph(data);
    	
		int maxI = 0;
		double max = 0;
    	for(int i = 0; i < data.length/2; i++) {
    		if(data[i] > max) {
    			max = data[i];
    			maxI = i;
    		}
    		if(data[i] > 800) {
    			System.out.println("Harmonic: " + i + " " + data[i] + " frequency: " + computeFrequency(i));
    		}
    	}
    	System.out.println("MAX: " + maxI);
    	//System.out.println(Collections.max(Arrays.asList(data)));
    	
    }
    
    private double computeFrequency(int bin) {
    	return bin * 16384/2048;
    }
    
    
    private void updateGraph(Double[] signal) {
    	series.getData().clear();
    	
        //populating the series with data
        int start = 0;// signal.length / 2;
        int end = signal.length;//start + 300;//(bites.length * 17) / 32;
        for(int i = start; i < end; i++) {
        	//fix for 32 bit
            series.getData().add(new XYChart.Data((i - start), signal[i]));
         /*   if(i % 100 == 0)
            	System.out.println((i - start)*2 + " " + bites.length);*/
        }
    }

    
    private void updateGraph(int[] signal) {
    	series.getData().clear();
    	
        //populating the series with data
        int start = signal.length / 2;
        int end = start + 300;//(bites.length * 17) / 32;
        for(int i = start; i < end; i++) {
        	//fix for 32 bit
            series.getData().add(new XYChart.Data((i - start), signal[i]));
         /*   if(i % 100 == 0)
            	System.out.println((i - start)*2 + " " + bites.length);*/
        }
    }
    
    private void playClip(byte[] signal) {
    	PlayAudio play = new PlayAudio(signal, format);
    	play.playClip();	
    }
    
    private HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        TextField captureField = new TextField();
        captureField.promptTextProperty().set(".wav");
        TextField openField = new TextField();
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
        
        hbox.getChildren().addAll(label1, captureField, captureButton, 
        		label2, openField, openButton);
        return hbox;
    }
    
    

	private void writeToFile(byte[] signalBites, File file) {
    	WriteAudioFile toFile = new WriteAudioFile(signalBites, format, file);
    	toFile.start();
	}
    
    public void startCapture() {
    	if(audio == null || audio.isStopped()) {
	    	try {
	        	initializeFormat();
				audio = new CaptureAudio(format,
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
	    	currentSignal = audio.getStream().toByteArray();
    		int[] signal = toIntArray(currentSignal);
	//		updateGraph(signal);
			
	    	writeToFile(currentSignal, file);
			playClip(currentSignal);
	    	
			//frequency = j * sampleRate/n
			//440 = j * 16384 / 2048 
			// j = 55
			
			//(bin_id * freq/2) / (N/2)
			//(bin_id * 16384/2) / (2048/2)  = 440
			//bin_id = 55
			//http://dsp.stackexchange.com/questions/15563/what-exactly-is-the-effect-of-a-hann-window-on-the-fft-output
	    	int start = signal.length / 2;
	    	int end = start + 2048; //let's start with hearing at least 880HZ (well probably little under that).
	    	Complex data[] = new Complex[2048];
	    	int count = 0;
	    	List<Double> weights = new ArrayList<Double>();
	    	for(int i = 0; i < 2048; i++) {
	    		double weight = 
	    				Math.pow(
	    	    				Math.sin((Math.PI*i) / (2048 -1)),
	    	    				2);
	    		weights.add(weight);
	    	}
	    	
//	    	double[] array = convertDoubles(weights);
//	    	updateGraph(array);
	    	
	    	
	    	for(int i = start; i < end; i++) {
	    		data[count] = new Complex(weights.get(count)*signal[i]);
	    		count++;
	    	}
	    	fft(data);

    		
    		
/*			captureButton.setDisable(false);
			captureButton.setText("Capture");*/
    	}
    }
    
    public static double[] convertDoubles(List<Double> doubles)
    {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ret[i] = iterator.next().doubleValue();
            i++;
        }
        return ret;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}