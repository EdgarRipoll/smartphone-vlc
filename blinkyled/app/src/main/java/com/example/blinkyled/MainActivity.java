package com.example.blinkyled;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] camerasIds;
        int wait=100000 , wait2=50;
        long startTime, estimatedTime;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            camerasIds = camManager.getCameraIdList();
            camManager.setTorchMode(camerasIds[0], true);

            for (int blinks=0;blinks<1;++blinks) {
                startTime = SystemClock.elapsedRealtime();
                camManager.setTorchMode(camerasIds[0], false);

                camManager.setTorchMode(camerasIds[0], true);

                camManager.setTorchMode(camerasIds[0], false);

                camManager.setTorchMode(camerasIds[0], true);

                camManager.setTorchMode(camerasIds[0], false);

                camManager.setTorchMode(camerasIds[0], true);


            estimatedTime = SystemClock.elapsedRealtime();
            estimatedTime -= startTime;
            Log.d("Time", "Tiempo de parpadeo " + Long.toString(estimatedTime));
            }
            camManager.setTorchMode(camerasIds[0], false);
        } catch (CameraAccessException /*| InterruptedException*/ e) {
            e.printStackTrace();
        }


    }
}
