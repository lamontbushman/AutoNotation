package lbushman.audioToMIDI.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * INPUT - input type that is added to the input queue.
 * OUTPUT - output type that is added to the output queue
 */
public abstract class ProcessQueue<INPUT,OUTPUT> extends Thread {
	private int index;
	private LinkedBlockingQueue<Element<INPUT>> inputQueue;
	private LinkedBlockingQueue<Element<OUTPUT>> outputQueue;
	final private Element<INPUT> POISON = new Element<INPUT>(-1,null);
	private int numThreads;
	private boolean poisonReceived;
	private boolean isFinished;
	
	//Calling class needs to set up the Element
	ProcessQueue(int numThreads) {
		this.numThreads = numThreads;
		index = 0;
		inputQueue = new LinkedBlockingQueue<Element<INPUT>>();
		outputQueue = new LinkedBlockingQueue<Element<OUTPUT>>();
		poisonReceived = false;
		isFinished = false;
	}
	
	public boolean add(List<INPUT> element) {
		//TODO
		//This might have some race conditions
		//Put the check if POISON in a synchronized block.
		if(!poisonReceived) {
			inputQueue.add(new Element<INPUT>(index, element));
			index++;
			return true;
		} else {
			return false;
		}
	}
	
	public LinkedBlockingQueue<Element<OUTPUT>> subscribe() {
		return outputQueue;
	}
	
	/**
	 * Required to be called. The processing will not be done unless it
	 * is called.
	 * 
	 * TODO maybe add an abstract add class that does this.
	 */
	public void signalFinished() {
		inputQueue.add(POISON);
	}

	@Override
	public void run() {
		Thread[] threads = new Thread[numThreads];
		for(int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(returnRunnable());
			threads[i].start();
		}
		
		for(Thread t : threads) {
			try {
				t.join();
				System.out.println("Thread finished: " + System.currentTimeMillis());
			} catch (InterruptedException e) {
				System.err.println("Error on join.");
				e.printStackTrace();
			}
		}
		isFinished = true;
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public ArrayList<List<OUTPUT>> processedList() {
		if(!isFinished) {
			return null;
		}
		
		ArrayList<List<OUTPUT>> results = new ArrayList<List<OUTPUT>>(outputQueue.size());
		
		//Initialize empty array for results
		for(int i = 0; i < outputQueue.size(); i++)
			results.add(null);
		
		for(Element<OUTPUT> element : outputQueue) {
			if(element == null) {
				System.err.println("An element was null");
			} else {
				results.set(element.index, element.value);
				//TODO find a better way to concatenate strings. Like PHP.
				System.out.println("[" + element.index + "] " + element.value.get(0));
			}
		}
		
		return results;
	}
	
	private Runnable returnRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				Element<INPUT> element;
				try {
					element = inputQueue.take();

					while(element != null && element != POISON) {		
						Element<OUTPUT> result =  new Element<OUTPUT>(element.index, process(element.value));
						outputQueue.add(result);
						element = inputQueue.take();
					}
					
					if(element == null) {
						System.err.println("In " + ProcessQueue.this + "an element was null");
					} else if(element == POISON) {
						inputQueue.add(POISON);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
	
	protected abstract List<OUTPUT> process(List<INPUT> element);
}
