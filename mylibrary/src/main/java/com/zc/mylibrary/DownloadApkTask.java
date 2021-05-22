package com.zc.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadApkTask extends AsyncTask<String, Float, String> {
    private static final String TAG = "DownloadApkTask";
    //构造方法初始化，服务
    private Context context;
    private MHandler mHandler;

    public DownloadApkTask(Context context) {
        this.context = context;
        mHandler = new MHandler();
    }

    /**
     * onPreExecute:执行后台耗时操作前被调用,通常用于进行初始化操作.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mHandler.sendEmptyMessage(100);
    }

    class MHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                DownloadProgressDialogUtils.showProgressDialog(context, "正在升级APK，请稍等");
            }
        }
    }

    /**
     * 必须重写,异步执行后台线程要完成的任务,耗时操作将在此方法中完成.
     * 返回保存apk绝对路径
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(String... params) {
        Log.i(TAG, "doInBackground: ");
        //删除方法  删除某个文件夹下的文件，及其文件 获取到绝对路径
        DataCleanManager.deleteFilesByDirectory2(getDownloadDir().getAbsolutePath());
        final String downloadUrl = params[0];
        //创建文件，保存文件的名字
        final File file = new File(getDownloadDir(), "update_new.apk");
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //网络下载
        HttpURLConnection httpConnection = null;
        InputStream is = null; //字节输入流
        FileOutputStream fos = null; //字节输出流
        int updateTotalSize = 0; //字节总数
        URL url;
        try {
            url = new URL(downloadUrl); //创建url
            httpConnection = (HttpURLConnection) url.openConnection(); //获取到里流
            httpConnection.setConnectTimeout(5000);
            httpConnection.setReadTimeout(5000);
            int statusCode = httpConnection.getResponseCode();
            Log.i(TAG, "doInBackground: statusCode = "+statusCode);
            if (statusCode != 200) {
                return null;
            }
            //获取到流的中总字节总数
            updateTotalSize = httpConnection.getContentLength();
            if (file.exists()) {
                if (updateTotalSize == file.length()) {
                    // 下载完成
                    return file.getAbsolutePath();
                } else {
                    file.delete();
                }
            }
            //生成新的文件
            file.createNewFile();
            is = httpConnection.getInputStream();
            fos = new FileOutputStream(file, false);
            byte buffer[] = new byte[1024];
            int readSize = 0;
            long currentSize = 0;
            float progress = 0;
            while ((readSize = is.read(buffer)) > -1) {
                fos.write(buffer, 0, readSize);
                currentSize += readSize;
                float value = (currentSize * 100 / updateTotalSize);
                if (value != progress) {
                    progress = value;
                    publishProgress(progress);
                }
            }
            // download success
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 当在doInBackground方法中调用publishProgress方法更新任务执行进度后,
     * 将调用此方法.通过此方法我们可以知晓任务的完成进度.
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        Log.i(TAG, "onProgressUpdate: " + values[0]);
        DownloadProgressDialogUtils.updateProgress(values[0]);
    }

    /**
     * 当doInBackground方法完成后,系统将自动调用此方法,
     * 并将doInBackground方法返回的值传入此方法.通过此方法进行UI的更新.
     *
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i(TAG, "onPostExecute: apk save path = " + s);
        DownloadProgressDialogUtils.closeProgressDialog();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File apkFile = new File(s);
        try {
            //兼容7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                //兼容8.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        startInstallPermissionSettingActivity();
                        return;
                    }
                }
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                context.startActivity(intent);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /*if(event.getMsg().equals("MSG_INSTALL_APK")){
        Log.i(TAG, "onEventMainThread: MSG_INSTALL_APK");
        ProgressDialogUtil.closeProgressDialog();
        File file = new File((String) event.getData());
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setAction(android.content.Intent.ACTION_VIEW);
        mIntent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        startActivity(mIntent);
    }*/

    private File getDownloadDir() {
        File downloadDir = null;
        //如果SD卡正常挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //非空判断
            downloadDir = new File(context.getExternalCacheDir(), "update");
        } else {//如果sd卡卸载了，获取到缓存目录，
            downloadDir = new File(context.getCacheDir(), "update");
        }
        //如果文件不存在，创建出来
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir;
    }
}