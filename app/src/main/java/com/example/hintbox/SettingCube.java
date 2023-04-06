package com.example.hintbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.RangeSlider;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class SettingCube extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame, mMask, mResult;

    private RangeSlider rangeH, rangeS, rangeV;
    private TextView tvHh, tvHl, tvSl, tvSh, tvVl, tvVh;
    private ColorHSV yellowHsv, orangeHsv, greenHsv, blueHsv, whiteHsv, redHsv, settingHsv;
    private Scalar scalarLowSetting, scalarHightSetting;
    private Button buttonYellow, buttonWhite, buttonOrange, buttonBlue, buttonRed, buttonSave;

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
        setContentView(R.layout.activity_setting_cube);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.myCameraView);
        mOpenCvCameraView.setMaxFrameSize(640, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.setting);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.solver:
                        startActivity(new Intent(getApplicationContext(), Solver.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cube:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.setting:
                        return true;
                }
                return false;
            }
        });

        yellowHsv = setSettingHSV("Y", "yellow");
        greenHsv = setSettingHSV("G", "green");
        orangeHsv = setSettingHSV("O", "orange");
        whiteHsv = setSettingHSV("W", "white");
        redHsv = setSettingHSV("R", "red");
        blueHsv = setSettingHSV("B", "blue");

        settingHsv = yellowHsv;
        scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
        scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());

        FrameLayout frameLayout = findViewById(R.id.frame_layout);

        buttonYellow = findViewById(R.id.yellow);
        buttonYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = yellowHsv;
                setProgress(yellowHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        buttonBlue = findViewById(R.id.blue);
        buttonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = blueHsv;
                setProgress(blueHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        Button buttonGreen = findViewById(R.id.green);
        buttonGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = greenHsv;
                setProgress(greenHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        buttonOrange = findViewById(R.id.orange);
        buttonOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = orangeHsv;
                setProgress(orangeHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        buttonRed = findViewById(R.id.red);
        buttonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = redHsv;
                setProgress(redHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        buttonWhite = findViewById(R.id.white);
        buttonWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = whiteHsv;
                setProgress(whiteHsv);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }
        });

        buttonSave = findViewById(R.id.save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorToSetting(yellowHsv, "Y");
                colorToSetting(greenHsv,  "G");
                colorToSetting(orangeHsv, "O");
                colorToSetting(whiteHsv,  "W");
                colorToSetting(redHsv,    "R");
                colorToSetting(blueHsv,   "B");
            }
        });

        rangeH = findViewById(R.id.rangeH);
        rangeH.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> list = rangeH.getValues();
                float min = list.get(0);
                float max = list.get(1);
                settingHsv.setLowH((int) min);
                settingHsv.setHightH((int) max);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {

            }
        });

        rangeS = findViewById(R.id.rangeS);
        rangeS.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> list = rangeS.getValues();
                float min = list.get(0);
                float max = list.get(1);
                settingHsv.setLowS((int) min);
                settingHsv.setHightS((int) max);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {

            }
        });

        rangeV = findViewById(R.id.rangeV);
        rangeV.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> list = rangeV.getValues();
                float min = list.get(0);
                float max = list.get(1);
                settingHsv.setLowV((int) min);
                settingHsv.setHightV((int) max);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {

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
        mMask = new Mat(height, width, CvType.CV_8UC4);
        mResult = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mFrame.release();
        mMask.release();
        mResult.release();
    }

    public void setProgress (ColorHSV colorHSV) {
        rangeH.setValues((float) colorHSV.getLowH(), (float) colorHSV.getHightH());
        rangeS.setValues((float) colorHSV.getLowS(), (float) colorHSV.getHightS());
        rangeV.setValues((float) colorHSV.getLowV(), (float) colorHSV.getHightV());
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

    public void colorToSetting (ColorHSV colorHSV, String color) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("ColorSetting", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(String.format(color + "Hl"), colorHSV.getLowH());
        editor.putInt(String.format(color + "Hh"), colorHSV.getHightH());
        editor.putInt(String.format(color + "Sl"), colorHSV.getLowS());
        editor.putInt(String.format(color + "Sh"), colorHSV.getHightS());
        editor.putInt(String.format(color + "Vl"), colorHSV.getLowV());
        editor.putInt(String.format(color + "Vh"), colorHSV.getHightV());

        editor.apply();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mFrame = inputFrame.rgba();
        Core.transpose(mFrame, mFrame);
        Imgproc.resize(mFrame, mFrame, mFrame.size(), 0,0, 0);
        Core.flip(mFrame, mFrame, 1 );

        Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_BGR2HSV_FULL);
        Core.inRange(mFrame, scalarLowSetting, scalarHightSetting, mMask);

//        Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_HSV2BGR_FULL);
//        Core.bitwise_and(mFrame, mFrame, mResult, mMask);

        return mMask;
    }
}