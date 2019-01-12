package com.example.nobuyasu.ticketresultandroid.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.nobuyasu.ticketresultandroid.main.HttpRequest;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HttpRequest HttpRequest = new HttpRequest();

        findViewById(R.id.testMethod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpRequest.httpRequest("https://qiita.com/walk8243/items/9bf842425414a8cd5ead", "GET");
//                HttpRequest.httpRequest("https://rt.tstar.jp/lots/review", "GET");
//                HttpRequest.httpRequest("https://rt.tstar.jp/lots/review", "POST");
            }
        });
    }
}
