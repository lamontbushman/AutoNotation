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
	
	public CaptureAudio(AudioFormat format) throws Exception {
		// Open TargetDataLine
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		if(!AudioSystem.isLineSupported(info)) {
			throw new Exception("DataLine Not Supported\n" + info);
		}
		
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					Type type = event.getType();
					if (type == Type.CLOSE || type == Type.STOP) {
						//stopped = true;
					}
				}
			});
			line.open(format); //open(format,bufferSize);
		} catch(LineUnavailableException ex) {
			ex.printStackTrace();
			throw new Exception();
		}
	}
	
	@Override
	public void run() {
		capture();
	}
	
	public void stopCapture() {
		stopped = true;
	}
	
	public void capture() {
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
		int dataBufferSize = line.getBufferSize() / 5;
		byte[] data = new byte[dataBufferSize];
		//only call once ready to capture so that the buffer doesn't overflow
		// Begin audio capture.
		line.start();
		
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
}
