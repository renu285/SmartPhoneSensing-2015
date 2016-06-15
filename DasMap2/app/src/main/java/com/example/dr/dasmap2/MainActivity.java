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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //    Views & buttons
    TextView angleTitle, angleValue, activityTitle, activityStatus, lastDegreeView, degreesMean;
    Button buttonRandomize, buttonSetHeight;
    ImageView mapView;
    EditText heightView;
    TextView cellStats1, cellStats2, cellStats3, particleCounter;

    //    Sensors
    SensorManager sm;
    Sensor acc, mag, mSensor;
    float azimuth;
    int mAzimuth, degrees;
    int[] lastDegrees = new int[15];
    
    //    Direction
    float[] mGravity, mGeomagnetic;
    float[] orientation = new float[3];
    float[] rMat = new float[9];

    float meanDirection= 0;
    NoiseDirection noise = new NoiseDirection();
    int directionalNoise = 0;

    //    Activity
    int stepCount;
    boolean lastPeak = true;
    boolean stepLimiter = true;

    float height = 180;
    String heightStr = "180";
    float stepLength = 0.4f*(height/100);
    float stepLengthDots = stepLength * 8;
    float stepLengthNoise = 0.0f;
    
    //    Particle related
    int n = 5000;
    Particle[] particles = new Particle[n];
    ArrayList<Particle> particle = new ArrayList<Particle>(n);
    Cell[] cells;
    int particleCounterAlive = n;
    int particleCounterDead = 0;
    int[][] moveHistory = new int[10][2];
    int cellInd = 0;
    float[] pos = new float[2];
    int xMin, xMax, yMin, yMax;
    int newCell;
    Particle p;
    Particle pNew;

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
        cellStats1 = (TextView) findViewById(R.id.cellStats1);
        cellStats2 = (TextView) findViewById(R.id.cellStats2);
        cellStats3 = (TextView) findViewById(R.id.cellStats3);
        particleCounter = (TextView) findViewById(R.id.particleCounter);

        buttonRandomize = (Button) findViewById(R.id.buttonRandomize);
//        buttonMoveLeft = (Button) findViewById(R.id.buttonMoveLeft);
//        buttonMoveRight = (Button) findViewById(R.id.buttonMoveRight);
//        buttonMoveUp = (Button) findViewById(R.id.buttonMoveUp);
//        buttonMoveDown = (Button) findViewById(R.id.buttonMoveDown);
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

                randomizeParticles();
                map.drawMap(bg, canvas, mapView);
                drawParticles();

                mapView.setBackgroundDrawable(new BitmapDrawable(bg));
                stepCount = 0;
                activityStatus.setText(Integer.toString(stepCount));
            }
        });

//        buttonMoveLeft.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (moving[0] == 0) {
//                    moving[1] = -1;
//                    moving[2] = 0;
//                    moving[0] = 1;
//                }
//                else {
//                    moving[0] = 0;
//                }
//            }
//        });

//        buttonMoveRight.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (moving[0] == 0) {
//                    moving[1] = 1;
//                    moving[2] = 0;
//                    moving[0] = 1;
//                }
//                else {
//                    moving[0] = 0;
//                }
//            }
//        });

//        buttonMoveUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (moving[0] == 0) {
//                    moving[1] = 0;
//                    moving[2] = -1;
//                    moving[0] = 1;
//                }
//                else {
//                    moving[0] = 0;
//                }
//            }
//        });

