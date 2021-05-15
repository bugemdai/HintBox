package com.example.hintbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame;

    private ColorHSV yellowHsv, orangeHsv, greenHsv, blueHsv, whiteHsv, redHsv;
    private Cuber[] temp, front, back, left, right, up, down, clear;
    private Button print;
    private String globalSide = "";
    private Boolean consrolFront, consrolBack, consrolLeft, consrolRight, consrolUp, consrolDown;
    private Button  button1, button2, button3, button4, button5, button6, button7, button8, button9;
    private FloatingActionButton save;
    private TextView side;
    private Button sideView;

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

        temp = new Cuber[9];
        clear = new Cuber[9];

        for (int i = 0; i < 9; i++){
            Cuber c = new Cuber(1, 1, 1, 1, "c");
            clear[i] = c;
        }

        if (globalSide == "")
            globalSide = "up";

        consrolFront = false;
        consrolBack = false;
        consrolDown = false;
        consrolUp = false;
        consrolLeft = false;
        consrolRight = false;

        front = new Cuber[9];
        left = new Cuber[9];
        back = new Cuber[9];
        right = new Cuber[9];
        up = new Cuber[9];
        down = new Cuber[9];

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);

        BottomNavigationView bottomNavigationView = findViewById(R.id.detection_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.prev:
                        switch (globalSide){
                            case "up":
                                globalSide = "back";
                                sideView.setText("back");
                                if (consrolBack)
                                    setStaticButton(back);
                                break;
                            case "back":
                                globalSide = "left";
                                sideView.setText("left");
                                if (consrolLeft)
                                    setStaticButton(left);
                                break;
                            case "left":
                                globalSide = "down";
                                sideView.setText("down");
                                if (consrolDown)
                                    setStaticButton(down);
                                break;
                            case "down":
                                globalSide = "front";
                                sideView.setText("front");
                                if (consrolFront)
                                    setStaticButton(front);
                                break;
                            case "front":
                                globalSide = "right";
                                sideView.setText("right");
                                if (consrolRight)
                                    setStaticButton(right);
                                break;
                            case "right":
                                globalSide = "up";
                                sideView.setText("up");
                                if (consrolUp)
                                    setStaticButton(up);
                                break;
                        }
                        return true;
                    case R.id.start:
                        String cube = "";
                        String u, r, f, d, l, b;

                        u = up[4].getColor();
                        r = right[4].getColor();
                        f = front[4].getColor();
                        d = down[4].getColor();
                        l = left[4].getColor();
                        b = back[4].getColor();

                        for (Cuber cuber : up) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }
                        for (Cuber cuber : right) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }
                        for (Cuber cuber : front) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }
                        for (Cuber cuber : down) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }
                        for (Cuber cuber : left) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }
                        for (Cuber cuber : back) {
                            cube = cube + colorConvectore(u, r, f, d, l, b, cuber.getColor());
                        }

