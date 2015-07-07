import java.util.Stack;

public class RunningWindowStats {
	Stack<Number> window;
	double total;
	final int CAPACITY;
	
	public RunningWindowStats(int windowLength) {
		window = new Stack<Number>();
		CAPACITY = windowLength;
		total = 0;
	}
	
	public void add(Number number) {
		window.push(number);
		total += number.doubleValue();
		if(window.size() > CAPACITY) {
			double value = window.pop().doubleValue();
			total -= value;
		}
	}
	
	public boolean isFull() {
		return window.size() == CAPACITY;
	}
	
	public double mean() {
		return total / window.size();
	}
	
	public double variance() {
		return variance(mean());
	}
	
	public double variance(double mean) {
		double sum = 0;
		for(Number number : window) {
			sum += Math.pow(number.doubleValue() - mean, 2);
		}
		
		return sum / window.size(); //(window.size() - 1);
	}
	
	public double stdDevi() {
		return Math.sqrt(variance());
	}
	
	public double zScore(Number number) {
		double avg = mean();		
		double diff = number.doubleValue() - avg;
		return diff / variance(avg);
	}
	
	public static void main(String args[]) {
		RunningWindowStats rws = new RunningWindowStats(4);
		for(int i = 0; i < 40; i++) {
			rws.add(i);
			System.out.println("i: " + i);
			System.out.println("mean: " + rws.mean());
			System.out.println("variance: " + rws.variance());
			System.out.println("dev: " + rws.stdDevi());
			System.out.println("zscore: " + rws.zScore(i+1));
			System.out.println("zscore: " + rws.zScore(rws.mean()+2.5));
			System.out.println();
		}
	}
}
