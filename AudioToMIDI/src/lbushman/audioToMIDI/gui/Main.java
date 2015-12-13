package lbushman.audioToMIDI.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import lbushman.audioToMIDI.io.CaptureAudio;
import lbushman.audioToMIDI.io.Note;
import lbushman.audioToMIDI.io.PlayAudio;
import lbushman.audioToMIDI.io.ReadAudioFile;
import lbushman.audioToMIDI.io.WriteAudioFile;
import lbushman.audioToMIDI.processing.AudioData;
import lbushman.audioToMIDI.processing.DownBeatData;
import lbushman.audioToMIDI.processing.DownBeatDetection;
import lbushman.audioToMIDI.processing.FrequencyToNote;
import lbushman.audioToMIDI.processing.FundamentalFrequency;
import lbushman.audioToMIDI.processing.PossibleDownBeat;
import lbushman.audioToMIDI.processing.ProcessSignal;
import lbushman.audioToMIDI.processing.RunningWindowStats;
import lbushman.audioToMIDI.util.Util;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
	CaptureAudio audio;
    Button captureButton;
    Button openButton;
    Button displayFFTButton;
    AudioData audioData;
//	private Graph acGraph;
    private Graph centerGraph;
    
	private Graph fftGraph;
		
	@Override public void start(Stage stage) {
		Util.setDebugMode(false);
		
        stage.setTitle("Signal Processing Senior Project");       
        
//        acGraph = new Graph("Autocorrelation", "Nth Sample", "Power");
        centerGraph = new Graph("Spectral Flux", "Nth sample", "Power");
        fftGraph = new Graph("FFT", "Nth Sample", "Power");
        
       	BorderPane border = new BorderPane();
    	HBox box = addHBox();
    	border.setTop(box);
//    	border.setCenter(acGraph);
    	border.setCenter(centerGraph);
    	border.setBottom(fftGraph);
    	
    	Scene scene = new Scene(border,1000,700);

    	box.requestFocus();
        stage.setScene(scene);
        stage.show();
/*        openButton.setDisable(false);
        openButton.fire();*/
    }
	        
    private void readFromFile(File file) {
    	Util.timeDiff("RD");
		ReadAudioFile audio = new ReadAudioFile(file);
		audio.readFile();
		audioData = null;
		audioData = new AudioData(
				audio.getStream().toByteArray(),
				audio.getFormat());
		Util.timeDiff("RD");
		processSignal(audioData, null);
    }
    
    private void processSignal(AudioData audioData, File file) {		
		// Play the data
		//playClip(audioData.getSampledData(), audioData.getFormat());
    	
		
		// Save the data
		if(file != null) {
	    	writeToFile(audioData.getSampledData(), file, audioData.getFormat());
		}
		Util.println("Reset2");
		Util.timeDiff("PS");
		ProcessSignal ps = new ProcessSignal(audioData, 
				/*0.120*/ /*0.25*/ 0.25 /*0.125 *//*overlap of FFTs*/, 2048 /*original fftLength */); //8192

		
		//16384//.25, 4096 moderate both
		//20480//.20, 4096 better but slower
		//40960//.20, 8192 better fft, worse spectrum and slower
		//5120//.20, 1024 horrible fft, not horrible spectrum, slow
		//2048//.5,  1024 horrible fft, almost perfect spectrum
		
		
		//.5, 4096, great for fft
		//.25, 1024 really great for spectrum
		
		
		
		ps.process();
		Util.timeDiff("PS");
		//TODO why is audioData being passed in?
		this.audioData = audioData;
		
		//TODO ensure multiple reads resets the data appropriately. I think it is.
		
		
		//TODO use this to limit graph indexing, etc.
		//	data.getNumFFT();
													
		
		//TODO possibly show frequencies, normalizedFrequencies, and note names in a graph.
		
		//TODO show notes where consecutive duplicates are not shown.
		//ps.printNonConsecutiveNotes(false);
if(false) {	
		List<Double> spectralFlux = audioData.getSpectralFlux();

		displayCenterGraph(spectralFlux);

		List<Double> normalized = new ArrayList<Double>();
		
//		Double[] amp = ProcessSignal.computeAmp(audioData);
		int i = 0;
		for(Double d : audioData.getFrequencies()) {
			Double dd = FrequencyToNote.findFrequency(d);
			normalized.add(dd);
			Util.println("[" + i + "] " + dd + "\t" + FrequencyToNote.findNote(dd) /*+ " " + amp[i]*/);
			i++;
		}
		audioData.setNormalizedFrequencies(normalized);
}		
		//Double fftAbsolute[] = audioData.getFftAbsolute();
		//Double fftLowpass[] = audioData.getFftLowPassAbsolute();
		
		/*for(int i = 0; i < fftAbsolute.length;i++) {
			if(fftAbsolute[i] != fftLowpass[i]) {
				Util.println(i + " " + fftAbsolute[i] + "\t" + fftLowpass[i]);
			}
		}
		System.err.println("END OF DIFF!");*/
		
		
		//displayAC(0);
	//displayFFt(0, audioData.getFftAbsolute()/*Arrays.asList(fftLowpass)*/);
		System.out.println("done with process signal");
    }
    
    private void updateGraph(Graph graph, Number[] data) {
    	graph.updateList(data);
    }
    
    private <T> void updateGraph(Graph graph, List<T> dataList) {
    	Number[] data = dataList.toArray(new Number[dataList.size()]);
    	graph.updateList(data);
    }
    
    private <T> void updateGraph(Graph graph, List<T> dataList, int index) {
    	Number[] data = dataList.toArray(new Number[dataList.size()]);
    	
    	int start = index * audioData.getFftLength();
    	int end = start + audioData.getFftLength();
    	Number[] range = Arrays.copyOfRange(data, start, end);
    	
    	graph.updateList(range);
    }
     
    private void playClip(byte[] signal, AudioFormat format) {
    	PlayAudio play = new PlayAudio(signal, format);
    	play.playClip();	
    }
    
    private void playClip(byte[] signal, AudioFormat format, int begin, int end) {
    	byte[] subSignal = Arrays.copyOfRange(signal, begin, end);
    	playClip(subSignal, audioData.getFormat());
    }
    
    private HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 12, 10, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        TextField captureField = new TextField();
        captureField.setPrefSize(125, 20);
        captureField.promptTextProperty().set(".wav");
        TextField openField = new TextField();
        openField.setPrefSize(125, 20);
        openField.promptTextProperty().set(".wav");
        captureButton = new Button("Capture");
        openButton = new Button("Open File");
        
        Label label1 = new Label("Save File:");

        
        captureField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if(newValue.length() < 5 || !newValue.endsWith(".wav")) {
					captureButton.setDisable(true);
				} else {
					captureButton.setDisable(false);
				}	
			}
         });
        
        captureButton.setDisable(true);
        captureButton.setPrefSize(100, 20);
        captureButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(captureButton.getText() == "Capture") {
					captureField.setDisable(true);
					//captureButton.setDisable(false);
					captureButton.setText("Stop");
					Thread myThread = new Thread() {
						@Override
						public void run() {
							//readData(false, new File(captureField.getText()));
							startCapture();
						}
					};
					myThread.start();
				} else {
					stopCapture(new File(captureField.getText()));
					captureButton.setText("Capture");
					captureField.setDisable(false);
				}	
			}
		});

        Label label2 = new Label("Open File:");
        openField.setText("maryDown.wav");
        
        openField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if(newValue.length() < 5 || !newValue.endsWith(".wav") || 
						!(new File(newValue).isFile())) {
					openButton.setDisable(true);
				} else {
					openButton.setDisable(false);
				}	
			}
         });
        
        openButton = new Button("Open File");
        openButton.setDisable(true);
        openButton.setPrefSize(100, 20);
        openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
