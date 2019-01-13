package com.example.nobuyasu.ticketresultandroid.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final HttpRequest HttpRequest = new HttpRequest();
    LinearLayout receiptBox;
    EditText phoneNumber;
    List<EditText> receiptNumbers = new ArrayList<EditText>();
    List<TextView> receiptResults = new ArrayList<TextView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiptBox = (LinearLayout) findViewById(R.id.receipts);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        phoneNumber.setText("09000000000");

        addReceipt("RT00000XXX00");
        addReceipt("RT00000XXX00");

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber.getText().length() == 0) {
                    System.out.println("phoneNumber Error");
                    return;
                }
                System.out.println(receiptNumbers.size());
                for (int i=0; i<receiptNumbers.size(); ++i) {
                    System.out.println(receiptNumbers.get(i).getText());
                    System.out.println(receiptResults.get(i).getText());
                    reviewTicket(i);
                }
            }
        });
    }

    protected void reviewTicket(int index) {
        EditText receiptNumber = receiptNumbers.get(index);
        TextView receiptResult = receiptResults.get(index);
        if (receiptNumber.getText().length() == 0) {
            System.out.println("receiptNumber Error");
            return;
        }
        HashMap<String, String> reviewData = new HashMap<String, String>();
        reviewData.put("entry_no", receiptNumber.getText().toString());
        reviewData.put("tel_no", phoneNumber.getText().toString());
        httpRequest("https://rt.tstar.jp/lots/review", "POST", reviewData, receiptResult);
    }

    protected void httpRequest(final String urlStr, final String method, final HashMap<String, String> data, final TextView target) {
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
                    target.setText("当選");
                } else if (result.indexOf("抽選の結果、お客様は残念ながら落選となりました。") > -1) {
                    System.out.println("落選");
                    target.setText("落選");
                } else if (result.indexOf("抽選結果発表") > -1) {
                    System.out.println("抽選前");
                    target.setText("抽選前");
                } else {
//                    System.out.println(result);
                    System.out.println("抽選申し込み番号または電話番号が違います。");
                    target.setText("抽選申し込み番号または電話番号が違います。");
                }
            }
        }).start();
    }

    protected void addReceipt(String receiptNum) {
        LinearLayout receiptAdd = new LinearLayout(findViewById(R.id.receipts).getContext());
        receiptAdd.setOrientation(LinearLayout.VERTICAL);
        receiptAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptBox.addView(receiptAdd);

        EditText receiptNumberAdd = new EditText(receiptAdd.getContext());
        receiptNumberAdd.setText(receiptNum);
        receiptNumberAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptAdd.addView(receiptNumberAdd);

        TextView receiptResultAdd = new TextView(receiptAdd.getContext());
//        receiptResultAdd.setText("ddd");
        receiptResultAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptAdd.addView(receiptResultAdd);

        receiptNumbers.add(receiptNumberAdd);
        receiptResults.add(receiptResultAdd);
    }
}
