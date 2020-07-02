package com.client.myapplication.client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class camera extends AppCompatActivity {
    Button btnUp, btnDown, btnLeft, btnRight, btnOp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //设置返回键
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //方向控制按键
        btnUp = findViewById(R.id.button_up);
        btnDown = findViewById(R.id.button_down);
        btnLeft = findViewById(R.id.button_left);
        btnRight = findViewById(R.id.button_right);
        btnOp = findViewById(R.id.button_op);

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "uuuuuuuuu";
                new Thread(new MainActivity.Thread2(message)).start();
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "ddddddddd";
                new Thread(new MainActivity.Thread2(message)).start();
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "lllllllll";
                new Thread(new MainActivity.Thread2(message)).start();
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "rrrrrrrrr";
                new Thread(new MainActivity.Thread2(message)).start();
            }
        });
        btnOp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "op";
                new Thread(new MainActivity.Thread2(message)).start();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
