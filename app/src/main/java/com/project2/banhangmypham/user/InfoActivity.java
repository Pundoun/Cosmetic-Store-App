package com.project2.banhangmypham.user;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.project2.banhangmypham.R;

public class InfoActivity extends AppCompatActivity {
    TextView thongtin;
    String info = " Hello " +
            " Day la ung dung ban my pham " +
            " Neu ban can gi hay lien he admin ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        thongtin = findViewById(R.id.thongtinungdung);

        thongtin.setText(info);
    }
}
