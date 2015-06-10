import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Main extends Application {
	AudioFormat format;
	public int[] toIntArray(byte[] bites) {
		if(format.getSampleSizeInBits() == 16) {
			int[] array = new int[bites.length / 2];
			int arrayIndex = 0;
			for(int i = 0; i < bites.length - 1; i+=2) {
				array[arrayIndex] = (bites[i] << 8) | (bites[i + 1] & 0xFF);
				arrayIndex++;
				System.out.print(Integer.toHexString(array[arrayIndex]) + " ");
			}
			return array;
		} else {
			int[] array = new int[bites.length];
			for(int i = 0; i < bites.length; i++) {
				array[i] = bites[i];
			}
			return array;
		}
	}
	
	public void initializeFormat() {
		// Initialize AudioFormat
		float sampleRate = 2000;//16000;
		int sampleSizeInBits = 8;//16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		format = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}
	
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public void start(Stage stage) {

    	
    	ByteArrayOutputStream stream = runReadAudio();
    	
    	
    	
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
        byte[] bites = stream.toByteArray();
        
        
        int start = bites.length / 2;
        int end = start + 150;//(bites.length * 17) / 32;
        for(int i = start; i < end; i++) {
        	//fix for 32 bit
            series.getData().add(new XYChart.Data((i - start), bites[i]));
         /*   if(i % 100 == 0)
            	System.out.println((i - start)*2 + " " + bites.length);*/
        }
        System.out.println("Got here");
 
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series);
       
        stage.setScene(scene);
        stage.show();
    }
    
    public ByteArrayOutputStream runReadAudio() {		
		ReadAudio audio = new ReadAudio();
		ByteArrayOutputStream stream = audio.getTargetStream();
		
		OutputThread myThread = new OutputThread(stream);
		System.out.println("STARTED");
		audio.start();
		myThread.start();
		
		//ten seconds
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			audio.stopCapture();
			System.out.println("STOPPED");
			
		/*	byte[] bites = stream.toByteArray();
			String str;
			for(byte bite : bites) {
				str = ((int)bite) + "";
				//str = String.format("%02X ", bite);
				//str = String.format("%8s", Integer.toBinaryString(bite & 0xFF)).replace(' ', '0');
				System.out.print(str + " ");
			}
			System.out.println("\n");
*/			
			
			
			myThread.stopOutput();
			audio.playClip();
			try {
				synchronized (audio) {
					while (audio.clipPlaying()) {
			        	 audio.wait();
			        	 System.out.println("In loop");
					}
			     }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println("Done waiting.");
			}
		}
		return stream;
	}
    
 
    public static void main(String[] args) {
        launch(args);
    }
}