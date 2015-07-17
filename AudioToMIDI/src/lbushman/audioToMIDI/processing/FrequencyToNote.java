package lbushman.audioToMIDI.processing;


public class FrequencyToNote {
	private static int midiOffset = 12;
	private static double[] frequencies = {
			15.43,//Below range
			16.35,17.32,18.35,19.45,20.6,21.83,23.12,24.5,25.96,27.5,29.14,
			30.87,32.7,34.65,36.71,38.89,41.2,43.65,46.25,49,51.91,55,58.27,
			61.74,65.41,69.3,73.42,77.78,82.41,87.31,92.5,98,103.83,110,116.54,
			123.47,130.81,138.59,146.83,155.56,164.81,174.61,185,196,207.65,
			220,233.08,246.94,261.63,277.18,293.66,311.13,329.63,349.23,369.99,
			392,415.3,440,466.16,493.88,523.25,554.37,587.33,622.25,659.25,
			698.46,739.99,783.99,830.61,880,932.33,987.77,1046.5,1108.73,
			1174.66,1244.51,1318.51,1396.91,1479.98,1567.98,1661.22,1760,
			1864.66,1975.53,2093,2217.46,2349.32,2489.02,2637.02,2793.83,
			2959.96,3135.96,3322.44,3520,3729.31,3951.07,4186.01,4434.92,
			4698.63,4978.03,5274.04,5587.65,5919.91,6271.93,6644.88,7040,
			7458.62,7902.13,
			8370.9 // Above range
			};
	private static String[] noteNames = {
			"C","C#","D","Eb","E","F","F#","G","G#","A","Bb","B"};
	
/*
23.4375 F#0 
31.25 B0 
23.4375 F#0 
39.0625 Eb1 39.0625 Eb1 
195.3125 G3 

		257.8125 C4 265.625 C4 265.625 C4 265.625 C4 265.625 C4 257.8125 C4 265.625 C4 265.625 C4 

39.0625 Eb1 39.0625 Eb1 
31.25 B0 


		289.0625 D4 296.875 D4 296.875 D4 296.875 D4 296.875 D4 296.875 D4

39.0625 Eb1 39.0625 Eb1 39.0625 Eb1 39.0625 Eb1 

		328.125 E4 

992.1875 B5 
	
		328.125 E4 328.125 E4 328.125 E4 328.125 E4 328.125 E4 328.125 E4 328.125 E4 328.125 E4

664.0625 E5 // octave

39.0625 Eb1 
	
		351.5625 F4 351.5625 F4 351.5625 F4 351.5625 F4 351.5625 F4 351.5625 F4

31.25 B0 
39.0625 Eb1 39.0625 Eb1 

		351.5625 F4 351.5625 F4
	 
		390.625 G4 390.625 G4 390.625 G4 390.625 G4 390.625 G4 390.625 G4 390.625 G4 
	
265.625 C4 ?????????????????????

125.0 B2 125.0 B2 

1179.6875 D6 1187.5 D6 1187.5 D6 ?????????????????

		437.5 A4 437.5 A4 445.3125 A4 437.5 A4 

140.625 C#3 140.625 C#3 

		437.5 A4 

23.4375 F#0 
		
		437.5 A4 

39.0625 Eb1 

		882.8125 A5 

31.25 B0 

		492.1875 B4 

2476.5625 Eb7 2476.5625 Eb7 

1484.375 F#6 

		500.0 B4 500.0 B4 500.0 B4 500.0 B4 492.1875 B4 492.1875 B4 492.1875 B4 492.1875 B4 492.1875 B4 492.1875 B4 
		
		515.625 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5 523.4375 C5
		
1765.625 A6 1765.625 A6 

		585.9375 D5 585.9375 D5 585.9375 D5 585.9375 D5 585.9375 D5 593.75 D5 593.75 D5 593.75 D5 593.75 D5 
		
1976.5625 B6 1976.5625 B6 

		656.25 E5 656.25 E5 656.25 E5 656.25 E5 664.0625 E5 

15.625 Out of range 

31.25 B0 

39.0625 Eb1 39.0625 Eb1

2125.0 C7 2117.1875 C7 

		703.125 F5 703.125 F5 703.125 F5 703.125 F5 703.125 F5 703.125 F5 703.125 F5 703.125 F5 703.125 F5
		
		789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5 789.0625 G5
		
		882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5 882.8125 A5
		
		992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5 992.1875 B5
		
15.625 Out of range 

23.4375 F#0 

3148.4375 G7 

		1046.875 C6 1039.0625 C6 1046.875 C6 1046.875 C6 1054.6875 C6 1054.6875 C6 1054.6875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1046.875 C6 1054.6875 C6 1054.6875 C6 
	
187.5 F#3 

31.25 B0 

0.0 Out of range 

31.25 B0 

23.4375 F#0 

31.25 B0 
 */
	
	
	/*  
C4 C4 C4 C4 C4 C4 C4 C4 
D4 D4 D4 D4 D4 D4 
Eb1 Eb1 Eb1 Eb1 
E4 E4 E4 E4 E4 E4 E4 E4
F4 F4 F4 F4 F4 F4
G4 G4 G4 G4 G4 G4 G4
A4 A4 A4 A4
B4 B4 B4 B4 B4 B4 B4 B4 B4 B4
C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5
D5 D5 D5 D5 D5 D5 D5 D5 D5
E5 E5 E5 E5 E5
F5 F5 F5 F5 F5 F5 F5 F5 F5
G5 G5 G5 G5 G5 G5 G5 G5 G5 G5 G5
A5 A5 A5 A5 A5 A5 A5 A5 A5 A5
B5 B5 B5 B5 B5 B5 B5 B5 B5 B5 B5
C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6
*/	
	

