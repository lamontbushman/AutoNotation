package lbushman.audioToMIDI.test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lbushman.audioToMIDI.io.KeySignature;
import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.util.Util;

public class ReadSongs {
	private final static String path = "testFiles";
	private final static String separator = ",";
	private final static String suffix = ".csv";
	
	public static Map<Integer, List<String[]>> readAll() {
		Map<Integer, List<String[]>> data = new HashMap<Integer, List<String[]>>();
		File folder = new File(path);
		System.out.println();
		File[] listOfFiles = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				if(fileName.contains(".csv")) {
					fileName = fileName.replace(".csv", "");
					try {
						Integer.parseInt(fileName);
					} catch(NumberFormatException nfe) {
						return false;
					}
					return true;
				}
				return false;
			}
		});
		for(File file : listOfFiles) {
			String fileName = file.getName();
			fileName = fileName.replace(".csv", "");
			Integer number = Integer.parseInt(fileName);
			data.put(number, read(file));
		}
		return data;
	}
	
	public static List<String[]> read(File file) {
		return CSVReader.readFile(file, separator);
	}
	
	public static List<String[]> read(int songNumber) {
		File file = new File(path + File.separator + songNumber + suffix);
		return CSVReader.readFile(file, separator);
	}
	
	public static List<Note> parseNotes(List<String[]> data) {
		List<Integer> positions = parseIntegers(data, 4, Integer.MAX_VALUE);
		List<String> namesNAccidentals = parseStrings(data, 3, Integer.MAX_VALUE);
		Util.assertBool(positions.size() == namesNAccidentals.size(), "Bad test file: Columns 3 and 4 are not the same size.");
		
		List<String> keySignatureColumn = parseStrings(data, 1, 2);
		Util.assertBool(keySignatureColumn.size() == 2, "Bad test file: Column 1 length is not 2");
		
		String majorMinorStr = Util.equalsOne(keySignatureColumn.get(0), "#", "b", "null");
		int numSharpsOrFlats = Util.validInteger(keySignatureColumn.get(1), 0, 7);
		Util.assertBool(keySignatureColumn.size() == 2 && numSharpsOrFlats != Integer.MIN_VALUE && majorMinorStr != null, 
				"Bad test file: Columns 1 doesn't have correct data.");
		
		Boolean majorMinor = majorMinorStr.equals("null")? null : (majorMinorStr.equals("#")? true : false);	
		KeySignature ks = new KeySignature(majorMinor, numSharpsOrFlats);
		
		List<Note> notes = new ArrayList<Note>();
		for(int i = 0; i < positions.size(); i++) {
			String nNA = namesNAccidentals.get(i);
			String sharpFlatEmpty = "";
			Util.assertBool(nNA.length() > 0 && nNA.length() < 3 &&
						Util.equalsOne(nNA.charAt(0) + "", "A","B","C","D","E","F","G") != null &&
						(nNA.length() != 2 || (sharpFlatEmpty = Util.equalsOne(nNA.charAt(1) + "", "#", "b")) != null), 
						"Note: " + nNA + " is invalid");
			Boolean sharpFlatNull = sharpFlatEmpty.equals("")? null : (sharpFlatEmpty.equals("#")? true : false);
			Note note = new Note(nNA.charAt(0), sharpFlatNull, positions.get(i));
			if(sharpFlatNull == null) {
				note = ks.conform(note);
			}
			notes.add(note);
		}
		return notes;
	}
	
	public static Map<Integer, List<Note>> parseAllNotes(Map<Integer, List<String[]>> data) {
		Map<Integer, List<Note>> notes = new HashMap<Integer, List<Note>>();
		for(Entry<Integer, List<String[]>> entry : data.entrySet()) {
			notes.put(entry.getKey(), parseNotes(entry.getValue()));
		}
		return notes;
	}
	
	public static List<Double> parseNoteLengths(List<String[]> data) {
		return parseDoubles(data, 0, Integer.MAX_VALUE);
	}
	
	public static Map<Integer, List<Double>> parseAllNoteLengths(Map<Integer, List<String[]>> data) {
		Map<Integer, List<Double>> doubles = new HashMap<Integer, List<Double>>();
		for(Entry<Integer, List<String[]>> entry : data.entrySet()) {
			doubles.put(entry.getKey(), parseNoteLengths(entry.getValue()));
		}
		return doubles;
	}
	
	public static List<Integer> parseDownBeatInfo(List<String[]> data) {
		return parseIntegers(data, 2, 2);
	}
	
	public static Map<Integer, List<Integer>> parseAllDownBeatInfo(Map<Integer, List<String[]>> data) {
		Map<Integer, List<Integer>> integers = new HashMap<Integer, List<Integer>>();
		for(Entry<Integer, List<String[]>> entry : data.entrySet()) {
			integers.put(entry.getKey(), parseDownBeatInfo(entry.getValue()));
		}
		return integers;
	}
	
	public static List<Double> parseDoubles(List<String[]> data, int position, int numRows) {
		List<Double> doubles = new ArrayList<Double>();
		int rowCount = 0;
		for(String[] row : data) {
			if(position < row.length)
				doubles.add(Double.parseDouble(row[position]));
			else
				doubles.add(0.0);
			rowCount++;
			if(rowCount == numRows)
				break;
		}
		return doubles;
	}
	
	public static List<Integer> parseIntegers(List<String[]> data, int position, int numRows) {
		List<Integer> integers = new ArrayList<Integer>();
		int rowCount = 0;
		for(String[] row : data) {
			if(position < row.length)
				integers.add(Integer.parseInt(row[position]));
			else
				integers.add(0);
			rowCount++;
			if(rowCount == numRows)
				break;
		}
		return integers;
	}
	
	public static List<String> parseStrings(List<String[]> data, int position, int numRows) {
		List<String> strings = new ArrayList<String>();
		int rowCount = 0;
		for(String[] row : data) {
			if(position < row.length)
				strings.add(row[position]);
			else
				strings.add("");
			rowCount++;
			if(rowCount == numRows)
				break;
		}
		return strings;
	}
	
	public static void main(String args[]) {
		List<String[]> data = read(3);
		List<Note> notes = parseNotes(data);
		System.out.println(notes);
		/*
		Map<Integer, List<String[]>> data = ReadSongs.readAll();
		Map<Integer, List<Double>> noteLengths = parseAllNoteLengths(data);
		for(Entry<Integer, List<Double>> song : noteLengths.entrySet()) {
			System.out.println(song.getKey() + " " + song.getValue());
		}
		Map<Integer, List<Integer>> downBeatInfo = parseAllDownBeatInfo(data);
		for(Entry<Integer, List<Integer>> song : downBeatInfo.entrySet()) {
			System.out.println("DB: " + song.getKey() + " " + song.getValue());
		}
		*/
	}
}
