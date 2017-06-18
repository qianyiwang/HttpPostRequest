package com.example.qianyiwang.httppostrequest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Random;

public class MainApp extends AppCompatActivity {

    Button httpPost;
    TextView responseText;
    URL url;
    private static final String urlString = "https://dynamicchart.mybluemix.net/receiveData/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        httpPost = (Button)findViewById(R.id.httpPost);
        httpPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                int  n = rand.nextInt(100) + 1;
                HttpPostRequest httpPost = new HttpPostRequest();
                httpPost.execute(n);
            }
        });

        try {
            url = new URL(urlString);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        responseText = (TextView)findViewById(R.id.responseText);
    }

    public class HttpPostRequest extends AsyncTask<Integer, Void, String>{
        String resultToDisplay = "";
        int id;
        @Override
        protected String doInBackground(Integer... ints) {

            for (int i : ints) {
                JSONObject jsonParam = new JSONObject();
                try {
                    HttpURLConnection client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod("POST");
                    client.setRequestProperty("Name", "Data");
                    client.setDoOutput(true);
                    jsonParam.put("Name", "Temperature");
                    jsonParam.put("Data", i);
                    Log.e("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(client.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();
                    Log.i("STATUS", String.valueOf(client.getResponseCode()));
                    Log.i("MSG", client.getResponseMessage());
                    resultToDisplay = client.getResponseMessage();
                    id = client.getResponseCode();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseText.setText(id + "--" + resultToDisplay);
                        }
                    });

                    client.disconnect();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return resultToDisplay;
        }
    }
}
