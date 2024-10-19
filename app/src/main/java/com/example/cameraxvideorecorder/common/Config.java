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

package com.example.cameraxvideorecorder.common;

import com.example.cameraxvideorecorder.MainActivity;

public class Config implements ISerializable{
    public final MainActivity activity;
    public float azimuth = 180;
    public float distance = 100;
    public float flyHeight = 100;
    public float flySpeed = 75;
    public int serialBaudRate = 115200;
    public int usbSerialPortIndex = 0;
    public boolean useNativeSerialPort = false;
    public String nativeSerialPort = "/dev/ttyS0";

    public Config(MainActivity activity) {
        this.activity = activity;
    }

    public void updateConfig(){
        azimuth = Float.parseFloat(activity.etAzimuth.getText().toString());
        distance = Float.parseFloat(activity.etDistance.getText().toString());
        flyHeight = Float.parseFloat(activity.etFlyHeight.getText().toString());
        flySpeed = Float.parseFloat(activity.etFlySpeed.getText().toString());
    }
}