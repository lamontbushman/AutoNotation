package lbushman.audioToMIDI.util;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Util {
	private Util() {}
	
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
    
    public static int maxIndex(List<Double> list, int lowerInc, int upperExc) {
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
    
    public static double average(List<Integer> modes) {
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
}
