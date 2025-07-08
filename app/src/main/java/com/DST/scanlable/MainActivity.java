package com.DST.scanlable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.DST.scanlable.adapter.FragmentAdapter;
import com.DST.scanlable.fragment.ReadFragment;
import com.DST.scanlable.fragment.ReadTagFragment;
import com.DST.scanlable.utils.SPUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rfid.PowerUtil;
import com.rfid.trans.ReaderParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavView;
    private FragmentAdapter adapter;
    private Fragment fg;

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private List<String> mPermissionList;
    private final int mRequestCode = 100;
    private AlertDialog mPermissionDialog;

    private static HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static AudioManager am;
    private long exitTime = 0;
    private String comPort = "/dev/ttyS3";

    //TODO:2023-12-27
    private static final String TAG = "LJH########";
    //TODO:END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        initPermission();
        setTitle(getString(R.string.tab_scan));
        setToolBarMenuOnclick(this);
        mViewPager = findViewById(R.id.view_pager);
        mBottomNavView = findViewById(R.id.bottom_nav_view);

        mViewPager.setOffscreenPageLimit(3);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ReadFragment());
        fragments.add(new ReadTagFragment());
        //fragments.add(new SettingFragment());
        adapter = new FragmentAdapter(fragments, getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        fg = adapter.getItem(0);

        mBottomNavView.setOnNavigationItemSelectedListener(menuItem -> {
            int menuId = menuItem.getItemId();
            switch (menuId) {
                case R.id.tab_one:
                    setTitle(getString(R.string.tab_scan));
                    mViewPager.setCurrentItem(0);
                    break;
                case R.id.tab_two:
                    setTitle(getString(R.string.tab_rw));
                    mViewPager.setCurrentItem(1);
                    break;
//                case R.id.tab_three:
//                    setTitle(getString(R.string.tab_param));
//                    mViewPager.setCurrentItem(2);
//                    break;
            }
            return false;
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    setTitle(getString(R.string.tab_scan));
                } else if (i == 1) {
                    setTitle(getString(R.string.tab_rw));
//                }
//                else if (i == 2) {
//                    setTitle(getString(R.string.tab_param));
                }
                mBottomNavView.getMenu().getItem(i).setChecked(true);
                fg = adapter.getItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        initSound();
        initSet();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initSet() {
        int i = Reader.rrlib.GetModuleVersion();
        if (i == 1){
            int tidlen = SPUtils.getInt(MainActivity.this, "tidlen", 0);
            int tidptr = SPUtils.getInt(MainActivity.this, "tidptr", 0);
            int qvalue = SPUtils.getInt(MainActivity.this, "qvalue", 4);
            int scanTime = SPUtils.getInt(MainActivity.this, "ScanTime", 0);
            int session = SPUtils.getInt(MainActivity.this, "session", 0);
            int jgTimes = SPUtils.getInt(MainActivity.this, "jgTimes", 0);
            ReaderParameter param = Reader.rrlib.GetInventoryParameter();
            param.QValue = qvalue;
            param.Session = session;
            param.TidLen = tidlen;
            param.TidPtr = tidptr;
            param.ScanTime = scanTime;
            int Session = session;
            if (Session == 4) Session = 255;
            param.Session = Session;
            param.Interval = jgTimes * 10;
            Reader.rrlib.SetInventoryParameter(param);
        }
    }

    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        //Reader.rrlib.setsoundid(soundMap.get(1), soundPool);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
        Reader.rrlib.DisConnect();
        PowerUtil.power("0");
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_btn_export) {
            if (fg instanceof ReadFragment) {
                ((ReadFragment) fg).exportExcel();
            }else if (fg instanceof ReadTagFragment) {
                ((ReadTagFragment) fg).exportExcel();
            }
            else {
                Toast.makeText(this, getString(R.string.toast_export_clear), Toast.LENGTH_SHORT).show();
            }
        }
        else if (menuItem.getItemId() == R.id.menu_btn_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        else if (menuItem.getItemId() == R.id.menu_btn_server) {
            Intent intent = new Intent(this, ConnectServer.class);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, getString(R.string.toast_exit), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                return true;
            }
        }
        if (fg instanceof ReadFragment) {
            ((ReadFragment) fg).onKeyDown(keyCode, event);
        }
        else if (fg instanceof ReadTagFragment) {
            ((ReadTagFragment) fg).onKeyDown(keyCode, event);
        }
        return false;
    }

    private void initPermission() {
        mPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        if (!mPermissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            if (hasPermissionDismiss) {
                showPermissionDialog();
            }
        }
    }

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动开启")
                    .setPositiveButton("确定", (dialog, which) -> cancelPermissionDialog())
                    .setNegativeButton("取消", (dialog, which) -> cancelPermissionDialog())
                    .create();
        }
        mPermissionDialog.show();
    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }
}