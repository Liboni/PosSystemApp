package com.DST.scanlable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectServer extends BaseActivity {
    public static final String CONNECTSERVER = "CONNECTSERVER";
    private TextView mConectButton;
    private EditText txtServer;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentLayout(R.layout.activity_connect_server);
        setTitle ("Server Connection");
        setBackArrow ();

        txtServer = findViewById(R.id.txtServer);
        mConectButton = findViewById(R.id.textview_connect_server);
        mConectButton.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                URL url = new URL(txtServer.getText().toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Toast.makeText(getApplicationContext(), getString(R.string.openport_success), Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = mContext.getSharedPreferences(CONNECTSERVER, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("SERVER", txtServer.getText().toString());
                    editor.apply();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.openport_failed), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.openport_failed),
                        Toast.LENGTH_SHORT).show();
            }
            }).start();
        });
    }
}
