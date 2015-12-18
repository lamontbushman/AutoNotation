package lbushman.audioToMIDI.io;

public class Note {
	public static final String SHARP = "#";//"\u266f";
	public static final String FLAT = "b";//"\u266d";
	public static final String NATURAL = "\u266e";
	public static final String EMPTY = "";
	public static final char INVALID = ' ';
	
	private Character name;
	private Boolean sharpFlatNull;
	private String sharpFlatEmpty;
	private int position;
	
	public Note() {
		name = INVALID;
		sharpFlatNull = null;
		sharpFlatEmpty = "";
		position = -1;
	}
	
	/**
	 * 
	 * @param name: i.e. A, B, C...
	 * @param sharpFlatNull: true == sharp, false == flat, null == natural
	 * @param position: Middle C is C4. B below C4 is B3. See http://www.phy.mtu.edu/~suits/notefreqs.html
	 */
	public Note(char name, Boolean sharpFlatNull, int position) {
		this.name = name;
		this.sharpFlatNull = sharpFlatNull;
		
		sharpFlatEmpty = "";
		if(sharpFlatNull != null) {
			sharpFlatEmpty = (sharpFlatNull)? SHARP : FLAT;
		}
		
		this.position = position;
	}
	
	public char getName() {
		return name;
	}
	
	public Boolean getSharpFlatNull() {
		return sharpFlatNull;
	}

	public String getSharpFlatEmpty() {
		return sharpFlatEmpty;
	}

	public int getPosition() {
		return position;
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	public boolean equals(Note note) {
		return sharpFlatNull == note.sharpFlatNull &&
				position == note.position &&
				name == note.name;
		
	}
	
	public String toString(boolean displayAccidental) {
		if(name == null) {
			return "Out of range";
		}
		String accidental = (displayAccidental)? sharpFlatEmpty : "";
		accidental = (displayAccidental && sharpFlatNull == null)? NATURAL : accidental;
		return name + accidental + position;
	}	
	
	public static void main(String args[]) {
		System.out.println(SHARP + FLAT + NATURAL);
		System.out.println("\u266f");
		String str = new String(new int[] { 0x266d }, 0, 1);
		System.out.println(str);
		System.out.println(Integer.toHexString(' ' | 0x10000).substring(1));
	}
 }
