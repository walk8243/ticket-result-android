package com.example.nobuyasu.ticketresultandroid.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    final HttpRequest HttpRequest = new HttpRequest();
    EditText phoneNumber;
    EditText receiptNumber;
    TextView receiptResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = (EditText) findViewById(R.id.phone_number);
        receiptNumber = (EditText) findViewById(R.id.receipt1_number);
        receiptResult = (TextView) findViewById(R.id.receipt1_result);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewTicket();
            }
        });
    }

    protected void reviewTicket() {
        if (phoneNumber.getText().length() == 0) {
            System.out.println("phoneNumber Error");
            return;
        }
        if (receiptNumber.getText().length() == 0) {
            System.out.println("receiptNumber Error");
            return;
        }
        HashMap<String, String> reviewData = new HashMap<String, String>();
        reviewData.put("entry_no", receiptNumber.getText().toString());
        reviewData.put("tel_no", phoneNumber.getText().toString());
        httpRequest("https://rt.tstar.jp/lots/review", "POST", reviewData);
    }

    protected void httpRequest(final String urlStr, final String method, final HashMap<String, String> data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = new String();
                try {
                    URL url = new URL(urlStr);
                    if (method == "GET") {
                        result = HttpRequest.getRequest(url, data);
                    } else if (method == "POST") {
                        result = HttpRequest.postRequest(url, data);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                if (result.indexOf("抽選の結果、お客様はご当選されました。") > -1) {
                    System.out.println("当選");
                    receiptResult.setText("当選");
                } else if (result.indexOf("抽選の結果、お客様は残念ながら落選となりました。") > -1) {
                    System.out.println("落選");
                    receiptResult.setText("落選");
                } else if (result.indexOf("抽選結果発表") > -1) {
                    System.out.println("抽選前");
                    receiptResult.setText("抽選前");
                } else {
//                    System.out.println(result);
                    System.out.println("抽選申し込み番号または電話番号が違います。");
                    receiptResult.setText("抽選申し込み番号または電話番号が違います。");
                }
            }
        }).start();
    }
}
