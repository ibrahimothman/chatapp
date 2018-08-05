package com.ibra.chatappdemo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.preferenceManage.SharedPreferenceStart;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener
,View.OnClickListener{
    private FirebaseAuth mAuth;
    private AppCompatEditText emailEditText;
    private AppCompatEditText passwordEditText;
    private ProgressDialog progressDialog;
    private TextInputLayout emailInputLayout,passInputLayout;
    private AppCompatButton loginButton;
    private TextView forgetPassText;
    private Toolbar mToolbar;
    SharedPreferenceStart pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.login_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        // setup firebase auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = (AppCompatEditText)findViewById(R.id.email_edittext);
        passwordEditText = (AppCompatEditText)findViewById(R.id.password_edittext);

        emailInputLayout = (TextInputLayout)findViewById(R.id.email_textInputLayout);
        passInputLayout = (TextInputLayout)findViewById(R.id.pass_textInputLayout);

        loginButton = (AppCompatButton)findViewById(R.id.btn_login);
        loginButton.setOnClickListener(this);

        forgetPassText = (TextView)findViewById(R.id.forget_password);
        forgetPassText.setOnClickListener(this);

        emailEditText.setOnFocusChangeListener(this);
        passwordEditText.setOnFocusChangeListener(this);


        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkTextNotEmpty(emailEditText,emailInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkTextNotEmpty(passwordEditText,passInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    // login into firebase auth
    private void login(){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this, getString(R.string.invalid_email_pass), Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setMessage(getString(R.string.login_dialog_message));
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
//                               String deviceToken = FirebaseInstanceId.getInstance().getToken();
//                               FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(deviceToken);
                                changeSharedPreference();
                               launchMainActivity();
                           } else{

                               Toast.makeText(LoginActivity.this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                           } progressDialog.cancel();






                        }
                    });
        }

    }


    // change preference to launch main activity directly
    private void changeSharedPreference() {
        pref = new SharedPreferenceStart(this);
        pref.changePref(getString(R.string.not_first_time));
    }


    // launch main activity
    private void launchMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(intent);
    }


    // setup text text input layout error
    private void checkTextNotEmpty(AppCompatEditText text, TextInputLayout layout){
        if(TextUtils.isEmpty(text.getText().toString())){
            layout.setErrorEnabled(true);
            layout.setError(getString(R.string.required_text));

        }else layout.setErrorEnabled(false);
    }



    // when home button is pressed
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(view == emailEditText)checkTextNotEmpty(emailEditText,emailInputLayout);
        else if(view == passwordEditText) checkTextNotEmpty(passwordEditText,passInputLayout);
    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_login){
            login();
        }else if(view == forgetPassText){
            String email = emailEditText.getText().toString();
            if(!TextUtils.isEmpty(email))
                restorePassword(email);
            else Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
    }

    // restore password if it forgot
    private void restorePassword(String email) {
        mAuth.sendPasswordResetEmail(email);
        Toast.makeText(this, getString(R.string.sendPassword), Toast.LENGTH_SHORT).show();
        
    }
}
