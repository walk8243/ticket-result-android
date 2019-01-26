package com.example.nobuyasu.ticketresultandroid.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "TicketResultAndroid";

    final HttpRequest HttpRequest = new HttpRequest();
    final Handler handler = new Handler();
    ReceiptInfo receiptInfo;
    LinearLayout receiptBox;
    EditText phoneNumberEdit;
    List<EditText> receiptNumbers = new ArrayList<EditText>();
    List<LinearLayout> receiptResults = new ArrayList<LinearLayout>();
    SharedPreferences settings;
    String regex = "(第.+希望).+?(<span>(.+?)</span>)";
    Pattern pattern = Pattern.compile(regex);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(PREFS_NAME, 0);
        receiptInfo = new ReceiptInfo(settings.getString("PhoneNumber", "09011112222"), settings.getString("ReceiptNumber", ""));

        receiptBox = (LinearLayout) findViewById(R.id.receipts);
        phoneNumberEdit = (EditText) findViewById(R.id.phone_number);
        phoneNumberEdit.setText(receiptInfo.getPhoneNumber());
        for (String receiptNumber : receiptInfo.getReceiptNumbers()) {
            addReceipt(receiptNumber);
        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiptInfo.setPhoneNumber(phoneNumberEdit.getText().toString());
                if (phoneNumberEdit.getText().length() == 0) {
                    Log.e("R.id.submit", "phoneNumber Error");
                    return;
                }

                String receiptNumberStr = new String();
                for (int i=0; i<receiptNumbers.size(); ++i) {
//                    Log.d("receiptNumber["+(i+1)+"]", receiptNumbers.get(i).getText().toString());
                    receiptNumberStr = receiptNumberStr.concat(",").concat(receiptNumbers.get(i).getText().toString());
                    reviewTicket(i);
                }
                receiptInfo.setReceiptNumberStr(receiptNumberStr.substring(1));
                setSettings();
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
        LinearLayout receiptResult = receiptResults.get(index);
        if (receiptNumber.getText().length() == 0) {
            Log.e("reviewTicket", "receiptNumber Error");
            return;
        }
        HashMap<String, String> reviewData = new HashMap<String, String>();
        reviewData.put("entry_no", receiptNumber.getText().toString());
        reviewData.put("tel_no", phoneNumberEdit.getText().toString());
        httpRequest("https://rt.tstar.jp/lots/review", "POST", reviewData, receiptResult);
    }

    protected void httpRequest(final String urlStr, final String method, final HashMap<String, String> data, final LinearLayout target) {
        if (target.getChildCount() > 1) {
            target.removeViews(1, target.getChildCount()-1);
        }

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

                String entryBody = result.substring(result.indexOf("<h2 class=\"title\">お申込内容</h2>") + "<h2 class=\"title\">お申込内容</h2>".length());
//                Log.d("response body", entryBody);
                while (true) {
                    Matcher matcher = pattern.matcher(entryBody);
                    if (matcher.find()) {
//                        Log.d("Pattern", matcher.group(1));
//                        Log.d("Pattern", matcher.group(3));
                        final String receiptResultTitle = matcher.group(3);

                        if (result.indexOf("抽選の結果、お客様はご当選されました。") > -1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addReceiptResult(target, receiptResultTitle, "当選");
                                }
                            });
                        } else if (result.indexOf("抽選の結果、お客様は残念ながら落選となりました。") > -1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addReceiptResult(target, receiptResultTitle, "落選");
                                }
                            });
                        } else if (result.indexOf("抽選結果発表") > -1) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addReceiptResult(target, receiptResultTitle, "抽選前");
                                }
                            });
                        } else {
                            Log.d("チケット結果 一致なし", result);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    addReceiptResult(target, receiptResultTitle, "一致なし");
                                }
                            });
                        }
//                        Log.d("Last matcher", String.valueOf(matcher.end()));
                        entryBody = entryBody.substring(matcher.end());
                    } else {
                        break;
                    }
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

        receiptNumbers.add(receiptNumberAdd);
        receiptResults.add(receiptAdd);
    }

    protected void addReceiptResult(LinearLayout linearLayout, String title, String result) {
        LinearLayout receiptResultAdd = new LinearLayout(linearLayout.getContext());
        receiptResultAdd.setOrientation(LinearLayout.HORIZONTAL);
        receiptResultAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptResultAdd.setGravity(Gravity.END);
        linearLayout.addView(receiptResultAdd);

        TextView titleAdd = new TextView(receiptResultAdd.getContext());
        titleAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4));
        titleAdd.setText(title);
        receiptResultAdd.addView(titleAdd);

        TextView resultAdd = new TextView(receiptResultAdd.getContext());
        resultAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        resultAdd.setText(result);
        receiptResultAdd.addView(resultAdd);
    }

    protected void setSettings() {
//        Log.d("PhoneNumber", receiptInfo.getPhoneNumber());
//        Log.d("ReceiptNumber", receiptInfo.getReceiptNumberStr());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("PhoneNumber", receiptInfo.getPhoneNumber());
        editor.putString("ReceiptNumber", receiptInfo.getReceiptNumberStr());
        editor.commit();
    }

    protected void openBrowser(final String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
