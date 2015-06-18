import javax.sound.sampled.AudioFormat;

public class AudioData {
	private AudioFormat format;
	private byte[] sampledData;
	
	//TODO get better name for this
	private int[] originalSignal;
	private Complex[] complexData;
	private double overlapPercent;
	private int fftLength;
	
	AudioData(byte[] samples, AudioFormat audioFormat) {
		format = audioFormat;
		sampledData = samples;
		//Format first needs to be set
		originalSignal = toIntArray(samples);
	}
	
	private int[] toIntArray(byte[] bites) {
		//TODO Check format is set
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
	
	public AudioFormat getFormat() {
		return format;
	}

	public byte[] getSampledData() {
		return sampledData;
	}
	
	public int[] getOriginalSignal() {
		return originalSignal;
	}
	
	public Complex[] getComplexData() {
		return complexData;
	}

	public void setComplexData(Complex[] complexData) {
		this.complexData = complexData;
	}

	public double getOverlapPercentage() {
		return overlapPercent;
	}

	public void setOverlapPercentage(double overlapPercentage) {
		overlapPercent = overlapPercentage;
	}
	
	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}
}
