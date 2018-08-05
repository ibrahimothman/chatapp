package com.ibra.chatappdemo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.dialog.ProgressDialoge;

public class ChangeInfoActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String CHANGE_NAME = "Change your name";
    private static final String CHANGE_STATUS = "Change your status";
    private static final String CHANGE_MOBILE = "Change your mobile";
    private static final String TAG = ChangeInfoActivity.class.getCanonicalName();
    ProgressDialoge mProgressDialoge;


    Toolbar mToolbar;
    Button btnOk, btnCancel;
    EditText changeInfotxt;
    String newInfo;
    DatabaseReference mDatabaseStatus,mDatabaseMobile,mDatabaseName;
    ProgressDialog progressDialog;
    String type,key;
    int inputType;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        // get type of change (user want to change status or name or mobile number)
        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra(SettingActivity.CHANGE_TYPE_EXTRA) != null){
            type = intent.getStringExtra(SettingActivity.CHANGE_TYPE_EXTRA);
            Log.d(TAG,"fromStatsustype is "+type);
            switch (type){
                case CHANGE_NAME:
                    key = getString(R.string.username_key);
                    inputType = InputType.TYPE_CLASS_TEXT;
                    break;
                case CHANGE_STATUS:
                    key = getString(R.string.status_key);
                    inputType = InputType.TYPE_CLASS_TEXT;
                    break;
                case CHANGE_MOBILE:
                    key = getString(R.string.mobile_key);
                    inputType = InputType.TYPE_CLASS_NUMBER;
                    break;
            }Log.d(TAG,"keyis "+key);
        }

        // setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(type);


        // setup ui views
        btnCancel = (Button)findViewById(R.id.cancel);
        btnOk = (Button)findViewById(R.id.ok);
        changeInfotxt = (EditText) findViewById(R.id.change_status_edit);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // cancel button then return to setting activity
        if(view == btnCancel){
            onBackPressed();
        }
        // ok button and change statsu
        if(view == btnOk){
            if(!TextUtils.isEmpty(key))
                changeInfo(key,inputType);
        }
    }


    // update status
    private void changeInfo(String keyChanged,int inputType) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseStatus = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_table))
                .child(uid).child(keyChanged);

//        changeInfotxt.setInputType(inputType);
        changeInfotxt.setRawInputType(inputType);
        newInfo = changeInfotxt.getText().toString();
        if(TextUtils.isEmpty(newInfo)){
            Toast.makeText(this, getString(R.string.invalid_input), Toast.LENGTH_LONG).show();
        }else{
            // setup progress dialoge
            mProgressDialoge = new ProgressDialoge(this);
            mProgressDialoge.showProgressDialoge(getString(R.string.waiting));
            mDatabaseStatus.setValue(newInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mProgressDialoge.dismissProgressDialoge();
                        Toast.makeText(ChangeInfoActivity.this, getString(R.string.status_changed), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else{
                        Toast.makeText(ChangeInfoActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                        mProgressDialoge.dismissProgressDialoge();
                    }
                }
            });

        }
    }


}
