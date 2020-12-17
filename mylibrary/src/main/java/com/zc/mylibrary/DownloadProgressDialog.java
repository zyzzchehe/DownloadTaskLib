package com.zc.mylibrary;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;


/**
 * <p> Title: CustomProgressDialog.java </p>
 * <p> Description: </p>
 * <p> Copyright：Copyrigth (c) 2014 </p>
 * <p> Company:Monda Group </P>
 *
 * @version 1.0.0
 * @Time 2014年11月19日
 */
public class DownloadProgressDialog extends Dialog {
    CircleProgressBar circleProgressBar;
    public DownloadProgressDialog(Context context) {
        super(context,R.style.Theme_AppCompat_Light_Dialog);
        this.setContentView(R.layout.download_progress_bar);
        setCancelable(true);
        circleProgressBar = findViewById(R.id.m_bar);
    }
    public void setMessage(CharSequence message) {
        ((TextView) findViewById(R.id.tv_dialog_title)).setText(message);
    }

    public void setPercent(float value){
        circleProgressBar.setProgress((int) value);
    }
}