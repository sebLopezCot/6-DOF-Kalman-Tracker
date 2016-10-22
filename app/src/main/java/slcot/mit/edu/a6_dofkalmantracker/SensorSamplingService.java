package slcot.mit.edu.a6_dofkalmantracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import Jama.Matrix;
import slcot.mit.edu.a6_dofkalmantracker.log.MatrixCSVLogger;
import slcot.mit.edu.a6_dofkalmantracker.math.TwoDOFPlanarKalmanFilter;

public class SensorSamplingService extends Service implements SensorEventListener {

    // Enumeration types for sensor values that come in
    public static final int ACCELEROMETER_SENSOR_TYPE = 0;
    public static final int GYROSCOPE_SENSOR_TYPE = 1;

    public static final double HIGH_PASS_THRESHOLD = 0.2;

    // Nanosecond to second conversion
    public static final float NS2S = 1.0f / 1000000000.0f;


    IBinder mBinder;
    SensorManager sensorManager;
    Sensor accel, gyro;

    TwoDOFPlanarKalmanFilter kalman;
    public Matrix measurement;
    public double runningTime;
    long previousTimestamp;

    MatrixCSVLogger stateLog;
    MatrixCSVLogger measurementLog;

    public SensorSamplingService() {
    }

    public class LocalBinder extends Binder {
        public SensorSamplingService getSensorSamplingServiceInstance(){
            return SensorSamplingService.this;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        // Initialize localbinder
        mBinder = new LocalBinder();

        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_UI);

        previousTimestamp = -1;
        runningTime = 0;

        measurement = new Matrix(2, 1);
        //kalman = new TwoDOFPlanarKalmanFilter();

        //stateLog = new MatrixCSVLogger("state_log.csv");
        //measurementLog = new MatrixCSVLogger("measurement_log.csv");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        // Unregister sensor event listener
        sensorManager.unregisterListener(this);

        previousTimestamp = -1;
        measurement = null;
    }

    private int getSensorType(Sensor sensor){
        if(sensor == accel)
        {
            return ACCELEROMETER_SENSOR_TYPE;
        }
        else if(sensor == gyro)
        {
            return GYROSCOPE_SENSOR_TYPE;
        }
        else
        {
            return -1;
        }
    }

    private double highPassFilter(double input){
        if(Math.abs(input) > HIGH_PASS_THRESHOLD){
            return input;
        } else {
            return 0.0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(getSensorType(event.sensor)){
            case ACCELEROMETER_SENSOR_TYPE:

                if(previousTimestamp >= 0L){
                    double dt = (double)(event.timestamp - previousTimestamp) * NS2S;

                    // Kalman time update
                    //kalman.timeUpdate(dt);

                    // Set values and update Kalman filter
                    measurement.set(0, 0, highPassFilter(event.values[0]));
                    measurement.set(1, 0, highPassFilter(event.values[1]));

                    // Kalman measurement update
                    //kalman.measurementUpdate(measurement);

                    // Log the values
                    //measurementLog.log(measurement, event.timestamp);
                    //stateLog.log(kalman.getState(), event.timestamp);

                    // Update previous timestamp
                    previousTimestamp = event.timestamp;
                    runningTime += dt;
                } else {
                    previousTimestamp = event.timestamp;
                }

                break;

            case GYROSCOPE_SENSOR_TYPE:
                break;

            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
