package com.example.renu.new_step_counter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView StepCounter;
    Button Reset;
    SensorManager sm;
    int Count = 0;
    private static final int INACTIVE_PERIODS = 32;
    double prevValue = 0.0f;
    final double alpha = 0.1;


    private int mInactiveCounter = 0;
    public boolean isActiveCounter = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StepCounter = (TextView) findViewById(R.id.stepCounter_textView);
        Reset = (Button) findViewById(R.id.rst);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(MainActivity.this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StepCounter.setText("Steps : 0");
                Count = 0;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            double res = Math.sqrt(z*z);

            /*
            res  = alpha * res + (1 - alpha) * prevValue;
            prevValue = res;
            */

            Log.d("Res :" , Double.toString(res));

            if (mInactiveCounter == INACTIVE_PERIODS) {
                mInactiveCounter = 0;
                if (!isActiveCounter)
                    isActiveCounter = true;
            }


            if (res >= 11.25f && res < 12.25f) {
                //Log.d("Accelerometer theshold", Double.toString(mAccelValues[i]));
                if (isActiveCounter) {
                    mInactiveCounter = 0;
                    isActiveCounter = false;
                    Count++;
                    StepCounter.setText("Steps : " + Integer.toString(Count));
                }
            }

            ++mInactiveCounter;


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
