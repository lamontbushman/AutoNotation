import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class WriteAudioFile extends Thread {
	private byte[] bites;
	private AudioFormat format;
	private File file;
	
	WriteAudioFile(byte[] bites, AudioFormat format, File file) {
		this.bites = bites;
		this.format = format;
		this.file = file;
	}
	
	@Override
	public void run() {
		writeFile();
	}
	
	private void writeFile() {
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