//				readData(true, new File(openField.getText()));
				openButton.setDisable(true);
				openField.setDisable(true);
				readFromFile(new File(openField.getText()));
				openButton.setDisable(false);
				openField.setDisable(false);
			}
		});
        
        Button frequencyButton = new Button("Show Frequencies");
        frequencyButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				displayFrequencies();
			}
		});
        
        TextField nthField = new TextField();
        nthField.setPrefSize(50, 20);
        nthField.promptTextProperty().set("Nth Graph");

/*        Button nthAutoCorrelation = new Button("Display AC[N]");
        nthAutoCorrelation.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				displayAC(Integer.parseInt(nthField.getText()));
			}
		});*/

        Button nthFFT = new Button("Display FFT[N]");
        nthFFT.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
//				displayFFt(Integer.parseInt(nthField.getText()), audioData.getAutoCorrelationAbsolute());
				displayFFt(Integer.parseInt(nthField.getText()), 
						audioData.getFftAbsolute()
						/*Arrays.asList(audioData.getFftLowPassAbsolute())*/
						);
			}
		});
        
        TextField freqIndex = new TextField();
        freqIndex.promptTextProperty().set("Note index");
        freqIndex.setPrefSize(50, 20);
        
        TextField noteAndFreqField = new TextField("Freq and Note");
        noteAndFreqField.setEditable(false);
        noteAndFreqField.setPrefSize(50, 20);
        
        Button noteAndFreqButton = new Button("Show Note");
        noteAndFreqButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String text = getFreqAndNote(Integer.parseInt(freqIndex.getText()));
				noteAndFreqField.setText(text);
			}
		});
        
        TextField playBeg = new TextField();
        playBeg.promptTextProperty().set("Beg");
        playBeg.setPrefSize(50, 20);
        
        TextField playEnd = new TextField();
        playEnd.promptTextProperty().set("End");
        playEnd.setPrefSize(50, 20);
        
        Button playButton = new Button("Play");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				int begin = Integer.parseInt(playBeg.getText());
				int end = Integer.parseInt(playEnd.getText());
				
				//int originalLen = audioData.getFftLength() / 2;
				//int overLapLen = originalLen / 2;
				int len = audioData.getFftLength();
				
				double percentage = audioData.getOverlapPercentage() * 2;
				len *= percentage;
				
				begin = begin * len;
				end = end * len;
				
		    	playClip(audioData.getSampledData(), audioData.getFormat(), begin, end);
			}
		});
        
        hbox.getChildren().addAll(label1, captureField, captureButton, 
        		label2, openField, openButton, frequencyButton, nthField, 
        		/*nthAutoCorrelation,*/ nthFFT, freqIndex, noteAndFreqButton,
        		noteAndFreqField, playBeg, playEnd, playButton);
        
        return hbox;
    }
    
    private String getFreqAndNote(int index) {
    	double frequency = FundamentalFrequency.computeFrequency(index, audioData);
    	String closestNote = FrequencyToNote.findNote(frequency).toString();
    	return frequency + "Hz " + closestNote;
    }
    
    public int avgOnsetDiff(List<Integer> differences) {
		List<Integer> modes = Util.mode(differences);
		// TODO There is a chance this may fail. Seek further implementation.
		Util.logIfFails(modes.size() == 1, "More than one mode for differences between onsets. modes: " + modes);
		return (int) Math.round(Util.average(modes));
    }
    
    public List<Integer> onsetDiff(List<Integer> onsets) {
    	Util.verify(onsets.size() >= 2, "There are less than two onsets.");
    	ListIterator<Integer> it = onsets.listIterator();
    	int previous = it.next();
    	int current = 0;
    	List<Integer> differences = new ArrayList<Integer>();
    	while(it.hasNext()) {
    		current = it.next();
    		differences.add(current - previous);
    		previous = current;
    	}
    	return differences;
    }
    
    private void displayFrequencies() {
    	Util.timeDiff("DF");
    	List<Double> ffts = audioData.getFftAbsolute();
    	int fftLen = audioData.getFftLength();
    	int halfFFtLen = fftLen / 2;
    	// Create function getHalfFft(num); / getFft(num)
    	
    	List<Double> corrValuesPerc = new ArrayList<Double>();
    	List<Double> corrValues = new ArrayList<Double>();
    	
    	List<Double> freqs = new ArrayList<Double>();
    	for(int time = 0; time < audioData.getNumFFT(); time++) {
    		int fromI = time * fftLen;
	    	int toI = fromI + halfFFtLen;
	    	List<Double> halfFft = ffts.subList(fromI, toI);
	    	
	    	List<Double> correlations = new ArrayList<Double>();
	    	int from = 1; // be careful if you change from 1; logic has to change for position in list.
	    	int to = halfFFtLen / 4;
	    	//TODO maybe create a width.
	    	double lkjh = 0;
	    	for(int corrI = from; corrI < to; corrI++) {
	    		double ampTotal = 0;
	    		int position = corrI;
	    		int numAmps = 0;
	    		while(position < halfFFtLen) {
	    			ampTotal += halfFft.get(position);
	    			position += corrI;
	    			numAmps++;
	    		}
	    		correlations.add(ampTotal / numAmps);
	    	}
	    	int correlation = Util.maxIndex(correlations, 0, correlations.size());
	    	int max = Util.maxIndex(halfFft, Math.max(0, correlation - 5), Math.min(halfFft.size() - 1, correlation + 5));
	    	
/*	    	RunningWindowStats rws = new RunningWindowStats(8);
	    	for(int i = 0; i < correlations.size(); i++) {
	    		double cor = correlations.get(i);
	    		double mean = rws.mean();
	    		if((cor - mean) / mean > 2.6) {
	    			max = i;
	    			break;
	    		}
	    		rws.add(cor);
	    	}
	    	//max = Util.maxIndex(halfFft, Math.max(0, max - 5), Math.min(halfFft.size() - 1, max + 5));
	    	*/
	    	
	    	int cor = -1;
	    	Double sum  = Util.sum(correlations);
	    	double average = sum / halfFFtLen;
	    	corrValuesPerc.add(correlations.get(correlation) / sum);
	    	List<Double> percValues = new ArrayList<Double>();
	    	for(int i = 0; i < correlations.size(); i++) {
	    		if(correlations.get(i) / average >= 0.03) {
	    			cor = i;
	    		}
	    		percValues.add(correlations.get(i) / average);
	    	}
	    //	int max = Util.maxIndex(halfFft, Math.max(0, cor - 5), Math.min(halfFft.size() - 1, cor + 5));
	    	
	    	corrValues.add(correlations.get(correlation));
	    	double frequency = FundamentalFrequency.computeFrequency(max/*correlation*/, audioData);
	    	frequency = FrequencyToNote.findFrequency(frequency);
	    	freqs.add(frequency);
	    	if(time == 25 &&  frequency == 880) {
	    		System.out.println("time: " + time);
	    		fftGraph.updateList(prepareValuesForDisplay(correlations, 1));
	    	//	fftGraph.updateList(prepareValuesForDisplay(percValues, 1/*00000*/));
	    		//fftGraph.update2List(prepareValuesForDisplay(halfFft, 1));
	    		fftGraph.clearData2();
	    		fftGraph.clearData3();
	    		return;
	    	}
	    	
    	}
    	
    	List<Integer> rollOnsets = new ArrayList<Integer>();
    	List<Double> ampsL = corrValuesPerc;// Arrays.asList(audioData.getAmp());
    	List<Double> onsetAmps = new ArrayList<Double>(); 
		boolean peakAddedSinceLastFall = false;
		double lastHeight = ampsL.get(0);
		for(int i = 1; i < ampsL.size(); i++) {
			
			if(ampsL.get(i) > lastHeight && (ampsL.get(i) - lastHeight) / lastHeight  > 1.1) {
				//peaks.add(0.0);
				peakAddedSinceLastFall = false;
			} else {
				if(!peakAddedSinceLastFall) {
					rollOnsets.add((i - 1) /*+1*/);
					onsetAmps.add(ampsL.get(i));
					/*int peak = i - 1;
					double total = 0;
					double maxValue = lastHeight;
					final int PEAK_HALF = 2;
					for(int j = 1; j <= PEAK_HALF; j++) {
						total += maxValue / Math.max(bts.get(peak - j), 0.1);
						total += maxValue / Math.max(bts.get(peak + j), 0.1);
					}
					peaks.add(Math.min((total/maxValue)*1000, 0.000001));*/
					
					
					
					peakAddedSinceLastFall = true;
				} else {
					//peaks.add(0.0);
				}
			}
			lastHeight = ampsL.get(i);
		}
		
/*		final int rollBack = 3;
		ListIterator<Integer> lit = rollOnsets.listIterator();
		while(lit.hasNext()) {
			int onset = lit.next();
			int upperExc = Math.min(onset + 1, ampsL.size() - 1);
			int lowerInc = Math.max(onset - rollBack, 0);
			onset = Util.minIndex(ampsL, lowerInc, upperExc);
			lit.set(onset);
		}*/
    	
		
		
		
    	
		//TODO I don't ever care about the beginning or end logic so I am lazy!
    	int sameCount = 1;
    	int samePass = 3;
    	ArrayList<Integer> code = new ArrayList<Integer>();
    	double previous = -1;
    	for(int i = 0; i < freqs.size(); i++) {
    		double freq = freqs.get(i);
    		if(freq == previous) {
    			sameCount++;
    		} else {
    			sameCount = 1;
    		}
    		code.add(sameCount);
    		previous = freq;
    	}
    	
    	Util.verify(code.size() == freqs.size(), "code.size() != freqs.size()");
    	
    	Double[] smoothedFreqs = new Double[freqs.size()];
    	for(int i = freqs.size() - 1; i >= 0; i--) {
    		if(code.get(i) >= samePass) {
    			int numSame = code.get(i);
    			for(int j = 0; j < numSame; j++) {
    				smoothedFreqs[i] = freqs.get(i);
    				i--;
    			}
    		} else {
    			int numSame = code.get(i);
    			for(int j = 0; j < numSame; j++) {
    				smoothedFreqs[i] = 0.0;
    				i--;
    			}   			
    		}
    		i++;
    	}
    	
    	/*
		RunningWindowStats rws = new RunningWindowStats(3);
		List<Double> smoothedFreqs = new ArrayList<Double>();
		for(int i = 0; i < smoothedFreqs.size(); i++) {
			rws.add(smoothedFreqs.get(i));
			if(rws.isFull()) {
				rws.
			}
		}*/
/*    	
    	Oldest
    	[-0.0031216500447718747, 0.011708984175072401, 0.014185809666414742, 0.01631187482038257, 0.018672953800970615, 0.020502533689564135, 0.022042370851026408, 0.023920198033373558, 0.024362381329691117, 0.025133889869362575, 0.02897203428733229, 0.031468202575217534, 0.03174206666227823, 0.031788275104681726, 0.03254362039123506, 0.03289635209100222, 0.03300398354643061, 0.03306811998824443, 0.033088979988583594, 0.03421609226248243, 0.03471173741684021, 0.03494827866779151, 0.03549081349922573, 0.03560146807605591, 0.03562614400733803, 0.035885244116674986, 0.03649254893419563, 0.03663896862134829, 0.0368666553076343, 0.03767511986476015, 0.03789553025393591, 0.038084178547641595, 0.03826187011145257, 0.038294791031407635, 0.03845488396226728, 0.03927871880010179, 0.039456834910201154, 0.039671022810335604, 0.0400621956160165, 0.04011223687500827, 0.04011719339434992, 0.04108691441582478, 0.04240746112755935, 0.042707003742932154, 0.04283500001828877, 0.04290762036750395, 0.043460516789511155, 0.043743362128466357, 0.04376286870958954, 0.044783818867611645, 0.045729879234574, 0.04581761311069273, 0.04605529760110366, 0.046748054246655305, 0.04695902668230029, 0.047536200245742614, 0.04753649732524676, 0.04759330515922378, 0.04767657076782517, 0.048008320695765924, 0.0482799348660148, 0.048405542650331014, 0.048613972486595755, 0.049348545476377556, 0.05043273274311001, 0.05075435861902375, 0.050884537954736034, 0.05093007129911879, 0.051318637848776064, 0.05162828392261099, 0.051692420270454426, 0.0520038470633888, 0.05207193646420752, 0.052782888672860094, 0.05293968360929065, 0.053037915188431733, 0.05306023793389813, 0.053633635539373795, 0.053785315224804224, 0.054357196176888486, 0.0544718110562612, 0.054516014101579385, 0.0549438808651312, 0.05510576958104394, 0.05517381387494223, 0.05520396260099003, 0.055229601949643034, 0.05552984971498214, 0.05557827515771525, 0.05559656529701969, 0.05573817472263393, 0.056542512212475124, 0.05712675201040497, 0.057129812243012466, 0.057260291927543776, 0.057391771935585034, 0.05762441227732354, 0.057656681723718926, 0.05830651420807385, 0.05832651497700895, 0.05847409747059209, 0.05876083491776282, 0.05913788478239463, 0.06043669507908729, 0.0610006700448217, 0.062493867242743896, 0.06368594189162534, 0.06421148713859601, 0.06434269433991802, 0.06529461959638169, 0.06892129247101039, 0.06895780231864514, 0.07062812627214542, 0.07084327132307124, 0.07179288059058102, 0.07384647475455386]





    	Current                       not a peak               not a peak            definitely a peak
    	        choosing 0.020    I can't hear anything   was a stray sound I played
    	[-1.7976931348623157E308, 0.01277231264672239,     0.019058583928809596,    0.023185927909769165, 0.027464068514968847, 0.028931624840993098, 0.030490229679923053, 0.03293620742835672, 0.03468993106441311, 0.034771944212066405, 0.03648912621414019, 0.03737822711558035, 0.038523686705373233, 0.0407426882272389, 0.04090737473311651, 0.041678774426673774, 0.042526720559831394, 0.0428198467654769, 0.0433271199529652, 0.04361415407924203, 0.043619514824280926, 0.043649340665174514, 0.04395387267028924, 0.044206986093592346, 0.04468764633079269, 0.044979708882702016, 0.045945981041447585, 0.04601163069483108, 0.04603787312050643, 0.04613158565621213, 0.04615845633235007, 0.04651886831384005, 0.04690963383753743, 0.04721777115180823, 0.04966899301252322, 0.04969987174235609, 0.04970694145389929, 0.0498008436828051, 0.0499590556058081, 0.050041552286143924, 0.05006232480180393, 0.050100837982463425, 0.05132701122851098, 0.05150559915443652, 0.05212624062265325, 0.05217107827694415, 0.052360932262377695, 0.05265253595977657, 0.052872234104009465, 0.05292660994037783, 0.05333822446194347, 0.05383776770503286, 0.05399384175170171, 0.054077664193013686, 0.054164509592680744, 0.05444151248862196, 0.0546814848721554, 0.055130387284308024, 0.055246323602385614, 0.05537661165851047, 0.055538039546267765, 0.05567098610089239, 0.05567174039019923, 0.05678646869228086, 0.056795582483139095, 0.056861669008128155, 0.05700712222417098, 0.05761454829342855, 0.059370645717634715, 0.05937943131853385, 0.05940023660325022, 0.059586529212986614, 0.060026728015179234, 0.06019991079985133, 0.06043367238635137, 0.06059316609570158, 0.06062361289999197, 0.06129459402019405, 0.061537216577105654, 0.06183138579438587, 0.06215819146496197, 0.06218178504782851, 0.06263575777988785, 0.06290352048077456, 0.06315328829857934, 0.06341587578901532, 0.06386169179031219, 0.06438903639376986, 0.06458648963274961, 0.06485490708878451, 0.06493157468199931, 0.06514954169335725, 0.06559774876513394, 0.06630284557935276, 0.06661408935474991, 0.06717031931415607, 0.06811097538628522, 0.06813138002212923, 0.06817081058164512, 0.06818444099471192, 0.06909663578854051, 0.06933117674140668, 0.07060430320374442, 0.07086139087164527, 0.07138196127316096, 0.07249818646957645, 0.07365114931726789, 0.07421833902006389, 0.07547967453970136, 0.07594445494117316, 0.07597450012844446, 0.0764111864801095, 0.07670461320071374, 0.07736054864807931, 0.07782663551878602, 0.07854689589175597]

    	Previous
    	[-0.0031216500447718747, 0.011708984175072401, 0.014185809666414742, 0.01631187482038257, 0.018672953800970615, 0.020502533689564135, 0.022042370851026408, 0.023920198033373558, 0.024362381329691117, 0.025133889869362575, 0.02897203428733229, 0.031468202575217534, 0.03174206666227823, 0.031788275104681726, 0.03254362039123506, 0.03289635209100222, 0.03300398354643061, 0.03306811998824443, 0.033088979988583594, 0.03421609226248243, 0.03471173741684021, 0.03494827866779151, 0.03549081349922573, 0.03560146807605591, 0.03562614400733803, 0.035885244116674986, 0.03649254893419563, 0.03663896862134829, 0.0368666553076343, 0.03767511986476015, 0.03789553025393591, 0.038084178547641595, 0.03826187011145257, 0.038294791031407635, 0.03845488396226728, 0.03927871880010179, 0.039456834910201154, 0.039671022810335604, 0.0400621956160165, 0.04011223687500827, 0.04011719339434992, 0.04108691441582478, 0.04240746112755935, 0.042707003742932154, 0.04283500001828877, 0.04290762036750395, 0.043460516789511155, 0.043743362128466357, 0.04376286870958954, 0.044783818867611645, 0.045729879234574, 0.04581761311069273, 0.04605529760110366, 0.046748054246655305, 0.04695902668230029, 0.047536200245742614, 0.04753649732524676, 0.04759330515922378, 0.04767657076782517, 0.048008320695765924, 0.0482799348660148, 0.048405542650331014, 0.048613972486595755, 0.049348545476377556, 0.05043273274311001, 0.05075435861902375, 0.050884537954736034, 0.05093007129911879, 0.051318637848776064, 0.05162828392261099, 0.051692420270454426, 0.0520038470633888, 0.05207193646420752, 0.052782888672860094, 0.05293968360929065, 0.053037915188431733, 0.05306023793389813, 0.053633635539373795, 0.053785315224804224, 0.054357196176888486, 0.0544718110562612, 0.054516014101579385, 0.0549438808651312, 0.05510576958104394, 0.05517381387494223, 0.05520396260099003, 0.055229601949643034, 0.05552984971498214, 0.05557827515771525, 0.05559656529701969, 0.05573817472263393, 0.056542512212475124, 0.05712675201040497, 0.057129812243012466, 0.057260291927543776, 0.057391771935585034, 0.05762441227732354, 0.057656681723718926, 0.05830651420807385, 0.05832651497700895, 0.05847409747059209, 0.05876083491776282, 0.05913788478239463, 0.06043669507908729, 0.0610006700448217, 0.062493867242743896, 0.06368594189162534, 0.06421148713859601, 0.06434269433991802, 0.06529461959638169, 0.06892129247101039, 0.06895780231864514, 0.07062812627214542, 0.07084327132307124, 0.07179288059058102, 0.07384647475455386]

*/    	
    	
    	ArrayList<Integer> onsets = new ArrayList<Integer>();
    	boolean atBase = false;
    	boolean atPeak = false;
    	double baseV = 0;
    	double topV = 0/*Double.MAX_VALUE*/;
    	int topI = 0;
    	
    	int onsetI = 0;
    	double onsetV = 0;
    	
    	double leastV = Double.MAX_VALUE;
    	int leastI = 0;
    	List<Double> values = new ArrayList<Double>();
    	double topAmpV = 0;
    	int topAmpI = 0;
    	
    	List<Double> ampsList = Arrays.asList(audioData.getAmp());
    	for(int i = 0; i < corrValuesPerc.size(); i++) {
    		double perc = corrValuesPerc.get(i);
    		// TODO probably switch to old numbers (maybe not, extra numbers might be needed???), but now include a rule that the highest in the range must be so much higher than the lowest found in the (first half of the) range. 
    		// Found bottom
    		if(perc < /*0.017*/0.018 ) { //(absolute min (0.009 0.008 too low)  .018 getting a little scary to be cutting into a peak.  At one point gives 2 .017 - 0.020 a decent number for matching amp peak.
    			if(atBase == false) {
    //onsets.add(topI);
//onsets.add(leastI);
	//onsets.add(i); // beginning of peak
    				//onsets.add(topAmpI);
    				//onsets.add(onsetI);
    				Util.verify(topI >= leastI, "topI <= leastI");
    				for(int beg = topI; beg >= leastI; beg--) {
    					if(corrValuesPerc.get(beg) <= 0.019) {
    						onsets.add(beg);
    						break;
    					}
    				}
    				
    				
						//didn't work this i and topI are not matching lower and greater indexes. onsets.add(Util.maxIndex(ampsList, i, topI + 1/* makes inclusive */)); 

			
    				baseV = perc;
    //				if((topV - baseV) / baseV);
    				System.out.println(topV - leastV);
    				values.add(topV - leastV);
    				
    				
    				
    				
    				/*if(topV - baseV < 0.015  0.017     0.0118  0.0089) {
    					System.out.println("removed one: " + onsets.get(onsets.size() -1) + " leastValue: " + leastV + " topV: " + topV);
    					onsets.remove(onsets.size() -1);
    				}*/
    				
    				if(topV - leastV < 0.020) {
    					System.out.println("removed one: " + onsets.get(onsets.size() -1) + " leastValue: " + leastV + " topV: " + topV + " diff: " + (topV - leastV));
    					onsets.remove(onsets.size() -1);
    				}
    				
    				topAmpV = 0;
    				topV = 0;
    				onsetV = 0;
    				leastV = perc; // start off new peak with current value
    				
    				// System.out.println("    " + baseV);
    			} else {
    				// System.out.println(baseV);
    			}
    			atBase = true;
    			atPeak = false;
    		} else if (perc > /*0.025*/0.026) {//maybe lower a little // Found  a definite peak (.030 highest (.031 too high)) 0.020 too low, 0.025 highest I'd like to be comfortable with.
    			if(atPeak == false) {
//    				onsets.add(i); // bottom of peak
    				//System.out.println("    				" + perc);
    			} else {
    				//System.out.println("    			" + perc);
    			}
    			

    			
    			atPeak = true;
    			atBase = false;
    		} else {
    			// in middle ground
    		}
    		
			if(atPeak && perc >= topV) {
				topV = perc;
				topI = i;
			}
			
			if(atBase && perc <= leastV) {
				leastV = perc;
				leastI = i;
			}
			
			double amp = ampsList.get(i);
			if(amp > topAmpV) {
				topAmpV = amp;
				topAmpI = i;
			}
			
			if(perc > 0.019 && onsetV == 0) {
				onsetV = perc;
				onsetI = i;
			}
    	}
    	
    	values.sort(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				return o1.compareTo(o2);
			}
		});
    	System.out.println(values);

    	ListIterator<Integer> pOLIter = onsets.listIterator();
												if(false) {
												    	/*ArrayList<Integer>*/ onsets = new ArrayList<Integer>();
												    	for(int i = 0; i < corrValuesPerc.size(); i++) {
												    		double perc = corrValuesPerc.get(i);
												    		if(perc > .000  && perc < 0.020  /* 0.027  */ /*0.0206*/ /*0.02055332*/ /*.0175*/ /*.017349*/ /*.0157*/) { //0.020 a decent number for matching amp peak.
												    			onsets.add(i);
												    		}
												    	}
												
												    	/*ListIterator<Integer>*/ pOLIter = onsets.listIterator();
												    	// I like when things blow up. It lets me know something is wrong.
												    	int lastOnset = pOLIter.next();
												    	while(pOLIter.hasNext()) {
												    		int onset = onsets.get(pOLIter.nextIndex());
												    		if(onset - lastOnset == 1) {
												    			pOLIter.remove();
												    		}
												    		pOLIter.next();
												    		lastOnset = onset;
												    	}
												}
    	
 if(false) {   	
    	// Validate onsets. Generally for either ends of the song.
    	int validWindow = 5;
    	int vWMax = corrValuesPerc.size() - 1;
    	
    	pOLIter = onsets.listIterator();
    	while(pOLIter.hasNext()) {
    		int onset = pOLIter.next();
    		int length = Math.min(onset + validWindow, vWMax);
    		int doRemove = onset;
    		
    		for(; doRemove < length; doRemove++) {
    			if(corrValuesPerc.get(doRemove) > 0.02) {//.0152056
    				doRemove = 0;
    				break;
    			}
    		}
    		if(doRemove != 0) {
    			pOLIter.remove(); 
    			System.out.println("removed during validation: " + onset);
    			// TODO log the onset being removed.
    		}
    	}
 }
 
    	
								if(false) {    	
								    	// add last offset to onsets temporarily just for elongating trackedBeats.
								    	// TODO This probably won't be even close to being accurate, unless I stop the string from vibrating at the right time.
								    	int findOffIterator = onsets.get(onsets.size() - 1);
								    	int window = 5;
								    	int lastS = Math.min(freqs.size() - 1, findOffIterator + window);
								    	List<Double> lastMode = Util.mode(freqs.subList(findOffIterator, lastS));
								    	Util.verify(lastMode.size() == 1, "Last note is not clear.");
								    	double freq = lastMode.get(0);
								    	int numBeforeFail = 3;
								    	int lastOffset = findOffIterator;
								    	while(findOffIterator < freqs.size() && numBeforeFail > 0) {
								    		
								    		if(freqs.get(findOffIterator) != freq) {
								    			numBeforeFail--;
								    		} else {
								    			lastOffset = findOffIterator;
								    		}
								    		findOffIterator++;
								    	}
								    	// onsets.add(lastOffset); // Will be removing lastOffset later.
								}   	
    	
    	
    	
    	// We might want to change this to the distance that divides other numbers the best.
    	// i.e. Praise to the man. Maybe what we have here is what we want, but later account for
    	// what a beat actually is not the average difference between onsets. 
    	int btdifference = avgOnsetDiff(onsetDiff(onsets));
    	// btdifference = 22;
    	List<Integer> trackedBeats = ProcessSignal.beatTracker(onsets, btdifference);
    	
    	// Here I removed lastOffset.
    	// This doesn't work onsets.remove(lastOffset);
    	// TODO make sure I am not making similar stupid situations.
    	// onsets.remove(onsets.size() - 1);
    	
    	List<Integer> notesOnBeat = new ArrayList<Integer>(trackedBeats);
    	List<Double> notesOnBeatD = new ArrayList<Double>();
    	for(int i = 0; i < freqs.size(); i++) { //TODO replace all arrays so that they are the same length. Except for onset ones. replace freqs.size() with that value.
    		notesOnBeatD.add(0.0);
    	}
    	
    	ListIterator<Integer> nOBIter = notesOnBeat.listIterator();
    	while(nOBIter.hasNext()) {
    		int onset = nOBIter.next();
    		if(!onsets.contains(onset)) {
    			nOBIter.set(null);
    			notesOnBeatD.set(onset, 0.5);
    		} else {
    			notesOnBeatD.add(onset, 1.0);
    		}
    	}
    	
    	System.out.println(notesOnBeat);
   	
    	
    	
    	
    	
    	
    	
    	
		List<Double> onsetAmps2 = ProcessSignal.onsetAmps(onsets, Arrays.asList(audioData.getAmp()), 200);
		
		DownBeatData data = new DownBeatData();
		data.avgBeatLength = btdifference;
		
		data.beats = trackedBeats;
		System.out.println("TB: " + trackedBeats);
		data.onsetAmps = onsetAmps2;
		//data.kSignature;
		data.onsets = onsets;		
		DownBeatDetection dbDetection = new DownBeatDetection(data);
		
		dbDetection.detect();
    	
		System.out.println("Note Durations:");
    	for(double i : data.noteDurations) {
    		System.out.print(i + " ");
    	}
    	System.out.println();
