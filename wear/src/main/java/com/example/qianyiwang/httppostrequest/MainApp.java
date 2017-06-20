package com.example.qianyiwang.httppostrequest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainApp extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Sensor mHeartRateSensor;
    SensorManager mSensorManager;
    int hrVal;

    ImageView heart_rate_img;
    TextView heart_rate_value;
    boolean sensor_on;

    private GoogleApiClient googleApiClient;
    public static String START_ACTIVITY_PATH = "/from-watch";
    private Node mNode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // declare watch phone connection
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        // get connected nodes
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for(Node node: getConnectedNodesResult.getNodes()){
                    mNode = node;
                    Log.v("Node", String.valueOf(mNode));
                }
            }
        });

        heart_rate_img = (ImageView)findViewById(R.id.heart_rate_img);
        heart_rate_value = (TextView)findViewById(R.id.heart_rate_value);
        sensor_on = false;
        heart_rate_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!sensor_on){
                    sensor_on = true;
                    startHrSensor();
                }
                else{
                    sensor_on = false;
                    heart_rate_img.setImageResource(R.drawable.heart_rate_off);
                    heart_rate_img.clearAnimation();
                    stopHrSensor();
                }
            }
        });

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (mHeartRateSensor == null) {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor1 : sensors) {
                Log.i("Sensor Type", sensor1.getName() + ": " + sensor1.getType());
            }
        }

    }

    private void startHrSensor(){

        heart_rate_img.setImageResource(R.drawable.heart_color_big);
        heart_rate_img.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation));

        // start heart rate sensor
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);//define frequency
        Toast.makeText(this, "HR Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopHrSensor(){
        heart_rate_img.setImageResource(R.drawable.heart_rate_off);
        heart_rate_img.clearAnimation();

        mSensorManager.unregisterListener(this);
        Toast.makeText(this, "HR Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            hrVal = (int) sensorEvent.values[0];
            heart_rate_value.setText(hrVal+"");
            Log.e("Sensor:", hrVal+"");
            // send hr to phone
            SendMessageToPhone sendMessageToPhone = new SendMessageToPhone();
            sendMessageToPhone.execute("hr:"+hrVal);

        } else
            Log.d("Sensor:", "Unknown sensor type");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sensor_on){
            sensor_on = false;
            stopHrSensor();
            disconnect();
        }
    }

    public void disconnect() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("GoogleApi", "onConnected "+ bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleApi", "onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("GoogleApi", "onConnectionFailed:" + connectionResult);
    }

    public class SendMessageToPhone extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            if(mNode!=null){
                for(String s: strings){
                    Wearable.MessageApi.sendMessage(googleApiClient, mNode.getId(), START_ACTIVITY_PATH, s.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(!sendMessageResult.getStatus().isSuccess()){
                                Log.e("GoogleApi","Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                            else{
                                Log.e("GoogleApi","success");
                            }
                        }
                    });
                }
            }
            return null;
        }
    }
}
