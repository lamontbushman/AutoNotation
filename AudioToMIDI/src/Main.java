import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Main extends Application {
	AudioFormat format;
	CaptureAudio audio;
	File file;
	
	public int[] toIntArray(byte[] bites) {
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
	
	public void initializeFormat() {
		// Initialize AudioFormat
		float sampleRate = 16000;//16000;
		int sampleSizeInBits = 16;//16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		format = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}
	
	
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override public void start(Stage stage) {
    	file = new File("capture.wav");
    	ByteArrayOutputStream stream;
    	byte[] signalBites;
    	
    	if(true) { 
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
	    	stopCapture();
	    	
	    	//TODO study about Thread.getStackTrace()
	    	stream = audio.getStream();
	    	signalBites = stream.toByteArray();
    	}
    	
    	writeToFile(signalBites);
    	
    	int[] data = toIntArray(signalBites);
    	
    	
    	
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
        XYChart.Series series = new XYChart.Series();
        series.setName("Original Line");

        //populating the series with data
        int start = data.length / 2;
        int end = start + 300;//(bites.length * 17) / 32;
        for(int i = start; i < end; i++) {
        	//fix for 32 bit
            series.getData().add(new XYChart.Data((i - start), data[i]));
         /*   if(i % 100 == 0)
            	System.out.println((i - start)*2 + " " + bites.length);*/
        }
        lineChart.getData().add(series);
        
        Scene scene  = new Scene(lineChart,800,600);
       
        stage.setScene(scene);
        stage.show();
        
    	PlayAudio play = new PlayAudio(signalBites, format);
    	play.playClip();
    }

	private void writeToFile(byte[] signalBites) {
    	WriteAudioFile toFile = new WriteAudioFile(signalBites, format, file);
    	toFile.start();
	}
    
    public void startCapture() {
    	if(audio == null || audio.isStopped()) {
	    	try {
				audio = new CaptureAudio(format,
						new LineListener() {
							@Override
							public void update(LineEvent event) {
								Type type = event.getType();
								if (type == Type.CLOSE || type == Type.STOP) {
									if(!audio.isStopped()) {
										audio.stopCapture();
										System.err.println("Audio capture was stopped unexpectedly");
									}
								}
							}
						});
				System.out.println("STARTED");
				audio.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	} else {
    		System.err.println("Capture is already in process");
    	}
    }
    
    public void stopCapture() {
    	if(audio != null && !audio.isStopped()) {
    		audio.stopCapture();
    		System.out.println("STOPPED");
    	}
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}