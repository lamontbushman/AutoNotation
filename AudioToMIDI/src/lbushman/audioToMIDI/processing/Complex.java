package lbushman.audioToMIDI.processing;

import java.io.IOException;
import java.util.Scanner;


public class Complex {
	private double re;
	private double im;
	
	public Complex() {
		re = 0;
		im = 0;
	}

	public Complex(double real) {
		re = real;
		im = 0;
	}
	
	public Complex(double real, double imaginary) {
		re = real;
		im = imaginary;
	}
	
	public static Complex add(Complex n1, Complex n2) {
		Complex val = new Complex();
		val.re = n1.re + n2.re;
		val.im = n1.im + n2.im;
		return val;
	}
	
	public static Complex subt(Complex n1, Complex n2) {
		Complex val = new Complex();
		val.re = n1.re - n2.re;
		val.im = n1.im - n2.im;
		return val;
	}

	public Complex add(Complex n) {
		re += n.re;
		im += n.im;
		return this;
	}

	//TODO test
	public static Complex mult(Complex n1, Complex n2) {
	/*	if(n1 == null || n2 == null)
			return new Complex(0);*/
		
		Complex val = new Complex();
		double ac = n1.re * n2.re;
		double bd = n1.im * n2.im;
		double abcd = (n1.re + n1.im) * (n2.re + n2.im);
		val.re = ac - bd;
		val.im = abcd - ac - bd;
		return val;
	}

	//TODO test
	public Complex mult(Complex n) {
/*		if(n == null)
			return new Complex(0);
*/
		Complex val = new Complex();
		double ac = re * n.re;
		double bd = im * n.im;
		double abcd = (re + im) * (n.re + n.im);
		val.re = ac - bd;
		val.im = abcd - ac - bd;
		return val;
	}
	
	public Complex div(int n) {
		Complex val = new Complex();
		double ac = re * n;
		double c2 = Math.pow(n,2);
		val.re = ac / c2;
		val.im = 0;
		return val;
	}
	
	public Complex div(Complex n) {
		Complex val = new Complex();
		double ac = re * n.re;
		double bd = im * n.im;
		double bc = im * n.re;
		double ad = re * n.im;
		double c2d2 = Math.pow(n.re,2) + Math.pow(n.im,2);
		val.re = (ac + bd) / c2d2;
		val.im = (bc - ad) / c2d2;
		return val;
	}
	
	/**
	 * Calculates e^n
	 * @param n
	 * @return
	 */
	public static Complex exp(Complex n) {
		Complex val = new Complex();
		val.re = Math.cos(n.re);
		val.im = Math.sin(n.im);
		return val;
	}
	
	/**
	 * Calculates e^(ni)
	 * @param n
	 * @return
	 */
	public static Complex expI(double n) {
		Complex val = new Complex();
		val.re = Math.cos(n);
		val.im = Math.sin(n);
		return val;
	}
	
	public static double absolute(Complex n) {
		//TODO Do I need to handle NaN;
		//return Math.sqrt(Math.pow(n.re,2) + Math.pow(n.im,2));
		return Math.sqrt(n.re * n.re + n.im * n.im);
	}
	
	public double absolute() {
		//TODO Do I need to handle NaN;
		//return Math.sqrt(Math.pow(re,2) + Math.pow(im,2));
		return Math.sqrt(re * re + im * im);
	}
	
	public double absoluteSquare() {
		//TODO replace with more efficient after it is working, change and then test.
		return Math.pow(absolute(),2);//Math.pow(re,2) + Math.pow(im,2);		
	}
	
	@Override
	public String toString() {
		return re + " " + im + "i";
	}
	
	
	public static void main(String args[]) throws IOException {
		Scanner in = new Scanner(System.in);
		
		Complex c1 = new Complex(7,-9);
		Complex c2 = new Complex(4,-6);
		Complex answer = Complex.mult(c1, c2);
		System.out.println(answer);
		System.out.println(answer.absolute());
		System.out.println(c1.mult(c2));
		System.out.println(c1.absolute());
		System.out.println(Complex.absolute(answer));
		System.exit(0);
		
	/*	while(true) {
			double j = in.nextInt();
			double m = in.nextInt();
			if(j == -1)
				break;*/
		double m = 128;
		for(int i = 0; i < 128;i++) {	
			double j = i;
			
			
			Complex one = Complex.expI(2.0*j/m*Math.PI);
			if(one.absolute() == 1.0) {
				//System.out.println(j + " " + m);
				//System.out.println(2.0*j/m*Math.PI);
				System.out.println(one.absolute() + " " + i + "  ");
				System.out.println(one);
			}
		}
		in.close();
	}

	public Complex conjugate() {
		return new Complex(re, im * -1);
	}
	
	public boolean isEqual(Complex n) {
		if(Math.abs((Math.floor(re) - Math.floor(n.re))) <= 1 && 
		  Math.abs(Math.floor(Math.abs(im)) - Math.floor(Math.abs(n.im))) <= 1)
			return true;
		else
			return false;
	}
}
