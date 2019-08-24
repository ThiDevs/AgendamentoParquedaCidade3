package com.thiagoalves;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

class RequestTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... uri) {
        String responseString = null;
        try {
            URL url = new URL(uri[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
                // Do normal input or output stream reading

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    StringWriter writer = new StringWriter();
                    Scanner s = new Scanner(in).useDelimiter("\\A");
                    responseString = s.hasNext() ? s.next() : "";


                }
                finally {
                    urlConnection.disconnect();
                }


            }
            else {
                String response = "FAILED"; // See documentation for more info on response handling
            }
        } catch (Exception e) {
            e.printStackTrace();

            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }



}