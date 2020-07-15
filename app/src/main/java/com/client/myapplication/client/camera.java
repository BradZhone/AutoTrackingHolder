package com.client.myapplication.client;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;

public class camera extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    private CameraBridgeViewBase cameraView;
    private CascadeClassifier classifier;
    private Mat mGray;
    private Mat mRgba;
    private int mAbsoluteFaceSize = 0;
    private boolean isFrontCamera;
    public float[] center = new float[2];//识别人脸的中心坐标
    public float[] width_range = new float[2];//中心框的宽度范围
    public float[] height_range = new float[2];//中心框的高度范围
    public float width_center, height_center;//屏幕的中心点坐标
    public float face_size;//前后两张人脸的大小，用于判断是否为误判

    private int time = 0; //计算检测到人脸超出框的时间，防止发出过多命令（每一个time35.8ms，每转1°6.9ms）

    // 自动装载openCV库文件，以保证手机无需安装OpenCV Manager
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowSettings();
        setContentView(R.layout.activity_camera);
        cameraView = findViewById(R.id.camera_view);
        cameraView.setCvCameraViewListener(this); // 设置相机监听
        initClassifier();

        cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        isFrontCamera = true;

        cameraView.enableView();
        Button switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this); // 切换相机镜头，默认后置

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        Log.v("screen_width", String.valueOf(width));
        Log.v("screen_height", String.valueOf(height));
        width_range = new float[]{(float) (width * 2 * 0.85 / 5.0), (float) (width * 3 * 0.81 / 5.0)};
        height_range = new float[]{(float) (height * 2 * 0.836 / 5.0), (float) (height * 3/ 5.0)};
        width_center = (width_range[0]+width_range[1])/2;
        height_center = (height_range[0]+height_range[1])/2;
        Log.v("screen_center", "("+width_center+","+height_center+")");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.switch_camera) {
            cameraView.disableView();
            if (isFrontCamera) {
                cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                isFrontCamera = false;
            } else {
                cameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                isFrontCamera = true;
            }
            cameraView.enableView();
        }
    }

    // 初始化窗口设置, 包括全屏、横屏、常亮
    @SuppressLint("SourceLockedOrientationActivity")
    private void initWindowSettings() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    // 初始化人脸级联分类器，必须先初始化
    private void initClassifier() {
        try {
            InputStream is = getResources()
                    .openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float distance(float p1x, float p1y, Point p2){
        double sqrt = Math.sqrt(Math.pow(p1x - p2.x, 2) + Math.pow(p1y - p2.y, 2));
        return (float) sqrt;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    // 这里执行人脸检测的逻辑, 根据OpenCV提供的例子实现(face-detection)
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // 翻转矩阵以适配前后置摄像头
        if (isFrontCamera) {
            Core.flip(mRgba, mRgba, 1);     //朝右翻转
            Core.flip(mGray, mGray, 1);     //朝右翻转
        }
        float mRelativeFaceSize = 0.2f;
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        MatOfRect faces = new MatOfRect();
        if (classifier != null)
            classifier.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();

        Scalar rangeRectColor = new Scalar(255, 255, 0, 255);
        Scalar centerColor = new Scalar(255, 0, 0, 255);
        Scalar faceRectColor = new Scalar(0, 255, 0, 255);
        try{    //尝试寻找人脸并标出第一个人脸
            //画出边框
            Point range_st = new Point(width_range[0], height_range[0]);
            Point range_en = new Point(width_range[1], height_range[1]);
            Point screen_center = new Point(width_center, height_center);
            Imgproc.circle(mRgba, screen_center, 5, centerColor, 10);
            Imgproc.rectangle(mRgba, range_st, range_en,rangeRectColor,3);
            Log.v("screen_width_range", Arrays.toString(width_range));
            Log.v("screen_height_range", Arrays.toString(height_range));

            //人脸中心
            Point face_center = new Point(facesArray[0].x+facesArray[0].width/2.0,facesArray[0].y + facesArray[0].height/2.0);
            float detect_size = facesArray[0].height*facesArray[0].width;

//            Imgproc.rectangle(mRgba,facesArray[0].tl(), facesArray[0].br(),faceRectColor,3);//标出误判人脸

            Log.v("Faces", String.valueOf(facesArray[0]));
            //标出人脸位置
//            if(Math.abs(distance(center[0], center[1],screen_center)-distance((float)face_center.x,(float)face_center.y,screen_center))<=100 && Math.abs(face_size-detect_size)<=detect_size*0.35||center[0]==0 && face_size==0){
                //如果依次检测到的两个人脸中心左坐标到屏幕中心距离之差小于100且人脸大小之差不超过35%，则更新人脸位置并画出人脸（已弃用）
            if(Math.abs(face_size-detect_size)<=detect_size*0.4||face_size==0){//当前后两张人脸大小之差不超过40%时认为是同一张人脸，更新人脸坐标
                center[0] = (float) face_center.x;
                center[1] = (float) face_center.y;
                face_size = detect_size;
                Log.v("face_center", Arrays.toString(center));

                //标出人脸
                Imgproc.rectangle(mRgba,facesArray[0].tl(), facesArray[0].br(),faceRectColor,3);
                Imgproc.circle(mRgba, face_center, 5, centerColor, 10);
                Imgproc.line(mRgba,face_center,screen_center,centerColor,3);
                Imgproc.putText(mRgba,distance((float) screen_center.x, (float) screen_center.y,face_center)+":"+face_size,face_center,1,3,centerColor,3);
            }
            if(time==0) {
                //  超出边框时控制云台转动

                float dx = (float) (center[0]-screen_center.x);
                float dy = (float) (center[1]-screen_center.y);
                float k = (float) 0.0396;
                float b = (float) -1.185;
                int theta_x = Math.abs((int) (k*dx+b));
                int theta_y = Math.abs((int) (k*dy+b));

                if (center[0] < width_range[0]) {//超出左边框
                    if (!isFrontCamera) {
                        String message = new DecimalFormat("000").format(theta_x)+".000xpp";
                        Log.v("test_x", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    } else {
                        String message = new DecimalFormat("000").format(theta_x)+".000xnp";
                        Log.v("test_x", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    }
                } else if (center[0] > width_range[1]) {//超出右边框
                    if (!isFrontCamera) {
                        String message = new DecimalFormat("000").format(theta_x)+".000xnp";
                        Log.v("test_x", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    } else{
                        String message = new DecimalFormat("000").format(theta_x)+".000xpp";
                        Log.v("test_x", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    }
                }

                if (center[1] < height_range[0]) {//超出上边框
                    if (!isFrontCamera) {
                        String message0 = new DecimalFormat("000").format(theta_y);
                        String message = "000."+message0+"ypp";
                        Log.v("test_y", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    } else {
                        String message0 = new DecimalFormat("000").format(theta_y);
                        String message = "000."+message0+"ypn";
                        Log.v("test_y", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    }
                } else if (center[1] > height_range[1]) {//超出下边框
                    if (!isFrontCamera) {
                        String message0 = new DecimalFormat("000").format(theta_y);
                        String message = "000."+message0+"ypn";
                        Log.v("test_y", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    } else {
                        String message0 = new DecimalFormat("000").format(theta_y);
                        String message = "000."+message0+"ypp";
                        Log.v("test_y", message);
                        new Thread(new MainActivity.Thread3(message)).start();
                    }
                }
            }

            time = (time+1)%15;//每计数15次调整一次位置，防止发送过多操控命令

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mRgba;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }
}