//        buttonMoveDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (moving[0] == 0) {
//                    moving[1] = 0;
//                    moving[2] = 1;
//                    moving[0] = 1;
//                }
//                else {
//                    moving[0] = 0;
//                }
//            }
//        });

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
//        for (int i=0; i<particles.length; i++) {
//            particles[i] = new Particle(0,0);
//            particles[i].setPos(2*i, 2*i);
//        }
        for (int i=0; i<n; i++) {
            particle.add(new Particle(0,0));
        }

        for (int i=0; i<lastDegrees.length; i++) {
            lastDegrees[i] = 0;
        }

        for (int i=1; i<moveHistory.length; i++) {
            for (int j=0; j<moveHistory[0].length; j++) {
                moveHistory[i][j] = 0;
            }
        }
        noise.init();

    }

    public void randomizeParticles() {

        int xMin, xMax, yMin, yMax;

        int counter = 0;
        for (int i=1; i<cells.length; i++) {
            for (int j = counter; j < (counter+((cells[i].prob)/2)); j++) {
                if (j<n) {
                    p = particle.get(j);
                    xMin = (int) cells[i].x1;
                    xMax = (int) cells[i].x2;
                    yMin = (int) cells[i].y1;
                    yMax = (int) cells[i].y2;
                    p.setPos(r.nextInt(xMax - xMin) + xMin, r.nextInt(yMax - yMin) + yMin);
                    cells[i].particleCellCounter += 1;
                    p.setBoundaries(cells, i);
                }
            }
            counter += cells[i].prob/2;
        }

    }

    public void drawParticles() {
        paint.setColor(Color.parseColor("#ff0000"));

        for (int i = 0; i < particle.size(); i++) {
            p = particle.get(i);
            pos = p.getPos();
            if (p.newLocation) {

                cellInd = map.whichCell(cells, pos[0], pos[1]);
                p.setBoundaries(cells, cellInd);
                p.newLocation = false;
            }
            canvas.drawPoint(pos[0], pos[1], paint);
        }
        for (int i = 0; i<particleCounterDead; i++) {
//            particle.add(new Particle(0,0));
            p = particle.get(r.nextInt(particleCounterAlive));
//            pNew = particle.get(particle.size()-1);
            pos = p.getPos();
            pNew = new Particle(pos[0], pos[1]);
            pNew.setBoundaries(cells, p.cell);
            pNew.cell = p.cell;
            pNew.isActive = true;
            pNew.wallHit = false;
            pNew.newLocation = false;
            particle.add(pNew);
            canvas.drawPoint(pos[0], pos[1], paint);
        }
        particleCounterAlive = n;
        particleCounterDead = 0;
        getStats();
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
                    Particle p;
                    map.drawMap(bg, canvas, mapView);
                    float[] pos = new float[2];
                    stepCount++;
                    stepLimiter = true;

                    activityStatus.setText(Integer.toString(stepCount));
                    lastPeak = true;

                    for (int i=0; i<particle.size(); i++) {
                        p = particle.get(i);
                        directionalNoise = noise.getValue(r.nextInt(noise.sum));
                        stepLengthNoise = r.nextFloat()*0.8f;
                        p.moveXY((float)Math.cos(Math.toRadians(meanDirection+directionalNoise))*(stepLengthDots+stepLengthNoise), (float)Math.sin(Math.toRadians(meanDirection+directionalNoise))*(stepLengthDots+stepLengthNoise), cells, map);
                        if (!p.isActive) {
                            particle.remove(i);
                            particleCounterAlive --;
                            particleCounterDead ++;
                        }
                    }

                    drawParticles();
                    mapView.setBackgroundDrawable(new BitmapDrawable(bg));

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
//            lastDegreeView.setText(Integer.toString(lastDegrees[0])+", "+Integer.toString(lastDegrees[1]));

            int sum = 0;
            for (int i=0; i<lastDegrees.length; i++) {
                sum += lastDegrees[i];
            }
            meanDirection= sum/lastDegrees.length;
//            degreesMean.setText(Float.toString(mean));
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

    public void getStats() {
        int[] topCellsIndex = {0,0,0};
        int[] maxCellCount = {-1, -1, -1};

        for (int i=1; i<cells.length; i++) {
           if (cells[i].particleCellCounter > maxCellCount[0]) {
               maxCellCount[2] = maxCellCount[1];
               maxCellCount[1] = maxCellCount[0];
               maxCellCount[0] = cells[i].particleCellCounter;
               topCellsIndex[2] = topCellsIndex[1];
               topCellsIndex[1] = topCellsIndex[0];
               topCellsIndex[0] = i;
           }
        }
//        float[][] cellOrder = new float[20][4];
//        float[][] temp = new float[20][4];
//        int sum = 0;
//
//        for (int i=1; i<cells.length; i++) {
//            cellOrder[i][0] = i;
//            cellOrder[i][1] = cells[i].particleCellCounter;
//            cellOrder[i][2] = (float)cells[i].particleCellCounter/n;
//            cellOrder[i][3] = (float)cells[i].prob;
//            temp[i] = cellOrder[i];
//            sum += cellOrder[i][1];
//        }
//        for (int j=0; j<cells.length; j++) {
//            for (int i=0; i<cells.length-1; i++) {
//                if (cellOrder[i][1] < cellOrder[i + 1][1]) {
//                    temp[i] = cellOrder[i];
//                    cellOrder[i] = cellOrder[i + 1];
//                    cellOrder[i + 1] = temp[i];
//                }
//            }
//        }
//        cellStats1.setText("C"+cellOrder[0][0]+" "+cellOrder[0][1]+" "+cellOrder[0][2]+" "+cells[(int)cellOrder[0][0]].prob);
//        cellStats2.setText("C"+cellOrder[1][0]+" "+cellOrder[1][1]+" "+cellOrder[1][2]+" "+cells[(int)cellOrder[1][0]].prob);
//        cellStats3.setText("C"+cellOrder[2][0]+" "+cellOrder[2][1]+" "+cellOrder[2][2]+" "+cells[(int)cellOrder[2][0]].prob);
        cellStats1.setText("C"+cells[topCellsIndex[0]]+" "+cells[topCellsIndex[0]].particleCellCounter+" "+cells[topCellsIndex[0]].prob);
        cellStats1.setText("C"+cells[topCellsIndex[1]]+" "+cells[topCellsIndex[1]].particleCellCounter+" "+cells[topCellsIndex[1]].prob);
        cellStats1.setText("C"+cells[topCellsIndex[2]]+" "+cells[topCellsIndex[2]].particleCellCounter+" "+cells[topCellsIndex[2]].prob);


//        particleCounter.setText(Integer.toString(sum));
    }

}
