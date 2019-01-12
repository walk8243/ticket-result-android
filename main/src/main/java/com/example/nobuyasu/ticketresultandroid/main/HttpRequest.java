package com.example.nobuyasu.ticketresultandroid.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HttpRequest {
    protected void httpRequest(final String urlStr, final String method) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = new String();
                try {
                    URL url = new URL(urlStr);
                    if (method == "GET") {
                        result = getRequest(url);
                    } else if (method == "POST") {
                        result = postRequest(url);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                System.out.println(result);
            }
        }).start();
    }

    protected String getRequest(final URL url) throws Exception {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();

            if(statusCode == HttpURLConnection.HTTP_OK){
                String result = new String();
                //responseの読み込み
                final InputStream in = urlConnection.getInputStream();
                final InputStreamReader inReader = new InputStreamReader(in);
                final BufferedReader bufferedReader = new BufferedReader(inReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    result = result.concat(line);
                }
                bufferedReader.close();
                inReader.close();
                in.close();

                System.out.println(result.length());
                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return new String();
    }

    protected String postRequest(final URL url) throws Exception {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();

            if(statusCode == HttpURLConnection.HTTP_OK){
                String result = new String();
                //responseの読み込み
                final InputStream in = urlConnection.getInputStream();
                final InputStreamReader inReader = new InputStreamReader(in);
                final BufferedReader bufferedReader = new BufferedReader(inReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    result.concat(line);
                }
                bufferedReader.close();
                inReader.close();
                in.close();

                return result;
            }
        } catch (Exception ex) {
            throw ex;
        }

        return new String();
    }
}
