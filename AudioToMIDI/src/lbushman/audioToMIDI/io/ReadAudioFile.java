package lbushman.audioToMIDI.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class ReadAudioFile {
	private File file;
	private ByteArrayOutputStream out;
	private AudioFormat format;
	
	public ReadAudioFile(File file) {
		this.file = file;
	}
	
	public void readFile() {
		try {
			FileInputStream fis = new FileInputStream(file);
			out = new ByteArrayOutputStream();
			byte[] array = new byte[(int) file.length()];
			fis.read(array);
			fis.close();
			out.write(array);
			
			format = CaptureAudio.getDefaultFormat();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	public void readFile(boolean old) {
		out = new ByteArrayOutputStream();
		
		int totalFramesRead = 0;
		try {
			AudioInputStream audioInputStream = 
					AudioSystem.getAudioInputStream(file);
			
			format = audioInputStream.getFormat();
			int bytesPerFrame = format.getFrameSize();
		    if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
		    	// some audio formats may have unspecified frame size
		    	// in that case we may read any amount of bytes
		    	bytesPerFrame = 1;
		    } 
		    // Set an arbitrary buffer size of 1024 frames.
		    int numBytes = 1024 * bytesPerFrame; 
		    byte[] audioBytes = new byte[numBytes];
		    try {
		    	int numBytesRead = 0;
		    	int numFramesRead = 0;
		    	// Try to read numBytes bytes from the file.
		    	while ((numBytesRead = 
		    			audioInputStream.read(audioBytes)) != -1) {
		    		out.write(audioBytes, 0, numBytesRead);
		    		
		    		
		    		// Calculate the number of frames actually read.
		    		numFramesRead = numBytesRead / bytesPerFrame;
		    		totalFramesRead += numFramesRead;
		    		// Here, do something useful with the audio data that's 
		    		// now in the audioBytes array...
		    	}
		    } catch (Exception ex) { 
		    	// Handle the error...
		    	//TODO have better errors
		    } finally {
		    	out.close();
		    }
	   } catch (Exception e) {
		  // Handle the error...
	   }
	}
	
	public ByteArrayOutputStream getStream() {
		return out;
	}
	
	public AudioFormat getFormat() {
		return format;
	}
}
