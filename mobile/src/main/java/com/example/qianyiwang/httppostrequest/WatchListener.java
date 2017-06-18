package com.example.qianyiwang.httppostrequest;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by qianyiwang on 2/7/17.
 */

public class WatchListener extends WearableListenerService {
    public static String START_ACTIVITY_PATH = "/from-watch";
    public static final String BROADCAST_ACTION = "message_from_watch";
    Intent broadCastIntent;
    URL url;
    private static final String urlString = "https://dynamicchart.mybluemix.net/receiveData/";

    @Override
    public void onCreate() {
        super.onCreate();
        broadCastIntent = new Intent(BROADCAST_ACTION);
        Toast.makeText(this, "WatchListener Bind", Toast.LENGTH_SHORT).show();

        try {
            url = new URL(urlString);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "WatchListener Unbind", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY_PATH)){
            String msg_watch = new String(messageEvent.getData());
            if(msg_watch.contains("hr:")){
                int hr = Integer.parseInt(msgParsing(msg_watch));
                HttpPostRequest httpPost = new HttpPostRequest();
                httpPost.execute(hr);
            }

            Log.e("WatchListener",msg_watch);
        }
    }

    // parse hr value
    private String msgParsing(String msg){
        int pos = msg.indexOf(':');
        return msg.substring(pos+1);
    }

    // **********send HTTP post request**********************
    public class HttpPostRequest extends AsyncTask<Integer, Void, String> {
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

                    // send response to main app
                    broadCastIntent.putExtra("msg_broadcast", id+resultToDisplay+" data:"+i);
                    sendBroadcast(broadCastIntent);

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
