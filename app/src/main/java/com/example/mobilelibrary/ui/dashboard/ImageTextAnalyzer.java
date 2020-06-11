package com.example.mobilelibrary.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Size;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.mobilelibrary.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static java.lang.System.exit;

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
        List <String> lines = null;
        for (FirebaseVisionText.TextBlock block : fb.getTextBlocks()) {
            //TODO: Catalin
            String text= block.getText();
            String translation= translate(text);
            }
        }

    public String translate(String text)
    {
        // Create an English-German translator:
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.ROMANIAN)
                        .build();
        final Translator englishRomanianTranslator =
                Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishRomanianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });

        englishRomanianTranslator.translate(text)
            .addOnSuccessListener(
                    new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(@NonNull String translatedText) {



                        }
                    })
            .addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {



                        }
                    });

        String translated= englishRomanianTranslator.toString();
        return translated;
    }
    

}
