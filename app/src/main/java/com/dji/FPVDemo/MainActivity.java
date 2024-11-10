package com.dji.FPVDemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


// OpenCV Android helper classes
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;



// OpenCV Core functionality
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

// OpenCV Image Processing (for functions like cvtColor, GaussianBlur, etc.)
import org.opencv.imgproc.Imgproc;

// OpenCV Feature Detection (for ORB, SIFT, etc.)
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;

// OpenCV Object Detection and ArUco
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.CascadeClassifier;
// Crear el diccionario
import org.opencv.objdetect.ArucoDetector; // Import para usar ArUco
import org.opencv.objdetect.Dictionary; // Import del diccionario de ArUco
import org.opencv.objdetect.DetectorParameters; // Parámetros de configuración
//import org.opencv.xfeatures2d.SURF;
import org.opencv.utils.Converters;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
//import org.opencv.aruco.Aruco;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.useraccount.UserAccountManager;

public class MainActivity extends Activity implements SurfaceTextureListener,OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    static {
        System.loadLibrary("opencv_java4"); // Asegura la carga de la librería OpenCV
    }
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Error: OpenCV no se pudo cargar.");
        } else {
            Log.d("OpenCV", "OpenCV se cargó correctamente.");
        }
    }

    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    private ExecutorService executor = Executors.newFixedThreadPool(3); // Cambia a 2 hilos    private Bitmap processedBitmap;

    // Nuevo ImageView para mostrar el frame procesado
    private ImageView processedFrameView;

    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn, mShootPhotoModeBtn, mRecordVideoModeBtn;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;

    private Spinner filterSpinner;

    private String selectedFilter;
    private Handler handler;

    Mat reusableMat;


    private CascadeClassifier faceCascade;




//    // Variables globales para ArUco
    private ArucoDetector arucoDetector;
    private Dictionary dictionary;

    private Bitmap reusableGrayBitmap = null;

    private Mat reusableGrayFrame = new Mat();

    // Añadir un contador global para limitar la cantidad de frames procesados
    private int frameCounter = 0;

//
//    private void initializeArucoDictionary() {
//        // Diccionario básico de ArUco
//        dictionary = ArucoDetector.getPredefinedDictionary(ArucoDetector.DICT_4X4_50);
//    }

//    private void initializeArucoDetector() {
//        // Inicialización de DetectorParameters
//        DetectorParameters parameters = new DetectorParameters(); // Usando el constructor directamente
//
//        // Inicialización de ArucoDetector
//        arucoDetector = new ArucoDetector(dictionary, parameters);
//    }

// Inicialización del diccionario de ArUco y del detector
//    dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);  // Selecciona el diccionario deseado
//    DetectorParameters parameters = DetectorParameters.create();
//    arucoDetector = new ArucoDetector(dictionary, parameters);



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler = new Handler();



        initUI();

// // Aseguramos que el filtro inicial sea "Grayscale".
    selectedFilter = "Grayscale";


//Inicialización del diccionario y del detector ArUco
//        dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
//        DetectorParameters parameters = new DetectorParameters();
//        arucoDetector = new ArucoDetector(dictionary, parameters);

//
//        initializeArucoDetector();
//



