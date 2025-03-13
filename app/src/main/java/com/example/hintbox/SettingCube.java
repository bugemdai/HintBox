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
import java.util.function.Consumer;

public class SettingCube extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame, mMask, mResult;

    private RangeSlider rangeH, rangeS, rangeV;
    private TextView tvHh, tvHl, tvSl, tvSh, tvVl, tvVh;
    private ColorHSV yellowHsv, orangeHsv, greenHsv, blueHsv, whiteHsv, redHsv, settingHsv;
    private Scalar scalarLowSetting, scalarHightSetting;
    private Button buttonYellow, buttonWhite, buttonOrange, buttonBlue, buttonRed, buttonSave;

    private static final String PREF_COLOR_SETTING = "ColorSetting";
    private static final String KEY_HL = "Hl";
    private static final String KEY_HH = "Hh";
    private static final String KEY_SL = "Sl";
    private static final String KEY_SH = "Sh";
    private static final String KEY_VL = "Vl";
    private static final String KEY_VH = "Vh";

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

        initializeCameraView();
        setupBottomNavigationView();
        initializeColorSettings();
        setupButtons();
        setupRangeSliders();
    }

    /**
     * Initializes the camera view and sets its properties.
     */
    private void initializeCameraView() {
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.myCameraView);
        mOpenCvCameraView.setMaxFrameSize(640, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    /**
     * Sets up the bottom navigation view and its item selection listener.
     */
    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.setting);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    /**
     * Handles navigation item selection.
     * @param item The selected menu item.
     * @return true if the item selection is handled.
     */
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.solver:
                startActivity(new Intent(getApplicationContext(), Solver.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.cube:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            case R.id.setting:
                return true;
        }
        return false;
    }

    /**
     * Initializes color settings for different colors.
     */
    private void initializeColorSettings() {
        yellowHsv = setSettingHSV("Y", "yellow");
        greenHsv = setSettingHSV("G", "green");
        orangeHsv = setSettingHSV("O", "orange");
        whiteHsv = setSettingHSV("W", "white");
        redHsv = setSettingHSV("R", "red");
        blueHsv = setSettingHSV("B", "blue");

        settingHsv = yellowHsv;
        updateScalars();
    }

    /**
     * Sets up button click listeners for color selection and saving.
     */
    private void setupButtons() {
        buttonYellow = findViewById(R.id.yellow);
        buttonYellow.setOnClickListener(v -> onColorButtonClicked(yellowHsv));

        buttonBlue = findViewById(R.id.blue);
        buttonBlue.setOnClickListener(v -> onColorButtonClicked(blueHsv));

        Button buttonGreen = findViewById(R.id.green);
        buttonGreen.setOnClickListener(v -> onColorButtonClicked(greenHsv));

        buttonOrange = findViewById(R.id.orange);
        buttonOrange.setOnClickListener(v -> onColorButtonClicked(orangeHsv));

        buttonRed = findViewById(R.id.red);
        buttonRed.setOnClickListener(v -> onColorButtonClicked(redHsv));

        buttonWhite = findViewById(R.id.white);
        buttonWhite.setOnClickListener(v -> onColorButtonClicked(whiteHsv));

        buttonSave = findViewById(R.id.save);
        buttonSave.setOnClickListener(v -> saveColorSettings());
    }

    /**
     * Handles color button click events.
     * @param colorHsv The selected color's HSV settings.
     */
    private void onColorButtonClicked(ColorHSV colorHsv) {
        settingHsv = colorHsv;
        setProgress(colorHsv);
        updateScalars();
    }

    /**
     * Sets up range sliders for HSV values.
     */
    private void setupRangeSliders() {
        rangeH = findViewById(R.id.rangeH);
        rangeH.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                updateHSVFromSlider(rangeH, settingHsv::setLowH, settingHsv::setHightH);
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {}
        });

        rangeS = findViewById(R.id.rangeS);
        rangeS.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                updateHSVFromSlider(rangeS, settingHsv::setLowS, settingHsv::setHightS);
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {}
        });

        rangeV = findViewById(R.id.rangeV);
        rangeV.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                updateHSVFromSlider(rangeV, settingHsv::setLowV, settingHsv::setHightV);
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {}
        });
    }

    /**
     * Updates HSV values from the slider.
     * @param slider The RangeSlider for HSV values.
     * @param setLow Consumer to set the low value.
     * @param setHigh Consumer to set the high value.
     */
    private void updateHSVFromSlider(RangeSlider slider, Consumer<Integer> setLow, Consumer<Integer> setHigh) {
        List<Float> values = slider.getValues();
        setLow.accept(values.get(0).intValue());
        setHigh.accept(values.get(1).intValue());
        updateScalars();
    }

    /**
     * Updates the scalar values for the current color setting.
     */
    private void updateScalars() {
        scalarLowSetting = new Scalar(settingHsv.getLowH(), settingHsv.getLowS(), settingHsv.getLowV());
        scalarHightSetting = new Scalar(settingHsv.getHightH(), settingHsv.getHightS(), settingHsv.getHightV());
    }

    /**
     * Saves the current color settings to SharedPreferences.
     */
    private void saveColorSettings() {
        colorToSetting(yellowHsv, "Y");
        colorToSetting(greenHsv, "G");
        colorToSetting(orangeHsv, "O");
        colorToSetting(whiteHsv, "W");
        colorToSetting(redHsv, "R");
        colorToSetting(blueHsv, "B");
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

    @Override
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

    /**
     * Sets the progress of the sliders based on the given color HSV values.
     * @param colorHSV The color HSV values to set.
     */
    public void setProgress(ColorHSV colorHSV) {
        rangeH.setValues((float) colorHSV.getLowH(), (float) colorHSV.getHightH());
        rangeS.setValues((float) colorHSV.getLowS(), (float) colorHSV.getHightS());
        rangeV.setValues((float) colorHSV.getLowV(), (float) colorHSV.getHightV());
    }

    /**
     * Retrieves and sets the HSV settings for a given color from SharedPreferences.
     * @param color The color code.
     * @param name The name of the color.
     * @return The ColorHSV object with the settings.
     */
    public ColorHSV setSettingHSV(String color, String name) {
        SharedPreferences setting = getApplicationContext().getSharedPreferences(PREF_COLOR_SETTING, 0);
        return new ColorHSV(name,
                setting.getInt(String.format(color + KEY_HL), 0),
                setting.getInt(String.format(color + KEY_HH), 0),
                setting.getInt(String.format(color + KEY_SL), 0),
                setting.getInt(String.format(color + KEY_SH), 0),
                setting.getInt(String.format(color + KEY_VL), 0),
                setting.getInt(String.format(color + KEY_VH), 0));
    }

    /**
     * Saves the HSV settings of a color to SharedPreferences.
     * @param colorHSV The ColorHSV object containing the settings.
     * @param color The color code.
     */
    public void colorToSetting(ColorHSV colorHSV, String color) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREF_COLOR_SETTING, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(String.format(color + KEY_HL), colorHSV.getLowH());
        editor.putInt(String.format(color + KEY_HH), colorHSV.getHightH());
        editor.putInt(String.format(color + KEY_SL), colorHSV.getLowS());
        editor.putInt(String.format(color + KEY_SH), colorHSV.getHightS());
        editor.putInt(String.format(color + KEY_VL), colorHSV.getLowV());
        editor.putInt(String.format(color + KEY_VH), colorHSV.getHightV());

        editor.apply();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mFrame = inputFrame.rgba();
        Core.transpose(mFrame, mFrame);
        Imgproc.resize(mFrame, mFrame, mFrame.size(), 0, 0, 0);
        Core.flip(mFrame, mFrame, 1);

        Imgproc.cvtColor(mFrame, mFrame, Imgproc.COLOR_BGR2HSV_FULL);
        Core.inRange(mFrame, scalarLowSetting, scalarHightSetting, mMask);

        return mMask;
    }
}
