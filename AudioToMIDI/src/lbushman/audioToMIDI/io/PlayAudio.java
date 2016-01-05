package lbushman.audioToMIDI.io;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.LineEvent.Type;

import lbushman.audioToMIDI.util.Util;


public class PlayAudio {
	private boolean stopped = false;
	private AudioFormat format;
	private byte[] data;
	
	public PlayAudio(byte[] data, AudioFormat format) {
		this.data = data;
		this.format = format;
		this.format = new AudioFormat(format.getSampleRate() / 2, format.getSampleSizeInBits(), format.getChannels(),
				true, format.isBigEndian());
	}
	
	public boolean isPlaying() {
		return !stopped;
	}
	
	public boolean playClip() {
		this.format = new AudioFormat(format.getSampleRate() * 2, format.getSampleSizeInBits(), format.getChannels(),
				true, format.isBigEndian());		
		
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
						//stopped = true;
						Util.println("Done playing");
						//synchronized (PlayAudio.this) {
						//	PlayAudio.this.notify();
						//}
					}
				}
			});

		    clip.open(format, data, 0, data.length);
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return false;
		}
		clip.start();
		return true;
	}
}
