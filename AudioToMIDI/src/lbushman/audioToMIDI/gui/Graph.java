package lbushman.audioToMIDI.gui;

import lbushman.audioToMIDI.util.Util;

import java.util.function.Predicate;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;


public class Graph extends BorderPane {
	private XYChart.Series<Number, Number> series;
	private LineChart<Number, Number> lineChart;
	private final int TEXT_FIELD_WIDTH = 90;
	private final int TEXT_FIELD_HEIGHT = 20;
	private Number[] data;
	private Integer low;
	private Integer high;
	FilteredList<Data<Number,Number>> filtList;
	//ArrayObservableList<Data<Number, Number>> obsList;
	private Series<Number, Number> series2;
	private Series<Number, Number> series3;
	private Number[] data2;
	private Number[] data3;
	
	Graph(String title, String xAxis, String yAxis) 
	{
		super();
		data2 = null;
		data3 = null;
		setCenter(title, xAxis, yAxis);
		setTop();
	}
	
	public void clearData() {
		series.getData().clear();
		
		//getData().clear();
	}
	
	public void clearData2() {
		series2.getData().clear();
	}
	
	public void clearData3() {
		series3.getData().clear();
	}
	
	private TextField newField(String hint, String contents) {
		TextField field = 
				new TextField((contents == null)? "" : contents);
		field.promptTextProperty().set(hint);
		field.setPrefSize(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT);
		return field;
	}
	
	private Button newButton(String text, EventHandler<ActionEvent> handler) {
		Button button = new Button(text);
		button.setOnAction(handler);
		return button;
	}
	
	public void setTop() {
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(10, 12, 10, 12));
		hbox.setSpacing(10);
		hbox.setStyle("-fx-background-color: #336699;");
		
		TextField begField = newField("Beginning Index", null);
		TextField endField = newField("Ending Index", null);
		Button displayFFTButton = newButton(
				"Display Between Range", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						setRange(Integer.parseInt(begField.getText().toString()),
								Integer.parseInt(endField.getText().toString()),
								true);
					}
		});
		
		TextField minMaxIndex = newField(null, "Min/Max Index");
		minMaxIndex.setPrefWidth(150);
		minMaxIndex.setEditable(false);		
		
		Button minimumIndexButton = newButton(
				"Min I", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						int minI = minIndex(Integer.parseInt(begField.getText().toString()),
								Integer.parseInt(endField.getText().toString()));
						minMaxIndex.setText(minI+"");
					}
		});
		
		Button maximumIndexButton = newButton(
				"Max I", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						int maxI = maxIndex(Integer.parseInt(begField.getText().toString()),
								Integer.parseInt(endField.getText().toString()));
						minMaxIndex.setText(maxI+"");
					}
		});
		
		TextField indexField = newField("index", null);
	    TextField valueAtIndex = newField(null, "Val@I");
	    valueAtIndex.setPrefWidth(2);
	    valueAtIndex.setEditable(false);
		
	    Button valueAtIndexButton = newButton(
	    		"Val@I", new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent arg0) {
	    				valueAtIndex.setText(valueAtIndex(Integer.parseInt(indexField.getText()))+"");
	    			}
		});
	    
	    
		TextField widthField = newField("width", null);
		widthField.setPrefWidth(50);
		TextField windowField = newField("nth", null);
	    valueAtIndex.setPrefWidth(50);
	    Button showWindow = newButton(
	    		"Show Window", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						int width = Integer.parseInt(widthField.getText().toString());
						int n = Integer.parseInt(windowField.getText().toString());
						int beg = n * width;
						int end = beg + width;
						setRange(beg, end, true);
					}
				});
	    Button nextWindow = newButton(
	    		"Next", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						int width = Integer.parseInt(widthField.getText().toString());
						int n = Integer.parseInt(windowField.getText().toString());
						n++;
						windowField.setText(n+"");
						int beg = n * width;
						int end = beg + width;
						setRange(beg, end, true);
					}
				});
	    Button previousWindow = newButton(
	    		"Previous", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						int width = Integer.parseInt(widthField.getText().toString());
						int n = Integer.parseInt(windowField.getText().toString());
						n--;
						windowField.setText(n+"");
						int beg = n * width;
						int end = beg + width;
						setRange(beg, end, true);
					}
				});
	    
	    hbox.getChildren().addAll(begField, endField, displayFFTButton, 
	    		minimumIndexButton, maximumIndexButton, minMaxIndex, indexField, 
	    		valueAtIndexButton, valueAtIndex, widthField, windowField, showWindow, nextWindow, previousWindow);
	    setTop(hbox);
	    
	    
	    
	    
