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
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Основная активность приложения для обработки изображений куба.
 * Реализует просчет контуров и сбор данных для последующего решения кубика.
 */
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mFrame;

    // Параметры HSV для каждого цвета
    private ColorHSV yellowHsv, orangeHsv, greenHsv, blueHsv, whiteHsv, redHsv;

    // Временное хранение данных для текущей стороны куба
    private Cuber[] temp = new Cuber[9];

    // Кнопки для отображения цветов отдельных элементов
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9;
    private TextView sideView;
    private FloatingActionButton save;

    // Указатель для анимации
    private volatile String pointer = "";

    // Используем перечисление для текущей стороны куба
    private CubeSide currentSide = CubeSide.FRONT;

    // Хранение данных для каждой стороны куба
    private Map<CubeSide, CubeFace> cubeFaces = new HashMap<>();

    // Колбэк для загрузки OpenCV
    private OpenCVLoaderCallback mLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.myCameraView);
        mOpenCvCameraView.setMaxFrameSize(640, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Загрузка настроек HSV для каждого цвета
        yellowHsv = setSettingHSV("Y", "yellow");
        greenHsv = setSettingHSV("G", "green");
        orangeHsv = setSettingHSV("O", "orange");
        whiteHsv = setSettingHSV("W", "white");
        redHsv = setSettingHSV("R", "red");
        blueHsv = setSettingHSV("B", "blue");

        // Инициализация временного массива (temp) и установка дефолтных значений для каждой ячейки
        for (int i = 0; i < 9; i++) {
            temp[i] = new Cuber(1, 1, 1, 1, "c");
        }

        // Инициализация хранения данных для сторон куба
        for (CubeSide side : CubeSide.values()) {
            cubeFaces.put(side, new CubeFace());
        }

        // Инициализация кнопок для отображения цветов
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);

        sideView = findViewById(R.id.sideView);
        sideView.setText(currentSide.getDisplayName());

        // Настройка нижней навигации
        BottomNavigationView bottomNavigationView = findViewById(R.id.detection_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> handleNavigation(item.getItemId()));

        // Обработка нажатия на кнопку сохранения текущей стороны
        save = findViewById(R.id.fab);
        save.setOnClickListener(v -> handleSaveCurrentSide());

        mLoaderCallback = new OpenCVLoaderCallback(this, mOpenCvCameraView);
    }

    /**
     * Обработка выбора пункта навигации.
     *
     * @param itemId идентификатор выбранного пункта
     * @return true, если событие обработано
     */
    private boolean handleNavigation(int itemId) {
        switch (itemId) {
            case R.id.prev:
                changeSide(getPreviousSide());
                break;
            case R.id.next:
                changeSide(getNextSide());
                break;
            case R.id.clear:
                clearCurrentSide();
                break;
            case R.id.start:
                startActivity(new Intent(getApplicationContext(), SettingCube.class));
                overridePendingTransition(0, 0);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Меняет текущую сторону на указанную.
     *
     * @param newSide новая сторона куба
     */
    private void changeSide(CubeSide newSide) {
        currentSide = newSide;
        sideView.setText(currentSide.getDisplayName());
        if (cubeFaces.get(currentSide).controlled) {
            setStaticButton(cubeFaces.get(currentSide).cubers);
        } else {
            setStaticButton(getClearCube());
        }
    }

    /**
     * Возвращает предыдущую сторону относительно текущей.
     */
    private CubeSide getPreviousSide() {
        return currentSide.getPrevious();
    }

    /**
     * Возвращает следующую сторону относительно текущей.
     */
    private CubeSide getNextSide() {
        return currentSide.getNext();
    }

    /**
     * Очищает данные для текущей стороны.
     */
    private void clearCurrentSide() {
        CubeFace face = cubeFaces.get(currentSide);
        face.controlled = false;
        face.cubers = new Cuber[9];
    }

    /**
     * Обработка сохранения данных текущей стороны и переход к следующей.
     * Если все стороны заполнены, производится построение строки куба и запуск активности-решателя.
     */
    private void handleSaveCurrentSide() {
        CubeFace currentFace = cubeFaces.get(currentSide);
        currentFace.controlled = true;
        currentFace.cubers = temp;
        temp = new Cuber[9];
        if (currentSide == CubeSide.FRONT || currentSide == CubeSide.RIGHT || currentSide == CubeSide.BACK) {
            animatePointer(new String[]{"R", ""}, new long[]{1000, 0});
        } else if (currentSide == CubeSide.LEFT) {
            animatePointer(new String[]{"R", "", "U", ""}, new long[]{1000, 500, 1000, 0});
        } else if (currentSide == CubeSide.UP) {
            animatePointer(new String[]{"D", "", "D", ""}, new long[]{1000, 500, 1000, 0});
        }
        CubeSide nextSide = getNextSide();
        changeSide(nextSide);

        if (allFacesControlled()) {
            String cube = buildCubeString();
            Intent intent = new Intent(getApplicationContext(), Solver.class);
            intent.putExtra("cube", cube);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    /**
     * Проверяет, заполнены ли данные для всех сторон куба.
     */
    private boolean allFacesControlled() {
        for (CubeSide side : CubeSide.values()) {
            if (!cubeFaces.get(side).controlled) {
                return false;
            }
        }
        return true;
    }

    /**
     * Собирает строку, описывающую состояние куба.
     * Порядок: Up, Right, Front, Down, Left, Back.
     */
    private String buildCubeString() {
        StringBuilder cube = new StringBuilder();
        // Получаем центральные цвета для определения преобразования цвета
        String u = cubeFaces.get(CubeSide.UP).cubers[4].getColor();
        String r = cubeFaces.get(CubeSide.RIGHT).cubers[4].getColor();
        String f = cubeFaces.get(CubeSide.FRONT).cubers[4].getColor();
        String d = cubeFaces.get(CubeSide.DOWN).cubers[4].getColor();
        String l = cubeFaces.get(CubeSide.LEFT).cubers[4].getColor();
        String b = cubeFaces.get(CubeSide.BACK).cubers[4].getColor();

        // Добавляем стороны в определённом порядке
        cube.append(convertFace(cubeFaces.get(CubeSide.UP).cubers, u, r, f, d, l, b));
        cube.append(convertFace(cubeFaces.get(CubeSide.RIGHT).cubers, u, r, f, d, l, b));
        cube.append(convertFace(cubeFaces.get(CubeSide.FRONT).cubers, u, r, f, d, l, b));
        cube.append(convertFace(cubeFaces.get(CubeSide.DOWN).cubers, u, r, f, d, l, b));
        cube.append(convertFace(cubeFaces.get(CubeSide.LEFT).cubers, u, r, f, d, l, b));
        cube.append(convertFace(cubeFaces.get(CubeSide.BACK).cubers, u, r, f, d, l, b));
        return cube.toString();
    }

    /**
     * Преобразует данные для одной грани куба в строку с обозначениями цветов.
     */
    private String convertFace(Cuber[] face, String u, String r, String f, String d, String l, String b) {
        StringBuilder faceStr = new StringBuilder();
        for (Cuber cuber : face) {
            faceStr.append(colorConvectore(u, r, f, d, l, b, cuber.getColor()));
        }
        return faceStr.toString();
    }

    /**
     * Устанавливает цвет для набора кнопок на основе переданного массива куберов.
     */
    public void setStaticButton(Cuber[] cubers) {
        // Массив кнопок для упрощения обхода
        Button[] buttons = new Button[]{button1, button2, button3, button4, button5, button6, button7, button8, button9};
        for (int i = 0; i < buttons.length; i++) {
            paintButton(buttons[i], cubers[i].getColor());
        }
    }

    /**
     * Получает массив «очищенных» куберов (с дефолтными значениями).
     */
    private Cuber[] getClearCube() {
        Cuber[] clear = new Cuber[9];
        for (int i = 0; i < 9; i++) {
            clear[i] = new Cuber(1, 1, 1, 1, "c");
        }
        return clear;
    }

    /**
     * Преобразует цвет из настроек в обозначение для кубика.
     */
    public String colorConvectore(String u_, String r_, String f_, String d_, String l_, String b_, String cube) {
        if (cube.equals(u_)) return "U";
        if (cube.equals(r_)) return "R";
        if (cube.equals(f_)) return "F";
        if (cube.equals(d_)) return "D";
        if (cube.equals(l_)) return "L";
        if (cube.equals(b_)) return "B";
        return "";
    }

    /**
     * Окрашивает кнопку в заданный цвет, определяемый именем цвета.
     */
    private Button paintButton(Button button, String colorName) {
        int colorResId = getColorResourceId(colorName);
        button.setBackgroundColor(getResources().getColor(colorResId));
        return button;
    }

    /**
     * Возвращает идентификатор ресурса цвета для заданного имени.
     */
    private int getColorResourceId(String colorName) {
        if ("yellow".equals(colorName)) return R.color.yellow;
        else if ("green".equals(colorName)) return R.color.green;
        else if ("white".equals(colorName)) return R.color.white;
        else if ("blue".equals(colorName)) return R.color.blue;
        else if ("orange".equals(colorName)) return R.color.orange;
        else if ("red".equals(colorName)) return R.color.red;
        return R.color.black;
    }

    /**
     * Загружает настройки HSV для указанного цвета.
     */
    public ColorHSV setSettingHSV(String color, String name) {
        SharedPreferences setting = getApplicationContext().getSharedPreferences("ColorSetting", 0);
        return new ColorHSV(name,
                setting.getInt(color + "Hl", 0),
                setting.getInt(color + "Hh", 0),
                setting.getInt(color + "Sl", 0),
                setting.getInt(color + "Sh", 0),
                setting.getInt(color + "Vl", 0),
                setting.getInt(color + "Vh", 0));
    }

    /**
     * Ищет контуры куба для указанного диапазона HSV и обрабатывает найденные области.
     *
     * @param mMat   исходное изображение
     * @param color  диапазон HSV
     * @param control флаг, указывающий, нужно ли обновлять кнопки
     * @return список найденных контуров
     */
    public List<MatOfPoint> findCubeContours(Mat mMat, ColorHSV color, boolean control) {
        List<MatOfPoint> allContours = new ArrayList<>();
        List<MatOfPoint> validContours = new ArrayList<>();
        Mat mInRange = new Mat();

        Scalar lowScalar = new Scalar(color.getLowH(), color.getLowS(), color.getLowV());
        Scalar highScalar = new Scalar(color.getHightH(), color.getHightS(), color.getHightV());

        Imgproc.cvtColor(mMat, mInRange, Imgproc.COLOR_BGR2HSV_FULL);
        Core.inRange(mInRange, lowScalar, highScalar, mInRange);
        Imgproc.GaussianBlur(mInRange, mInRange, new Size(5, 5), 0);
        Imgproc.findContours(mInRange, allContours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));
        mInRange.release();

        // Определение областей по координатам для 9 кнопок (индекс, диапазоны x и y)
        Region[] regions = new Region[]{
            new Region(0, 150, 0, 120, button1, 0),
            new Region(180, 330, 0, 120, button2, 1),
            new Region(380, 540, 0, 120, button3, 2),
            new Region(0, 150, 200, 350, button4, 3),
            new Region(180, 330, 200, 350, button5, 4),
            new Region(380, 540, 200, 350, button6, 5),
            new Region(0, 150, 370, 540, button7, 6),
            new Region(180, 330, 370, 540, button8, 7),
            new Region(380, 540, 370, 540, button9, 8)
        };

        // Перебор контуров и проверка попадания в заданные области
        for (MatOfPoint contour : allContours) {
            double area = Imgproc.contourArea(contour);
            if (area > 15000 && area < 30000) {
                validContours.add(contour);
                Rect rect = Imgproc.boundingRect(contour);
                int x = rect.x, y = rect.y;
                for (Region region : regions) {
                    if (region.contains(x, y)) {
                        saveResultColor(rect.x, rect.y, rect.width, rect.height, color.getName(), currentSide.getDisplayName(), region.index);
                        if (!control) {
                            paintButton(region.button, color.getName());
                        }
                    }
                }
            }
        }
        return validContours;
    }

    /**
     * Сохраняет данные для отдельного элемента куба во временный массив.
     */
    public void saveResultColor(int x_, int y_, int w_, int h_, String color, String side, int index) {
        Cuber item = new Cuber(x_, y_, w_, h_, color);
        temp[index] = item;
    }

    /**
     * Отрисовывает контуры куба и, при необходимости, линии указателя.
     */
    public List<MatOfPoint> drawCubeContours(Mat mMat, ColorHSV colorHSV, Scalar colorLine) {
        // Используем контрольную метку для текущей стороны
        boolean controlFlag = cubeFaces.get(currentSide).controlled;
        List<MatOfPoint> contours = findCubeContours(mMat, colorHSV, controlFlag);

        // Отрисовка контуров или специальных линий в зависимости от значения указателя
        if (pointer.isEmpty()) {
            for (MatOfPoint cnt : contours) {
                Rect rect = Imgproc.boundingRect(cnt);
                Imgproc.rectangle(mMat, rect.tl(), rect.br(), colorLine, 5);
            }
        } else if (pointer.equals("R")) {
            // Отрисовка линий для указателя "R"
            drawHorizontalMarker(mMat, 120, 520, 200, 160, 240, colorLine);
            drawHorizontalMarker(mMat, 120, 520, 400, 360, 440, colorLine);
        } else if (pointer.equals("U")) {
            // Отрисовка линий для указателя "U"
            drawVerticalMarker(mMat, 200, 520, 200, 160, 240, colorLine);
            drawVerticalMarker(mMat, 400, 520, 200, 360, 440, colorLine);
        } else if (pointer.equals("D")) {
            // Отрисовка линий для указателя "D"
            drawVerticalMarker(mMat, 200, 520, 480, 160, 240, colorLine);
            drawVerticalMarker(mMat, 400, 520, 480, 360, 440, colorLine);
        }
        return contours;
    }

    /**
     * Рисует горизонтальные линии-маркеры.
     */
    private void drawHorizontalMarker(Mat mMat, double startX, double endX, double y, double offset1, double offset2, Scalar colorLine) {
        Point p1 = new Point(startX, y);
        Point p2 = new Point(endX, y);
        Point pHigh = new Point(endX - offset1, y - offset1);
        Point pLow = new Point(endX - offset1, y + offset2);
        Imgproc.line(mMat, p1, p2, colorLine, 5);
        Imgproc.line(mMat, pHigh, p2, colorLine, 5);
        Imgproc.line(mMat, pLow, p2, colorLine, 5);
    }

    /**
     * Рисует вертикальные линии-маркеры.
     */
    private void drawVerticalMarker(Mat mMat, double x, double startY, double endY, double offset1, double offset2, Scalar colorLine) {
        Point p1 = new Point(x, startY);
        Point p2 = new Point(x, endY);
        Point pHigh = new Point(x - offset1, startY + offset1);
        Point pLow = new Point(x + offset2, startY + offset1);
        Imgproc.line(mMat, p1, p2, colorLine, 5);
        Imgproc.line(mMat, pHigh, p1, colorLine, 5);
        Imgproc.line(mMat, pLow, p1, colorLine, 5);
    }

    /**
     * Анимация указателя. Принимает последовательность значений указателя и соответствующие задержки.
     *
     * @param sequence массив значений указателя
     * @param delays   задержки в миллисекундах для каждого шага
     */
    private void animatePointer(final String[] sequence, final long[] delays) {
        new Thread(new PointerAnimator(this, sequence, delays)).start();
    }

    // Переопределённые методы жизненного цикла камеры OpenCV

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
    }

    @Override
    public void onCameraViewStopped() {
        mFrame.release();
    }

    /**
     * Обработка каждого кадра камеры.
     * Применяются преобразования изображения, отрисовка контуров для каждого цвета.
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mFrame = inputFrame.rgba();
        Core.transpose(mFrame, mFrame);
        Imgproc.resize(mFrame, mFrame, mFrame.size());
        Core.flip(mFrame, mFrame, 1);

        // Отрисовка контуров для каждого цвета
        drawCubeContours(mFrame, redHsv, new Scalar(255, 0, 0));
        drawCubeContours(mFrame, blueHsv, new Scalar(0, 0, 255));
        drawCubeContours(mFrame, greenHsv, new Scalar(0, 255, 0));
        drawCubeContours(mFrame, yellowHsv, new Scalar(200, 255, 0));
        drawCubeContours(mFrame, orangeHsv, new Scalar(255, 165, 0));
        drawCubeContours(mFrame, whiteHsv, new Scalar(255, 255, 255));

        return mFrame;
    }

    // --- Вспомогательные классы и перечисления ---

    /**
     * Перечисление сторон куба с методами для перехода к следующей/предыдущей стороне.
     */
    private enum CubeSide {
        FRONT, RIGHT, BACK, LEFT, UP, DOWN;

        public CubeSide getNext() {
            if (this == FRONT) return RIGHT;
            else if (this == RIGHT) return BACK;
            else if (this == BACK) return LEFT;
            else if (this == LEFT) return UP;
            else if (this == UP) return DOWN;
            else if (this == DOWN) return FRONT;
            return FRONT;
        }

        public CubeSide getPrevious() {
            if (this == FRONT) return DOWN;
            else if (this == DOWN) return UP;
            else if (this == UP) return LEFT;
            else if (this == LEFT) return BACK;
            else if (this == BACK) return RIGHT;
            else if (this == RIGHT) return FRONT;
            return FRONT;
        }

        public String getDisplayName() {
            return this.name().toLowerCase();
        }
    }

    /**
     * Класс для хранения данных по одной стороне куба.
     */
    private static class CubeFace {
        public Cuber[] cubers;
        public boolean controlled;

        public CubeFace() {
            cubers = new Cuber[9];
            controlled = false;
        }
    }

    /**
     * Класс для определения региона на экране.
     */
    private static class Region {
        int minX, maxX, minY, maxY;
        Button button;
        int index;

        public Region(int minX, int maxX, int minY, int maxY, Button button, int index) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.button = button;
            this.index = index;
        }

        /**
         * Проверяет, попадают ли координаты в данный регион.
         */
        public boolean contains(int x, int y) {
            return x > minX && x < maxX && y > minY && y < maxY;
        }
    }

    // Extract the BaseLoaderCallback as a separate class
    private static class OpenCVLoaderCallback extends BaseLoaderCallback {
        private final CameraBridgeViewBase mOpenCvCameraView;

        public OpenCVLoaderCallback(AppCompatActivity appCompatActivity, CameraBridgeViewBase cameraView) {
            super(appCompatActivity);
            this.mOpenCvCameraView = cameraView;
        }

        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    }

    // Extract animatePointer method as a separate class
    private static class PointerAnimator implements Runnable {
        private final String[] sequence;
        private final long[] delays;
        private final MainActivity activity;

        public PointerAnimator(MainActivity activity, String[] sequence, long[] delays) {
            this.activity = activity;
            this.sequence = sequence;
            this.delays = delays;
        }

        @Override
        public void run() {
            for (int i = 0; i < sequence.length; i++) {
                activity.pointer = sequence[i];
                try {
                    Thread.sleep(delays[i]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            activity.pointer = "";
        }
    }
}
