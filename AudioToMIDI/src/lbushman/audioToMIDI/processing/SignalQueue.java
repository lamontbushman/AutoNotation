package lbushman.audioToMIDI.processing;

import java.util.Collection;
import java.util.LinkedList;

public class SignalQueue<INPUT> {
	private LinkedList<INPUT> queue;
	private boolean isFinished;
	
	public SignalQueue() {
		queue = new LinkedList<INPUT>();
		isFinished = false;
	}
	
	public boolean addAll(Collection<INPUT> elements) {
		return queue.addAll(elements);
	}
	
	public boolean add(INPUT element) {
		//TODO
		//This might have some race conditions
		//Put the check if POISON in a synchronized block.
		
		
		return queue.add(element);
		
		// Removed in order to be faster.
		// Should be a verify() not an assert()
		/*if(!isFinished) {
			if(queue.add(element)) {
				return true;
			}
			throw new Error(BlockingQueue.this + " Queue full?");
		}
		System.out.println(BlockingQueue.this + " Finished adding to queue.");
		return false;*/
	}
	
	public INPUT take() {
		return queue.remove();
	}
	
	public boolean isEmpty() {
		return queue.size() == 0;
	}
	
	public void signalFinished() {
		isFinished = true;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
}
