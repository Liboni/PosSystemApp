package com.DST.scanlable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class AboutActivity extends BaseActivity {

    private ImageView mBtnAbout;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentLayout (R.layout.activity_about);
        setTitle (getString (R.string.app_set_about));
        setBackArrow ();

        TextView txt_version = findViewById (R.id.txt_version);
        txt_version.setText ("DST1.0.0.0");
        txt_version.setText ("DST1.0.0.0");
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void toSelfSetting(Context context) {
        Intent mIntent=new Intent ();
        mIntent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            mIntent.setAction ("android.settings.APPLICATION_DETAILS_SETTINGS");
            mIntent.setData (Uri.fromParts ("package", context.getPackageName (), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            mIntent.setAction (Intent.ACTION_VIEW);
            mIntent.setClassName ("com.android.settings", "com.android.setting.InstalledAppDetails");
            mIntent.putExtra ("com.android.settings.ApplicationPkgName", context.getPackageName ());
        }
        context.startActivity (mIntent);

    }
}
