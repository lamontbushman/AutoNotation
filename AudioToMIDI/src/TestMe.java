import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import lbushman.audioToMIDI.processing.Complex;
import lbushman.audioToMIDI.util.Util;


public class TestMe {
	
	public static void manipulateArray(byte[] array) {
		array[0] = 5;
		array[1] = 6;
		array[2] = 7;
		array[3] = 8;
	}
	
/*	public static <?> void getList() {
	}*/
	
	public static void main(String args[]) {
		Complex[] complexData = new Complex[10];
		for(int i = 0; i < complexData.length; i++) {
			complexData[i] = new Complex(i);
		}
		
		Complex[] toFft = Arrays.copyOfRange(complexData, 0, complexData.length*2);
		
		for(Complex c : toFft) {
			Util.println(c.toString());
		}
		
/*		byte[] bites = new byte[4];
		bites[0] = 1;
		bites[1] = 2;
		bites[2] = 3;
		bites[3] = 4;
		
		manipulateArray(bites);
		for(byte b : bites) {
			System.out.println(b);
		}*/
		
/*		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		GenerateNumbers gn = new GenerateNumbers(stream);
		ConsumeNumbers cn = new ConsumeNumbers(stream);
		gn.start();
		cn.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gn.stopIt();
		cn.stopIt();*/
	}
}

class GenerateNumbers extends Thread {
	boolean stopped = false;
	ByteArrayOutputStream stream;
	GenerateNumbers(ByteArrayOutputStream baos) {
		stream = baos;
	}
	
	@Override
	public void run() {
		int i = 0;
		while(!stopped) {
			stream.write(i);
			Util.print("G:" + i);
			i++;
		}
	}	
	
	public void stopIt() {
		stopped = true;
	}
}

class ConsumeNumbers extends Thread {
	boolean stopped = false;
	ByteArrayOutputStream stream;
	ConsumeNumbers(ByteArrayOutputStream baos) {
		stream = baos;
	}
	
	@Override
	public void run() {
		while(!stopped) {
			Util.println("Size:" + stream.size());
			byte[] bites = stream.toByteArray();
			for(int i = 0; i < bites.length; i++) {
				Util.println("C:" + (int)bites[i]);
			}
		}
	}	
	
	public void stopIt() {
		stopped = true;
	}
}
