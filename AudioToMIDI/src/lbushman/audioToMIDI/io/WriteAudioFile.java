package lbushman.audioToMIDI.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class WriteAudioFile extends Thread {
	private byte[] bites;
	private AudioFormat format;
	private File file;
	
	public WriteAudioFile(byte[] bites, AudioFormat format, File file) {
		this.bites = bites;
		this.format = format;
		this.file = file;
	}
	
	@Override
	public void run() {
		writeBytes();
	}
	
	private void writeBytes() {
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(file);
			stream.write(bites);
			stream.close();
		}  
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	}
	
	private void writeFile() {
		//TODO see if I can save it in big-endian instead of little-endian
		ByteArrayInputStream bais = new ByteArrayInputStream(bites);
	    AudioInputStream audioInputStream = 
	    		new AudioInputStream(bais, format, 
	    				bites.length / format.getFrameSize());
	    try {
			AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
		} catch (IOException e) {
			// TODO Auto-generated catch blockThread
			e.printStackTrace();
			System.exit(0);
		}
	
//	    long length = (long)(totalByteArray.length / audioFormat.getFrameSize());
//	    AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
	    
	}
}

