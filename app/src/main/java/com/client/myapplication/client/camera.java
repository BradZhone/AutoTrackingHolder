package com.client.myapplication.client;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
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
import java.util.Arrays;

public class camera extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    private CameraBridgeViewBase cameraView;
    private CascadeClassifier classifier;
    private Mat mGray;
    private Mat mRgba;
    private int mAbsoluteFaceSize = 0;
    private boolean isFrontCamera;
    private OrientationEventListener mOreientationListener;
    public float[] center = new float[2];
    public float[] width_range = new float[2];
    public float[] height_range = new float[2];

    private int x=0;//统计已经发送出的命令数，防止转到90度还一直发
    private int y=0;
    private int time = 0; //计算检测到人脸超出框的时间，防止发出过多命令

    // 手动装载openCV库文件，以保证手机无需安装OpenCV Manager
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
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        Scalar faceRectColor = new Scalar(0, 255, 0, 255);
        try{    //尝试寻找人脸并标出第一个人脸
            //画出边框
            Point range_st = new Point(width_range[0], height_range[0]);
            Point range_en = new Point(width_range[1], height_range[1]);
            Imgproc.rectangle(mRgba, range_st, range_en,rangeRectColor,3);
            Log.v("screen_width_range", Arrays.toString(width_range));
            Log.v("screen_height_range", Arrays.toString(height_range));

            Imgproc.rectangle(mRgba,facesArray[0].tl(), facesArray[0].br(),faceRectColor,3);
            Log.v("Faces", String.valueOf(facesArray[0]));
            //标出人脸位置
            if(Math.abs(center[0] - (facesArray[0].x+facesArray[0].width/2.0))<=60 && Math.abs(center[1] - (facesArray[0].y + facesArray[0].height/2.0))<=60){
                //如果依次检测到的两个矩阵左上角坐标之差小于50，则更新人脸中心坐标
                center[0] = (float) (facesArray[0].x + (facesArray[0].width/2.0));
                center[1] = (float) (facesArray[0].y + (facesArray[0].height/2.0));
                Log.v("face_center", Arrays.toString(center));
            }else{
                center[0] = (float) (facesArray[0].x + (facesArray[0].width/2.0));
                center[1] = (float) (facesArray[0].y + (facesArray[0].height/2.0));
            }

            //  超出边框时控制云台转动
            if(time==0) {
                if (center[0] < width_range[0]) {//超出左边框
                    if (!isFrontCamera && x < 9) {
                        Log.v("command_left", "r");
                        String message = "r";
                        new Thread(new MainActivity.Thread2(message)).start();
                        x += 1;
                    } else if (isFrontCamera && x > -9) {
                        Log.v("command_right", "l");
                        String message = "l";
                        new Thread(new MainActivity.Thread2(message)).start();
                        x -= 1;
                    }
                } else if (center[0] > width_range[1]) {//超出右边框
                    if (!isFrontCamera && x > -9) {
                        Log.v("command_right", "l");
                        String message = "l";
                        new Thread(new MainActivity.Thread2(message)).start();
                        x -= 1;
                    } else if (isFrontCamera && x < 9) {
                        Log.v("command_left", "r");
                        String message = "r";
                        new Thread(new MainActivity.Thread2(message)).start();
                        x += 1;
                    }
                } else if (center[1] < height_range[0]) {//超出上边框
                    if (!isFrontCamera && y > -9) {
                        Log.v("command_up", "d");
                        String message = "d";
                        new Thread(new MainActivity.Thread2(message)).start();
                        y -= 1;
                    } else if (isFrontCamera && y < 9) {
                        Log.v("command_down", "u");
                        String message = "u";
                        new Thread(new MainActivity.Thread2(message)).start();
                        y += 1;
                    }
                } else if (center[1] > height_range[1]) {//超出下边框
                    if (!isFrontCamera && y < 9) {
                        Log.v("command_down", "u");
                        String message = "u";
                        new Thread(new MainActivity.Thread2(message)).start();
                        y += 1;
                    } else if (isFrontCamera && y > -9) {
                        Log.v("command_up", "d");
                        String message = "d";
                        new Thread(new MainActivity.Thread2(message)).start();
                        y -= 1;
                    }
                }
            }

            time = (time+1)%7;

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

