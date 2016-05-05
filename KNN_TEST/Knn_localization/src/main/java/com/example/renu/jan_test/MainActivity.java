package com.example.renu.jan_test;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public abstract class MainActivity extends AppCompatActivity implements SensorEventListener {
public class MainActivity extends AppCompatActivity implements SensorEventListener {


    Map<Integer, Integer> roomMap = new HashMap<>();
    Map<Integer, Double> distanceMap = new HashMap<>();
    Map<Integer, Integer> numberMap = new HashMap<>();
    Button buttonMeasure, buttonDetermine;
    ProgressBar pb;


    TextView VerboseText;
    TextView Cell;
    int InstanceCounter = 1;
    int counter1 = 0;
    int counter2 = 0;
    int roomCounter = 0;
    double sigma = 0;
    double mean = 0;
    int MeanIncrement = 0;
    double mahaDist = 0;
    int eucDist = 0;
    //private double eucDist = 0;
    int Cell_1Value = 0;
    int Cell_2Value = 0;
    ArrayList<Double> xArr, yArr, zArr;


    SensorManager senSensorManager;
    Sensor senAccelerometer;


    File RawValues = new File(Environment.getExternalStorageDirectory(),
            "Localization/RawValues");  // see if the required folders already exist. >>
    File APData = new File(Environment.getExternalStorageDirectory(), "Localization/APData");

    File Acc_Data = new File(Environment.getExternalStorageDirectory(),
            "Localization/ACCData/" + ".txt");


    WifiManager wifi;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VerboseText = (TextView) findViewById(R.id.viewText);
        Cell = (TextView) findViewById(R.id.CellText);
        buttonMeasure = (Button) findViewById(R.id.button);
        buttonDetermine = (Button) findViewById(R.id.button2);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        /* Initialize the wifiManager */
        init_wifi();

        buttonMeasure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                initiate_measurements();
            }
        });

        buttonDetermine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VerboseText.setText(VerboseText.getText() + "Starting measurements\n");
                find_location();
            }
        });


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //Sensor Management
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, 1000);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    void init_wifi() {
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //wifiReceiver = new WifiReceiver();
        // registerReceiver(wifiReceiver, new
        //        IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }


    void initiate_measurements() {

        if (!RawValues.exists()) {
            RawValues.mkdirs();
        }
        if (!APData.exists()) {
            APData.mkdirs();
        }
        File TrainedDataValues = new File(RawValues, "MAC" +
                String.format("%05d", InstanceCounter) + ".txt");
        InstanceCounter = 0;
        while (TrainedDataValues.exists())                                                                                                               // Check if it already exist
        {                                                                                                                                                   // if yes,
            InstanceCounter++;                                                                                                                                     // increase the InstanceCounter
            TrainedDataValues = new File(RawValues, "MAC" +
                    String.format("%05d", InstanceCounter) + ".txt");                                                    // and try again
        }
        FileWriter Writer;                                                                                                                            // write room number to this file >>
        try {
            Writer = new FileWriter(TrainedDataValues, true);
            Writer.append(Cell.getText() + "\n");
            VerboseText.setText("\n\tCell value : " + Cell.getText());
            Writer.flush();
            Writer.close();

            VerboseText.setText(VerboseText.getText()
                    + "\n\tAdding measurement: " + InstanceCounter);
            // wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);                                                                           // Scan the ether and list results...
            wifi.startScan();

            List<ScanResult> scanResult = wifi.getScanResults();                                                                                     // <<
            VerboseText.setText(VerboseText.getText() + "\n\t"
                    + scanResult.size() + "  Stations in vicinity");

            for (int i = 0; i < scanResult.size(); i++) {                                                                                                              // Do for every AP measured.
                try {
                    File APDataFle = new File(APData, scanResult.get(i).BSSID.replaceAll(":", "") + ".txt");                                                           // create new file for this AP (if it doesn't exist yet) >>
                    FileWriter apWriter;
                    if (!APDataFle.exists()) {
                        apWriter = new FileWriter(APDataFle);
                    } else {
                        apWriter = new FileWriter(APDataFle, true);
                    }                                                                                                                                               // <<
                    apWriter.append(Integer.toString(scanResult.get(i).level) + "\n");                                                                              // and write the strength to it.
                    apWriter.flush();
                    apWriter.close();                                                                                                                            // flushed and closed.


                    Writer = new FileWriter(TrainedDataValues, true);                                                                                         // now, only append
                    Writer.append(scanResult.get(i).BSSID.replaceAll(":", "") + " ");                                                                        // the MAC of the AP and a space
                    Writer.append(Integer.toString(scanResult.get(i).level) + "\n");                                                                          // and the level and a string
                    Writer.flush();
                    Writer.close();
                    VerboseText.setText(VerboseText.getText() + "\n\t Recoreded");
                } catch (IOException e) {
                    e.printStackTrace();
                    VerboseText.setText(e.getMessage().toString());
                }
            }
        } catch (IOException e) {
            VerboseText.setText(e.getMessage().toString());
        }


    }

    double CalculateDistane(int Pa, int Pb) {

        /* calculate Euceldian distance */
        return Math.sqrt((Pa - Pb) * (Pa - Pb));
    }

    void knn_classifier() {


        int chosenOne = 0;
        Cell_1Value = 0;
        Cell_2Value = 0;
        double minVal;
        double lowestminVal = 0;
        for (int k = 0; k < 5; k++) {
            minVal = 99999;
            for (int j : distanceMap.keySet()) {
                if ((distanceMap.get(j) < minVal) && (distanceMap.get(j) > lowestminVal)) {
                    minVal = distanceMap.get(j);
                    chosenOne = j;
                }
            }
            if (roomMap.get(chosenOne) == 1) {
                Cell_1Value++;
            } else if (roomMap.get(chosenOne) == 2) {
                Cell_2Value++;
            }
            VerboseText.setText(VerboseText.getText() + "Closest point "
                    + chosenOne + ": " + minVal + "\n\t");
            lowestminVal = minVal;
        }
    }


    void find_location() {

        pb.setVisibility(ProgressBar.VISIBLE);

        VerboseText.setText("");
        FileReader reader;
        distanceMap.clear();
        roomMap.clear();
        numberMap.clear();


        BufferedReader inReader;
        wifi.startScan();
        List<ScanResult> scanResult = wifi.getScanResults();

        for (int i = 0; i < scanResult.size(); i++) {  // <<
            File BSSID_Data = new File(Environment.getExternalStorageDirectory(),
                    "Localization/APData/" + scanResult.get(i).BSSID.replaceAll(":", "") + ".txt");
            if (BSSID_Data.exists()) {
                try {
                    reader = new FileReader(BSSID_Data);    // open the file
                    inReader = new BufferedReader(reader);
                    sigma = 0;
                    mean = 0;
                    MeanIncrement = 0;
                    String str;
                    while ((str = inReader.readLine()) != null) {
                        mean = mean + Double.parseDouble(str);
                        MeanIncrement++;
                    }
                    mean = mean / ((double) MeanIncrement);
                    reader = new FileReader(BSSID_Data);    // open the file
                    inReader = new BufferedReader(reader);
                    while ((str = inReader.readLine()) != null) {
                        sigma = sigma + Math.pow((Double.parseDouble(str) - mean), 2);
                    }
                    sigma = sigma / ((double) MeanIncrement); // found the standard deviation squared!!
                    InstanceCounter = 1;
                    File MacValuesfile = new File(Environment.getExternalStorageDirectory(),
                            "Localization/RawValues/MAC" +
                                    String.format("%05d", InstanceCounter) + ".txt");

                    while (MacValuesfile.exists()) {
                        try {
                            reader = new FileReader(MacValuesfile);
                            inReader = new BufferedReader(reader);
                            String str2;
                            String[] tokens;
                            roomCounter = Integer.parseInt(inReader.readLine());
                            while ((str2 = inReader.readLine()) != null) {
                                tokens = str2.split(" ");
                                if (tokens[0].equals(scanResult.get(i).BSSID.replaceAll(":", ""))) {
                                    eucDist = Integer.parseInt(tokens[1]) - scanResult.get(i).level;

                                    //eucDist = CalculateDistane(Integer.parseInt(tokens[1]) *-1,scanResult.get(i).level * -1);

                                    mahaDist = Math.pow((double) eucDist, 2) / sigma;
                                    if (!distanceMap.containsKey(InstanceCounter)) {
                                        distanceMap.put(InstanceCounter, mahaDist);
                                        roomMap.put(InstanceCounter, roomCounter);
                                        numberMap.put(InstanceCounter, 1);

                                    } else {
                                        mahaDist = mahaDist + distanceMap.get(InstanceCounter);
                                        distanceMap.put(InstanceCounter, mahaDist);
                                        numberMap.put(InstanceCounter, numberMap.get(InstanceCounter) + 1);
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        InstanceCounter++;
                        MacValuesfile = new File(Environment.getExternalStorageDirectory(),
                                "Localization/RawValues/MAC" + String.format("%05d", InstanceCounter) + ".txt");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
            }


            double intermediateDist;
            for (int j : distanceMap.keySet()) {

                intermediateDist = distanceMap.get(j);
                intermediateDist = Math.sqrt(intermediateDist / ((double) numberMap.get(j)) * (double) scanResult.size());
                distanceMap.put(j, intermediateDist);
            }

            knn_classifier();

        }

        if (Cell_1Value > Cell_2Value) {
            Toast.makeText(getBaseContext(), "Location : Cell-1", Toast.LENGTH_SHORT).show();
            VerboseText.setText(VerboseText.getText() + "Cell1\n\t");
            counter1++;
            // textCounter1.setText(Integer.toString(counter1));
        } else if (Cell_2Value > Cell_1Value) {
            Toast.makeText(getBaseContext(), "Location : Cell-2", Toast.LENGTH_SHORT).show();
            VerboseText.setText(VerboseText.getText() + "Cell2\n\t");
            counter2++;
            //textCounter2.setText(Integer.toString(counter2));
        } else {
            Toast.makeText(getBaseContext(), "Could not find location", Toast.LENGTH_SHORT).show();
        }

        pb.setVisibility(ProgressBar.INVISIBLE);


    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.renu.jan_test/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.renu.jan_test/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}



    class WifiReceiver extends BroadcastReceiver {
        // An access point scan has completed and results are sent here
        public void onReceive(Context c, Intent intent) {


        }

    }




