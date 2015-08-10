package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;

import lbushman.audioToMIDI.util.Util;

public class Group {
	Integer min;
	Integer max;
	Double  average;
	Integer lowerBound;
	Integer upperBound;
	private Double value = null;;
	public Integer size() {
		return upperBound - lowerBound;
	}
	public boolean contains(Integer number) {
		return (min <= number && number <= max);
	}
	public void print(double mode, int minMax) {
		Double fraction = minMax / mode;
		Double ceil = Util.fractionCeil(fraction, 2);
		Double floor = Util.fractionFloor(fraction, 2);
		Double rounded = Util.fractionRound(fraction, 2);
		
		System.out.println(minMax);
		System.out.println(fraction);
		System.out.println(ceil);
		System.out.println(floor);
		System.out.println(rounded);
		System.out.println();
	}
	public void calculateValue(Group modeGroup) {
		if(modeGroup == Group.this) {
			value = 1.0;
			return;
		}
	
		System.out.println("\n\n");
		System.out.println("this.max " + max + " this.min " + min + " that.max " + modeGroup.max + " that.min " + modeGroup.min);
		
		List<Double> roundings = new ArrayList<Double>(4);
		/*int count = 0;
		double total = 0;*/
		for(int i = min; i <= max; i++) {
			for(double j = modeGroup.min; j <= modeGroup.max; j++) {
				Double fraction = (i / j);
				/*total += fraction;
				count++;*/
				roundings.add(Util.fractionRound(fraction, 4));
			}
		}
/*		double mean = total / count;
		double meanRounded  = Util.fractionRound(mean, 2);*/
		
		/*		
		Double fraction1 = (max / (double)modeGroup.max);
		Double fraction2 = (min / (double)modeGroup.max);
		Double fraction3 = (max / (double)modeGroup.min);
		Double fraction4 = (min / (double)modeGroup.min);
		List<Double> roundings = new ArrayList<Double>(4);
		roundings.add(Util.fractionRound(fraction1, 2));
		roundings.add(Util.fractionRound(fraction2, 2));
		roundings.add(Util.fractionRound(fraction3, 2));
		roundings.add(Util.fractionRound(fraction4, 2));
		*/
		
		List<Double> modes = Util.mode(roundings);
		
		System.out.print("Rondings: ");
		for(Double round : roundings) {
			System.out.print(round + " ");
		}
		System.out.println();
		
		if(modes.size() != 1) {
			System.err.println("A single mode doesn't exist!");
			//Make it less precise if there is no agreement.
			value = Util.fractionRound(Util.averageD(modes), 2);
		} else {
			value = modes.get(0);
		}
	}
	public Double value(Group modeGroup) {
		if(value == null) {
			calculateValue(modeGroup);
		}
		return value;
		
		/*//This value is temporary
		System.out.println(min + " " + max);
		print(modeGroup.average, min);
		print(modeGroup.average, max);
		
		return (int) Math.round(average);*/
	}
}
