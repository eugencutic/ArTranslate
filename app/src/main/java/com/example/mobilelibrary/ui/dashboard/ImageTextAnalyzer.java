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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ImageTextAnalyzer implements ImageAnalysis.Analyzer {
    private long lastAnalyzedTimestamp = 0L;

    private Activity activity;
    private Size previewSize;
    private ImageView overlay;

    public ImageTextAnalyzer(Activity activity, Size previewSize, ImageView overlay) {
        this.activity = activity;
        this.previewSize = previewSize;
        this.overlay = overlay;
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
}