	/*  
C4 C4 C4 C4 C4 C4 C4 C4 
Eb1 Eb1 B0 
D4 D4 D4 D4 D4 D4 
Eb1 Eb1 Eb1 Eb1 
E4 E4 E4 E4 E4 E4 E4 E4
F4 F4 F4 F4 F4 F4
G4 G4 G4 G4 G4 G4 G4
D6 D6 D6
A4 A4 A4 A4
B4 B4 B4 B4 B4 B4 B4 B4 B4 B4
C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5
D5 D5 D5 D5 D5 D5 D5 D5 D5
E5 E5 E5 E5 E5
F5 F5 F5 F5 F5 F5 F5 F5 F5
G5 G5 G5 G5 G5 G5 G5 G5 G5 G5 G5
A5 A5 A5 A5 A5 A5 A5 A5 A5 A5
B5 B5 B5 B5 B5 B5 B5 B5 B5 B5 B5
C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6
*/	

	
	/*
Eb1 Eb1  
C4 C4 C4 C4 C4 C4 C4 C4 
Eb1 Eb1 B0 
D4 D4 D4 D4 D4 D4 
Eb1 Eb1 Eb1 Eb1 
E4 E4 E4 E4 E4 E4 E4 E4
F4 F4 F4 F4 F4 F4
Eb1 Eb1
F4 F4
G4 G4 G4 G4 G4 G4 G4
B2 B2
D6 D6 D6
A4 A4 A4 A4
C#3 C#3
Eb7 Eb7
B4 B4 B4 B4 B4 B4 B4 B4 B4 B4
C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5
A6 A6
D5 D5 D5 D5 D5 D5 D5 D5 D5
B6 B6
E5 E5 E5 E5 E5
Eb1 Eb1
C7 C7
F5 F5 F5 F5 F5 F5 F5 F5 F5
G5 G5 G5 G5 G5 G5 G5 G5 G5 G5 G5
A5 A5 A5 A5 A5 A5 A5 A5 A5 A5
B5 B5 B5 B5 B5 B5 B5 B5 B5 B5 B5
C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6
*/	
	
	
	/*
F#0 B0 F#0 Eb1 Eb1 
G3 
C4 C4 C4 C4 C4 C4 C4 C4 
Eb1 Eb1 B0 
D4 D4 D4 D4 D4 D4 
Eb1 Eb1 Eb1 Eb1 
E4 
B5 
E4 E4 E4 E4 E4 E4 E4 E4
E5
Eb1
F4 F4 F4 F4 F4 F4
B0 
Eb1 Eb1
F4 F4
G4 G4 G4 G4 G4 G4 G4
C4
B2 B2
D6 D6 D6
A4 A4 A4 A4
C#3 C#3
A4 
F#0
A4
Eb1
A5
B0
B4
Eb7 Eb7
F#6
B4 B4 B4 B4 B4 B4 B4 B4 B4 B4
C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5 C5
A6 A6
D5 D5 D5 D5 D5 D5 D5 D5 D5
B6 B6
E5 E5 E5 E5 E5
Out of range
B0
Eb1 Eb1
C7 C7
F5 F5 F5 F5 F5 F5 F5 F5 F5
G5 G5 G5 G5 G5 G5 G5 G5 G5 G5 G5
A5 A5 A5 A5 A5 A5 A5 A5 A5 A5
B5 B5 B5 B5 B5 B5 B5 B5 B5 B5 B5
Out of range 
F#0
G7
C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6 C6
F#3
B0
Out of range
B0
F#0
B0
	*/
	private FrequencyToNote() {}
	
	public static String toNoteName(int index) {
		if(index == 0 || index == frequencies.length - 1)
			return "Out of range";
		return noteNames[(index - 1) % noteNames.length] + ((index -1) / noteNames.length);
	}
	
	public static int toMidiNote(int index) {
		return index - 1 + midiOffset;
	}
	
	public static int findIndex(double frequency) {//440/466.16    450
		int index = 0;
		while(index + 1 < frequencies.length &&
				 frequencies[index+1] <= frequency) {
			index++;
		}
		double first = frequency - frequencies[index];
		double second;
		if(index + 1 < frequencies.length)
			second = frequencies[index + 1] - frequency;
		else {
			return index;
		}
		
		double diff = second - first;
		double devi = Math.abs(diff) / frequencies[index + 1];
		
		if(devi < .01) {
			System.err.println("Found a frequency within 5% of the center between two other frequencies."
					+ " deviation: " + devi + "frequency: " + frequency + 
					" lower: " + frequencies[index] + " upper: " + frequencies[index + 1]);
		}
		
		/*if (first / frequencies[index+1] == second / frequencies[index+1]) {
			System.err.println("Found a frequency in the dead center!!!! Perhaps change algorithm.");
		}*/
		
/*		System.out.println(first + " " + second);*/
		
		if(diff >= 0) {//(first <= second) {
			return index;
		} else {
			return index + 1;
		}
	}
	
	public static String findNote(double frequency) {
		return toNoteName(findIndex(frequency)) /*+ " " + findIndex(frequency) + " " + frequencies[findIndex(frequency)]*/;
		//return frequencies[findIndex(frequency)] +"";
	}
	
	public static double findFrequency(double frequency) {
		return frequencies[findIndex(frequency)]; /*+ " " + findIndex(frequency) + " " + frequencies[findIndex(frequency)]*/
		//return frequencies[findIndex(frequency)] +"";
	}
	
	public static int findMidiNote(double frequency) {
		return toMidiNote(findIndex(frequency));
	}
	
	public static void main(String args[]) {
		double frequency = 10000;
		System.out.println(FrequencyToNote.findNote(frequency));//8136.515 // 16.835
		System.out.println(FrequencyToNote.findMidiNote(frequency));
	}

}
