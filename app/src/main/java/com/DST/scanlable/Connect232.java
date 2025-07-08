package com.DST.scanlable;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lckj.lcrrgxmodule.factory.LcModule;
import com.rfid.PowerUtil;

import java.util.HashMap;

public class Connect232 extends AppCompatActivity {
    private static final String TAG = "COONECTRS232";
    private static String devport = "/dev/ttyS3";
    private static String devport2 = "/dev/ttyS2";
    private static final boolean DEBUG = true;
    private TextView mConectButton, txtSerialPort;
    private RadioButton mBaud57600View, mBaud115200View;
    private int mPosPort = -1;

    private static int RFID_TYPE = 0;

    private Context mContext;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_connect232);
        PowerUtil.power("1");//上电

        txtSerialPort = (TextView) findViewById(R.id.txtSerialPort);
        mConectButton = (TextView) findViewById(R.id.textview_connect);
        mBaud57600View = (RadioButton) findViewById(R.id.baud_57600);
        mBaud115200View = (RadioButton) findViewById(R.id.baud_115200);

        //TODO:2023-12-29 :根据系统接口值来连接
        /*mContext = this;
        RFIDManager rfidManager = RFIDManager.getDefaultInstance(mContext);
        Log.e(TAG, "getRFIDType --> " + rfidManager.getRFIDType() );
        if(rfidManager.getRFIDType() == 0x10 || rfidManager.getRFIDType() == 0x20){
            Reader.rrlib = new LcModule(this).createProduct();
            txtSerialPort.setText(devport);
            ConnectPort();
        } else {
            Toast.makeText(mContext, "其它异常状态值", Toast.LENGTH_SHORT).show();
        }*/

        /*Reader.rrlib = new LcModule(this).createProduct();
        ConnectPort();*/
        //TODO: end


        try {
            Reader.rrlib = new LcModule(this).createProduct();
            txtSerialPort.setText(devport);
            ConnectPort();
        } catch (Throwable ex) {
            mBaud57600View.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reader.rrlib = new LcModule().createProduct(0x20);
                    Log.e(TAG, "onClick: Reader.rrlib gx-->" + Reader.rrlib );
                }
            });
            mBaud115200View.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: --> rr ");
                    Reader.rrlib = new LcModule().createProduct(0x10);
                    Log.e(TAG, "onClick: Reader.rrlib rr-->" + Reader.rrlib );
                }
            });

            mConectButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int result;
                    try {

                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) { //安卓9
                            result = Reader.rrlib.Connect(devport2, 115200);//波特率是115200
                            if (result == 48){
                                result = Reader.rrlib.Connect(devport, 57600);//波特率是57600
                            }
                        } else { // 安卓其它版本
                            result = Reader.rrlib.Connect(devport, 115200);//波特率是115200
                            if (result == 48){
                                result = Reader.rrlib.Connect(devport, 57600);//波特率是57600
                            }
                        }

                        if (result == 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.openport_success), Toast.LENGTH_SHORT).show();
                            Intent intent;
                            intent = new Intent().setClass(Connect232.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.openport_failed), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(
                                getApplicationContext(),
                                getString(R.string.openport_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
    }

    private void ConnectPort() {
        int res;
        try{
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) { //安卓9
                res = Reader.rrlib.Connect(devport2, 115200);//波特率是115200
                if (res == 48){
                    res = Reader.rrlib.Connect(devport, 57600);//波特率是57600
                }
                Log.e(TAG, "ConnectPort: devport2 -->" + res );
            } else { // 安卓其它版本
                res = Reader.rrlib.Connect(devport, 115200);//波特率是115200
                if(res == 48){ //修改波特率
                    res = Reader.rrlib.Connect(devport, 57600);//荣睿E710模块的波特率是57600
                }
                Log.e(TAG, "ConnectPort: devport -->" + res );
            }

            if (res == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.openport_success), Toast.LENGTH_SHORT).show();
                Intent intent;
                intent = new Intent().setClass(Connect232.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {//连接失败 , res = -1
                Toast.makeText(getApplicationContext(), getString(R.string.openport_failed), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "ConnectPort: -->" + RFID_TYPE);
                ConnectPort();//递归 去连接
            }
        } catch (Exception e) {
            Log.e(TAG, "ConnectPort Exception: --> " + RFID_TYPE);
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.openport_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
//        Log.e(TAG, "onDestroy: RFID_TYPE --> " + RFID_TYPE );
        super.onDestroy();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    static HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static float volumnRatio;
    private static AudioManager am;


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
