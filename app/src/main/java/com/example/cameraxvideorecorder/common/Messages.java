package com.example.cameraxvideorecorder.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class Messages {
    public static MutableLiveData<Boolean> onStartCameraDetection = new MutableLiveData<>() {
    };
    public static MutableLiveData<DetectedTargetsData> onTargetsDetected = new MutableLiveData<>() {
    };
    public static MutableLiveData<Config> onStartMission = new MutableLiveData<>() {
    };
}
