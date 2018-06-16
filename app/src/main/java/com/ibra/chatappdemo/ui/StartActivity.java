package com.ibra.chatappdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import com.ibra.chatappdemo.R;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = StartActivity.class.getCanonicalName();
    AppCompatButton loginButton,registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        loginButton = (AppCompatButton)findViewById(R.id.btn_login_start);
        registerButton = (AppCompatButton)findViewById(R.id.btn_signup_start);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_login_start){
            Log.d(TAG,"goto login");
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);

        }else if(view.getId() == R.id.btn_signup_start){
            Log.d(TAG,"goto redister");
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        }

    }
}
