package com.example.mobilelibrary.ui.dashboard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Size;
import android.widget.ImageView;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ImageTextAnalyzer implements ImageAnalysis.Analyzer {
    private long lastAnalyzedTimestamp = 0L;

    private Activity activity;
    private Size previewSize;
    private ImageView overlay;

    private HashMap<FirebaseVisionText.TextBlock, Integer> currentBlocks;
    private HashSet<Integer> toBeTranslated;
    private Integer lastInsertedValue;

    public ImageTextAnalyzer(Activity activity, Size previewSize, ImageView overlay) {
        this.activity = activity;
        this.previewSize = previewSize;
        this.overlay = overlay;
        this.currentBlocks = new HashMap<>();
        this.toBeTranslated = new HashSet<>();
        this.lastInsertedValue = -1;
    }

    private byte[] toByteArray(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return bytes;
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

            uBuffer.rewind();
            uBuffer.rewind();
            vBuffer.rewind();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] imageBytes = toByteArray(buffer);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {

            Bitmap bitmap = imageToBitmap(image);

            Matrix matrix = new Matrix();
            float centerX = image.getWidth() / 2f;
            float centerY = image.getHeight() / 2f;
            int rot = 90;
            matrix.postRotate(rotationDegrees, centerX, centerY);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, previewSize.getWidth(), previewSize.getHeight(), true);

            Bitmap mutableBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
            // TODO: Get text blocks detections
            try {
                textAnalyzer(mutableBitmap);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(10f);

            canvas.drawRect(0, 0, 100, 100, paint);
            activity.runOnUiThread(() -> overlay.setImageBitmap(mutableBitmap));
        }
    }


    public void textAnalyzer(Bitmap bitmap) throws ExecutionException, InterruptedException {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        processImage(firebaseVisionImage);
    }

    public void processImage(FirebaseVisionImage firebaseVisionImage) throws ExecutionException, InterruptedException {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(firebaseVisionImage);
                       /* .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...



                                //Toast.makeText(getApplicationContext(), extractedResult, Toast.LENGTH_LONG).show();


                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...


                                    }
                                });*/
        Tasks.await(result);
        FirebaseVisionText extractedResult = result.getResult();
        String text= extractedResult.getText();
        splitText(extractedResult);



    }

    public void splitText(FirebaseVisionText fb) {
        toBeTranslated.clear(); // Clear the blocks that had to be translated the previous frame
                                // in order to fill it with new blocks that need to be translated

        HashSet<Integer> foundBlocks = new HashSet<>();  // HashSet of all the blocks that exist
                                                            // in the current frame

        for (FirebaseVisionText.TextBlock block : fb.getTextBlocks()) {
            int blockExists = -1;

            // Check if the block found in the current frame was already translated
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                blockExists = currentBlocks.getOrDefault(block, -1);
            }

            // If the block is new, we add it to HashMap and in the
            if(blockExists == -1){
                currentBlocks.put(block, ++lastInsertedValue);
                toBeTranslated.add(lastInsertedValue);
            }
            // Add the Int value of the block in the HashMap into the Set of all blocks found
            foundBlocks.add(blockExists != -1 ? blockExists : lastInsertedValue);
        }

        // Create an auxiliary HashMap in order to find all the blocks that we don't need anymore
        HashMap<FirebaseVisionText.TextBlock, Integer> blocksToBeRemoved = new HashMap<>(currentBlocks);

        // Iterate through all foundBlocks
        for (Integer valueToBeRemoved : foundBlocks) {
            // Use an iterator to find the value of the foundBlock in the HashMap
            Iterator<Map.Entry<FirebaseVisionText.TextBlock, Integer>> iterator = blocksToBeRemoved.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<FirebaseVisionText.TextBlock, Integer> entry = iterator.next();

                // Remove all the blocks from the blocksToBeRemoved HashMap, because if it is found,
                // it doesn't need removing from the main HashMap
                if (valueToBeRemoved.equals(entry.getValue())) {
                    iterator.remove();
                }
            }
        }

        // Remove from the currentBlocks HashMap all the blocks that weren't found again
        currentBlocks.keySet().removeAll(blocksToBeRemoved.keySet());

    }

}
