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
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

public class SettingCube extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame;

    private SeekBar seekBarHl, seekBarHh, seekBarSl, seekBarSh, seekBarVl, seekBarVh;
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

        buttonYellow = findViewById(R.id.yellow);
        buttonYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = yellowHsv;
                setProgress(yellowHsv);
            }
        });

        buttonBlue = findViewById(R.id.blue);
        buttonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = blueHsv;
                setProgress(blueHsv);
            }
        });

        Button buttonGreen = findViewById(R.id.green);
        buttonGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = greenHsv;
                setProgress(greenHsv);
            }
        });

        buttonOrange = findViewById(R.id.orange);
        buttonOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = orangeHsv;
                setProgress(orangeHsv);
            }
        });

        buttonRed = findViewById(R.id.red);
        buttonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = redHsv;
                setProgress(redHsv);
            }
        });

        buttonWhite = findViewById(R.id.white);
        buttonWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingHsv = whiteHsv;
                setProgress(whiteHsv);
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

        tvHl = findViewById(R.id.tvhl);
        seekBarHl = findViewById(R.id.SeekHl);
        seekBarHl.setProgress(settingHsv.getLowH());
        seekBarHl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setLowH(progress);
                tvHl.setText("Hlow " + progress);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvHh = findViewById(R.id.tvhh);
        seekBarHh = findViewById(R.id.SeekHh);
        seekBarHh.setProgress(settingHsv.getHightH());
        seekBarHh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setHightH(progress);
                tvHh.setText("Hhight " + progress);
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvSl = findViewById(R.id.tvsl);
        seekBarSl = findViewById(R.id.SeekSl);
        seekBarSl.setProgress(settingHsv.getLowS());
        seekBarSl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setLowS(progress);
                tvSl.setText("Slow " + progress);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowV(), settingHsv.getLowS());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvSh = findViewById(R.id.tvsh);
        seekBarSh = findViewById(R.id.SeekSh);
        seekBarSh.setProgress(settingHsv.getHightS());
        seekBarSh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setHightS(progress);
                tvSh.setText("Shight " + progress);
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvVl = findViewById(R.id.tvvl);
        seekBarVl = findViewById(R.id.SeekVL);
        seekBarVl.setProgress(settingHsv.getLowV());
        seekBarVl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setLowV(progress);
                tvVl.setText("Vlow " + progress);
                scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowV(), settingHsv.getLowS());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvVh = findViewById(R.id.tvvh);
        seekBarVh = findViewById(R.id.SeekVH);
        seekBarVh.setProgress(settingHsv.getHightV());
        seekBarVh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingHsv.setHightV(progress);
                tvVh.setText("Vhight " + progress);
                scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

    public void setProgress (ColorHSV colorHSV) {
        seekBarHl.setProgress(colorHSV.getLowH());
        seekBarHh.setProgress(colorHSV.getHightH());
        seekBarSl.setProgress(colorHSV.getLowS());
        seekBarSh.setProgress(colorHSV.getHightS());
        seekBarVl.setProgress(colorHSV.getLowV());
        seekBarVh.setProgress(colorHSV.getHightV());
        tvHl.setText("Hlow " + colorHSV.getLowH());
        tvHh.setText("Hhight " + colorHSV.getHightH());
        tvSl.setText("Slow " + colorHSV.getLowS());
        tvSh.setText("Shight " + colorHSV.getHightS());
        tvVl.setText("Vlow " + colorHSV.getLowV());
        tvVh.setText("Vhight " + colorHSV.getHightV());
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

        Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_BGR2HSV);
        Core.inRange(mFrame, scalarLowSetting, scalarHightSetting, mFrame);

        return mFrame;
    }
}