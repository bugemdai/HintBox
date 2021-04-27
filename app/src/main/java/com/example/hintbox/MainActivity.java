package com.example.hintbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame;

    private ColorHSV yellowHsv, orangeHsv, greenHsv, blueHsv, whiteHsv, redHsv;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void MainActivity_show_camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.myCameraView);
        mOpenCvCameraView.setMaxFrameSize(640, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        yellowHsv = setSettingHSV("Y", "yellow");
        greenHsv = setSettingHSV("G", "green");
        orangeHsv = setSettingHSV("O", "orange");
        whiteHsv = setSettingHSV("W", "white");
        redHsv = setSettingHSV("R", "red");
        blueHsv = setSettingHSV("B", "blue");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.cube);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.cube:
                        return true;
                    case R.id.setting:
                            startActivity(new Intent(getApplicationContext(), SettingCube.class));
                            overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });




    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mFrame = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mFrame.release();
    }

    public ColorHSV setSettingHSV (String color, String name) {
        ColorHSV temp = null;
        SharedPreferences setting = getApplicationContext().getSharedPreferences("ColorSetting", 0);
        temp = new ColorHSV(name,
                setting.getInt(String.format(color + "Hl"), 0),
                setting.getInt(String.format(color + "Hh"), 0),
                setting.getInt(String.format(color + "Sl"), 0),
                setting.getInt(String.format(color + "Sh"), 0),
                setting.getInt(String.format(color + "Vl"), 0),
                setting.getInt(String.format(color + "Vh"), 0));
        return temp;
    }

    public List<MatOfPoint> findCubeContours (Mat mMat, ColorHSV color) {
        List<MatOfPoint> contour = new ArrayList<MatOfPoint>();
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        Mat mInRange = new Mat();

        Scalar scalarLow = new Scalar(color.getLowH(), color.getLowS(), color.getLowV());
        Scalar scalarHight = new Scalar(color.getHightH(), color.getHightS(), color.getHightV());

        Imgproc.cvtColor(mMat, mInRange, Imgproc.COLOR_BGR2HSV);
        Core.inRange(mInRange, scalarLow, scalarHight, mInRange);
        Imgproc.GaussianBlur(mInRange, mInRange, new Size(5, 5), 0);
        Imgproc.findContours(mInRange, contour, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        for (int idx = 0; idx < contour.size(); idx++)
            if ((Imgproc.contourArea(contour.get(idx)) > 15000) && (Imgproc.contourArea(contour.get(idx)) < 30000))
                result.add(contour.get(idx));

        return result;
    }

    public void drawCubeContours (Mat mMat, ColorHSV colorHSV, Scalar colorLine) {
        Rect rectContours = new Rect();
        List<MatOfPoint> cnt = new ArrayList<MatOfPoint>();
        cnt = findCubeContours(mMat, colorHSV);
        for (MatOfPoint point : cnt) {
            Log.d("Area :", String.valueOf(Imgproc.contourArea(point)));
            rectContours = Imgproc.boundingRect(point);
            Imgproc.rectangle(mMat, rectContours.tl(), rectContours.br(), colorLine, 5);
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mFrame = inputFrame.rgba();
        Core.transpose(mFrame, mFrame);
        Imgproc.resize(mFrame, mFrame, mFrame.size(), 0,0, 0);
        Core.flip(mFrame, mFrame, 1 );

        drawCubeContours(mFrame, yellowHsv, new Scalar(200, 255, 0));
        drawCubeContours(mFrame, redHsv, new Scalar(255, 0, 0));
        drawCubeContours(mFrame, blueHsv, new Scalar(0, 0, 255));
        drawCubeContours(mFrame, orangeHsv, new Scalar(255, 165, 0));
        drawCubeContours(mFrame, greenHsv, new Scalar(0, 255, 0));
        drawCubeContours(mFrame, whiteHsv, new Scalar(255, 255, 255));

        return mFrame;
    }
}