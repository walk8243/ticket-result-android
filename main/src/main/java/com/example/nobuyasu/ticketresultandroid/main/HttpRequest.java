package com.example.nobuyasu.ticketresultandroid.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class HttpRequest {
    protected String getRequest(final URL url, final HashMap<String, String> data) throws Exception {
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

                return result;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return new String();
    }

    protected String postRequest(final URL url, final HashMap<String, String> data) throws Exception {
        try {
//            System.out.println(url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            String postParam = new String();
            for (String key : data.keySet()) {
                postParam = postParam.concat("&"+key+"="+data.get(key));
            }
//            System.out.println(postParam.substring(1));
            OutputStream outputStream = null;
            outputStream = urlConnection.getOutputStream();
            outputStream.write(postParam.substring(1).getBytes("UTF-8"));
            outputStream.flush();

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
                if(outputStream != null) {
                    outputStream.close();
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
