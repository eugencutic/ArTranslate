package com.example.mobilelibrary.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.example.mobilelibrary.R;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment implements LifecycleOwner {

    private static final String TAG = "CAMERA";

    private DashboardViewModel dashboardViewModel;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextureView textureView;

    private ImageView overlay;
    private int imageViewWidth;
    private int imageViewHeight;

    private Context context;
    private Activity activity;

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        context = this.getContext();
        activity = this.getActivity();

        textureView = root.findViewById(R.id.texture_view_camera);
        textureView.postDelayed(this::startCamera, 100);
        textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateTransform();
            }
        });

        overlay = root.findViewById(R.id.overlay);
        overlay.bringToFront();
        ViewTreeObserver vto = overlay.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                overlay.getViewTreeObserver().removeOnPreDrawListener(this);
                imageViewHeight = overlay.getMeasuredHeight();
                imageViewWidth = overlay.getMeasuredWidth();
                return true;
            }
        });
        return root;
    }


    private void startCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        textureView.getDisplay().getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);

        PreviewConfig previewConfigBuilder = new PreviewConfig
                .Builder()
                .setTargetResolution(screenSize)
                .setTargetRotation(textureView.getDisplay().getRotation())
                .build();
        Preview preview = new Preview(previewConfigBuilder);



        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);
            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        ImageAnalysisConfig analysisConfig = new ImageAnalysisConfig
                .Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        ImageAnalysis analysis = new ImageAnalysis(analysisConfig);

        analysis.setAnalyzer(executor, new ImageTextAnalyzer(activity, new Size(imageViewWidth, imageViewHeight), overlay));
        CameraX.bindToLifecycle(this, preview, analysis);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        float centerX = textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;

        int rotationDegrees;
        switch(textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
            default:
                return;
        }

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        textureView.setTransform(matrix);
    }
}
