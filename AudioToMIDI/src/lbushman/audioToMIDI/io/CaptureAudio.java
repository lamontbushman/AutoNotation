package lbushman.audioToMIDI.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.LineEvent.Type;


public class CaptureAudio extends Thread {
	private boolean stopped = false;
	private TargetDataLine line;
	private ByteArrayOutputStream out;
	private AudioFormat format;
	private LineListener listener;
	
	public CaptureAudio(LineListener listener) {
		this.format = getDefaultFormat();
		this.listener = listener;
	}
	
	public static AudioFormat getDefaultFormat() {
		// Initialize AudioFormat
		//4186.01 highest piano key
		//8372.02 twice that
		//16384 multiple of two 
		//log2(16384) = 14;
		float sampleRate = 16384;//32768;//44100;//16384;//16000; 16384  // 16000  // Max: 192002
		int sampleSizeInBits = 8;//16;   //was 8 
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
	}
	
	private void openTargetDataLine() {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		if(!AudioSystem.isLineSupported(info)) {
			try {
				throw new Exception("DataLine Not Supported\n" + info);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.addLineListener(listener);
			//Probably don't need this since a close will end this thread anyway.
/*			line.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					Type type = event.getType();
					if (type == Type.CLOSE || type == Type.STOP) {
						stopped = true;
					}
				}
			});*/
			line.open(format); //open(format,bufferSize);
		} catch(LineUnavailableException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public void run() {
		openTargetDataLine();
		capture();
	}
	
	public void stopCapture() {
		stopped = true;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	private void capture() {
		int numBytesRead = 0;
		/*
		 * https://docs.oracle.com/javase/tutorial/sound/capturing.html
		 * Notice that in this example, the size of the byte array into which the data is read is 
		 * set to be one-fifth the size of the line's buffer. If you instead make it as big as the
		 *  line's buffer and try to read the entire buffer, you need to be very exact in your 
		 *  timing, because data will be dumped if the mixer needs to deliver data to the line 
		 *  while you are reading from it. By using some fraction of the line's buffer size, as 
		 *  shown here, your application will be more successful in sharing access to the line's 
		 *  buffer with the mixer.
		 */
/*		int dataBufferSize = line.getBufferSize() / 5;*/
		int dataBufferSize = 4096;
		byte[] data = new byte[dataBufferSize];
		//only call once ready to capture so that the buffer doesn't overflow
		// Begin audio capture.
		line.start();
		
		out = new ByteArrayOutputStream();
		// Here, stopped is a global boolean set by another thread.
		while (!stopped) {
			// Read the next chunk of data from the TargetDataLine.
			numBytesRead = line.read(data, 0, dataBufferSize);
			// Save this chunk of data.
			out.write(data, 0, numBytesRead);
		}
		line.stop();
		numBytesRead = 0;
		do {
			// I don't think this is ever reading anything
			numBytesRead = line.read(data, 0, dataBufferSize);
		} while(numBytesRead > 0);
		line.close();
	}
	
	public ByteArrayOutputStream getStream() {
		return out;
	}
	
	public AudioFormat getFormat() {
		return format;
	}
	
	public void setFormat(AudioFormat audioFormat) {
		format = audioFormat;
	}
}
