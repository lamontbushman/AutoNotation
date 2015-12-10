/*package lbushman.audioToMIDI.processing;

import java.util.List;

public abstract class ProcessQueueSingle<INPUT,OUTPUT> extends ProcessQueue<INPUT, OUTPUT> {
	ProcessQueueSingle(int numThreads) {
		super(numThreads);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Runnable returnRunnable() {
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
						System.err.println("In " + ProcessQueueSingle.this + "an element was null");
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
*/