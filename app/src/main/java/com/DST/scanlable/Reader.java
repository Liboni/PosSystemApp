package com.DST.scanlable;

import android.annotation.SuppressLint;
import android.widget.TextView;

import com.lckj.lcrrgxmodule.factory.ILcUhfProduct;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Reader {
    public static ILcUhfProduct rrlib;
    public static void writelog(String log, TextView tvResult) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String textlog = simpleDateFormat.format(date) + " " + log;
        tvResult.setText(textlog);
    }
}
