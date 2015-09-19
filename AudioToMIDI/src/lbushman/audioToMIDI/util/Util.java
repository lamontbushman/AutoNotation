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
import java.util.Set;
import java.util.TreeSet;


public class Util {
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
    	for(Number n : modes) {
    		total += n.doubleValue();
    	}
    	return total / modes.size();
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
	
	public static void main(String args[]) {
		System.out.println(gcdError(84,171,0));
		System.out.println(0.5 % 0.3);
		System.out.println(171 % 84);
	}

	public static List<Integer> findIntegersInSortedRange(List<Integer> ints, int lowerInc, int upperExc) {
		List<Integer> foundInts = new LinkedList<Integer>();
		for(Integer i : ints) {
			if(i >= lowerInc && i < upperExc) {
				foundInts.add(i);
			}
		}
		return foundInts;
	}
}
