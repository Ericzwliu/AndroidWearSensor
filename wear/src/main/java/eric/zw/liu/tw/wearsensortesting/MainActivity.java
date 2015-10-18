package eric.zw.liu.tw.wearsensortesting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class MainActivity extends Activity {

    private TextView mTextView;
    private Button btnStart;
    private Button btnStop;
    private SensorManager mSensorManager = null;
    private Sensor mSAccelerometer = null, mSGyrosc = null;
    private String LOG_TAG = "Gsensor_Test";
    private final static int X_INDEX = 0;
    private final static int Y_INDEX = 1;
    private final static int Z_INDEX = 2;
    private GSensorRawDataCollector aCollector,gCollector;

    private SensorEventListener aSensorEvent = null, gSensorEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aCollector = new GSensorRawDataCollector();
        gCollector = new GSensorRawDataCollector();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                btnStart = (Button) stub.findViewById(R.id.btnStart);
                btnStop = (Button) stub.findViewById(R.id.btnStop);

                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

                        /**
                                                 *  SENSOR_DELAY_FASTEST    -> 0 ms : as soon as possible
                                                 *  SENSOR_DELAY_GAME       -> 20 ms
                                                 *  SENSOR_DELAY_UI         -> 60 ms
                                                 *  SENSOR_DELAY_NORMAL     -> 200 ms
                                                 */

                        if( (mSAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ) != null ) {

                            aSensorEvent = new SensorEventListener() {
                                @Override
                                public void onSensorChanged(SensorEvent event) {
                                    Log.d(LOG_TAG," acceletometer event.values.length : " + event.values.length);

                                    String[] data = new String[4];
                                    data[GSensorRawDataCollector.TIMESTAMP_INDEX] = getCurrentTimeStamp()+"";
                                    data[GSensorRawDataCollector.X_INDEX] = event.values[X_INDEX] + "";
                                    data[GSensorRawDataCollector.Y_INDEX] = event.values[Y_INDEX] + "";
                                    data[GSensorRawDataCollector.Z_INDEX] = event.values[Z_INDEX] + "";

                                    String strForValues = "X : " + event.values[X_INDEX] + "\n" + "Y : " + event.values[Y_INDEX] + "\n" + "Z : " + event.values[Z_INDEX];
                                    mTextView.setText(strForValues);

                                    aCollector.addData( data );
                                }

                                @Override
                                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                                }
                            };
                            mSensorManager.registerListener(aSensorEvent, mSAccelerometer, SensorManager.SENSOR_DELAY_UI);
                        }

                        if( ( mSGyrosc = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ) != null) {
                            gSensorEvent = new SensorEventListener() {
                                @Override
                                public void onSensorChanged(SensorEvent event) {
                                    Log.d(LOG_TAG," gyroscope event.values.length : " + event.values.length);

                                    String[] data = new String[4];
                                    data[GSensorRawDataCollector.TIMESTAMP_INDEX] = getCurrentTimeStamp()+"";
                                    data[GSensorRawDataCollector.X_INDEX] = event.values[X_INDEX] + "";
                                    data[GSensorRawDataCollector.Y_INDEX] = event.values[Y_INDEX] + "";
                                    data[GSensorRawDataCollector.Z_INDEX] = event.values[Z_INDEX] + "";

                                    gCollector.addData( data );
                                }

                                @Override
                                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                                }
                            };
                            mSensorManager.registerListener(gSensorEvent, mSGyrosc, SensorManager.SENSOR_DELAY_UI);
                        }




                    }
                });

                btnStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(aSensorEvent != null) mSensorManager.unregisterListener(aSensorEvent);
                        if(gSensorEvent != null) mSensorManager.unregisterListener(gSensorEvent);

                        aSensorEvent = null; gSensorEvent = null;

                        Toast.makeText(MainActivity.this, "Unregister event sensors  Listener", Toast.LENGTH_SHORT).show();

                        exportRawDataCSV();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(aSensorEvent != null) mSensorManager.unregisterListener(aSensorEvent);
            if(gSensorEvent != null) mSensorManager.unregisterListener(gSensorEvent);
        }catch(NullPointerException e){

        } finally {

        }
    }

    public void exportRawDataCSV() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/GSensorRawData");

        boolean var = false;
        if(!folder.exists()) var = folder.mkdir();

        final String filename = folder.toString() + "/" + "RawData_" + getCurrentTimeStamp() + ".csv";
        Log.d(LOG_TAG,filename);

        CharSequence contentTitle = getString(R.string.app_name);
        final ProgressDialog progDialog = ProgressDialog.show(
                MainActivity.this,
                contentTitle,
                "Exporting raw data ...",
                true
        );

        new Thread(){
            @Override
            public void run() {
                try {
                    FileWriter fw = new FileWriter(filename);
                    if(aCollector != null) {
                        fw.append("Accelerometer TimeStamp");
                        fw.append(',');
                        fw.append("X(A)");
                        fw.append(',');
                        fw.append("Y(A)");
                        fw.append(',');
                        fw.append("Z(A)");
                        fw.append(',');
                        fw.append('\n');

                        for(int i=0; i< aCollector.size(); i++) {
                            String[] data = aCollector.getRecord(i);
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
                    }

                    if(gCollector != null) {
                        fw.append("Gyroscope TimeStamp");
                        fw.append(',');
                        fw.append("X(G)");
                        fw.append(',');
                        fw.append("Y(G)");
                        fw.append(',');
                        fw.append("Z(G)");
                        fw.append(',');
                        fw.append('\n');

                        for(int i=0; i<gCollector.size(); i++) {
                            String[] data = gCollector.getRecord(i);
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
                    }



                    fw.close();
                } catch( IOException e ) {
                    e.printStackTrace();
                }

                progDialog.dismiss();
            }
        }.start();
    }

    public String getCurrentTimeStamp(){
        long timestamp = System.currentTimeMillis();
        Timestamp tsTemp = new Timestamp(timestamp);
        return tsTemp.getTime()+"";
    }
}
