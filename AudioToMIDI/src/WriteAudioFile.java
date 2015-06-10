import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class WriteAudioFile extends Thread {
	
	WriteAudioFile(ByteArray)
    ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
    AudioInputStream audioInputStream;
    audioInputStream = new AudioInputStream(bais, format,buffer.length);
    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);

    long length = (long)(totalByteArray.length / audioFormat.getFrameSize());
    AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
}
