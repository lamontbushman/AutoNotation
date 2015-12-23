package lbushman.audioToMIDI.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import lbushman.audioToMIDI.io.Note;


public class Util {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static final Comparator<Integer> COMPARATOR_INTEGER = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}
	};

	private Util() {}

	public static List<Integer> halfList(List<Integer> values) {
		ListIterator<Integer> it = values.listIterator();
		List<Integer> list = new ArrayList<Integer>();
    	while(it.hasNext()) {
    		list.add(it.next() / 2);
    	}
    	return list;
	}
	
	public static List<Integer> doubleList(List<Integer> values) {
		ListIterator<Integer> it = values.listIterator();
		List<Integer> list = new ArrayList<Integer>();
    	while(it.hasNext()) {
    		list.add(it.next() * 2);
    	}
    	return list;
	}
	
	public static Double[] stretchList(Double[] values) {
		Double[] newList = new Double[values.length];
		int j = 0;
		for(int i = 0; i < newList.length; i++) {
			if(i % 2 == 0)
				newList[i] = values[j++];
			else
				newList[i] = 0.0;
		}
		return newList;
	}
	
	public static Double sum(List<Double> values) {
		Double sum = 0.0;
		for(Double value : values) {
			sum += value.doubleValue();
		}
		return sum;
	}

	public static int sumInt(List<Integer> values) {
		int sum = 0;
		for(int value : values) {
			sum += value;
		}
		return sum;
	}
	
    public static double[] convertDoubles(List<Double> doubles)
    {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ret[i] = iterator.next().doubleValue();
            i++;
        }
        return ret;
    }
    
    public static <T> List<T> mode(List<T> peaks) {
    	Map<T, Integer> counts = new HashMap<T, Integer>(); 
    	for(T n : peaks) {
    		counts.compute(n, (k, v) -> (v == null) ? 1 : v+1);
    	}

		// Convert Map to List
		List<Map.Entry<T, Integer>> countList = 
			new LinkedList<Map.Entry<T, Integer>>(counts.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(countList, new Comparator<Map.Entry<T, Integer>>() {
			public int compare(Map.Entry<T, Integer> o1,
                                           Map.Entry<T, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		
		List<T> modes = new LinkedList<T>();
		int maxCount = Integer.MIN_VALUE;
		for(int i = 0; i < countList.size(); i++) {//TODO change to iterator
			int currCount = countList.get(i).getValue();
			if(maxCount == Integer.MIN_VALUE || maxCount == currCount) {
				maxCount = currCount;
				modes.add(countList.get(i).getKey());
			}
		}
		
		return modes;
/*		
		
    	
    	list.sort(new NumberComparator());
    	for(Number n : list) {
    		System.out.println(n);
    	}
    	int currCount = 0;
    	int prevCount = 0;
    	List<Number> modes = new ArrayList<Number>();
    	double currValue = Double.NEGATIVE_INFINITY;
    	for(int i = 0; i < list.size(); i++) {
    		double valueI = list.get(i).doubleValue();
    		if(currValue == valueI) {
    			currCount++;
    		}

    	}*/
    }
    
    public static int minIndex(Number[] list, int lowerInc, int upperExc) {
    	Double min = Double.POSITIVE_INFINITY;
    	int index = -1;
    	for(int i = lowerInc; i < upperExc; i++) {
    		if(list[i].doubleValue() < min) {
    			index = i;
    			min = list[i].doubleValue();
    		}
    	}
    	return index;
    }
    
    //TODO study extends versus super
    /**
     * Gets the EARLIEST max index.
     * @param list
     * @param lowerInc
     * @param upperExc
     * @return
     */
    public static int maxIndex(List<? extends Number> list, int lowerInc, int upperExc) {
    	Double max = Double.NEGATIVE_INFINITY;
    	int index = -1;
    	for(int i = lowerInc; i < upperExc; i++) {
    		if(list.get(i).doubleValue() > max) {
    			index = i;
    			max = list.get(i).doubleValue();
    		}
    	}
    	return index;
    }
    
    //TODO study extends versus super
    /**
     * Gets the EARLIEST minimum index.
     * @param list
     * @param lowerInc
     * @param upperExc
     * @return
     */
    public static int minIndex(List<? extends Number> list, int lowerInc, int upperExc) {
    	Double min = Double.POSITIVE_INFINITY;
    	int index = -1;
    	for(int i = lowerInc; i < upperExc; i++) {
    		if(list.get(i).doubleValue() < min) {
    			index = i;
    			min = list.get(i).doubleValue();
    		}
    	}
    	return index;
    }
    
    private static boolean debug = false; 
    public static void setDebugMode(boolean doDebug) {
    	debug = doDebug;
    }
    
    private static boolean verify = true; 
    public static void setVerify(boolean doVerify) {
    	verify = doVerify;
    }
    
    public static void logIfFails(boolean passed, String str) {
    	if(!passed) {
    		System.err.println(str);
    		System.err.flush();   		
    	}
    }
    
    public static void verify(boolean passed, String str) {
    	if(!passed) {
    		System.err.println("<<<" +str + ">>>");
    		System.err.flush();
    		if(verify) {
    			System.out.println("Hitting the breakpoint");
    		} else {
    			System.exit(1);
    		}
    	}
    }
    
    public static void printErrorln(String str) {
    	if(debug) {
    		System.err.println(str);
    	}
    }
    
    public static void printError(String str) {
    	if(debug) {
    		System.err.print(str);
    	}
    }
    
    public static void println(String str) {
    	if(debug) {
    		System.out.println(str);
    	}
    }
    
    public static void print(String str) {
    	if(debug) {
    		System.out.print(str);
    	}
    }
    
    /**
     * Gets the LATEST max index.
     * @param list
     * @param lowerInc
     * @param upperExc
     * @return
     */
    public static int lastMaxIndex(List<? extends Number> list, int lowerInc, int upperExc) {
    	Double max = Double.NEGATIVE_INFINITY;
    	int index = -1;
    	if(lowerInc > list.size() || upperExc > list.size()) {
    		System.err.println("Error in Util.lastMaxIndex: out of bounds");
    	}
    	for(int i = lowerInc; i < upperExc; i++) {
    		if(list.get(i).doubleValue() >= max) {
    			index = i;
    			max = list.get(i).doubleValue();
    		}
    	}
    	return index;
    }
    
    public static double average(List<Integer> modes) {
    	double total = 0;
    	for(Number n : modes) {
    		total += n.doubleValue();
    	}
    	return total / modes.size();
    }
    
    public static double averageD(List<Double> modes) {
    	double total = 0;
    	for(double n : modes) {
    		total += n;
    	}
    	return total / modes.size();
    }
    
    public static double average(double[] values) {
    	double total = 0;
    	for(double n : values) {
    		total += n;
    	}
    	return total / values.length;
    }
    
    public static int round(double number) {
    	return (int) Math.round(number);
    }
    
    //TODO remove after peakIndex(Number[]..) is removed from main
    public static int maxIndex(Number[] list, int lowerInc, int upperExc) {
    	Double max = Double.NEGATIVE_INFINITY;
    	int index = -1;
    	for(int i = lowerInc; i < upperExc; i++) {
    		if(list[i].doubleValue() > max) {
    			index = i;
    			max = list[i].doubleValue();
    		}
    	}
    	return index;
    }
    
    private class NumberComparator implements Comparator<Number> {
		@Override
		public int compare(Number o1, Number o2) {
			return (int) Math.ceil(o1.doubleValue() - o2.doubleValue());
		}
    	
    }
/*    
    public static void main(String args[]) {
    	List<Number> numbers = new ArrayList<Number>();
    	numbers.add(9);
    	numbers.add(9);
    	numbers.add(9);
    	numbers.add(1);
    	numbers.add(9);
    	numbers.add(1);
    	numbers.add(9);
    	numbers.add(1);
    	numbers.add(1);
    	numbers.add(9);
    	numbers.add(1);
    	numbers.add(1);
    	numbers.add(2);
    	numbers.add(3);
    	numbers.add(3);
    	numbers.add(3);
    	numbers.add(3);
    	numbers.add(3);
    	numbers.add(3);

    	List<Number> modes = Util.mode(numbers);
    	for(Number n : modes) {
    		System.out.println(n);
    	}
    	
    }*/

    /**
     * Rounds to the nearest fraction 1/oneOver
     * 
     * fraction_round(number, oneOver) rounds to the closest 1/(4*oneOver) note, 
     * which is 1/oneOver of a beat.
     * 
     * 
     * @param number
     * @param oneOver
     * @return
     */
	public static double fractionCeil(double number, double oneOver) {
		/*
			1. multiply by oneOver
			2. round to whole number
			3. divide by oneOver
		*/
		return Math.ceil(number * oneOver) / oneOver;
	}
	
	public static double fractionFloor(double number, double oneOver) {
		/*
			1. multiply by oneOver
			2. round to whole number
			3. divide by oneOver
		*/
		return Math.floor(number * oneOver) / oneOver;
	}
	
	public static double fractionRound(double number, double oneOver) {
		/*
			1. multiply by oneOver
			2. round to whole number
			3. divide by oneOver
		*/
		return Math.round(number * oneOver) / oneOver;
	}
	
	/**
	 * Ordered for efficiency sake
	 * @param a
	 * @param b
	 * @return
	 */
	private static int gcdOrdered(int a, int b) {		
		if (b == 0) {
			return a;
		} else {
			return gcd(b, a % b);
		}
	}
	
	public static int gcd(int a, int b) {
		if(a >= b) {
			return gcdOrdered(a,b);
		} else {
			return gcdOrdered(b,a);
		}
	}
	
	/**
	 * Ordered for efficiency sake
	 * @param a
	 * @param b
	 * @return
	 */
	private static double gcdErrorOrdered(double a, double b) {
		Util.println("a: " + a + "\tb: " + b);
		Util.println((a/b) + "");
		Util.println(((a-b)/b) + "");
		
		if (b == 0) {
			return a;
		} else {
			return gcdErrorOrdered(b, a % b);
		}
	}
	
	private static double percentError(double a, double b) {
		double d = Math.abs(a - b) / b;
		Util.println(d + "");
		return d;
	}
	
	private static double gcdErrorOrdered2(double num1, double num2) {
		//a > b
		double a = num1;
		double b = num2;
		percentError(a, b);
		
		int start = (int) Math.round(a / b);
		
		while(b > 1) {
			
			System.out.println();
		}
		
		System.out.println("a: " + a + "\tb: " + b);
		System.out.println(a/b);
		System.out.println((a-b)/b);
		
		if (b == 0) {
			return a;
		} else {
			return gcdErrorOrdered(b, a % b);
		}
	}
	
	public static int gcdError(int a, int b, double error) {
		if(a >= b) {
			return (int) Math.round(gcdErrorOrdered(a,b));
		} else {
			return (int) Math.round(gcdErrorOrdered(b,a));
		}
	}
	
/*	public static void main(String args[]) {
		System.out.println(gcdError(84,171,0));
		System.out.println(0.5 % 0.3);
		System.out.println(171 % 84);
	}*/

	public static List<Integer> findIntegersInSortedRange(List<Integer> ints, int lowerInc, int upperExc) {
		List<Integer> foundInts = new LinkedList<Integer>();
		for(Integer i : ints) {
			if(i >= lowerInc && i < upperExc) {
				foundInts.add(i);
			}
		}
		return foundInts;
	}
	
	private static Map<String, TimeData> currentTmes = new HashMap<String, TimeData>();
	private static Map<String, Long> totalTimes = new HashMap<String, Long>();
	public static int integerValue;

	public static long timeDiff(String text, boolean display) {
		TimeData td = currentTmes.get(text);
		long time = 0;
		if(td == null) {
			td = new TimeData(text);
			currentTmes.put(text,td);
		} else {
			time = td.stop();
			currentTmes.remove(text);
		}
		
		if(display) {
			System.out.println(td);
		}
		return time;
	}
	
	public static long timeDiff(String text) {
		return timeDiff(text, true);
	}
	
	public static void totalTimeDiff(String text) {
		long time = timeDiff(text, false);
		if(time != 0) {
			Long currentTime = totalTimes.get(text);
			if(currentTime == null) {
				currentTime = 0L;
			}
			totalTimes.put(text, currentTime + time);
		}
	}
	
	public static void totalTime(String text) {
		Long currentTime = totalTimes.remove(text);
		if(currentTime == null) {
			currentTime = 0L;
		}
		System.out.println("TT: " + text + " " + currentTime);
	}

	public static void equal(List<Double> list1, List<Double> list2) {
		int smallestSize = list1.size();
		if(list1.size() != list2.size()) {
			System.out.println("Lists are not the same size");
			if(smallestSize > list2.size()) {
				smallestSize = list2.size();
			}
		}
		System.out.print("List differences: ");
		for(int i = 0; i < smallestSize; i++) {
			if(list1.get(i).compareTo(list2.get(i)) != 0) {
				System.out.print("(" + i + "):[" + list1.get(i) + "," + list2.get(i) + "]");
			}
		}
		System.out.println();
	}
	
	public static <T> void compareLists(List<T> list1, List<T> list2) {
		int size1 = list1.size();
		int size2 = list2.size();
		if(size1 != size2) {
			System.out.println("Lists are not the same size");
		}
		int numDifferent = 0;
		int largest = (size1 > size2)? size1 : size2;
		for(int i = 0; i < largest; i++) {
			if( i >= size1 || i>= size2 || !list1.get(i).equals(list2.get(i))) {
				numDifferent++;
				System.out.format("%4s ", i);
			} else {
				System.out.print("     ");
			}
		}		
		System.out.println();
		for(int i = 0; i < size1; i++) {
			if(i < size2 && list1.get(i).equals(list2.get(i))) {
				System.out.format(" %3s ", list1.get(i));
			} else {
				System.out.format(" %3s "/*ANSI_CYAN + */, list1.get(i) /*ANSI_RESET + */);
			}
		}
		System.out.println();
		for(int i = 0; i < size2; i++) {
			System.out.format(" %3s ", list2.get(i));
/*			if(i < size2 && list1.get(i).equals(list2.get(i))) {
				System.out.print(list2.get(i));
			} else {
				System.out.print(ANSI_CYAN + " " + list2.get(i) + " " + ANSI_RESET);
			}*/
		}
		System.out.println();
		System.out.println("numDifferent: " + numDifferent);
	}

	public static void compareNotes(List<Note> list1, List<Note> list2) {
		int size1 = list1.size();
		int size2 = list2.size();
		if(size1 != size2) {
			System.out.println("Lists are not the same size");
		}
		int numDifferent = 0;
		int largest = (size1 > size2)? size1 : size2;
		for(int i = 0; i < largest; i++) {
			if( i >= size1 || i>= size2 || !list1.get(i).equals(list2.get(i))) {
				numDifferent++;
				System.out.format("%4d ", i);
			} else {
				System.out.print("     ");
			}
		}		
		System.out.println();
		for(int i = 0; i < size1; i++) {
			if(i < size2 && list1.get(i).equals(list2.get(i))) {
				System.out.print(" " + list1.get(i) + " ");
			} else {
				System.out.print(/*ANSI_CYAN + */" " + list1.get(i) + /*ANSI_RESET + */" ");
			}
		}
		System.out.println();
		for(int i = 0; i < size2; i++) {
			System.out.print(" " + list2.get(i) + " ");
/*			if(i < size2 && list1.get(i).equals(list2.get(i))) {
				System.out.print(list2.get(i));
			} else {
				System.out.print(ANSI_CYAN + " " + list2.get(i) + " " + ANSI_RESET);
			}*/
		}
		System.out.println();
		System.out.println("numDifferent: " + numDifferent);
	}
	
	public static int floorBase2(int number) {
		int ceil = ceilBase2(number);
		return (ceil == number)? ceil : ceil / 2;
	}

	public static int ceilBase2(int number) {
		int ceil = 1;
		while(ceil < number) {
			ceil *= 2;
		}
		return ceil;
	}
	
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int num;
		while((num = in.nextInt()) != -1) {
			System.out.println(Util.ceilBase2(num));
		}
		in.close();
	}

	public static int maxIndex(List<Double> list) {
		return maxIndex(list, 0, list.size());
	}

	public static int firstPeakAbove(List<Double> values, int start, int noFurther, double value) {
		boolean foundStartOfPeak = false;
		double lastValue = 0;
		double currValue = 0;
		for(int i = start; i <= noFurther; i++) {
			currValue = values.get(i);
			if(foundStartOfPeak) {
				if(currValue < lastValue) {
					return i - 1;
				}
				lastValue = currValue;
			} else if(currValue >= value) {
				foundStartOfPeak = true;
				lastValue = currValue;
			}
		}
		return -1;
	}
	
	public static int firstIndexAbove(List<Double> values, int start, int noFurther, double value) {
		double currValue = 0;
		for(int i = start; i <= noFurther; i++) {
			currValue = values.get(i);
			if(currValue >= value) {
				return i;
			}
		}
		return -1;
	}
	
	public static int lastIndexAboveButBefore(List<Double> values, int start, int noFurther, double value, double before) {
		double currValue = 0;
		int index = -1;
		for(int i = start; i <= noFurther; i++) {
			currValue = values.get(i);
			if(currValue >= value) {
				index = i;
			}
			if(currValue <= before) {
				break;
			}
		}
		return index;
	}

	public static double error(double val1, double val2) {
		return (val1 - val2) / val1;
	}
	
	public static double absoluteError(double val1, double val2) {
		return Math.abs(error(val1, val2));
	}
}