/*	    	        
	        Button frequencyButton = new Button("Show Frequencies");
	        frequencyButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					updateGraph(audioData.getFrequencies());
				}
			});
*/	        
	}

	public Number valueAtIndex(int index) {
		return data[index];
	}
	
	public int minIndex(int lowerInc, int upperExc) {
		return Util.minIndex(data, lowerInc, upperExc);
	}
	
	public int maxIndex(int lowerInc, int upperExc) {
		return Util.maxIndex(data, lowerInc, upperExc);
	}
	
	public void setCenter(String title, String xAxis, String yAxis) {
		lineChart = new LineChart<Number, Number>(new NumberAxis(), new NumberAxis());
		lineChart.getXAxis().setLabel(xAxis);//frames
		lineChart.getYAxis().setLabel(yAxis);//amplitude
		lineChart.setTitle(title);//"Audio Signal"
		

		
		lineChart.getStylesheets().add("graphs.css");
        
        series = new XYChart.Series<Number, Number>();
        series.setName(title);//"Original Line"
        //x500principal? security class?
        
        series2 = new XYChart.Series<Number, Number>();
        series2.setName("Beats");//"Original Line"
        
      
        series3 = new XYChart.Series<Number, Number>();
        series3.setName("Beats");//"Original Line"
        
        
        lineChart.getData().add(series);
        lineChart.getData().add(series2);
        lineChart.getData().add(series3);
        
        setCenter(lineChart);
	}

/*    series.getData().filtered(isBetweeen(5, 10));
    series.getData().fi
*/
/*	public static List<Number> filterData() {
		return employees.stream().filter( predicate ).collect(Collectors.<Employee>toList());	
	}
	
	public static Predicate<Number> isBetweeen(Number beg, Number end) {
		return p -> p.doubleValue() >= beg.doubleValue() && p.doubleValue() <= end.doubleValue();
	}
*/	
/*	public void updateRange() {
		lineChart.updateAxisRange();
	}	*/
	
	
	public void setRange(int begIndex, int endIndex, boolean startZero) {
		low = begIndex;
		high = endIndex;
		/*clearData();
		if(data2 != null)
			clearData2();
		if(data3 != null)
			clearData3();*/
		
		if(data != null && !series.getData().isEmpty()) {
			clearData();
			for(int i = begIndex; i < endIndex; i++) {
				if(i == data.length) {
					break;
				}
				if(startZero) {
					series.getData().add(new Data<Number, Number>(i - begIndex, data[i]));
				}
				else {
					series.getData().add(new Data<Number, Number>(i, data[i]));
				}
			}
		}
		
		if(data2 != null && !series2.getData().isEmpty()) {
			clearData2();
			for(int i = begIndex; i < endIndex; i++) {
				if(i == data2.length) {
					break;
				}
				if(startZero) {
					series2.getData().add(new Data<Number, Number>(i - begIndex, data2[i]));
				}
				else {
					series2.getData().add(new Data<Number, Number>(i, data2[i]));
				}
			}
		}
		
		if(data3 != null && !series3.getData().isEmpty()) {
			clearData3();	
			for(int i = begIndex; i < endIndex; i++) {
				if(i == data3.length) {
					break;
				}
				if(startZero) {
					series3.getData().add(new Data<Number, Number>(i - begIndex, data3[i]));
				}
				else {
					series3.getData().add(new Data<Number, Number>(i, data3[i]));
				}
			}
		}
		
		//filtList.setPredicate(between(low,high));
		
		
	/*	series.getData().addAll(
				obsList.subList(low,high));*/
 
		
	///	clearData();
	//	series.getData().addAll(filtList);
//		System.out.println("Here");
	}
	
	public static Predicate<Data<Number, Number>> doTrue() {
		return p-> p.getXValue().intValue() > -1;
	}
	
	public static Predicate<Data<Number, Number>> between(Integer low, Integer high) {
		return p-> p.getXValue().intValue() >= low && p.getXValue().intValue() <= high;
	}
	
	public void update2List(Number[] signal) {
		clearData2();
		data2 = signal;
		for(int i = 0; i < signal.length; i++) {
			series2.getData().add(new Data<Number, Number>(i, signal[i])); 
		}
	}
	
	public void update3List(Number[] signal) {
		clearData3();
		data3 = signal;
		for(int i = 0; i < signal.length; i++) {
			series3.getData().add(new Data<Number, Number>(i, signal[i])); 
		}
	}
		
	public void updateList(Number[] signal) {
		clearData();
		data = signal;
		for(int i = 0; i < signal.length; i++) {
			series.getData().add(new Data<Number, Number>(i, signal[i])); 
		}
		
		
		
/*		low = 0;
		high = signal.length / 2;
		
		List<Data<Number, Number>> list = new ArrayList<Data<Number, Number>>();
		for(int i = 0; i < signal.length; i++) {
			list.add(new Data<Number, Number>(i, signal[i])); 
		}
		obsList = 
				new ArrayObservableList<Data<Number,Number>>(list);
		
		series.getData().addAll(obsList);*/
//		this.filtList = new FilteredList<Data<Number,Number>>(obsList, between(low,high));
		
		//filtList.setPredicate(null);
		
		
//		series.getData().addAll(filtList);

		
		//		series.getData().a
//		data = signal;

		
/*        int start = 0;// signal.length / 2;
        int end = signal.length;//start + 300;//(bites.length * 17) / 32;
        for(int i = start; i < end; i++) {
        	//fix for 32 bit
            series.getData().add(new XYChart.Data<Number, Number>((i - start), signal[i]));
            if(i % 100 == 0)
            	System.out.println((i - start)*2 + " " + bites.length);
        }*/
	}
}
