package de.rub.rus.inertialnavi;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SurfaceView surfaceView;
    private MainSurfaceView mainSurfaceView;

    private SensorManager mySensorManager;
    private Sensor myAccSensor, myGyroSensor;

    private TextView txtAx, txtAy, txtAz;
    private TextView txtAccStatus, txtGyroStatus;
    private CheckBox checkBoxTransform;

    private long startTime = 0, endTime = 0; // Hilfsgroessen start und endzeit in systemzeit
    private double T = 0; // Taktzeit

    private double[] w_b_ib = new double[3]; // omega_b_ib, drehrate gemessen mit Gyro

    private double[] a_b_ib = new double[3]; // gemessene Beschl im body KS durch Accelerometer
    private double[] a_r = new double[3]; // Beschleunigung im referenz-  KS

    private double[] C_k = new double[9]; // Richtungskosinusmatrix (DCM, RKM)

    private double[] sumGyro = new double[3]; // Hilfsgroesse bei der Berechnung des Gyro drift
    private double[] driftGyro = new double[3]; // Drift des Gyro

    private boolean corrGyroDrift = false; // Drift korrigieren ja/nein
    private int k; // Hilfsgroesse bei der Berechnung des Gyro drift


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Plot auf surface view erstellen
        surfaceView = (SurfaceView)findViewById(R.id.idSurfaceView);
        mainSurfaceView = new MainSurfaceView(this, surfaceView);

        // define sensor
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myAccSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myGyroSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // define some textViews
        txtAx = (TextView) findViewById(R.id.txtAx);
        txtAy = (TextView) findViewById(R.id.txtAy);
        txtAz = (TextView) findViewById(R.id.txtAz);
        txtAccStatus = (TextView) findViewById(R.id.AccStatus);
        txtGyroStatus = (TextView) findViewById(R.id.GyroStatus);

        // get checkbox
        checkBoxTransform = (CheckBox)findViewById(R.id.checkBoxTransform);
        checkBoxTransform.setChecked(true);

        // check if accelerometer and Gyroscope are available
        checkSensor(Sensor.TYPE_ACCELEROMETER,txtAccStatus);
        checkSensor(Sensor.TYPE_GYROSCOPE,txtGyroStatus);
    }

    /**
     * Funktion prueft Vorhandensein eines Sensors
     * @param sensorType zu pruefender Sensor
     * @param infoText Text View zur Ausgabe
     */
    private void checkSensor(int sensorType, TextView infoText){
        if (hasSensor(sensorType)){
            // sensor vorhanden
            infoText.setText("TRUE!");
        } else{
            // sensor nicht vorhanden
            infoText.setText("FALSE!");
        }
    }

    /**
     * Hilfsfunktion von checkSensor
     * @param sensorType
     * @return true if there exists a default sensor of the specified type
     */
    private boolean hasSensor(int sensorType){
        return (mySensorManager.getDefaultSensor(sensorType) != null);
    }

    /**
     * Kalibrieren des Gyro Sensors und Ruecksetzen von Variablen
     * @param view
     */
    public void btnCalibrate(View view) {
        Toast.makeText(getApplicationContext(),"Kalibriere. Nicht bewegen!",Toast.LENGTH_SHORT).show();
        corrGyroDrift = true;
        k= 0;

        // reset gyro drift
        driftGyro = zeroVector(driftGyro);
        sumGyro = zeroVector(sumGyro);

        // reset dcm matrix
        C_k = Navigation.initDCM();
    }


    /**
     * Berechnen des Gyro Drift falls globale Variable corrGyroDrift true
     * @param w_b_ib drehraten aus Gyro
     */
    public void getGyroDrift(double[] w_b_ib){
        // collect data and calculate mean drift
        if (corrGyroDrift == true && k < 200) {
            // collect gyro values
            sumGyro[0] += w_b_ib[0];
            sumGyro[1] += w_b_ib[1];
            sumGyro[2] += w_b_ib[2];

            k = k+1;
        } else {
            // 200 gyro values collected, calculate mean
            if (k >= 200) {
                corrGyroDrift = false;
                k = 0;

                // save mean gyro drift
                driftGyro[0] = sumGyro[0] / 200.0;
                driftGyro[1] = sumGyro[1] / 200.0;
                driftGyro[2] = sumGyro[2] / 200.0;

                C_k = Navigation.initDCM();

                // zeige Textnachricht
                Toast.makeText(getApplicationContext(), "Drift berechnet", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     *  fuellen eines Vektors mit Nullen
     * @param vector
     * @return
     */
    public double[] zeroVector (double[] vector){
        for (int i= 0; i<vector.length; i++) {
            vector[i] = 0;
        }
        return vector;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //register sensor listener
        mySensorManager.registerListener(this, myAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myGyroSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //do something with accelerometer data
            a_b_ib[0] = event.values[0];
            a_b_ib[1] = event.values[1];
            a_b_ib[2] = event.values[2];

            /*
             Drehen des f_b_ib vektors in das i KS
             Matrix multiplication sigma*f_b_ib
             Nur wenn die Checkbox aktiviert ist!
              */
            if (checkBoxTransform.isChecked()) {
                a_r = Navigation.rotateVectorDCM(C_k,a_b_ib);
            }else{
                a_r = a_b_ib;
            }

            // Ausgabe in TextView
            txtAx.setText(String.format("%.02f", a_r[0]));
            txtAy.setText(String.format("%.02f", a_r[1]));
            txtAz.setText(String.format("%.02f", a_r[2]));
            // Ausgabe als Graph
            mainSurfaceView.drawBarPlot((float)a_r[0], (float)a_r[1], (float)a_r[2]);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // do something with gyro data
            //compensate measured data
            w_b_ib[0] = event.values[0] - driftGyro[0];
            w_b_ib[1] = event.values[1] - driftGyro[1];
            w_b_ib[2] = event.values[2] - driftGyro[2];

            // compensate drift if button is pressed
            getGyroDrift(w_b_ib);

            // Messen der Zeit zwischen zwei sensor updates
            endTime = System.currentTimeMillis();
            T = (endTime - startTime) / 1000.0; //berechnen der Taktzeit in sek!
            startTime = endTime;

            //Update der Richtungskosinusmatrix
            C_k = Navigation.updateDCM(C_k,w_b_ib,T);

        }//gyro end
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

