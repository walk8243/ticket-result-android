package com.example.nobuyasu.ticketresultandroid.main;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "TicketResultAndroid";

    final HttpRequest HttpRequest = new HttpRequest();
    LinearLayout receiptBox;
    EditText phoneNumberEdit;
    List<EditText> receiptNumbers = new ArrayList<EditText>();
    List<TextView> receiptResults = new ArrayList<TextView>();
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(PREFS_NAME, 0);
        String phoneNumber = settings.getString("PhoneNumber", "09011112222");

        receiptBox = (LinearLayout) findViewById(R.id.receipts);
        phoneNumberEdit = (EditText) findViewById(R.id.phone_number);
        phoneNumberEdit.setText(phoneNumber);

        String receiptNumberStr = settings.getString("ReceiptNumber", "");
        for (String receiptNumber : receiptNumberStr.split(",")) {
            addReceipt(receiptNumber);
        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumberEdit.getText().length() == 0) {
                    System.out.println("phoneNumber Error");
                    return;
                } else {
                    setSettingsPhoneNumber(phoneNumberEdit.getText().toString());
                }

                for (int i=0; i<receiptNumbers.size(); ++i) {
//                    System.out.println(receiptNumbers.get(i).getText());
                    reviewTicket(i);
                }
                setSettingsReceiptNumber();
            }
        });
        findViewById(R.id.add_receipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReceipt("");
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
        reviewData.put("tel_no", phoneNumberEdit.getText().toString());
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
//                    System.out.println("当選");
                    target.setText("当選");
                } else if (result.indexOf("抽選の結果、お客様は残念ながら落選となりました。") > -1) {
//                    System.out.println("落選");
                    target.setText("落選");
                } else if (result.indexOf("抽選結果発表") > -1) {
//                    System.out.println("抽選前");
                    target.setText("抽選前");
                } else {
//                    System.out.println(result);
                    target.setText("一致なし");
                }
            }
        }).start();
    }

    protected void addReceipt(String receiptNum) {
        LinearLayout receiptAdd = new LinearLayout(findViewById(R.id.receipts).getContext());
        receiptAdd.setOrientation(LinearLayout.HORIZONTAL);
        receiptAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptAdd.setGravity(Gravity.END);
        receiptBox.addView(receiptAdd);

        EditText receiptNumberAdd = new EditText(receiptAdd.getContext());
        receiptNumberAdd.setText(receiptNum);
        receiptNumberAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
        receiptAdd.addView(receiptNumberAdd);

        TextView receiptResultAdd = new TextView(receiptAdd.getContext());
        receiptResultAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        receiptAdd.addView(receiptResultAdd);

        receiptNumbers.add(receiptNumberAdd);
        receiptResults.add(receiptResultAdd);
    }

    protected void setSettingsPhoneNumber(String phoneNumber) {
//        System.out.println(phoneNumber);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("PhoneNumber", phoneNumber);
        editor.commit();
    }

    protected void setSettingsReceiptNumber() {
        String receiptNumberStr = new String();
        for (EditText receiptNumber : receiptNumbers) {
            if (receiptNumber.getText().length() > 0) {
                receiptNumberStr = receiptNumberStr.concat("," + receiptNumber.getText().toString());
            }
        }
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ReceiptNumber", receiptNumberStr.substring(1));
        editor.commit();
    }
}
