package com.ashishbharam.remotebindingdemoclientbinderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnUnbind, btnBind, btnShowNumber;
    TextView tvShowNumber;
    private Intent serviceIntent;
    private boolean isServiceBound;

    private int randomNumberValue;
    Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;
    private static final int GET_RANDOM_NUMBER_FLAG = 0;


    class ReceiveRandomNumberHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            randomNumberValue = 0;
            if (msg.what == GET_RANDOM_NUMBER_FLAG) {
                randomNumberValue = msg.arg1;
                tvShowNumber.setText("Random Number: " + randomNumberValue);
            }
            super.handleMessage(msg);
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            randomNumberRequestMessenger = new Messenger(service);
            randomNumberReceiveMessenger = new Messenger(new ReceiveRandomNumberHandler());
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TAG", "onServiceDisconnected: " + name);
            randomNumberRequestMessenger = null;
            randomNumberReceiveMessenger = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("TAG", "MainActivity thread ID" + Thread.currentThread().getId());


        btnBind = findViewById(R.id.btnBindService);
        btnUnbind = findViewById(R.id.btnUnbindService);
        btnShowNumber = findViewById(R.id.btnShowNumber);
        tvShowNumber = findViewById(R.id.tvRandomNum);

        serviceIntent = new Intent();

        serviceIntent.setComponent(new ComponentName("com.ashishbharam.remotebindingdemoserviceapp",
                "com.ashishbharam.remotebindingdemoserviceapp.MyService"));
        serviceIntent.setPackage(getPackageName());

        btnBind.setOnClickListener(v -> {
            bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE);
            Toast.makeText(this, "Client App Service Bound", Toast.LENGTH_SHORT).show();
        });
        btnUnbind.setOnClickListener(v -> {
            if (isServiceBound) {
                unbindService(serviceConnection);
                isServiceBound = false;
                Toast.makeText(this, "Client App Service UnBound", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowNumber.setOnClickListener(v -> {
            if (isServiceBound) {
                Message requestMessage = Message.obtain(null, GET_RANDOM_NUMBER_FLAG);
                requestMessage.replyTo = randomNumberReceiveMessenger;
                try {
                    randomNumberRequestMessenger.send(requestMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                tvShowNumber.setText("Service not Bound");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceConnection = null;
    }
}