//    filterSpinner.setSelection(0); // Selecciona el primer filtro en el Spinner, que debe ser "Grayscale".
// Cargar el archivo de clasificador en cascada
        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            faceCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (faceCascade.empty()) {
                Log.e(TAG, "Error al cargar el clasificador de caras");
                faceCascade = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error cargando el archivo de cascada: " + e.getMessage());
        }

        // The callback for receiving the raw H264 video data for camera live view
        // Asegúrate de que mReceivedVideoDataListener está en el hilo correcto y actualizando en el principal
        // Listener para recibir y procesar los datos de video
        // En el listener, reutiliza reusableBitmap y reusableGrayFrame
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }

                // Verificar si el decodificador está listo y procesar en un hilo separado
                executor.execute(() -> {
                    if (mCodecManager != null && mCodecManager.isDecoderOK() && mVideoSurface != null) {
                        processFrame();
                    } else {
                        Log.d(TAG, "Decodificador no está listo o superficie no disponible.");
                    }
                });
            }
        };

        Camera camera = FPVDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording){
                                    recordingTime.setVisibility(View.VISIBLE);
                                }else
                                {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }

    }


    private void processFrame() {
        int frequency = 20; // Frecuencia por defecto

        // Ajusta la frecuencia según el filtro seleccionado
        if ("facedetection".equals(selectedFilter)) {
            frequency = 40; // Procesa un frame cada 50 si es detección de rostros
        }

        // Procesa solo si el contador alcanza el múltiplo de la frecuencia deseada
        if (frameCounter % frequency != 0) {
            frameCounter++;
            return;
        }
        frameCounter++;

        if (mCodecManager == null || !mCodecManager.isDecoderOK() || processedFrameView == null || processedFrameView.getDisplay() == null) {
            Log.d(TAG, "CodecManager o superficie gráfica no disponibles.");
            return;
        }

        int width = reusableGrayBitmap.getWidth();
        int height = reusableGrayBitmap.getHeight();

        try {
            byte[] rgbaData = mCodecManager.getRgbaData(width, height);
            if (rgbaData == null) {
                Log.d(TAG, "No se pudo obtener rgbaData.");
                return;
            }

            Mat rgbaMat = new Mat(height, width, CvType.CV_8UC4);
            rgbaMat.put(0, 0, rgbaData);

            if (selectedFilter == null) {
                selectedFilter = "Grayscale"; // Valor predeterminado en caso de que sea null
            }

            // Normalizamos el valor de selectedFilter
            String normalizedFilter = selectedFilter != null ? selectedFilter.trim().toLowerCase() : "none";
            Log.d(TAG, "Filtro normalizado: '" + normalizedFilter + "'");

            // Aplicar el filtro correspondiente
            switch (normalizedFilter) {
                case "grayscale":
                    Imgproc.cvtColor(rgbaMat, reusableGrayFrame, Imgproc.COLOR_RGBA2GRAY);
                    Utils.matToBitmap(reusableGrayFrame, reusableGrayBitmap);
                    break;
                case "smoothing":
                    applySmoothingFilter(rgbaMat);
                    break;
                case "sharpening":
                    applySharpeningFilter(rgbaMat);
                    break;
                case "canny":
                    applyCannyEdgeDetection(rgbaMat);
                    break;
                case "houghlines":
                    applyHoughLines(rgbaMat);
                    break;
                case "houghcircles":
                    applyHoughCircles(rgbaMat);
                    break;
                case "thresholding":
                    applyThresholding(rgbaMat);
                    break;
                case "orb":
                    applyORB(rgbaMat);
                    break;
                case "sift":
                    applySIFT(rgbaMat);
                    break;
                case "facedetection":
                    applyFaceDetection(rgbaMat);
                    break;
//                case "arucomarkers":
//                    applyArucoMarkers(rgbaMat);
//                    break;
                default:
                    Log.d(TAG, "Filtro no reconocido o sin filtro aplicado.");
                    Utils.matToBitmap(rgbaMat, reusableGrayBitmap); // Sin filtro
                    break;
            }

            // Actualización en la interfaz de usuario
            if (processedFrameView != null && processedFrameView.getDisplay() != null) {
                runOnUiThread(() -> processedFrameView.setImageBitmap(reusableGrayBitmap));
            }

            rgbaMat.release();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error en getRgbaData: " + e.getMessage());
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }


    // Métodos de los filtros
    // Filtro de suavizado (smoothing) usando un desenfoque gaussiano
    private void applySmoothingFilter(Mat inputMat) {
        Mat smoothedMat = new Mat();
        Imgproc.GaussianBlur(inputMat, smoothedMat, new org.opencv.core.Size(15, 15), 0);
        Utils.matToBitmap(smoothedMat, reusableGrayBitmap); // Almacenamos el resultado en reusableGrayBitmap
        smoothedMat.release();
    }

    // Filtro de afilado (sharpening) usando un kernel de afilado
    private void applySharpeningFilter(Mat inputMat) {
        Mat sharpenedMat = new Mat();
        Mat kernel = new Mat(3, 3, CvType.CV_32F) {
            {
                put(0, 0, -1); put(0, 1, -1); put(0, 2, -1);
                put(1, 0, -1); put(1, 1, 9); put(1, 2, -1);
                put(2, 0, -1); put(2, 1, -1); put(2, 2, -1);
            }
        };
        Imgproc.filter2D(inputMat, sharpenedMat, inputMat.depth(), kernel);
        Utils.matToBitmap(sharpenedMat, reusableGrayBitmap);
        sharpenedMat.release();
    }

    // Filtro de detección de bordes Canny
    private void applyCannyEdgeDetection(Mat inputMat) {
        Mat edgesMat = new Mat();
        Imgproc.Canny(inputMat, edgesMat, 100, 200);
        Utils.matToBitmap(edgesMat, reusableGrayBitmap);
        edgesMat.release();
    }

    // Filtro de umbralización (thresholding) con umbral fijo
    private void applyThresholding(Mat inputMat) {
        Mat grayMat = new Mat();
        Mat thresholdedMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.threshold(grayMat, thresholdedMat, 128, 255, Imgproc.THRESH_BINARY);
        Utils.matToBitmap(thresholdedMat, reusableGrayBitmap);
        grayMat.release();
        thresholdedMat.release();
    }
    private void applyHoughLines(Mat inputMat) {
        Mat grayMat = new Mat();
        Mat edges = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY); // Convertimos a escala de grises
        Imgproc.Canny(grayMat, edges, 50, 150, 3, false); // Aplicamos Canny antes de HoughLines

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, 50, 10); // Parámetros ajustables

        // Dibujamos las líneas detectadas
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i, 0);
            Point pt1 = new Point(points[0], points[1]);
            Point pt2 = new Point(points[2], points[3]);
            Imgproc.line(inputMat, pt1, pt2, new Scalar(255, 0, 0), 2); // Línea azul con grosor de 2px
        }
        Utils.matToBitmap(inputMat, reusableGrayBitmap); // Convertimos a Bitmap
        grayMat.release();
        edges.release();
        lines.release();
    }
    private void applyHoughCircles(Mat inputMat) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY); // Convertimos a escala de grises
        Imgproc.medianBlur(grayMat, grayMat, 5); // Suavizado para reducir ruido

        Mat circles = new Mat();
        Imgproc.HoughCircles(grayMat, circles, Imgproc.HOUGH_GRADIENT, 1.0, grayMat.rows()/16, 100, 30, 1, 30); // Parámetros ajustables

        // Dibujamos los círculos detectados
        for (int i = 0; i < circles.cols(); i++) {
            double[] circle = circles.get(0, i);
            Point center = new Point(circle[0], circle[1]);
            int radius = (int) Math.round(circle[2]);
            Imgproc.circle(inputMat, center, radius, new Scalar(0, 255, 0), 2); // Círculo verde con grosor de 2px
        }
        Utils.matToBitmap(inputMat, reusableGrayBitmap);
        grayMat.release();
        circles.release();
    }

    private void applyORB(Mat inputMat) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        ORB orb = ORB.create();
        orb.detect(grayMat, keyPoints);

        Features2d.drawKeypoints(grayMat, keyPoints, inputMat); // Dibujamos puntos clave
        Utils.matToBitmap(inputMat, reusableGrayBitmap);

        grayMat.release();
        keyPoints.release();
    }

    private void applySIFT(Mat inputMat) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        SIFT sift = SIFT.create();
        sift.detect(grayMat, keyPoints);

        Features2d.drawKeypoints(grayMat, keyPoints, inputMat);
        Utils.matToBitmap(inputMat, reusableGrayBitmap);

        grayMat.release();
        keyPoints.release();
    }

    private void applyFaceDetection(Mat inputMat) {
        // Convertir la imagen a escala de grises
        Mat grayMat = new Mat();
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);

        // Mejorar el contraste para mejorar la detección
        Imgproc.equalizeHist(grayMat, grayMat);

        // Detectar rostros
        MatOfRect faces = new MatOfRect();
        if (faceCascade != null) {
            faceCascade.detectMultiScale(grayMat, faces, 1.1, 3, 0,
                    new Size(30, 30), new Size());
        }

        // Dibujar rectángulos alrededor de las caras detectadas
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(inputMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
        }

        // Convertir la imagen procesada a Bitmap para mostrarla en la interfaz de usuario
        Utils.matToBitmap(inputMat, reusableGrayBitmap);

        // Liberar memoria
        grayMat.release();
    }

