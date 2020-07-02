package com.client.myapplication.client;

import android.annotation.SuppressLint;
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
import java.io.PrintWriter;
import java.net.Socket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend, btnUp, btnDown, btnLeft, btnRight, btnOp, btnTracking;
    Switch aSwitch;
    String SERVER_IP;
    int SERVER_PORT;
    boolean switchOnce = false;
    Socket socket;
    private PrintWriter output;

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
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                }
            }
        });
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "uuuuuuuuu";
                new Thread(new Thread3(message)).start();
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "ddddddddd";
                new Thread(new Thread3(message)).start();
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "lllllllll";
                new Thread(new Thread3(message)).start();
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "rrrrrrrrr";
                new Thread(new Thread3(message)).start();
            }
        });
        btnOp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "op";
                new Thread(new Thread3(message)).start();
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !switchOnce){
                    //选中状态 且已经打开过（因为是一直监听开关的开启情况）
                    SERVER_IP = etIP.getText().toString().trim();
                    SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                    Thread1 = new Thread(new Thread1());
                    Thread1.start();
                    switchOnce = true;

                }else if(!isChecked && switchOnce){
                    //未选中状态 且选过一次 且选过的那次连接成功
                    try {
                        socket.close();
                        tvMessages.append("Disconnected from server!\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    switchOnce = false;
                }
            }
        });
    }

    public void TrackingMode(View view){
        Intent intent = new Intent(this, camera.class);
        startActivity(intent);
    }


    class Thread1 implements Runnable {
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


    class Thread3 implements Runnable {
        private String message;

        Thread3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                }
            });
        }
    }
}


