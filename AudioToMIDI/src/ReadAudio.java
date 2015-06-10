import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;


public class ReadAudio extends Thread {
	private boolean stopped;
	private TargetDataLine line;
	private ByteArrayOutputStream out;
	private AudioFormat format;
	private boolean clipStopped;
	
	public ReadAudio() {
		stopped = false;
		clipStopped = false;
		out = new ByteArrayOutputStream();
		initAudioFormat();
		openTargetLine();
	}
	
	public boolean clipPlaying() {
		return !clipStopped;
	}
	
	@Override
	public void run() {
		try {
			capture();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*int bufferSize = line.getBufferSize();
		byte[] b = new byte[bufferSize];
		int offset = 0;
		int length = bufferSize;
		line.read(b, offset, length);*/
	}
	
	public ByteArrayOutputStream getTargetStream() {
		return out;
	}
	
	public void getMixerInfo() {
		Info[] infos = AudioSystem.getMixerInfo();
		for(Info info : infos) {
			System.out.println(info);
			System.out.println(info.getDescription());
		}
	}
/*	ByteArrayOutputStream inMemory = new ByteArrayOutputStream ();
	DataOutputStream out = new DataOutputStream (inMemory);

	DataInputStream in = new DataInputStream (new ByteArrayInputStream  (inMemory.toByteArray()));
*/
		
	private void initAudioFormat() {
		float sampleRate = 2000;//16000;
		int sampleSizeInBits = 8;//16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		format = new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);	
	}
	
	public boolean playClip() {
		Clip clip;
		DataLine.Info info = new DataLine.Info(Clip.class, 
		    format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			System.err.println("Clip Not Supported\n" + info);
			return false;
		}
		    // Obtain and open the line.
		try {
		    clip = (Clip) AudioSystem.getLine(info);
		    clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					if(event.getType() == Type.STOP) {
						clip.close();
						clipStopped = true;
						System.out.println("Done playing");
						synchronized (ReadAudio.this) {
							ReadAudio.this.notify();//Probably need to call synchronized method.
						}
					}
				}
			});
		    byte[] audio = out.toByteArray();
		    clip.open(format, audio, 0, audio.length);
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return false;
		}
		clip.start();
		/*try {
			Thread.sleep(1000);
			clipStopped = true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Donasde playing");*/
		
		
		return true;
	}
	
	private boolean openTargetLine() {

		
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		if(!AudioSystem.isLineSupported(info)) {
			System.err.println("DataLine Not Supported\n" + info);
			return false;
		}
		
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					// TODO Auto-generated method stub
					Type type = event.getType();
					if (type == Type.CLOSE || type == Type.STOP) {
						stopped = true;
					}
				}
			});
			
			
			line.open(format); //open(format,bufferSize);
		} catch(LineUnavailableException ex) {
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void capture() throws Exception {
		if(line == null) {
			throw new NullPointerException("The target data line is null.");
		} else if(!line.isOpen()) {
			throw new Exception("The target data line hasn't been opened. (Call openTargetLine())");
		} else if(line.isRunning()) {
			throw new Exception("The target data line is already capturing.");
		}
		
		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		int numBytesRead;
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
		
		AudioInputStream ais = new AudioInputStream(line);
		
		// Here, stopped is a global boolean set by another thread.
		while (!stopped) {
		   // Read the next chunk of data from the TargetDataLine.
			
			
		 //  numBytesRead = line.read(data, 0, dataBufferSize);
		   numBytesRead = ais.read(data, 0, dataBufferSize);

		   
		/*   if(numBytesRead < 1600)
			   System.out.println("less");*/
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
		//TODO study wait, notify, and monitors
	}
	
	public void stopCapture() {
		stopped = true;
	}
	
	
	public static void main(String args[]) {
	}
}

class OutputThread extends Thread {
	ByteArrayOutputStream out;
	private boolean stopped = false;
	
	OutputThread(ByteArrayOutputStream stream) {
		out = stream; 
	}
	
	//public 
	
	@Override
	public void run() {
		while(!stopped) {
			System.out.println(out.size());
			byte[] bites = out.toByteArray();
			String str;
			
/*			int signal = 0;
			//previous first
			signal <<= 8;
			signal = 1;
//			System.out.println(String.format("%32s",Integer.toBinaryString(signal)).replace(' ', '0'));
			signal <<= 8;
			signal += 3;
//			System.out.println(String.format("%32s",Integer.toBinaryString(signal)).replace(' ', '0'));
			signal <<= 8;
			signal += 7;
//			System.out.println(String.format("%32s",Integer.toBinaryString(signal)).replace(' ', '0'));
			signal <<= 8;
			signal += 15;
			System.out.println(String.format("%32s",Integer.toBinaryString(signal)).replace(' ', '0'));
			System.exit(0);*/
			
			
			
			//String signal = "";
			int signal = 0;
			/* int signal = 0;
			if(bites != null && bites.length != 0) {
				signal = bites[0];
			}*/
			
			for(int i = 0; i < bites.length; i++) {
				signal <<= 8;
				signal += bites[i];
				
				if(i % 2 == 0) {
					if (i != 0)
						System.out.print(Integer.toHexString(signal) + " ");
//						System.out.print(signal + " ");
						/*System.out.println(String.format("%32s",Integer.toBinaryString(signal)).replace(' ', '0'));*/
					signal = 0;
				}
				
				
							
			/*	if(i % 4 != 0) {
					//signal += String.format("%8s", Integer.toBinaryString(bites[i] & 0xFF)).replace(' ', '0');
				} else {
					
					//temp = 0;
				}*/
				/*signa *= 100;
				for(int j = 0; j < 4; j++) {
					bites.
				}*/
			}
/*			
			for(byte bite : bites) {
				str = ((int)bite) + "";
				
				
				//str = String.format("%02X ", bite);
				//str = String.format("%8s", Integer.toBinaryString(bite & 0xFF)).replace(' ', '0');
				System.out.print(str + " ");
			}
			System.out.println("\n");
			//out.writeTo(out);
*/		
		
	    	/*		byte b = 0;
			String str;
			for(b = 0; b < 100; b++) {
				str = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
				System.out.println(str);
			}
			System.exit(0);*/
			
		
		}
	}

	public void stopOutput() {
		stopped = true;
	}
};