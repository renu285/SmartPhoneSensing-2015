package com.example.renu.rotation_vector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sm;
    TextView angle,Count;
    TextView gVal;
    Button reset;
    float azimut;
    int mAzimuth;
    int Degrees;
    Sensor acc;
    Sensor mag;
    Sensor mSensor;
    OutputStreamWriter MagFileOSW;
    File MagFolder;
    File MagFile;
    FileOutputStream fos;
    OutputStreamWriter myOutWriter;
    ArrayList<Integer> vals;

    static final float ALPHA = 0.20f;


    float[] mGravity;
    float[] mGeomagnetic;
    int StepCount = 0;
    boolean lastPeak = true;

    float[] orientation = new float[3];
    float[] rMat = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        angle = (TextView)findViewById(R.id.azimuth);
        Count  = (TextView)findViewById(R.id.steps);
        gVal  = (TextView)findViewById(R.id.gCalc);
        reset = (Button)findViewById(R.id.reset);

        init_files();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sm.registerListener(this,acc,sm.SENSOR_DELAY_FASTEST);
        sm.registerListener(this,mag,sm.SENSOR_DELAY_NORMAL);
        sm.registerListener(this,mSensor,sm.SENSOR_DELAY_FASTEST);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StepCount = 0;
                Count.setText("Steps : 0 ");

            }
        });

    }


    void init_files()

    {
        try {
            MagFolder = new File(Environment.getExternalStorageDirectory(), "Report");
            if (!MagFolder.exists()) {
                MagFolder.mkdir();
            }
            try {
                 MagFile = new File(MagFolder, "AngelValues" + ".txt");
                MagFile.createNewFile();
                fos = new FileOutputStream(MagFile,true);
                myOutWriter = new OutputStreamWriter(fos);
            } catch (Exception ex) {
                System.out.println("ex: " + ex);
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }


    }


    @Override
    public void onSensorChanged(SensorEvent event) {



         if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double x , y, z;
            double g_earth = SensorManager.GRAVITY_EARTH;

             float[] gravSensorVals = new float[3];


            //gravSensorVals = lowPass(event.values.clone(), gravSensorVals);


            //x = gravSensorVals[0];
            //y=  gravSensorVals[1];
            //z = gravSensorVals[2];
            // x = event.values[0];
            y = event.values[1];
            z = event.values[2];

             //double g = ((x * x) + (y * y) + (z * z)) / (g_earth * g_earth);
             double g = ((y*y)+ (z * z)) / (g_earth * g_earth);

             //Log.d("G value ",String.valueOf(g));
             //gVal.setText("g : " + String.valueOf(g));
            double threshold = 1.60;
            if (g > threshold && !lastPeak) {
                StepCount++;
                Log.d("Testing :",Integer.toString(StepCount));
                Count.setText("Steps :" + Integer.toString(StepCount));
                lastPeak = true;
            }
            if (g < threshold) {
                //gVal.setText("gval :" + String.valueOf(g));
                lastPeak = false;
            }


        }

        if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ) {
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            // get the azimuth value (orientation[0]) in degree
            // mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]));

            angle.setText("Angle: " + Integer.toString(mAzimuth));


            try {

                String test = Integer.toString(Degrees) + "\n";
                byte[] bytes = test.getBytes();
                myOutWriter.write(test);
                myOutWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                Degrees = (int) Math.round(Math.toDegrees(azimut));


            }
        }
    }

    float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
    

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}
