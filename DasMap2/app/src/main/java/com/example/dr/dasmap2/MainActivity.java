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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //    Views & buttons
    TextView angleTitle, angleValue, activityTitle, activityStatus, lastDegreeView, degreesMean;
    Button buttonRandomize, buttonMoveLeft, buttonMoveRight, buttonMoveUp, buttonMoveDown, buttonSetHeight;
    ImageView mapView;
    EditText heightView;

    //    Sensors
    SensorManager sm;
    Sensor acc, mag, mSensor;
    float azimuth;
    int mAzimuth, degrees;
    int[] lastDegrees = new int[10];
    
    //    Direction
    float[] mGravity, mGeomagnetic;
    float[] orientation = new float[3];
    float[] rMat = new float[9];

    int mapUp = 70, mapDown = -90;
    int mapRight = 170, mapLeft = 10;
    float mean = 0;

    //    Activity
    int stepCount;
    boolean lastPeak = true;
    boolean stepLimiter = true;

    float height = 180;
    String heightStr = "180";
    float stepLength = 0.4f*(height/100);
    float stepLengthDots = stepLength * 8;
    
    //    Particle related
    int n = 500;
    Particle[] particles = new Particle[n];
    Cell[] cells;
    float[] moving = {0,0,0};

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
        lastDegreeView = (TextView) findViewById(R.id.lastDegrees);
        degreesMean = (TextView) findViewById(R.id.degreesMean);
        mapView = (ImageView) findViewById(R.id.image);
        heightView = (EditText) findViewById(R.id.heightView);

        buttonRandomize = (Button) findViewById(R.id.buttonRandomize);
        buttonMoveLeft = (Button) findViewById(R.id.buttonMoveLeft);
        buttonMoveRight = (Button) findViewById(R.id.buttonMoveRight);
        buttonMoveUp = (Button) findViewById(R.id.buttonMoveUp);
        buttonMoveDown = (Button) findViewById(R.id.buttonMoveDown);
        buttonSetHeight = (Button) findViewById(R.id.buttonSetHeight);

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

        initStuff();

        r = new Random();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stepLimiter) {
                            stepLimiter = false;
                        }
//                        if (moving[0] == 1) {
//                            map.drawMap(bg, canvas, mapView);
//                            for (int i=0; i<particles.length; i++) {
//                                particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
//                                particles[i].moveXY(moving[1],moving[2]);
//                            }
//                            drawParticles();
//                            mapView.setBackgroundDrawable(new BitmapDrawable(bg));
//                        }
                    }
                });
            }
        }, 0, 700);


        buttonRandomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                randomizeParticles(particles);
                map.drawMap(bg, canvas, mapView);
                drawParticles();

                mapView.setBackgroundDrawable(new BitmapDrawable(bg));
                stepCount = 0;
                activityStatus.setText(Integer.toString(stepCount));
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

        buttonSetHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heightStr = heightView.getText().toString();
                height = Integer.parseInt(heightStr);
                stepLength = 0.4f*(height/100);
                stepLengthDots = stepLength * 8;
//                showFilename.setText(filename);
            }
        });
    }



    public void initStuff() {
        cells = new Cell[20];
        map.initCells(cells);
        for (int i=0; i<particles.length; i++) {
            particles[i] = new Particle(0,0);
            particles[i].setPos(2*i, 2*i);
        }

        for (int i=0; i<lastDegrees.length; i++) {
            lastDegrees[i] = 0;
        }

    }

    public void randomizeParticles(Particle[] p) {
        initStuff();
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
            double threshold = 1.50;
            if (!stepLimiter) {
                if (g > threshold && !lastPeak) {
                    onPause();
                    stepCount++;
                    stepLimiter = true;
//                Log.d("Testing :",Integer.toString(stepCount));
                    activityStatus.setText(Integer.toString(stepCount));
                    lastPeak = true;
                    if ((mean < mapUp + 20) && (mean > mapUp - 20)) {
                        moving[1] = 0;
                        moving[2] = -stepLengthDots;
                        moving[0] = 1;

                        map.drawMap(bg, canvas, mapView);
                        for (int i=0; i<particles.length; i++) {
                            particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                            particles[i].moveXY(moving[1], moving[2]);
                        }
                        drawParticles();
                        mapView.setBackgroundDrawable(new BitmapDrawable(bg));


                    }

                    if ((mean < mapDown + 20) && (mean > mapDown - 20)) {
                        moving[1] = 0;
                        moving[2] = stepLengthDots;
                        moving[0] = 1;

                        map.drawMap(bg, canvas, mapView);
                        for (int i=0; i<particles.length; i++) {
                            particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                            particles[i].moveXY(moving[1], moving[2]);
                        }
                        drawParticles();
                        mapView.setBackgroundDrawable(new BitmapDrawable(bg));


                    }

                    if (( (mean < mapRight + 20)) && (mean > mapRight - 20)) {
                        moving[1] = stepLengthDots;
                        moving[2] = 0;
                        moving[0] = 1;

                        map.drawMap(bg, canvas, mapView);
                        for (int i=0; i<particles.length; i++) {
                            particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                            particles[i].moveXY(moving[1], moving[2]);
                        }
                        drawParticles();
                        mapView.setBackgroundDrawable(new BitmapDrawable(bg));
                    }
                    if ((mean < mapLeft + 20) && (mean > mapLeft - 20)) {
                        moving[1] = -stepLengthDots;
                        moving[2] = 0;
                        moving[0] = 1;

                        map.drawMap(bg, canvas, mapView);
                        for (int i=0; i<particles.length; i++) {
                            particles[i].cell = map.whichCell(cells, particles[i].getPos()[0], particles[i].getPos()[1]);
                            particles[i].moveXY(moving[1], moving[2]);
                        }
                        drawParticles();
                        mapView.setBackgroundDrawable(new BitmapDrawable(bg));
                    }

                    onResume();
                }
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
            for (int i=lastDegrees.length-1; i>0; i--) {
                if (mAzimuth < -140) {
                    mAzimuth += 360;
                }
                lastDegrees[i] = lastDegrees[i-1];
            }
            lastDegrees[0] = mAzimuth;
            lastDegreeView.setText(Integer.toString(lastDegrees[0])+", "+Integer.toString(lastDegrees[1]));

            int sum = 0;
            for (int i=0; i<lastDegrees.length; i++) {
                sum += lastDegrees[i];
            }
            mean = sum/lastDegrees.length;
            degreesMean.setText(Float.toString(mean));
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

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this,acc,sm.SENSOR_DELAY_FASTEST);
        sm.registerListener(this,mag,sm.SENSOR_DELAY_NORMAL);
        sm.registerListener(this,mSensor,sm.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

}