if(false) {     	

	
	/*    	final int[] POSSIBLE_BTS_PER_MSURE = {2,3,4,6,8};
    	final int MAX_BTS_A_PICKUP_MSURE = POSSIBLE_BTS_PER_MSURE[POSSIBLE_BTS_PER_MSURE.length - 1] - 1;
    	List<Pair<Integer, Integer>> offsetNMeasureLengths = new ArrayList<Pair<Integer, Integer>>();
    	for(int offset = 0; offset < MAX_BTS_A_PICKUP_MSURE; offset++) {
    		for(int btsPerMsure : POSSIBLE_BTS_PER_MSURE) {
    			offsetNMeasureLengths.add(new Pair<Integer, Integer>(offset, btsPerMsure));
    		}
    	}
    	
    	System.out.println("offsetNMeasureLengths");
    	for(Pair<Integer, Integer> p : offsetNMeasureLengths) {
    		System.out.print(p + " ");
    	}*/
    	
    	List<PossibleDownBeat> pDownBeats = PossibleDownBeat.intitalList();
    	/***********************************************************************************************************/
    	// Simplest and most accurate (non) down beat filter
    	// A note (at least in hymns never crosses a measure). In other words, an onset must occur on the down beat.
    	// For High on the Mountain Top, this removed all but 5 of the 40 possible offsetNMeasureLengths.
    	/**********************************************************************************************************/
    	ListIterator<PossibleDownBeat> pdbIter = pDownBeats.listIterator();
    	while(pdbIter.hasNext()) {
    		PossibleDownBeat p = pdbIter.next();
    		int offset = p.getOffset();
    		int msurLen = p.getLength();
    		for(int i = offset; i < notesOnBeat.size(); i += msurLen) {
    			if(notesOnBeat.get(i) == null) {
    				System.out.println("Removed: " + p);
    				pdbIter.remove();
    				break;
    			}
    		}
    	}
    	
    	System.out.println("Valid downbeats");
    	pdbIter = pDownBeats.listIterator();
    	while(pdbIter.hasNext()) {
    		System.out.print(pdbIter.next() + " ");
    	}
    	
    	
    	
    	pdbIter = pDownBeats.listIterator();
    	System.out.println("asdffdas");
    	while(pdbIter.hasNext()) {
    		PossibleDownBeat p = pdbIter.next();
    		int offset = p.getOffset();
    		int msurLen = p.getLength();
    		
    		//This is banking on noteDurations to be exactly correct.
    		ListIterator<Double> nDIter = data.noteDurations.listIterator(offset);
    		double sum = 0;
    		double first = 0;
    		double second = 0;
    		int numMeasures = 0;
    		int numFirstLarger = 0;
    		while(nDIter.hasNext()) {
    			double duration = nDIter.next();
    			sum += duration;
				if(first == 0) {
					first = duration;
				} else if(second == 0) {
					second = duration;
				}
    			if(sum >= msurLen) {
    				// I am wondering if this shouldn't happen because of the first removal of down beats.
    				// if(sum != msurLen) {System.out.println("pppppppppppppppppppppppppp"); pdbIter.remove(); break;}
    				Util.verify(sum == msurLen,
    						"This probably means that this isn't the correct downbeat."
    						+ "Or if it is, note durations aren't correct / We have a meausre that doesn't add up for some reason.");
    				numMeasures++;
    				if(first > second) {
    					numFirstLarger++; // TODO Add these to a list.
    				}
    				// We need another percentage. What though? all possible numFirstLarger's? All ones that are in the current pDownBeats.
    				// 
    				p.setScore(numFirstLarger/* / (double) numMeasures*/);
    				System.out.println("\t\t" + p);
    				first = second = sum = 0;
    				
    			}
    		}
    		
    		// Maybe We could do add another one to see how the total amount of measures fit into groups of (4?)?
    		
    		/*for(int i = offset; i < notesOnBeat.size(); i += msurLen) {
    			Util.verify(notesOnBeat.get(i) != null, "Should not be null");
    			if(notesOnBeat.get(i) == null) {
    				System.out.println("Removed: " + p);
    				pdbIter.remove();
    				break;
    			}
    		}*/    		
    	}
    	System.out.println(pDownBeats);
    	//
    	// Can make above more efficient.
    	// 		listOfIndexesWhenNull - loop notesOnBeat and insert when null
    	// 		loop listOfIndexesWhenNull
    	//			loop remaining offsetNMeasureLengths
    	//				if(index divides (msurLen - offset)
    	//					remove offsetNMeasureLen
    	// Output of above: 0:2 0:4 0:6 0:8 1:4 1:8 2:2 2:4 2:6 2:8 4:2 4:4 4:6 4:8 5:4 5:8 6:2 6:4 6:6 6:8 
        // 7, 11, 15 - index of nulls -- they invalidate anything that is a multiple of these (with offset)
    	// 2,4,6, and 8 doesn't divide 7 or 11 or 15
    	// 4 and 8 doesn't divide 7-1 or 11-1 or 15-1 but 2 does divide 6, 10, and 14
    	//
 }  	
		int c=0;
		for(int i = 0; i < audioData.getFftAbsolute().size(); i+= fftLen ) {
			int beg = i;
			int end = beg + (fftLen/2);
			List<Double> sub = audioData.getFftAbsolute().subList(beg,end);
			
			freqs.set(c, FundamentalFrequency.computeFrequency(Util.maxIndex(sub, 0, sub.size()), audioData));
			c++;
		}
			


		Util.verify(!onsets.isEmpty(), "Onsets are empty");
		Integer prevOnset = onsets.get(0);
		for(int i = 1; i < onsets.size(); i++) {
			Integer onset = onsets.get(i);
			double l = data.noteDurations.get(i - 1);
			List<Double> modes = Util.mode(freqs.subList(prevOnset, prevOnset + 10/*onset*/));
			//Util.verify(modes.size() == 1, "Too many frequencies between onset: " + prevOnset + " and " + onset);
			double freq = modes.get(0);
			Note note = FrequencyToNote.findNote(freq);
			System.out.print(l + "" + note + " ");
			
			prevOnset = onset;
		}
    	
    	//TODO get its own graph
