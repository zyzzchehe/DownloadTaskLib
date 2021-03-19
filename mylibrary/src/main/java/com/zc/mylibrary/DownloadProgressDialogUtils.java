package com.zc.mylibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;


/**
 *ProgressDialog
 */
public class DownloadProgressDialogUtils {

    private static ProgressDialog progressDialog;

    public static void showProgressDialog(Context mContext, String message) {
        closeProgressDialog();
        progressDialog = new ProgressDialog(mContext,R.style.Theme_MaterialComponents_Light_Dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        if (null != progressDialog
                && !progressDialog.isShowing()
                && !((Activity)mContext).isFinishing()) {//检查activity是否finishing!!!
            progressDialog.show();
        }
    }

    public static void showCircleProgressDialog(int maxValue,Context mContext, String message) {
        closeProgressDialog();
        progressDialog = new ProgressDialog(mContext,R.style.Theme_MaterialComponents_Light_Dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(maxValue);
        if (null != progressDialog
                && !progressDialog.isShowing()
                && !((Activity)mContext).isFinishing()) {//检查activity是否finishing!!!
            progressDialog.show();
        }
    }

    public static void updateProgress(float value){
        progressDialog.setProgress((int) value);
    }

    public static void closeProgressDialog() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;//此处一定置空，否则就容易导致下一个Activity show闪退！！！
    }

}
