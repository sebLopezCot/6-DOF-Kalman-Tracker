package slcot.mit.edu.a6_dofkalmantracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.ArrayList;

import Jama.Matrix;
import slcot.mit.edu.a6_dofkalmantracker.socketio.TrackerServer;

public class MainActivity extends AppCompatActivity {

    TrackerServer trackerServer;
    ServiceConnection mConnection;
    SensorSamplingService sensorSamplingService;
    SocketIOThread siothread;

    boolean isBoundToService;
    boolean isMeasuring;

    public class SocketIOThread extends Thread {
        @Override
        public void run(){
            while(isBoundToService){
                try {
                    if(trackerServer == null){
                        trackerServer = TrackerServer.getInstance();
                    }

                    if(sensorSamplingService != null && sensorSamplingService.measurement != null && sensorSamplingService.kinematics != null && sensorSamplingService.kinematics.size() > 1){
                        // send data
//                        trackerServer.send("T: "+ String.format("%6.2f", sensorSamplingService.runningTime)
//                                        + ", X: " + String.format("%,6.4f", sensorSamplingService.measurement.get(0,0))
//                                        + ", Y: " + String.format("%,6.4f", sensorSamplingService.measurement.get(1,0)) );
                        String[] labels = { "X", "Y", "Vx", "Vy", "Ax", "Ay" };
                        String output = "";
                        Matrix values = sensorSamplingService.kinematics.get(0);
                        output += "T: " + String.format("%6.2f", sensorSamplingService.runningTime);
                        for (int i=0; i < values.getRowDimension(); i++){
                            output += ", " + labels[i] + ": " + String.format("%,6.4f", values.get(i, 0));
                        }

                        trackerServer.send(output);
                    }

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMeasuring(isChecked);
            }
        });

        isBoundToService = false;

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                isBoundToService = true;
                SensorSamplingService.LocalBinder mLocalBinder = (SensorSamplingService.LocalBinder) service;
                sensorSamplingService = mLocalBinder.getSensorSamplingServiceInstance();

                siothread = new SocketIOThread();
                siothread.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBoundToService = false;
                sensorSamplingService = null;
            }
        };
    }

    public void setMeasuring(boolean state){
        if(state){
            Intent serviceIntent = new Intent(getApplicationContext(), SensorSamplingService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        } else {
            // Safely unbind
            if (isBoundToService) {
                unbindService(mConnection);
                isBoundToService = false;
            }

            if(trackerServer != null){
                trackerServer.disconnect();
            }
        }
    }

}