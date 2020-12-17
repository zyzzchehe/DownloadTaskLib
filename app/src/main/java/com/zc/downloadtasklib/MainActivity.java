package com.zc.downloadtasklib;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.zyzzc_permission_lib.ZyzzcPermissionUtils;
import com.zc.mylibrary.DownloadApkTask;
import com.zc.mylibrary.DownloadProgressDialogUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements ZyzzcPermissionUtils.CallBack {
    String TAG = "MainActivity";
    private ZyzzcPermissionUtils zyzzcPermissionUtils;
    private int reqCode = 100;
    private List<String> perList;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        zyzzcPermissionUtils = new ZyzzcPermissionUtils(this);
        perList = new ArrayList<>();
        perList.add(Manifest.permission.INTERNET);
        perList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        perList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        zyzzcPermissionUtils.request(perList, reqCode, this);
    }

    public void update(View view) {
        new DownloadApkTask(this).execute("http://weitu.shuduier.com/upload/borrow_20201216_1656.apk");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void grantAll() {
        Log.i(TAG, "grantAll: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断是否可以写入数据到系统
            if (!Settings.System.canWrite(context)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                i.setData(Uri.parse("package:" + context.getPackageName()));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
    @Override
    public void denied() {
        Log.i(TAG, "denied: ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == reqCode) {
            AtomicBoolean grantAll = new AtomicBoolean(true);
            //遍历每一个授权结果
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    grantAll.set(false);
                    Toast.makeText(this, permissions[i] + "未授权", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            if (grantAll.get()) {
                grantAll();
            } else {
                denied();
            }
        }
    }
}