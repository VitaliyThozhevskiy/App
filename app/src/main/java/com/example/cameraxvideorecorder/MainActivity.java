package com.example.cameraxvideorecorder;

import static com.example.cameraxvideorecorder.Constants.LABELS_PATH;
import static com.example.cameraxvideorecorder.Constants.MODEL_PATH;
import static com.example.cameraxvideorecorder.Constants.TARGET_LABELS_PATH;
import static com.example.cameraxvideorecorder.Constants.TARGET_MODEL_PATH;
import static com.example.cameraxvideorecorder.common.Logcat.log;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.ImageAnalysis;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cameraxvideorecorder.common.FcCommon;
import com.example.cameraxvideorecorder.common.FcInfo;
import com.example.cameraxvideorecorder.infrastructure.DDService;
import com.example.cameraxvideorecorder.infrastructure.Serial;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private ImageAnalysis imageAnalyzer = null;
    private ProcessCameraProvider cameraProvider = null;
    private Detector detector;
    private Detector targetDetector;
    public static String versionName;
    private MainActivity activity;
    private Button bStartStopService;
    private LinearLayout main;
    Spinner connectionMode;
    EditText etIp, etPort, etKey;
    private TextView tvNetworkStatus, tvFcStatus, tvConnectionModeHint;
    CheckBox cbConnectOnStartup;
    private Timer uiTimer;
    private boolean isPaused = false;
    TextView result = null;
    ExecutorService service;
    Camera camera = null;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OpenCVLoader.initLocal();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detector = new Detector(getBaseContext(), MODEL_PATH, LABELS_PATH);
        targetDetector = new Detector(getBaseContext(), TARGET_MODEL_PATH, TARGET_LABELS_PATH);
        detector.setup();
        targetDetector.setup();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        activity = this;
        main = findViewById(R.id.llMain);
        main.setKeepScreenOn(true);
        bStartStopService = findViewById(R.id.startStopService);
        bStartStopService.setOnClickListener(v -> {
            if (checkPermissions()) startStopService();
        });
        etIp = findViewById(R.id.editText_ip);
        etPort = findViewById(R.id.editText_port);
        etKey = findViewById(R.id.editText_key);
        tvNetworkStatus = findViewById(R.id.tvNetworkConnectionStatus);
        tvFcStatus = findViewById(R.id.tvFcConnectionStatus);
        tvConnectionModeHint = findViewById(R.id.tvConnectionModeHint);
        connectionMode = findViewById(R.id.connectionMode);
        if (!DDService.isRunning){
            new Thread(() -> {
                try{
                    Thread.sleep(3000);
                    startStopService();
                }catch (Exception ignore){
                }
            }).start();
        }

        service = Executors.newSingleThreadExecutor();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    private void updateUi(){
        if (DDService.isRunning){
            bStartStopService.setText(getResources().getString(R.string.disconnect));
            if (DDService.isConnected){
                tvNetworkStatus.setText(getResources().getString(R.string.status_connected));
                tvNetworkStatus.setTextColor(Color.GREEN);
            }else{

                tvNetworkStatus.setText(getResources().getString(R.string.status_awaiting_connection));
                tvNetworkStatus.setTextColor(Color.BLACK);
            }
            int serialPortStatus = DDService.getSerialPortStatus();
            switch (serialPortStatus){
                case Serial.STATUS_NOT_INITIALIZED:
                case Serial.STATUS_DEVICE_NOT_CONNECTED:
                    tvFcStatus.setText(getResources().getString(R.string.status_disconnected));
                    tvFcStatus.setTextColor(Color.RED);
                    break;
                case Serial.STATUS_DEVICE_FOUND:
                    tvFcStatus.setText(getResources().getString(R.string.fc_status_device_found));
                    tvFcStatus.setTextColor(Color.BLUE);
                    break;
                case Serial.STATUS_USB_PERMISSION_REQUESTED:
                    tvFcStatus.setText(getResources().getString(R.string.fc_status_permission_requested));
                    tvFcStatus.setTextColor(Color.BLUE);
                    break;
                case Serial.STATUS_USB_PERMISSION_DENIED:
                    tvFcStatus.setText(getResources().getString(R.string.fc_status_permission_denied));
                    tvFcStatus.setTextColor(Color.RED);
                    break;
                case Serial.STATUS_USB_PERMISSION_GRANTED:
                    tvFcStatus.setText(getResources().getString(R.string.fc_status_permission_granted));
                    tvFcStatus.setTextColor(Color.BLUE);
                    break;
                case Serial.STATUS_SERIAL_PORT_ERROR:
                    tvFcStatus.setText(getResources().getString(R.string.fc_status_serial_port_error));
                    tvFcStatus.setTextColor(Color.RED);
                    break;
                case Serial.STATUS_SERIAL_PORT_OPENED:
                    tvFcStatus.setTextColor(Color.BLUE);
                    String status = getResources().getString(R.string.fc_status_serial_port_opened);
                    FcInfo fcInfo = DDService.getFcInfo();
                    if (fcInfo == null){
                        status += getResources().getString(R.string.check_fc_version);
                    }else{
                        tvFcStatus.setTextColor(Color.GREEN);
                        status += " " + fcInfo.getFcName() + " Ver. " + fcInfo.getFcVersionStr()
                                + " (" + fcInfo.getPlatformTypeName() + ") "
                                + getResources().getString(R.string.detected);
                        int fcApiCompatibilityLevel = DDService.getFcApiCompatibilityLevel();
                        switch (fcApiCompatibilityLevel){
                            case FcCommon.FC_API_COMPATIBILITY_UNKNOWN:
                            case FcCommon.FC_API_COMPATIBILITY_ERROR:
                                status += getResources().getString(R.string.fc_api_compatibility_error);
                                tvFcStatus.setTextColor(Color.RED);
                                break;
                            case FcCommon.FC_API_COMPATIBILITY_WARNING:
                                status += getResources().getString(R.string.fc_api_compatibility_warning);
                                tvFcStatus.setTextColor(Color.YELLOW);
                                break;
                        }
                    }
                    tvFcStatus.setText(status);
                    break;
            }
            connectionMode.setEnabled(false);
            etIp.setEnabled(false);
            etPort.setEnabled(false);
            etKey.setEnabled(false);
            cbConnectOnStartup.setEnabled(false);
        }else{
            bStartStopService.setText(getResources().getString(R.string.connect));
            tvNetworkStatus.setText(getResources().getString(R.string.status_disconnected));
            tvNetworkStatus.setTextColor(Color.RED);
            tvFcStatus.setText(getResources().getString(R.string.status_disconnected));
            tvFcStatus.setTextColor(Color.RED);
            connectionMode.setEnabled(true);
            etIp.setEnabled(true);
            etPort.setEnabled(true);
            etKey.setEnabled(true);
            cbConnectOnStartup.setEnabled(true);
        }
    }

    private void startStopService(){
        Intent intent = new Intent(getApplicationContext(), DDService.class);
        if (DDService.isRunning){
            stopService(intent);
        }else{
            if (!config.updateConfig()) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(intent);
            }else{
                getApplicationContext().startService(intent);
            }
        }
        updateUi();
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(MainActivity.this);

        processCameraProvider.addListener(() -> {
            try {
                cameraProvider = processCameraProvider.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                var rotation = Surface.ROTATION_0;
                var preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .build();

                imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetRotation(rotation)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalyzer.setAnalyzer(service, imageProxy -> {
                    try {
                        Bitmap bitmapBuffer = Bitmap.createBitmap(
                                imageProxy.getWidth(),
                                imageProxy.getHeight(),
                                Bitmap.Config.ARGB_8888
                        );
                        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
                        imageProxy.close();

                        Matrix matrix = new Matrix();
                        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                                bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(),
                                matrix, false
                        );

                        var targetBoxes = targetDetector.detect(rotatedBitmap);
                        var boxes = detector.detect(rotatedBitmap);
                        var allBoxes = Stream.concat(targetBoxes.stream(), boxes.stream()).toArray();
                    } catch(Throwable e){
                        setText(e.toString());
                    }
                });

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageAnalyzer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(MainActivity.this));
    }


    private boolean checkPermissions(){
        boolean cameraPermission = isCameraPermissionGranted();
        boolean storagePermission = isStoragePermissionGranted();
        return cameraPermission && storagePermission;
    }

    private boolean isCameraPermissionGranted(){
        if (this.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            activity.requestPermission(Manifest.permission.CAMERA, 0);
            return false;
        }
        return true;
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT <= 28) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        isPaused = false;
        main.setKeepScreenOn(true);
        updateUi();
        uiTimer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (isPaused){
                    uiTimer.cancel();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });
            }
        };
        uiTimer.schedule(tt, 10, 1000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        isPaused = true;
        if (uiTimer != null) {
            uiTimer.cancel();
            uiTimer.purge();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permissionName}, permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode >= 0 && requestCode <= 3 && checkPermissions()) startStopService();
        } else {
            if (requestCode == 0) log("Camera permission denied!");
            if (requestCode == 1) log("Storage permission denied!");
            if (requestCode == 2) log("Audio permission denied!");
            if (requestCode == 3) log("Phone state permission denied!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdown();
    }

    private void setText(String s){
        if(s != null && s != "")
        runOnUiThread(() -> {
            result.setText(s);
        });
    }
}