//

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }else {
            Log.e(TAG, "mVideoSurface está listo en onResume.");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");

        // Libera recursos de Mat y cierra ExecutorService
        if (reusableGrayFrame != null) {
            reusableGrayFrame.release();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        processedFrameView = (ImageView) findViewById(R.id.processed_frame_view);


        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mShootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
        mRecordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        mShootPhotoModeBtn.setOnClickListener(this);
        mRecordVideoModeBtn.setOnClickListener(this);

        recordingTime.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });
        // Nueva sección para inicializar el Spinner y el botón
        filterSpinner = findViewById(R.id.filter_spinner);

        // Set the initial selection to "Grayscale" (index may va   ry based on your array)
        filterSpinner.setSelection(0); // Assuming "Grayscale" is the first item in the array
        selectedFilter = "Grayscale"; // Initialize selectedFilter with "Grayscale"

        // Configurar el Spinner para capturar la selección del filtro
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString().toLowerCase().trim();

                switch (selected) {
                    case "escala de grises":
                        selectedFilter = "grayscale";
                        break;
                    case "suavizado":
                        selectedFilter = "smoothing";
                        break;
                    case "afilado":
                        selectedFilter = "sharpening";
                        break;
                    case "bordes de canny":
                        selectedFilter = "canny";
                        break;
                    case "líneas de hough":
                        selectedFilter = "houghlines";
                        break;
                    case "círculos de hough":
                        selectedFilter = "houghcircles";
                        break;
                    case "umbral":
                        selectedFilter = "thresholding";
                        break;
                    case "orb":
                        selectedFilter = "orb";
                        break;
                    case "sift":
                        selectedFilter = "sift";
                        break;
                    case "surf":
                        selectedFilter = "surf";
                        break;
                    case "rostros":
                        selectedFilter = "facedetection";
                        break;
//                    case "aruco markers":
//                        selectedFilter = "arucomarkers";
//                        break;
                    default:
                        selectedFilter = "none"; // Asignar "none" si no se reconoce el filtro
                        break;
                }

                Log.d(TAG, "Filtro seleccionado (ajustado): " + selectedFilter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFilter = "grayscale"; // Valor predeterminado
            }
        });


    }

    private void initPreviewer() {

        BaseProduct product = FPVDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
            Log.e(TAG, "Producto no conectado.");
        } else {
            Log.e(TAG, "Producto DJI conectado: " + product.getModel().getDisplayName());

            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
                Log.e(TAG, "VideoDataListener agregado al VideoFeeder.");
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    // Inicializa reusableBitmap y reusableGrayFrame una sola vez en onSurfaceTextureAvailable
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable: Surface disponible con tamaño: " + width + "x" + height);

        int reducedWidth = width / 2;
        int reducedHeight = height / 2;

        // Inicialización fija de reusableGrayBitmap y reusableGrayFrame
        if (reusableGrayBitmap == null) {
            reusableGrayBitmap = Bitmap.createBitmap(reducedWidth, reducedHeight, Bitmap.Config.ARGB_8888);
            Log.d(TAG, "reusableGrayBitmap creado con tamaño: " + reducedWidth + "x" + reducedHeight);
        }

        if (reusableGrayFrame.empty()) {
            reusableGrayFrame = new Mat(reducedHeight, reducedWidth, CvType.CV_8UC1);
            Log.d(TAG, "reusableGrayFrame creado con tamaño: " + reducedHeight + "x" + reducedWidth);
        }

        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
            Log.e(TAG, "DJICodecManager inicializado.");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture:
                captureAction();
                break;
            case R.id.btn_shoot_photo_mode:
                if (isMavicAir2() || isM300()) {
                    switchCameraFlatMode(SettingsDefinitions.FlatCameraMode.PHOTO_SINGLE);
                }else {
                    switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                }
                break;
            case R.id.btn_record_video_mode:
                if (isMavicAir2() || isM300()) {
                    switchCameraFlatMode(SettingsDefinitions.FlatCameraMode.VIDEO_NORMAL);
                }else {
                    switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                }
                break;
            default:
                break;
        }
    }

    private void switchCameraFlatMode(SettingsDefinitions.FlatCameraMode flatCameraMode){
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setFlatMode(flatCameraMode, error -> {
                if (error == null) {
                    showToast("Switch Camera Flat Mode Succeeded");
                } else {
                    showToast(error.getDescription());
                }
            });
        }
    }

    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode){
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, error -> {
                if (error == null) {
                    showToast("Switch Camera Mode Succeeded");
                } else {
                    showToast(error.getDescription());
                }
            });
        }
    }

    // Method for taking photo
    private void captureAction(){
        final Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            if (isMavicAir2() || isM300()) {
                camera.setFlatMode(SettingsDefinitions.FlatCameraMode.PHOTO_SINGLE, djiError -> {
                    if (null == djiError) {
                        takePhoto();
                    }
                });
            }else {
                camera.setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.SINGLE, djiError -> {
                    if (null == djiError) {
                        takePhoto();
                    }
                });
            }
        }
    }

    private void takePhoto(){
        final Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera == null){
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                camera.startShootPhoto(djiError -> {
                    if (djiError == null) {
                        showToast("take photo: success");
                    } else {
                        showToast(djiError.getDescription());
                    }
                });
            }
        }, 2000);
    }

    // Method for starting recording
    private void startRecord(){

        final Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(djiError -> {
                if (djiError == null) {
                    showToast("Record video: success");
                }else {
                    showToast(djiError.getDescription());
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(djiError -> {
                if(djiError == null) {
                    showToast("Stop recording: success");
                }else {
                    showToast(djiError.getDescription());
                }
            }); // Execute the stopRecordVideo API
        }
    }

    private boolean isMavicAir2(){
        BaseProduct baseProduct = FPVDemoApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_AIR_2;
        }
        return false;
    }

    private boolean isM300(){
        BaseProduct baseProduct = FPVDemoApplication.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MATRICE_300_RTK;
        }
        return false;
    }
}