//                        cube = "DRUUUDRBLBFLDRLDLFFDUFFRFRLRFBRDFUDLRBUBLURUDFBBUBLDLB";

                        Intent intent = new Intent(getApplicationContext(), Solver.class);
                        intent.putExtra("cube", cube);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.clear:
                        switch (globalSide){
                            case "front":
                                consrolFront = false;
                                front = new Cuber[9];
                                break;
                            case "left":
                                consrolLeft = false;
                                left = new Cuber[9];
                                break;
                            case "back":
                                consrolBack = false;
                                back = new Cuber[9];
                                break;
                            case "right":
                                consrolRight = false;
                                right = new Cuber[9];
                                break;
                            case "up":
                                consrolUp = false;
                                up = new Cuber[9];
                                break;
                            case "down":
                                consrolDown = false;
                                down = new Cuber[9];
                                break;
                        }
                        return true;
                    case R.id.next:
                        switch (globalSide){
                            case "up":
                                globalSide = "right";
                                sideView.setText("right");
                                if (consrolRight)
                                    setStaticButton(right);
                                break;
                            case "right":
                                globalSide = "front";
                                sideView.setText("front");
                                if (consrolFront)
                                    setStaticButton(front);
                                break;
                            case "front":
                                globalSide = "down";
                                sideView.setText("down");
                                if (consrolDown)
                                    setStaticButton(down);
                                break;
                            case "down":
                                globalSide = "left";
                                sideView.setText("left");
                                if (consrolLeft)
                                    setStaticButton(left);
                                break;
                            case "left":
                                globalSide = "back";
                                sideView.setText("back");
                                if (consrolBack)
                                    setStaticButton(back);
                                break;
                            case "back":
                                globalSide = "up";
                                sideView.setText("up");
                                if (consrolUp)
                                    setStaticButton(up);
                                break;
                        }
                        return true;
                }
                return false;
            }
        });

        sideView = findViewById(R.id.sideView);

        save = findViewById(R.id.fab);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (globalSide){
                    case "up":
                        if (!consrolUp) {
                            consrolUp = true;
                            up = temp;
                            temp = new Cuber[9];
                            globalSide = "right";
                            sideView.setText("right");
                            if (consrolRight) {
                                setStaticButton(right);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                    case "right":
                        if (!consrolRight) {
                            consrolRight = true;
                            right = temp;
                            temp = new Cuber[9];
                            globalSide = "front";
                            sideView.setText("front");
                            if (consrolFront) {
                                setStaticButton(front);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                    case "front":
                        if (!consrolFront) {
                            consrolFront = true;
                            front = temp;
                            temp = new Cuber[9];
                            globalSide = "down";
                            sideView.setText("down");
                            if (consrolDown) {
                                setStaticButton(down);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                    case "down":
                        if (!consrolDown) {
                            consrolDown = true;
                            down = temp;
                            temp = new Cuber[9];
                            globalSide = "left";
                            sideView.setText("left");
                            if (consrolLeft) {
                                setStaticButton(left);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                    case "left":
                        if (!consrolLeft) {
                            consrolLeft = true;
                            left = temp;
                            temp = new Cuber[9];
                            globalSide = "back";
                            sideView.setText("back");
                            if (consrolBack) {
                                setStaticButton(back);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                    case "back":
                        if (!consrolBack) {
                            consrolBack = true;
                            back = temp;
                            temp = new Cuber[9];
                            globalSide = "up";
                            sideView.setText("up");
                            if (consrolUp) {
                                setStaticButton(up);
                            } else {
                                setStaticButton(clear);
                            }
                        }
                        break;
                }
            }
        });
    }

    public String colorConvectore (String u_, String r_, String f_, String d_, String l_, String b_, String cube) {
       if (cube == u_) {return "U";}
       if (cube == r_) {return "R";}
       if (cube == f_) {return "F";}
       if (cube == d_) {return "D";}
       if (cube == l_) {return "L";}
       if (cube == b_) {return "B";}
       return "";
    }

    public void setStaticButton (Cuber[] cubers) {
        paintButton(button1, cubers[0].getColor());
        paintButton(button2, cubers[1].getColor());
        paintButton(button3, cubers[2].getColor());
        paintButton(button4, cubers[3].getColor());
        paintButton(button5, cubers[4].getColor());
        paintButton(button6, cubers[5].getColor());
        paintButton(button7, cubers[6].getColor());
        paintButton(button8, cubers[7].getColor());
        paintButton(button9, cubers[8].getColor());
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

    public List<MatOfPoint> findCubeContours (Mat mMat, ColorHSV color, Boolean control) {
        List<MatOfPoint> contour = new ArrayList<MatOfPoint>();
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        Mat mInRange = new Mat();

        Scalar scalarLow = new Scalar(color.getLowH(), color.getLowS(), color.getLowV());
        Scalar scalarHight = new Scalar(color.getHightH(), color.getHightS(), color.getHightV());

        Imgproc.cvtColor(mMat, mInRange, Imgproc.COLOR_BGR2HSV_FULL);
        Core.inRange(mInRange, scalarLow, scalarHight, mInRange);
        Imgproc.GaussianBlur(mInRange, mInRange, new Size(5, 5), 0);
        Imgproc.findContours(mInRange, contour, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));
        mInRange.release();

        Rect rectContours = new Rect();
        int xContours, yContours;

        for (int idx = 0; idx < contour.size(); idx++)
            if ((Imgproc.contourArea(contour.get(idx)) > 15000) && (Imgproc.contourArea(contour.get(idx)) < 30000)) {
                result.add(contour.get(idx));
                rectContours = Imgproc.boundingRect(contour.get(idx));
                xContours = rectContours.x;
                yContours = rectContours.y;

                if ((xContours > 0) && (xContours < 150) && (yContours > 0) && (yContours < 120)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 0);
                    if (!control)
                        paintButton(button1, color.getName());
                }
                if ((xContours > 180) && (xContours < 330) && (yContours > 0) && (yContours < 120)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 1);
                    if (!control)
                        paintButton(button2, color.getName());
                }
                if ((xContours > 380) && (xContours < 540) && (yContours > 0) && (yContours < 120)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 2);
                    if (!control)
                        paintButton(button3, color.getName());
                }
                if ((xContours > 0) && (xContours < 150) && (yContours > 200) && (yContours < 350)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 3);
                    if (!control)
                        paintButton(button4, color.getName());
                }
                if ((xContours > 180) && (xContours < 330) && (yContours > 200) && (yContours < 350)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 4);
                    if (!control)
                        paintButton(button5, color.getName());
                }
                if ((xContours > 380) && (xContours < 540) && (yContours > 200) && (yContours < 350)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 5);
                    if (!control)
                        paintButton(button6, color.getName());
                }
                if ((xContours > 0) && (xContours < 150) && (yContours > 370) && (yContours < 540)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 6);
                    if (!control)
                        paintButton(button7, color.getName());
                }
                if ((xContours > 180) && (xContours < 330) && (yContours > 370) && (yContours < 540)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 7);
                    if (!control)
                        paintButton(button8, color.getName());
                }
                if ((xContours > 380) && (xContours < 540) && (yContours > 370) && (yContours < 540)){
                    saveResultColor(rectContours.x, rectContours.y, rectContours.width, rectContours.height, color.getName(), globalSide, 8);
                    if (!control)
                        paintButton(button9, color.getName());
                }
            }
        return result;
    }

    public void saveResultColor (int x_, int y_, int w_, int h_, String color, String side, int index){
        Cuber item = new Cuber(x_, y_, w_, h_, color);
        temp[index] = item;
    }

    private Button paintButton(Button button, String sCube){
        switch (sCube){
            case "yellow":
                button.setBackgroundColor(getResources().getColor(R.color.yellow));
                break;
            case "green":
                button.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case "white":
                button.setBackgroundColor(getResources().getColor(R.color.white));
                break;
            case "blue":
                button.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case "orange":
                button.setBackgroundColor(getResources().getColor(R.color.orange));
                break;
            case "red":
                button.setBackgroundColor(getResources().getColor(R.color.red));
                break;
            default:
                button.setBackgroundColor(getResources().getColor(R.color.black));
                break;
        }
        return button;
    }

    public List<MatOfPoint> drawCubeContours (Mat mMat, ColorHSV colorHSV, Scalar colorLine) {
        Rect rectContours = new Rect();
        List<MatOfPoint> cnt = new ArrayList<MatOfPoint>();
        switch (globalSide){
            case "front":
                cnt = findCubeContours(mMat, colorHSV, consrolFront);
                break;
            case "left":
                cnt = findCubeContours(mMat, colorHSV, consrolLeft);
                break;
            case "back":
                cnt = findCubeContours(mMat, colorHSV, consrolBack);
                break;
            case "right":
                cnt = findCubeContours(mMat, colorHSV, consrolRight);
                break;
            case "up":
                cnt = findCubeContours(mMat, colorHSV, consrolUp);
                break;
            case "down":
                cnt = findCubeContours(mMat, colorHSV, consrolDown);
                break;
        }

        for (MatOfPoint point : cnt) {
            rectContours = Imgproc.boundingRect(point);
            Imgproc.rectangle(mMat, rectContours.tl(), rectContours.br(), colorLine, 5);
//            Imgproc.putText(mMat, String.valueOf(String.valueOf(rectContours.x) + " " + String.valueOf(rectContours.y)), new Point(rectContours.x, rectContours.y), Core.FONT_HERSHEY_SIMPLEX, 1.0,
//                    new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, false);
        }

        return cnt;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mFrame = inputFrame.rgba();
        Core.transpose(mFrame, mFrame);
        Imgproc.resize(mFrame, mFrame, mFrame.size(), 0,0, 0);
        Core.flip(mFrame, mFrame, 1 );

        List<MatOfPoint> cnt = new ArrayList<MatOfPoint>();

        drawCubeContours(mFrame, redHsv, new Scalar(255, 0, 0));
        drawCubeContours(mFrame, blueHsv, new Scalar(0, 0, 255));
        drawCubeContours(mFrame, greenHsv, new Scalar(0, 255, 0));
        drawCubeContours(mFrame, yellowHsv, new Scalar(200, 255, 0));
        drawCubeContours(mFrame, orangeHsv, new Scalar(255, 165, 0));
        drawCubeContours(mFrame, whiteHsv, new Scalar(255, 255, 255));

        return mFrame;
    }
}