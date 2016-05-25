package com.example.dr.dasmap2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //    Views & buttons
    TextView angleTitle, angleValue, activityTitle, activityStatus;
    Button buttonRandomize, buttonMoveLeft, buttonMoveRight, buttonMoveUp, buttonMoveDown;
    ImageView mapView;

    //    Sensors
    SensorManager sm;
    Sensor acc, mag, mSensor;
    float azimuth;
    int mAzimuth, degrees;
    
    //    Direction
    float[] mGravity, mGeomagnetic;
    float[] orientation = new float[3];
    float[] rMat = new float[9];
    
    //    Activity
    int stepCount;
    boolean lastPeak = true;
    
    //    Particle related
    int n = 500;
    Particle[] particles = new Particle[n];
    Cell[] cells;
    int[] moving = {0,0,0};

    //    Drawing objects
    Bitmap bg;
    Canvas canvas;
    Paint paint;
    Map map;

    //    Utilities
    Random r;
    Timer timer;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        angleTitle = (TextView) findViewById(R.id.angleTitle);
        angleValue = (TextView) findViewById(R.id.angleValue);
        activityTitle = (TextView) findViewById(R.id.activityTitle);
        activityStatus = (TextView) findViewById(R.id.activityStatus);
        mapView = (ImageView) findViewById(R.id.image);

        buttonRandomize = (Button) findViewById(R.id.buttonRandomize);
        buttonMoveLeft = (Button) findViewById(R.id.buttonMoveLeft);
        buttonMoveRight = (Button) findViewById(R.id.buttonMoveRight);
        buttonMoveUp = (Button) findViewById(R.id.buttonMoveUp);
        buttonMoveDown = (Button) findViewById(R.id.buttonMoveDown);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sm.registerListener(this,acc,sm.SENSOR_DELAY_FASTEST);
        sm.registerListener(this,mag,sm.SENSOR_DELAY_NORMAL);
        sm.registerListener(this,mSensor,sm.SENSOR_DELAY_FASTEST);
        
        map = new Map();
        bg = Bitmap.createBitmap(140, 600, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);
        paint = new Paint();
        map.drawMap(bg, canvas, mapView);
        initParticles(particles);

        r = new Random();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (moving[0] == 1) {
                            map.drawMap(bg, canvas, mapView);
                            for (int i=0; i<particles.length; i++) {
                                particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                                particles[i].moveXY(moving[1],moving[2]);
                            }
                            drawParticles();
                            mapView.setBackgroundDrawable(new BitmapDrawable(bg));
                        }
                    }
                });
            }
        }, 0, 200);


        buttonRandomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                randomizeParticles(particles);
                map.drawMap(bg, canvas, mapView);
                drawParticles();

                mapView.setBackgroundDrawable(new BitmapDrawable(bg));
            }
        });

        buttonMoveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moving[0] == 0) {
                    moving[1] = -1;
                    moving[2] = 0;
                    moving[0] = 1;
                }
                else {
                    moving[0] = 0;
                }
            }
        });

        buttonMoveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moving[0] == 0) {
                    moving[1] = 1;
                    moving[2] = 0;
                    moving[0] = 1;
                }
                else {
                    moving[0] = 0;
                }
            }
        });

        buttonMoveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moving[0] == 0) {
                    moving[1] = 0;
                    moving[2] = -1;
                    moving[0] = 1;
                }
                else {
                    moving[0] = 0;
                }
            }
        });

        buttonMoveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moving[0] == 0) {
                    moving[1] = 0;
                    moving[2] = 1;
                    moving[0] = 1;
                }
                else {
                    moving[0] = 0;
                }
            }
        });
    }



    public void initParticles(Particle[] p) {
        cells = new Cell[20];
        map.initCells(cells);
        for (int i=0; i<p.length; i++) {
            p[i] = new Particle(0,0);
            p[i].setPos(2*i, 2*i);
        }
    }

    public void randomizeParticles(Particle[] p) {
        initParticles(particles);
        int xMin, xMax, yMin, yMax;
        Random rX = new Random();
        Random rY = new Random();
        int counter = 0;
        for (int i=1; i<cells.length; i++) {
            for (int j = counter; j < (counter+((cells[i].prob)/20)); j++) {
                if (j<n) {

                    xMin = (int) cells[i].x1;
                    xMax = (int) cells[i].x2;
                    yMin = (int) cells[i].y1;
                    yMax = (int) cells[i].y2;
                    p[j].setPos(rX.nextInt(xMax - xMin) + xMin, rY.nextInt(yMax - yMin) + yMin);
                }
            }
            counter += cells[i].prob/20;
        }

    }

    public void drawParticles() {
        for (int i = 0; i < particles.length; i++) {
            int cellInd = 0;
            if (particles[i].isActive == true) {
                cellInd = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                particles[i].setBoundaries(cells, cellInd);

                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawPoint(particles[i].getPos()[0], particles[i].getPos()[1], paint);
            }
            else {
                int xMin, xMax, yMin, yMax;
                Random rand = new Random();
                int newCell;
                int cellIs = rand.nextInt(10000)+1;
                newCell = map.isCell(cells, cellIs);

                xMin = (int) cells[newCell].x1;
                xMax = (int) cells[newCell].x2;
                yMin = (int) cells[newCell].y1;
                yMax = (int) cells[newCell].y2;
                particles[i].setPos(rand.nextInt(xMax - xMin) + xMin, rand.nextInt(yMax - yMin) + yMin);
                particles[i].bringToLife(cells, newCell);
                particles[i].isActive = true;
                paint.setColor(Color.parseColor("#ff0000"));
                canvas.drawPoint(particles[i].getPos()[0], particles[i].getPos()[1], paint);
            }
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
                stepCount++;
//                Log.d("Testing :",Integer.toString(stepCount));
                activityStatus.setText(Integer.toString(stepCount));
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
            // mAzimuth = (int) ( Math.todegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]));

            angleValue.setText(Integer.toString(mAzimuth));


//            try {
//
//                String test = Integer.toString(degrees) + "\n";
//                byte[] bytes = test.getBytes();
//                myOutWriter.write(test);
//                myOutWriter.flush();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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
                azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
                degrees = (int) Math.round(Math.toDegrees(azimuth));


            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
