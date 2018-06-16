package com.ibra.chatappdemo.dialog;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialoge {


    private Context mContext;
    private ProgressDialog progressDialog;
    public ProgressDialoge(Context context) {
        this.mContext = context;
        progressDialog = new ProgressDialog(mContext);
    }

    public void showProgressDialoge(String content){
        progressDialog.setMessage(content);
        progressDialog.show();
    }

    public void dismissProgressDialoge(){
        progressDialog.dismiss();
    }
}
