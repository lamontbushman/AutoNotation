
public class FFT {
	final static double LOG2 = Math.log(2);
	
	private FFT() {
	}
	
	public static void fft(Complex input[]) {
		int n = input.length;
		int numBits = (int) (Math.log(n) / LOG2);
		if (Math.pow(2,numBits) != n) {
			System.err.println("Length for DFT must be a power of 2.\n"
					+ "Length: " + n + " numBits: " + numBits);
			System.exit(0);
		}
		
		revBinaryPermute(input);
		
		for(int ldm = 1; ldm <= numBits; ldm++) {
			int m = (int) Math.pow(2,ldm);
			int mh = m/2;
			
			//TODO make sure <= not <
			for(int r = 0; r <= n - m; r+=m) { // n/m iterations
				for(int j = 0; j < mh; j++) { // m/2 iterations
					Complex e = Complex.expI(2.0*Math.PI*j/m);//check this
					Complex u = input[r + j];
					Complex v = Complex.mult(input[r + j + mh], e);
					input[r + j] = Complex.add(u,v);
					input[r + j + mh] = Complex.subt(u,v); //subtract wasn't tested
				}
			}
		}
	}
	
	public static void revBinaryPermute(Complex input[]) {
		for(int i = 0; i < input.length; i++) {
			int r = revbin(i,input.length);
			if(r > i) {
				Complex temp = input[i];
				input[i] = input[r];
				input[r] = temp;
			}
		}
	}

	public static int revbin(int toReverse, int dataLength) {
		int reverse = 0;
		int numBits = (int) (Math.log(dataLength) / LOG2);
		while(numBits > 0) {
			reverse <<= 1;
			reverse += toReverse & 1;
			toReverse >>= 1;
			numBits--;
		}
		System.out.println(reverse);
		return reverse;
	}
	
	public static void main(String args[]) {
		Complex[] complexes = {
				new Complex(0),
				new Complex(1),
				new Complex(2),
				new Complex(3),
				new Complex(4),
				new Complex(5),
				new Complex(6),
				new Complex(7)
		};
		for(Complex c : complexes) {
			System.out.println(c);
		}
		revBinaryPermute(complexes);
		for(Complex c : complexes) {
			System.out.println(c);
		}
	}
}

/*	private static void swap(Complex complex1, Complex complex2) {
Complex temp = complex1;
complex1 = complex2;
complex2 = temp;
System.out.println(complex1 + " " + complex2);
}*/