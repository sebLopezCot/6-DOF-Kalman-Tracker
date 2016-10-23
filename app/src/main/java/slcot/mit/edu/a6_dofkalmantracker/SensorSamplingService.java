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

import java.util.ArrayList;

import Jama.Matrix;
import slcot.mit.edu.a6_dofkalmantracker.log.MatrixCSVLogger;
import slcot.mit.edu.a6_dofkalmantracker.math.TwoDOFPlanarKalmanFilter;

public class SensorSamplingService extends Service implements SensorEventListener {

    // Enumeration types for sensor values that come in
    public static final int ACCELEROMETER_SENSOR_TYPE = 0;
    public static final int GYROSCOPE_SENSOR_TYPE = 1;

    public static final double HIGH_PASS_THRESHOLD = 0.5;

    // Nanosecond to second conversion
    public static final float NS2S = 1.0f / 1000000000.0f;

    public ArrayList<Matrix> kinematics;

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

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);

        previousTimestamp = -1;
        runningTime = 0;

        kinematics = new ArrayList<Matrix>();
        for (int i=0; i < 2; i++)
            kinematics.add(new Matrix(6, 1));
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
                    measurement.set(0, 0, highPassFilter(event.values[0])); // accel x
                    measurement.set(1, 0, highPassFilter(event.values[1])); // accel y

                    // Double integrate for the lulz
                    // Move the kinematics queue forward one timestep
                    kinematics.remove(0);
                    kinematics.add(new Matrix(6, 1));

                    Matrix oneStepBack = kinematics.get(0);
                    Matrix currentKinematics = kinematics.get(1);

                    // set current accelerations
                    currentKinematics.set(4,0, highPassFilter(event.values[0])); // accel x
                    currentKinematics.set(5,0, highPassFilter(event.values[1])); // accel y

                    // calculate current velocities
                    currentKinematics.set(2,0, 0.5 * (currentKinematics.get(4,0) + oneStepBack.get(4,0)) * dt + oneStepBack.get(2,0)); // vel x
                    currentKinematics.set(3,0, 0.5 * (currentKinematics.get(5,0) + oneStepBack.get(5,0)) * dt + oneStepBack.get(3,0)); // vel y

                    // calculate current positions
                    currentKinematics.set(0,0, 0.5 * (currentKinematics.get(2,0) + oneStepBack.get(2,0)) * dt + oneStepBack.get(0,0)); // pos x
                    currentKinematics.set(1,0, 0.5 * (currentKinematics.get(3,0) + oneStepBack.get(3,0)) * dt + oneStepBack.get(1,0)); // pos x

                    // Kalman measurement update
                    //kalman.measurementUpdate(measurement);

                    // Log the values
                    //measurementLog.log(measurement, event.timestamp);
                    //stateLog.log(kalman.getState(), event.timestamp);

                    // Update previous timestamp
                    previousTimestamp = event.timestamp;
                    runningTime += dt;
                } else {
                    Matrix x0 = new Matrix(6, 1);
                    x0.set(4,0, highPassFilter(event.values[0]));
                    x0.set(5,0, highPassFilter(event.values[1]));
                    kinematics.remove(1);
                    kinematics.add(x0);

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
