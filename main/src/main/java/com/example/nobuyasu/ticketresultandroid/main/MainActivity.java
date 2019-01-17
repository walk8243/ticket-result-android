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
        LinearLayout receiptResult = receiptResults.get(index);
        if (receiptNumber.getText().length() == 0) {
            System.out.println("receiptNumber Error");
            return;
        }
        HashMap<String, String> reviewData = new HashMap<String, String>();
        reviewData.put("entry_no", receiptNumber.getText().toString());
        reviewData.put("tel_no", phoneNumberEdit.getText().toString());
        httpRequest("https://rt.tstar.jp/lots/review", "POST", reviewData, receiptResult);
    }

    protected void httpRequest(final String urlStr, final String method, final HashMap<String, String> data, final LinearLayout target) {
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
                Matcher matcher = pattern.matcher(entryBody);
                if(matcher.find()) {
                    Log.d("Pattern", matcher.group(1));
                    Log.d("Pattern", matcher.group(3));
                    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                    final String receiptResultTitle = matcher.group(3);

                    if (result.indexOf("抽選の結果、お客様はご当選されました。") > -1) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(urlStr);
                                TextView textView1, textView2;
                                textView1 = (TextView)target.getChildAt(0);
                                textView1.setText(receiptResultTitle);
                                textView2 = (TextView)target.getChildAt(1);
                                textView2.setText("当選");
                            }
                        });
                    } else if (result.indexOf("抽選の結果、お客様は残念ながら落選となりました。") > -1) {
//                    System.out.println("落選");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(urlStr);
                                TextView textView1, textView2;
                                textView1 = (TextView)target.getChildAt(0);
                                textView1.setText(receiptResultTitle);
                                textView2 = (TextView)target.getChildAt(1);
                                textView2.setText("落選");
                            }
                        });
                    } else if (result.indexOf("抽選結果発表") > -1) {
//                    System.out.println("抽選前");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(urlStr);
                                TextView textView1, textView2;
                                textView1 = (TextView)target.getChildAt(0);
                                textView1.setText(receiptResultTitle);
                                textView2 = (TextView)target.getChildAt(1);
                                textView2.setText("抽選前");
                            }
                        });
                    } else {
//                    System.out.println(result);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(urlStr);
                                TextView textView1, textView2;
                                textView1 = (TextView)target.getChildAt(0);
                                textView1.setText(receiptResultTitle);
                                textView2 = (TextView)target.getChildAt(1);
                                textView2.setText("一致なし");
                            }
                        });
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

        LinearLayout receiptResultAdd = new LinearLayout(receiptAdd.getContext());
        receiptResultAdd.setOrientation(LinearLayout.HORIZONTAL);
        receiptResultAdd.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        receiptResultAdd.setGravity(Gravity.END);
        receiptAdd.addView(receiptResultAdd);

        TextView receiptResultTitleAdd = new TextView(receiptResultAdd.getContext());
        receiptResultTitleAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4));
        receiptResultAdd.addView(receiptResultTitleAdd);

        TextView receiptResultResultAdd = new TextView(receiptResultAdd.getContext());
        receiptResultResultAdd.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        receiptResultAdd.addView(receiptResultResultAdd);

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

    protected TextView setLinkOnTextView(TextView target, String message, String link) {
        target.setText(createSpannableString(message, link));
        target.setMovementMethod(LinkMovementMethod.getInstance());
        return target;
    }

    protected SpannableString createSpannableString(final String message, final String link) {
        SpannableString spannableString = new SpannableString(message);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            }
        }, 0, message.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    protected void openBrowser(final String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