//		updateGraph(fftGraph, audioData.getFrequencies());
Util.timeDiff("DF");
    	fftGraph.clearData();
		fftGraph.clearData2();
		fftGraph.clearData3();
/*		for(double freq : freqs) {
			System.out.print(FrequencyToNote.findNote(freq) + " ");
		}
		System.out.println();*/
		
    	if(true) {
    		//updateGraph(fftGraph, Arrays.asList(prepareValuesForDisplay(audioData.getNormalizedFrequencies(), 70)));
    		//updateGraph(fftGraph, Arrays.asList(prepareValuesForDisplay(freqs/*audioData.getNormalizedFrequencies()*/, 0.01)));
    											fftGraph.update3List(prepareValuesForDisplay(freqs, 4));
    												//fftGraph.update2List(prepareValuesForDisplay(onsetAmps2/*freqs*/, 5));
    		//fftGraph.update3List(prepareValuesForDisplay(Arrays.asList(smoothedFreqs),20));
    		
    											//fftGraph.updateList(prepareValuesForDisplay(notesOnBeatD, 27));
		    									fftGraph.update2List(preparePositionsForDisplay(onsets, 4050/*25000*/));
//		    		fftGraph.update3List(preparePositionsForDisplay(trackedBeats, 5000));
    		//fftGraph.update3List(preparePositionsForDisplay(onsets, 19000));
//		    									fftGraph.updateList(prepareValuesForDisplay(corrValuesPerc, 55000));
		    									fftGraph.updateList(prepareValuesForDisplay(Arrays.asList(audioData.getAmp()), 1));
		    		//fftGraph.update3List(prepareValuesForDisplay(onsetAmps2, 4));
      		
    	//	updateGraph(fftGraph, Arrays.asList(preparePositionsForDisplay(rollOnsets, 19000)));
   // 		updateGraph(fftGraph, Arrays.asList(preparePositionsForDisplay(onsets, 19000)));
   // 		updateGraph(fftGraph, prepareValuesForDisplay(corrValues, 500));
//			fftGraph.update2List(prepareValuesForDisplay(audioData.getNormalizedFrequencies(), 15));
		    									//fftGraph.update3List(prepareValuesForDisplay(corrValuesPerc, 150/*150000*/));
			//fftGraph.update3List(prepareValuesForDisplay(corrValuesPerc, 15000000));
    	} else {
    		fftGraph.clearData();
    		fftGraph.clearData2();
    		fftGraph.clearData3();
    	}
    	//fftGraph.clearData();
    	
