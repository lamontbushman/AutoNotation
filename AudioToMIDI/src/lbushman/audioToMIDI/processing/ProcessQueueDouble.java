/*package lbushman.audioToMIDI.processing;

import java.util.List;

public abstract class ProcessQueueDouble<INPUT,OUTPUT> extends ProcessQueue<INPUT,OUTPUT> {

	*//**
	 * Since processing two at the same time order must matter. Thus only one thread must access it at a time.
	 *//*
	ProcessQueueDouble() {
		super(1);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Runnable returnRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				Element<INPUT> element1;
				Element<INPUT> element2;
				try {
					element1 = inputQueue.take();
					element2 = inputQueue.take();
					
					while(element1 != POISON) {
						Element<OUTPUT> result =  new Element<OUTPUT>(element.index, process(element.value));
						outputQueue.add(result);
						element = inputQueue.take();
					}
					
					if(element == null) {
						System.err.println("In " + ProcessQueueDouble.this + "an element was null");
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
	
	protected abstract List<OUTPUT> process(List<INPUT> element1, List<INPUT> element2);
}*/
