package eric.zw.liu.tw.wearsensortesting;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GSensorRawDataCollector {
	public final static int SENSORTYPE_INDEX = 0;
	public final static int TIMESTAMP_INDEX = 1;
	public final static int X_INDEX = 2;
	public final static int Y_INDEX = 3;
	public final static int Z_INDEX = 4;
	private String LOG_TAG = "Gsensor_Test";
	private String currentFileName = "";
	
	private ArrayList<String[]> rawData;
	
	public GSensorRawDataCollector(String fileName){

		this.rawData = new ArrayList<String[]>();
		this.currentFileName = fileName;
	}
	
	public void addData(String[] data) {

		this.rawData.add(data);
//		Log.d(LOG_TAG,"add type : " + data[SENSORTYPE_INDEX] + ", size : " + rawData.size());
		if(this.rawData.size() >= 255 ) {
			GSensorRawDataCollector dataCollector = new GSensorRawDataCollector(currentFileName);
			dataCollector.setRecords(rawData);
			rawData.clear();
			this.exportToFile(dataCollector,this.currentFileName);
		}
	}
	
	public ArrayList<String[]> getAllRecords(){
		return this.rawData;
	}
	
	public String[] getRecord(int index){
		return this.rawData.get(index);
	}

	public void setRecords(ArrayList<String[]> list){
		this.rawData.addAll(list);
	}
	
	public int size(){
		return this.rawData.size();
	}

	public void clear(){
		this.rawData.clear();
	}

	private void exportToFile(final GSensorRawDataCollector dataCollector, final String fileName) {


			new Thread(){
				@Override
				public void run() {



					try {
						Log.d(LOG_TAG, "dataCollector.size() : " + dataCollector.size() );
						Log.d(LOG_TAG, fileName);
						Log.d(LOG_TAG, "SENSORTYPE_INDEX : " + dataCollector.getRecord(0)[GSensorRawDataCollector.SENSORTYPE_INDEX] );
						FileWriter fw = new FileWriter(fileName);
//						synchronized (fw){
							for(int i=0; i< dataCollector.size(); i++) {
								String[] data = dataCollector.getRecord(i);
								fw.append(data[GSensorRawDataCollector.SENSORTYPE_INDEX]);
								fw.append(',');
								fw.append(data[GSensorRawDataCollector.TIMESTAMP_INDEX]);
								fw.append(',');
								fw.append(data[GSensorRawDataCollector.X_INDEX]);
								fw.append(',');
								fw.append(data[GSensorRawDataCollector.Y_INDEX]);
								fw.append(',');
								fw.append(data[GSensorRawDataCollector.Z_INDEX]);
								fw.append(',');
								fw.append('\n');
							}
							fw.append('\n');
							fw.close();
//						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}.start();

	}
}


