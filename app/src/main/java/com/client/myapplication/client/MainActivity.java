package com.client.myapplication.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    Thread Thread1 = null;      //线程1用于建立连接
    EditText etIP, etPort;      //服务器ip、端口输入框
    static TextView tvMessages;     //消息框内容
    static EditText etMessage;      //编辑框
    Button btnSend, btnUp, btnDown, btnLeft, btnRight, btnOp, btnTracking;
    Switch aSwitch;

    String SERVER_IP;   //服务器ip
    int SERVER_PORT;    //服务器端口

    boolean switchOn = false;     //是否开启连接
    Socket socket;      // 建立socket连接
    public static PrintWriter output;   //输出至服务器的内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ip和端口
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        etIP.setText("192.168.1.1");
        etPort.setText("8266");

        tvMessages = findViewById(R.id.tvMessages);
        tvMessages.setMovementMethod(ScrollingMovementMethod.getInstance());
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        //方向控制按键
        btnUp = findViewById(R.id.button_up);
        btnDown = findViewById(R.id.button_down);
        btnLeft = findViewById(R.id.button_left);
        btnRight = findViewById(R.id.button_right);
        btnOp = findViewById(R.id.button_op);

        //跟踪模式按键
        btnTracking = findViewById(R.id.Tracking);
        aSwitch = findViewById(R.id.switch_connect);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "uuuuuuuuu";
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "ddddddddd";
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "lllllllll";
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "rrrrrrrrr";
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        btnOp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "op";
                if (!switchOn){
                    ToastUtil.showToast(MainActivity.this, "请先连接服务器再进行本操作");
                }else{
                    new Thread(new Thread2(message)).start();
                }
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !switchOn){
                    //选中状态 且已经打开过（因为是一直监听开关的开启情况）
                    SERVER_IP = etIP.getText().toString().trim();
                    SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                    Thread1 = new Thread(new Thread1());
                    Thread1.start();
                    switchOn = true;

                }else if(!isChecked && switchOn){
                    //未选中状态 且选过一次 且选过的那次连接成功
                    try {
                        socket.close();
                        tvMessages.append("Disconnected from server!\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    switchOn = false;
                }
            }
        });
    }

    public void TrackingMode(View view){
        //切换跟随模式
        Intent intent = new Intent(this, camera.class);
        startActivity(intent);
    }

    public static class ToastUtil {

        private static Toast toast;

        @SuppressLint("ShowToast")
        public static void showToast(Context context,
                                     String content) {
            if (toast == null) {
                toast = Toast.makeText(context, content, Toast.LENGTH_LONG);
            } else {
//                toast.setText(content);
                toast.cancel();
                toast = Toast.makeText(context, content, Toast.LENGTH_LONG);
            }
            toast.show();
        }

    }


    public class Thread1 implements Runnable {
        @Override
        public void run() {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                tvMessages.append("Connected to server!\n");

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }


    public static class Thread2 implements Runnable {
        //线程2用于发送数据至服务器
        private String message;

        Thread2(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            tvMessages.append("client: " + message + "\n");
            etMessage.setText("");
        }
    }
}


