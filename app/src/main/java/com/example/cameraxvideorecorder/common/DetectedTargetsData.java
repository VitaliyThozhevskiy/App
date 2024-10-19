package com.example.cameraxvideorecorder.common;

import com.example.cameraxvideorecorder.BoundingBox;

import java.util.Collection;

public class DetectedTargetsData implements ISerializable {
    public BoundingBox[] boundingBoxes;
    public float width;
    public float heigth;
}
