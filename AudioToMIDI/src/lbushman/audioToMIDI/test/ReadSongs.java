package lbushman.audioToMIDI.test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public static void main(String args[]) {
		Map<Integer, List<String[]>> data = ReadSongs.readAll();
		Map<Integer, List<Double>> noteLengths = parseAllNoteLengths(data);
		for(Entry<Integer, List<Double>> song : noteLengths.entrySet()) {
			System.out.println(song.getKey() + " " + song.getValue());
		}
		Map<Integer, List<Integer>> downBeatInfo = parseAllDownBeatInfo(data);
		for(Entry<Integer, List<Integer>> song : downBeatInfo.entrySet()) {
			System.out.println("DB: " + song.getKey() + " " + song.getValue());
		}
	}
}