if(true) {
	return;
}
    	
		Double[] beats = new Double[audioData.getNumFFT()];
		Double[] secondBeats = new Double[audioData.getNumFFT()]; 
		Double[] beatPerc = new Double[audioData.getNumFFT()];
		for(int i = 0; i < audioData.getNumFFT()/*audioData.getNormalizedFrequencies().size()*/; i++) {
			beats[i] = 0.0;
			beatPerc[i] = 0.0;
			secondBeats[i] = 0.0;
		}

		
		int count = 0; //classify as agogic or dynamic or both: An accent of length is called an agogic accent. An accent of loudness is called a dynamic accent.
		for(Integer i : audioData.getBeats()) {
			beats[i] = 3500.0;
			
		//	beatPerc[i] = (audioData.getBeatsPercent().get(count) - 1.2) * 2500;
			/*if(beatPerc[i] < 0 ) {
				beatPerc[i] = 0.0;
			}*/
			count++;
		}
		
		for(Integer i : audioData.getBeats2()) {
			secondBeats[i] = 800.0;
		}
		

		List<Integer> beatsList = audioData.getBeats();
		int positionSum = beatsList.get(0);
		int last = positionSum;
		double percentSum = audioData.getBeatsPercent().get(0);
		int percPosition = 0;
		int sumCount = 1;
		
		for(int i = 1; i < beatsList.size(); i++) {
			int onset = beatsList.get(i);
			if((onset - last) / 14.0 < .30) {//.30
				//TODO if percent is above a threshold. Probably not (well that is how I can adjust threshold)
				percentSum += audioData.getBeatsPercent().get(percPosition /*i*/);
				positionSum += onset;
				sumCount++;
			} else {

/*				if(positionSum == 0) {
					beatPerc[i] = percentSum / sumCount;
				} else {
					
*/				
				int index = (int) Math.round(positionSum/ ((double) sumCount)); 
				if(index == -1)
					index = positionSum;
				beatPerc[index] = percentSum /*/ sumCount*/;
//	}

				percentSum = audioData.getBeatsPercent().get(i /*i*/);
				positionSum = onset;
				sumCount = 1;
			}
			last = onset;
			percPosition++;
		}
		
		for(int i = 0; i < beatPerc.length; i++) {
			beatPerc[i] = (beatPerc[i] - 1.2) * 700/*2500*/;
			if(beatPerc[i] < 0 ) {
				beatPerc[i] = 0.0;
			}
		}
		
		
		
