package lbushman.audioToMIDI.processing;

public class Pair<LEFT,RIGHT> {
	public Pair(LEFT left, RIGHT right) {
		this.left = left;
		this.right = right;
		// TODO Auto-generated constructor stub
	}
	public LEFT left;
	public RIGHT right;
	
	@Override
	public String toString() {
		return left.toString() + ":" + right.toString();
	}
}
