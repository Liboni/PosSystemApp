package com.DST.scanlable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ConnectServer extends BaseActivity {
    public static final String CONNECTSERVER = "CONNECTSERVER";
    private TextView mConectButton;
    private EditText txtServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentLayout(R.layout.activity_connect_server);
        setTitle("Server Connection");
        setBackArrow();

        txtServer = findViewById(R.id.txtServer);
        mConectButton = findViewById(R.id.textview_connect_server);

        // Single click listener
        mConectButton.setOnClickListener(v -> {
            new Thread(() -> {
                HttpURLConnection conn = null;
                try {
                    String serverUrl = txtServer.getText().toString().trim();

                    // Basic URL validation
                    if (serverUrl.isEmpty()) {
                        runOnUiThread(() ->
                                Toast.makeText(getApplicationContext(), "Please enter server URL", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    // Add http:// if no protocol specified
                    if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                        serverUrl = "http://" + serverUrl;
                    }

                    URL url = new URL(serverUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setInstanceFollowRedirects(false);

                    int responseCode = conn.getResponseCode();

                    // Consider 2xx and 3xx as successful connection
                    if (responseCode >= 200 && responseCode < 400) {
                        String finalServerUrl = serverUrl;
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.openport_success), Toast.LENGTH_SHORT).show();

                            // Save the server URL - use 'this' or getApplicationContext() instead of mContext
                            SharedPreferences preferences = getSharedPreferences(CONNECTSERVER, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("SERVER", finalServerUrl);
                            editor.apply();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(getApplicationContext(),
                                        "Server responded with code: " + responseCode, Toast.LENGTH_SHORT).show()
                        );
                    }

                } catch (MalformedURLException e) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(),
                                    "Invalid URL format", Toast.LENGTH_SHORT).show()
                    );
                } catch (ConnectException e) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(),
                                    "Cannot connect to server", Toast.LENGTH_SHORT).show()
                    );
                } catch (SocketTimeoutException e) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(),
                                    "Connection timeout", Toast.LENGTH_SHORT).show()
                    );
                } catch (IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(),
                                    "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.openport_failed), Toast.LENGTH_SHORT).show()
                    );
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }).start();
        });
    }
}