/*		onsets.remove(new Integer(951));
		onsets.remove(new Integer(947));
		onsets.add(new Integer(949));
		onsets.sort(new Comparator<Integer>() {
		    public int compare(Integer o1, Integer o2) {
		        return o1.compareTo(o2);
		    }amps
		});*/
		
		
		if(false) {
			fftGraph.updateList(secondBeats);
		} else if (true){
			
			
//			fftGraph.updateList(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,800.0));
			
			
		} else if(false) {
//			fftGraph.updateList(onsetAmps.toArray(new Double[onsetAmps.size()]));
		} else {
			fftGraph.clearData();
		}
		
/*		
		int values[] = {46, 66, 78, 84, 114, 124, 134, 144, 154, 166, 206, 218, 226, 280, 292, 300, 354, 372, 386, 392, 416, 426, 436, 446, 456, 466, 502, 514, 522, 558, 570, 578, 614, 624, 634, 644, 656, 676, 686, 696, 706, 718, 738, 748, 758, 770, 780, 802, 812, 822, 830, 844, 876, 888, 900, 910, 938, 946, 950, 960, 972, 1012, 1024, 1032, 1072, 1086, 1096};
		int output[] = new int[values[values.length - 1] + 1];
		
		for(int i = 0; i < values.length; i++) {
			output[values[i] - 1] = values[i];
		}
		
		System.out.println("Output");
		for(int i = 0; i < output.length; i++) {
			System.out.println(output[i]);
		}
*/		
		
   // 	Number[] data = beats.toArray(new Number[dataList.size()]);
		if(false) {
			audioData.getBtClass().detect();
			fftGraph.clearData2();
			fftGraph.update2List(prepareValuesForDisplay(audioData.getBtClass().percentages, 1));
			
			List<Double> bts = audioData.getBtClass().percentages;
			ArrayList<Double> peaks = new ArrayList<Double>();
			peakAddedSinceLastFall = false;
			lastHeight = bts.get(0);
			for(int i = 1; i < bts.size(); i++) {
				if(bts.get(i) >= lastHeight) {
					//peaks.add(0.0);
					peakAddedSinceLastFall = false;
				} else {
					if(!peakAddedSinceLastFall) {
						peaks.add((double) (i - 1));
						
						/*int peak = i - 1;
						double total = 0;
						double maxValue = lastHeight;
						final int PEAK_HALF = 2;
						for(int j = 1; j <= PEAK_HALF; j++) {
							total += maxValue / Math.max(bts.get(peak - j), 0.1);
							total += maxValue / Math.max(bts.get(peak + j), 0.1);
						}
						peaks.add(Math.min((total/maxValue)*1000, 0.000001));*/
						
						
						
						peakAddedSinceLastFall = true;
					} else {
						//peaks.add(0.0);
					}
				}
				lastHeight = bts.get(i);
			}
			//peaks.add(0.0);
			fftGraph.update3List(prepareValuesForDisplay(peaks, 1));
			
		}
		else if(false) {
			fftGraph.update2List(preparePositionsForDisplay(onsets, 3500));
		} else if(false) {
			fftGraph.update2List(beats);
		} else if(false) {
			fftGraph.clearData2();
		}
		
		if(false) {
			fftGraph.update3List(secondBeats /*beatPerc*/);			
		} else if (false) {
			Double[] amps = audioData.getAmp();
			Double[] ampsAmped = new Double[amps.length];
			for(int i = 0; i < ampsAmped.length; i++) {
				if(i < 130)
					ampsAmped[i] = amps[i] * 240000;//290;//28;
				else
					ampsAmped[i] = amps[i] * 240000;
			}
			
			fftGraph.update3List(ampsAmped);
		} else if (true) {
			//trackedBeats.removeAll(audioData.getBeats());
//			fftGraph.update3List(preparePositionsForDisplay(trackedBeats/*audioData.getTrackedBeats()*/,1200.0));
		} else if(false) {
			Integer actualBeats[] = {46, 66, 81, 86, 116, 126, 136, 146, 156, 166, 206, 221, 226, 266, 281, 286, 326, 346, 361, 366, 396, 406, 416, 426, 436, 446, 486, 501, 506, 546, 561, 566, 586, 596, 606, 616, 626, 646, 656, 666, 676, 686, 706, 716, 726, 736, 746, 766, 776, 786, 796, 806, 821, 831, 841, 851, 866, 876, 886, 896, 936, 951, 956, 996, 1011, 1016};
		    Double[] numbers = preparePositionsForDisplay(Arrays.asList(actualBeats),1200);
		    fftGraph.update3List(numbers);
		} else {
			fftGraph.clearData3();
		}

		
		
		/*Double[] beats2 = new Double[audioData.getNumFFT()];
		for(int i = 0; i < audioData.getNumFFT()audioData.getNormalizedFrequencies().size(); i++) {
			beats2[i] = 0.0;
		}
		
		for(Integer i : audioData.getBeats2()) {
			beats2[i] = 1000.0;
		}
		*/
		
		//fftGraph.update3List(beats2);
    }
    
    private Double[] prepareValuesForDisplay(List<Double> values, double height) {
    	if(values.isEmpty()) {
    		return null;
    	}
		Double[] graph = new Double[values.size()];
		for(int i = 0; i < graph.length; i++) {
			graph[i] = values.get(i) * height;
		}	

		return graph;
    }
    
    private Double[] preparePositionsForDisplay(List<Integer> positions, double height) {
    	if(positions.isEmpty()) {
    		return null;
    	}
    	List<Integer> sortedPositions = new LinkedList<Integer>(positions);
    	sortedPositions.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		});
    	int last = Integer.MIN_VALUE;
    	for(int pos : sortedPositions) {
    		Util.verify(last != pos, "Two positions are the same");
    		last = pos;
    	}
    	
		Double[] graph = new Double[sortedPositions.get(0) + 1 /*positions.get(positions.size() - 1) + 1*/];
		for(int i = 0; i < graph.length; i++) {
			graph[i] = 0.0;
		}	
		for(Integer i : positions) {
			graph[i] = height;
		}
		return graph;
    }
    
    private void displayFFt(int n, final List<Double> absoluteData) {
		//updateGraph(fftGraph, Arrays.asList(absoluteData), n);
    	updateGraph(fftGraph, absoluteData, n);
    	
		int start = n * audioData.getFftLength();
    	int end = start + (audioData.getFftLength() / 2);
/*    	List<Double> subList = Arrays.asList(absoluteData).subList(start, end);*/
    	List<Double> subList = absoluteData.subList(start, end);
    	
    	
/*		FundamentalFrequency ff = new FundamentalFrequency(audioData, subList);
		ff.computeFrequency(subList);*/
		
		
//		Util.println("Fundamental Frequency Index: " + audioData.getFrequencies().get(n));
    }

	//Number[] range = Arrays.copyOfRange(audioData.getFftAbsolute(), start, end);
	//List<Double> data = Arrays.asList(fftData).subList(start, end);
    
    
    private <T> void displayCenterGraph(List<T> data) {
    	updateGraph(centerGraph, data);
    }
    
    private <T> void displayCenterGraph(int n, List<T> data) {
		updateGraph(centerGraph, data, n);
    }
    
