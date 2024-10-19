/*
 *  This file is part of DroidDrone.
 *
 *  DroidDrone is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DroidDrone is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DroidDrone.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.cameraxvideorecorder.infrastructure;

import static com.example.cameraxvideorecorder.common.Logcat.log;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.cameraxvideorecorder.MainActivity;
import com.example.cameraxvideorecorder.R;
import com.example.cameraxvideorecorder.common.FcCommon;
import com.example.cameraxvideorecorder.common.FcInfo;
import com.example.cameraxvideorecorder.common.Messages;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Timer;
import java.util.TimerTask;

public class AutopilotService extends Service {
    public static final String CHANNEL_ID = "AutopilotServiceChannel";
    public static boolean isRunning = false;
    public static boolean isConnected = false;
    private static int serialPortStatus = Serial.STATUS_NOT_INITIALIZED;
    private static FcInfo fcInfo = null;
    private static int fcApiCompatibilityLevel = FcCommon.FC_API_COMPATIBILITY_UNKNOWN;
    private Timer mainTimer;
    private Serial serial;
    private Msp msp;

    public AutopilotService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isConnected = false;

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Autopilot is running")
                .setSmallIcon(R.drawable.baseline_rocket_launch_24)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 30) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                    | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    | ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        }else{
            startForeground(1, notification);
        }

        this.getExternalMediaDirs();
        if (serial != null) serial.close();
        serial = new Serial(this, MainActivity.config);
        msp = new Msp(serial);
        serial.initialize(msp);
        isRunning = true;
        startMainTimer();
        Messages.onTargetsDetected.observeForever(value -> {
            try {
                msp.sendDetectedTargets(value);
            } catch (JsonProcessingException e) {
                log(e.toString());
            }
        });
        Messages.onStartMission.observeForever(value -> {
            try {
                msp.setMissionConfig(value);
            } catch (JsonProcessingException e) {
                log(e.toString());
            }
        });
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        closeAll();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Autopilot", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static int getSerialPortStatus(){
        return serialPortStatus;
    }

    public static FcInfo getFcInfo(){
        return fcInfo;
    }

    public static int getFcApiCompatibilityLevel(){
        return fcApiCompatibilityLevel;
    }

    private void startMainTimer() {
        if (mainTimer != null) {
            mainTimer.cancel();
            mainTimer.purge();
        }
        mainTimer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if (!isRunning){
                    mainTimer.cancel();
                    return;
                }
                serialPortStatus = serial.getStatus();
                if (serialPortStatus == Serial.STATUS_SERIAL_PORT_ERROR){
                    if (serial != null && msp != null) {
                        fcInfo = null;
                        serial.initialize(msp);
                    }
                }
                if (serial != null && fcInfo == null) {
                    if (msp != null) fcInfo = msp.getFcInfo();
                    fcApiCompatibilityLevel = FcCommon.getFcApiCompatibilityLevel(fcInfo);
                }
            }
        };
        mainTimer.schedule(tt, 1000, 1000);
    }

    private void closeAll() {
        isRunning = false;
        try {
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer.purge();
            }
        }catch (Exception e){
            //
        }
        if (serial != null) serial.close();
        isConnected = false;
        fcInfo = null;
        System.gc();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}