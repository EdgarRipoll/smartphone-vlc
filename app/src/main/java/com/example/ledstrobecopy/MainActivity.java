package com.example.ledstrobecopy;


import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity  {

    private static final String tagLogd = MainActivity.class.getSimpleName();
    private TextView frequencyProgressText;
    public int timeSleep_ms = 115;
    public boolean permissionFlag = true;
    private SeekBar slideBar;
    private SharedPreferences widget_Preferences;
    private ToggleButton buttonOnOff;
    private Camera cameraManager;
    private Parameters parametersCamara_ON;
    private Parameters parametersCamara_OFF;
    private classRunnableThread thread1;
    private Executor executorOfThread1;
    private List<String> listOfSupportedModes;

    public void initParametersCamera() {
        if (this.cameraManager == null) {
            try {
                this.cameraManager = Camera.open();
                this.parametersCamara_ON = this.cameraManager.getParameters();
                this.parametersCamara_OFF = this.cameraManager.getParameters();
                this.parametersCamara_ON.setFlashMode("torch");
                this.parametersCamara_OFF.setFlashMode("off");
                this.listOfSupportedModes = this.parametersCamara_ON.getSupportedFlashModes();
            } catch (Exception e) {
                Log.e(tagLogd, "Error to open camera: " + e.getMessage());
            }
            try {
                this.cameraManager.startPreview();
            } catch (Exception e2) {
                Log.e(tagLogd, "Error start camera preview: " + e2.getMessage());
                e2.printStackTrace();
            }
        }
        this.thread1.releaseRequested_CameraAttribute = false;
    }

    public void releaseCamera() {
        if (this.cameraManager != null) {
            try {
                this.cameraManager.stopPreview();
                this.cameraManager.setPreviewCallback(null);
            } catch (Exception e) {
                Log.e(tagLogd, "Error stop camera preview: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (this.cameraManager != null) {
            this.cameraManager.release();
            this.cameraManager = null;
        }
        this.thread1.releaseRequested_CameraAttribute = false;
    }

    public void setparametersCamara_ON() {
        this.cameraManager.setParameters(this.parametersCamara_ON);
    }

    public void setparametersCamara_OFF() {
        this.cameraManager.setParameters(this.parametersCamara_OFF);
    }

    public void checkIfSupported() {
        if (this.listOfSupportedModes == null) {
            //showMessageNotSupported_maybe();
            Log.e(tagLogd, "FLASH_MODE_TORCH not supported");
            //this.buttonOnOff.setClickable(false);
            this.permissionFlag = false;
        } else if (!this.listOfSupportedModes.contains("torch")) {
            //showMessageNotSupported_maybe();
            Log.e(tagLogd, "FLASH_MODE_TORCH not supported2");
            //this.buttonOnOff.setClickable(false);
            this.permissionFlag = false;
        }
    }

    @SuppressLint("WrongConstant")
    private void checkPermissionsCamera() {
        if (VERSION.SDK_INT < 23) {
            initParametersCamera();
            checkIfSupported();
        } else if (checkSelfPermission("android.permission.CAMERA") == 0) {
            initParametersCamera();
            checkIfSupported();
        } else {
            Log.d(tagLogd, "no permission");
            if (shouldShowRequestPermissionRationale("android.permission.CAMERA")) {
                Toast.makeText(this, "Camera permission is needed to turn on flashlight", 1).show();
            }
            requestPermissions(new String[]{"android.permission.CAMERA"}, 1);
        }
    }

    private class classRunnableThread extends Thread implements Runnable {

        volatile boolean onBlink_CameraAttribute;
        volatile boolean releaseRequested_CameraAttribute;
        final /* synthetic */ MainActivity threadCameraAttribute;

        private classRunnableThread(MainActivity mainActivity) {
            this.threadCameraAttribute = mainActivity;
            this.onBlink_CameraAttribute = false;
            this.releaseRequested_CameraAttribute = false;
        }

        public void run() {
            if (this.releaseRequested_CameraAttribute) {
                this.threadCameraAttribute.releaseCamera();
                return;
            }
            while (this.onBlink_CameraAttribute && this.threadCameraAttribute.cameraManager != null) {
                this.threadCameraAttribute.setparametersCamara_OFF();
                try {
                    Thread.sleep((long) 1/*this.threadCameraAttribute.timeSleep_ms*/, 10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.threadCameraAttribute.setparametersCamara_ON();
                /*if (!this.onBlink_CameraAttribute) {
                    this.threadCameraAttribute.setparametersCamara_OFF();
                }*/
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
        this.thread1.onBlink_CameraAttribute = false;
        this.thread1.releaseRequested_CameraAttribute = true;
        this.executorOfThread1.execute(this.thread1);
        //this.buttonOnOff.setChecked(false);
    }

    @SuppressLint("WrongConstant")
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i != 1) {
            super.onRequestPermissionsResult(i, strArr, iArr);
        } else if (iArr.length <= 0 || iArr[0] != 0) {
            Log.e(tagLogd, "No Camera permission");
            //this.buttonOnOff.setClickable(false);
            this.permissionFlag = false;
            Toast.makeText(this, "Camera permission was not granted. Application will not work properly", 1).show();
        } else {
            initParametersCamera();
            checkIfSupported();
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStart() {
        super.onStart();
        checkPermissionsCamera();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.executorOfThread1 = Executors.newSingleThreadExecutor();
        this.thread1 = new classRunnableThread(this);
        thread1.setPriority(Thread.MAX_PRIORITY);
        int progress = 90;//this.slideBar.getProgress() + 1;
        this.timeSleep_ms = Math.round((float) (1000 / progress)) - 10;
        if (this.cameraManager != null) {
            this.cameraManager.release();
            this.cameraManager = null;
        }
        this.thread1.onBlink_CameraAttribute = true;
        onStart();
        onResume();
        //this.executorOfThread1.execute(this.thread1);
        this.thread1.run();
    }
}