/*    private void displayAC(int n) {
    	Number[] data = audioData.getAutoCorrelationAbsolute();
    	Number[] logData = new Number[data.length];
    	for(int i = 0; i < data.length; i++) {
    		logData[i] = Math.log10(data[i].doubleValue());
    	}
		updateGraph(acGraph, logData, n);
    }*/
    
	private void writeToFile(byte[] signalBites, File file, AudioFormat format) {
    	WriteAudioFile toFile = new WriteAudioFile(signalBites, format, file);
    	toFile.start();
	}
    
    public void startCapture() {
    	if(audio == null || audio.isStopped()) {
	    	try {
				audio = new CaptureAudio(
						new LineListener() {
							@Override
							public void update(LineEvent event) {
								Type type = event.getType();
								if (type == Type.CLOSE || type == Type.STOP) {
									if(!audio.isStopped()) {
										audio.stopCapture();
										System.err.println("Audio capture was stopped unexpectedly.");
									}
								}
							}
						});
				System.out.println("STARTED");
				audio.start();
				captureButton.setText("Stop");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	} else {
    		System.err.println("Capture is already in process");
    	}
    }
    
    public void stopCapture(File file) {
    	if(audio != null && !audio.isStopped()) {
    		audio.stopCapture();
    		System.out.println("STOPPED");
    		
    		
	    	//TODO study about Thread.getStackTrace()
	  //  	currentSignal = audio.getStream().toByteArray();
    	//	int[] signal = toIntArray(currentSignal);
	//		updateGraph(signal);

    		
    		AudioData audioData = new AudioData(
    				audio.getStream().toByteArray(),
    				audio.getFormat());
    		processSignal(audioData, file);
    		
/*			captureButton.setDisable(false);
			captureButton.setText("Capture");*/
    	}
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}



/*private int calculateBaseFrequencyIndex(Double[] fftData, int nth, int startOffset) {
	int start = nth * audioData.getFftLength();
	//Only look at half of the FFT.
	int end = start + (audioData.getFftLength() / 2);
	int maxI = findMax(fftData, start, end, 0);
	
	int subMaxI = maxI - start;
	

	List<Integer> peaks = Peaks.findPeaks(data, subMaxI, 7, .05);
	    	
	List<Integer> peakDiff = new LinkedList<Integer>();
	for(int i = 0; i < peaks.size() - 1; i++) {
		peakDiff.add(peaks.get(i+1) - peaks.get(i));
	}
	//This all will not work if the peaks are not exactly correct.
	List<Integer> modes = Util.mode(peakDiff);
	if(modes.size() > 1) {
		System.err.println("You have multiple modes at FFT number: " + nth);
		//System.exit(1);
	}
	return (modes.size() >= 1) ? Util.round(Util.average(modes))modes.get(0) : -1;
}
*/

/*for(int i = 0; i < audioData.getNumFFT(); i++) {
	//TODO needs to be the updated FFT length.
	int start = i * audioData.getFftLength();
	int end = start + audioData.getFftLength();
	

	
	int indexF = findMax(Arrays.copyOfRange(fftLowpass, start, end), 0);
	double freqF = FundamentalFrequency.computeFrequency(indexF, audioData);
	String noteF = FrequencyToNote.findNote(freqF);
	
	int baseFI = calculateBaseFrequencyIndex(fftLowpass, i, 0);
	double baseF = FundamentalFrequency.computeFrequency(baseFI, audioData);
	String baseNote = FrequencyToNote.findNote(baseF);

	System.out.println(i + " " + baseF + " " + baseNote);
	
//	System.out.println("FFT  i: " + i + "Index: " + indexF + " frequency: " + freqF + " note: " + noteF + " maxAmp[i]: "/* + maxAmp[i]*);
	//System.out.println("AC   i: " + i + "Index: " + index + " frequency: " + freq + " note: " + note + " maxAmp[i]: " + maxAmp[i]);
//  	System.out.println("Base i: " + i + "Index: " + baseFI + " frequency: " + baseF + " note: " + baseNote + " maxAmp[i]: " /*+ maxAmp[i]*);
//      	System.out.println();
}
*/