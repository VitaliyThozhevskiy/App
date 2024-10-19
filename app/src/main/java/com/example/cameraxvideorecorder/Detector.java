package com.example.cameraxvideorecorder;

import android.content.Context;
import android.graphics.Bitmap;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Detector {
    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final float CONFIDENCE_THRESHOLD = 0.3F;
    private static final float IOU_THRESHOLD = 0.5F;

    private final Context context;
    private final String modelPath;
    private final String labelPath;

    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();

    public int tensorWidth = 0;
    public int tensorHeight = 0;
    private int numChannel = 0;
    private int numElements = 0;

    Map<Integer, BoundingBox> trackingObjects = new HashMap<>();
    int trackId = 0;
    private final ImageProcessor imageProcessor;

    public Detector(Context context,
                    String modelPath,
                    String labelPath) {
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.imageProcessor = new ImageProcessor.Builder()
                .add(new NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
                .add(new CastOp(INPUT_IMAGE_TYPE))
                .build();
    }

    public void setup() {
        try {
            var model = FileUtil.loadMappedFile(context, modelPath);
            var options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(model, options);

            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();

            tensorWidth = inputShape[1];
            tensorHeight = inputShape[2];
            numChannel = outputShape[1];
            numElements = outputShape[2];

            try (InputStream inputStream = context.getAssets().open(labelPath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    labels.add(line);
                }
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void clear() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public void trackObjects(List<BoundingBox> boxes){
        Map<Integer, BoundingBox> trackingObjectsCopy = new HashMap<>(trackingObjects);

        for (Map.Entry<Integer, BoundingBox> entry : trackingObjectsCopy.entrySet()) {
            int objectId = entry.getKey();
            BoundingBox box = entry.getValue();
            boolean objectExists = false;

            for (var newBox : boxes) {
                double distance = Math.hypot(box.cx - newBox.cx, box.cy - newBox.cy);
                if (distance < 20) {
                    newBox.id = objectId;
                    trackingObjects.put(objectId, newBox);
                    objectExists = true;
                    break;
                }
            }

            if (!objectExists) {
                trackingObjects.remove(objectId);
            }
        }

        var newBoxes = boxes.stream().filter((BoundingBox x) -> x.id == null).toArray();

        for (var box : newBoxes) {
            var item = (BoundingBox) box;

            item.id = trackId;

            trackingObjects.put(trackId, item);
            trackId++;
        }
    }

    public Collection<BoundingBox> detect(Bitmap frame) {
        if (interpreter == null || tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            return new ArrayList<>();
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        var imageBuffer = processedImage.getBuffer();

        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);
        interpreter.run(imageBuffer, output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());

        trackObjects(bestBoxes);

        return trackingObjects.values();
    }

    private List<BoundingBox> bestBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = -1.0f;
            int maxIdx = -1;
            int j = 4;
            int arrayIdx = c + numElements * j;
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx];
                    maxIdx = j - 4;
                }
                j++;
                arrayIdx += numElements;
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                String clsName = labels.get(maxIdx);
                float cx = array[c]; // 0
                float cy = array[c + numElements]; // 1
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w/2F);
                float y1 = cy - (h/2F);
                float x2 = cx + (w/2F);
                float y2 = cy + (h/2F);
                if (x1 < 0F || x1 > 1F) continue;
                if (y1 < 0F || y1 > 1F) continue;
                if (x2 < 0F || x2 > 1F) continue;
                if (y2 < 0F || y2 > 1F) continue;
                var boundingBox = new BoundingBox();
                boundingBox.x1 = x1;
                boundingBox.y1 = y1;
                boundingBox.x2 = x2;
                boundingBox.y2 = y2;
                boundingBox.cx = cx;
                boundingBox.cy = cy;
                boundingBox.w = w;
                boundingBox.h = h;
                boundingBox.cnf = maxConf;
                boundingBox.cls = maxIdx;
                boundingBox.clsName = clsName;
                boundingBoxes.add(boundingBox);
            }
        }

        if (boundingBoxes.isEmpty()) return null;

        return applyNMS(boundingBoxes);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        Collections.sort(sortedBoxes, (a, b) -> Float.compare(b.cnf, a.cnf));
        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while(!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.get(0);
            selectedBoxes.add(first);
            sortedBoxes.remove(0);

            Iterator<BoundingBox> iterator = sortedBoxes.iterator();
            while (iterator.hasNext()) {
                BoundingBox nextBox = iterator.next();
                float iou = calculateIoU(first, nextBox);
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove();
                }
            }
        }

        return selectedBoxes;
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.x1, box2.x1);
        float y1 = Math.max(box1.y1, box2.y1);
        float x2 = Math.min(box1.x2, box2.x2);
        float y2 = Math.min(box1.y2, box2.y2);
        float intersectionArea = Math.max(0F, x2 - x1) * Math.max(0F, y2 - y1);
        float box1Area = box1.w * box1.h;
        float box2Area = box2.w * box2.h;
        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }
}