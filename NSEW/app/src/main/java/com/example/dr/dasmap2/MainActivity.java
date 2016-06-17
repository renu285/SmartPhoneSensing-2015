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
    Button buttonRandomize, buttonSetHeight, buttonSetDegreeOffset;
    ImageView mapView;
    EditText heightView, degreeOffset;
    TextView cellStats1, cellStats2, cellStats3, particleCounter, particleCounter2, ageCounter, location;

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
    int right = 0, left = 180, down = 90, up = -90;
    int directionOffset = 0;

    //    Activity
    int stepCount;
    boolean lastPeak = true;
    boolean stepLimiter = true;

    float height = 180;
    String heightStr = "180";
    float stepLength = 0.35f*(height/100);
    float stepLengthDots = stepLength * 8;
    float stepLengthNoise = 0.0f;
    
    //    Particle related
    int n = 5000;
    ArrayList<Particle> particles = new ArrayList<Particle>(1);
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
    int freshLocation = 0;
    float freshLocationRatio = 0.1f;
    int maxAge = 0;
    int maxAgeCounter = 0;
    int[] maxAgeIndex = {-1, -1, -1, -1, -1};


    //    Drawing objects
    Bitmap bg;
    Canvas canvas;
    Paint paint;
    Map map;

    //    Utilities
    Random r;
    Timer timer;
    int timerCountDown = 60;


    

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
        particleCounter2 = (TextView) findViewById(R.id.particleCounter2);
        ageCounter = (TextView) findViewById(R.id.ageCounter);
        location = (TextView) findViewById(R.id.location);
        degreeOffset = (EditText) findViewById(R.id.degreeOffset);

        buttonRandomize = (Button) findViewById(R.id.buttonRandomize);
        buttonSetHeight = (Button) findViewById(R.id.buttonSetHeight);
        buttonSetDegreeOffset = (Button) findViewById(R.id.buttonSetDegreeOffset);

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

        r = new Random();
        initStuff();



        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stepLimiter) {
                            timerCountDown -= 1;
                            if (timerCountDown <= 0) {
                                stepLimiter = false;
                            }
                        }

                    }
                });
            }
        }, 0, 10);


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

        buttonSetDegreeOffset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directionOffset = Integer.parseInt(degreeOffset.getText().toString());
                up += directionOffset;
                down += directionOffset;
                if (down > 180) {
                    down -= 360;
                }
                left += (directionOffset-360);
                right += directionOffset;
            }
        });
    }



    public void initStuff() {
        cells = new Cell[20];
        map.initCells(cells);

        for (int i=0; i<n; i++) {
            particles.add(new Particle(0,0));
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

        randomizeParticles();
        map.drawMap(bg, canvas, mapView);
        drawParticles();

        mapView.setBackgroundDrawable(new BitmapDrawable(bg));
        stepCount = 0;
        activityStatus.setText(Integer.toString(stepCount));

    }

    public void randomizeParticles() {

        int counter = 0;
        for (int i=1; i<cells.length; i++) {
            cells[i].particleCellCounter = 0;
            for (int j = counter; j < (counter+((cells[i].prob)/2)); j++) {
                if (j<n) {
                    p = particles.get(j);
                    xMin = (int) cells[i].x1;
                    xMax = (int) cells[i].x2;
                    yMin = (int) cells[i].y1;
                    yMax = (int) cells[i].y2;
                    p.setPos(r.nextInt(xMax - xMin) + xMin, r.nextInt(yMax - yMin) + yMin);
                    p.cell = i;
                    cells[i].particleCellCounter ++;
                    p.setBoundaries(cells, i);
                }
            }
            counter += cells[i].prob/2;
        }
        getStats();

    }

    public void drawParticles() {
        paint.setColor(Color.parseColor("#ff0000"));
        particleCounter.setText("Alive: " + Integer.toString(particleCounterAlive) + "   Dead: " + Integer.toString(particleCounterDead));
        maxAge = 0;
        maxAgeCounter = 0;
        for (int i=0; i<maxAgeIndex.length; i++) {
            maxAgeIndex[i] = 0;
        }

        for (int i = 0; i < particles.size(); i++) {
            p = particles.get(i);
            pos = p.getPos();
            if (p.newLocation) {
                cells[p.cell].particleCellCounter --;
                cellInd = map.whichCell(cells, pos[0], pos[1]);
                p.cell = cellInd;
                cells[p.cell].particleCellCounter ++;
                p.setBoundaries(cells, cellInd);
                p.newLocation = false;
            }
            p.age ++;
            if (p.age > maxAge) {
                maxAgeCounter = 1;
                maxAge = p.age;
                maxAgeIndex[4] = maxAgeIndex[3];
                maxAgeIndex[3] = maxAgeIndex[2];
                maxAgeIndex[2] = maxAgeIndex[1];
                maxAgeIndex[1] = maxAgeIndex[0];
                maxAgeIndex[0] = i;
                canvas.drawPoint(pos[0], pos[1], paint);
            }
            else if (p.age == maxAge) {
                maxAgeCounter ++;
                maxAgeIndex[4] = maxAgeIndex[3];
                maxAgeIndex[3] = maxAgeIndex[2];
                maxAgeIndex[2] = maxAgeIndex[1];
                maxAgeIndex[1] = maxAgeIndex[0];
                maxAgeIndex[0] = i;
                canvas.drawPoint(pos[0], pos[1], paint);
            }
            else {
                canvas.drawPoint(pos[0], pos[1], paint);
            }
        }

        freshLocation = (int)(freshLocationRatio * (float)particleCounterDead);
        for (int i = 0; i<(particleCounterDead-freshLocation); i++) {

            p = particles.get(r.nextInt(particleCounterAlive));

            pos = p.getPos();
            pNew = new Particle(pos[0], pos[1]);
            pNew.setBoundaries(cells, p.cell);
            pNew.cell = p.cell;
            cells[p.cell].particleCellCounter ++;
            pNew.isActive = true;
            pNew.wallHit = false;
            pNew.newLocation = false;
            particles.add(pNew);
            canvas.drawPoint(pos[0], pos[1], paint);
        }

        for (int i=0; i<freshLocation; i++) {
            newCell = r.nextInt(10000)+1;
            pNew = new Particle(0,0);
            pNew.cell = map.isCell(cells, newCell);
            pNew.setBoundaries(cells, pNew.cell);
            xMin = (int) cells[pNew.cell].x1;
            xMax = (int) cells[pNew.cell].x2;
            yMin = (int) cells[pNew.cell].y1;
            yMax = (int) cells[pNew.cell].y2;
            pos[0] = r.nextInt(xMax - xMin) + xMin;
            pos[1] = r.nextInt(yMax - yMin) + yMin;
            pNew.setPos((int)pos[0], (int)pos[1]);
            cells[pNew.cell].particleCellCounter ++;
            particles.add(pNew);
            canvas.drawPoint(pos[0], pos[1], paint);
        }

        if (stepCount>0) {
            paint.setColor(Color.parseColor("#1DC400"));
            for (int i=0; i<maxAgeIndex.length; i++) {
                if (maxAgeIndex[i]>0) {
                    p = particles.get(maxAgeIndex[i]);
                    canvas.drawCircle(p.xPos, p.yPos, 3, paint);
                }
            }

        }

        particleCounterAlive = particles.size();
        particleCounterDead = 0;

        particleCounter2.setText("A: " + Integer.toString(particles.size()) + "  D: " + Integer.toString(particleCounterDead));
        ageCounter.setText("Max age: " + Integer.toString(maxAge) + "   Count: " + Integer.toString(maxAgeCounter));
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
                    timerCountDown = 60;

                    activityStatus.setText(Integer.toString(stepCount));
                    lastPeak = true;

                    for (int i=0; i<particles.size(); i++) {
                        p = particles.get(i);
                        directionalNoise = noise.getValue(r.nextInt(noise.sum));
                        stepLengthNoise = (r.nextFloat()*0.8f)-0.4f;
//                        p.moveXY(-(float)Math.cos(Math.toRadians(meanDirection+directionalNoise))*(stepLengthDots+stepLengthNoise), -(float)Math.sin(Math.toRadians(meanDirection+directionalNoise))*(stepLengthDots+stepLengthNoise), cells, map);

                        if( (meanDirection< (up+45)) && (meanDirection> (up-45)) ) {
                            p.moveXY(0.0f, -(stepLengthDots+stepLengthNoise), cells, map);
                        }
                        if( (meanDirection< (down+45)) && (meanDirection> (down-45)) ) {
                            p.moveXY(0.0f, (stepLengthDots+stepLengthNoise), cells, map);
                        }
                        if( (meanDirection< (left+45)) && (meanDirection> (left-45)) ) {
                            p.moveXY(-(stepLengthDots+stepLengthNoise), 0.0f, cells, map);
                        }
                        if( (meanDirection< (right+45)) && (meanDirection> (right-45)) ) {
                            p.moveXY((stepLengthDots+stepLengthNoise), 0.0f, cells, map);
                        }
                        if (!p.isActive) {
                            cells[p.cell].particleCellCounter --;
                            particles.remove(i);
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
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]))-60;

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
            meanDirection = sum/lastDegrees.length;
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
        boolean sorted;

        for (int i=1; i<cells.length; i++) {
            sorted = false;
            if( (cells[i].particleCellCounter > maxCellCount[0])) {
                maxCellCount[2] = maxCellCount[1];
                maxCellCount[1] = maxCellCount[0];
                maxCellCount[0] = cells[i].particleCellCounter;
                topCellsIndex[2] = topCellsIndex[1];
                topCellsIndex[1] = topCellsIndex[0];
                topCellsIndex[0] = i;
                sorted = true;
            }
            if( (cells[i].particleCellCounter > maxCellCount[1]) && !sorted) {
                maxCellCount[2] = maxCellCount[1];
                maxCellCount[1] = cells[i].particleCellCounter;
                topCellsIndex[2] = topCellsIndex[1];
                topCellsIndex[1] = i;
                sorted = true;
            }
            if( (cells[i].particleCellCounter > maxCellCount[2]) && !sorted) {
                maxCellCount[2] = cells[i].particleCellCounter;
                topCellsIndex[2] = i;
            }

//            if(stepCount > 5) {
//                p = particles.get(maxAgeIndex[0]);
//                if (p.cell == topCellsIndex[0]) {
//                    location.setText("Cell " + Integer.toString(p.cell));
//                }
//                else {
//                    location.setText("Not sure");
//                }
//            }


        }
        int maxAgePerCell = 0;
        if (stepCount > 12) {
            for (int i=0; i<maxAgeIndex.length; i++) {
                if (maxAgeIndex[i]>0) {
                    p = particles.get(maxAgeIndex[i]);
                    if (p.cell == topCellsIndex[0]) {
                        maxAgePerCell ++;
                    }
                }
            }
            if (maxAgePerCell>2) {
                location.setText("Cell " + Integer.toString(p.cell));
            }
            else location.setText("Not sure");
        }

        cellStats1.setText("C"+topCellsIndex[0]+": P "+Integer.toString(cells[topCellsIndex[0]].particleCellCounter)+"   "+Integer.toString(cells[topCellsIndex[0]].prob));
        cellStats2.setText("C"+topCellsIndex[1]+": P "+Integer.toString(cells[topCellsIndex[1]].particleCellCounter)+"   "+Integer.toString(cells[topCellsIndex[1]].prob));
        cellStats3.setText("C"+topCellsIndex[2]+": P "+Integer.toString(cells[topCellsIndex[2]].particleCellCounter)+"   "+Integer.toString(cells[topCellsIndex[2]].prob));


    }

}
