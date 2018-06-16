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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibra.chatappdemo.R;
import com.ibra.chatappdemo.model.User;
import com.ibra.chatappdemo.preferenceManage.SharedPreferenceStart;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,
View.OnFocusChangeListener{
    private FirebaseAuth mAuth;

    private AppCompatEditText emailEditText;
    private AppCompatEditText passwordEditText;
    private AppCompatEditText nameEditText;
    private ProgressDialog progressDialog;
    private TextInputLayout emailInputLayout,passInputLayout,nameInputLayout;
    private AppCompatButton signUpButton;
    private Toolbar mToolbar;
    private SharedPreferenceStart pref;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        // setup toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.register_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        emailEditText = (AppCompatEditText)findViewById(R.id.email_edittext_reg);
        passwordEditText = (AppCompatEditText)findViewById(R.id.password_edittext_reg);
        nameEditText = (AppCompatEditText)findViewById(R.id.name_edittext_reg);

        emailInputLayout = (TextInputLayout)findViewById(R.id.email_textInputLayout_reg);
        passInputLayout = (TextInputLayout)findViewById(R.id.pass_textInputLayout_reg);
        nameInputLayout = (TextInputLayout)findViewById(R.id.name_textInputLayout_reg);

        signUpButton = (AppCompatButton)findViewById(R.id.btn_signup);
        signUpButton.setOnClickListener(this);

        emailEditText.setOnFocusChangeListener(this);
        passwordEditText.setOnFocusChangeListener(this);
        nameEditText.setOnFocusChangeListener(this);

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

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkTextNotEmpty(nameEditText,nameInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    // when user want to create a new account
    @Override
    public void onClick(View view) {
        if(view == signUpButton){
            createAccount();
        }
    }


    // create a new account
    private void createAccount() {
        final String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String pass = passwordEditText.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(name)){
            Toast.makeText(this, "please enter email and password", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setMessage(getString(R.string.create_account_dialog_message));
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                changeSharedPreference();
                                // get user id
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();

                                // save user data into database
                                saveUserInfoIntoDB(uid,name);

                            } else{
                                FirebaseException e = (FirebaseException) task.getException();
                                Log.d("fromregister","error is "+e.getMessage());
                                Toast.makeText(RegisterActivity.this, getString(R.string.register_error), Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.cancel();

                        }
                    });
        }
    }

    private void saveUserInfoIntoDB(String uid,String userName) {
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid);
        User user = new User();
        user.setuName(userName);
        user.setuStatus(getString(R.string.default_status));
        user.setuImage(getString(R.string.default_main_image));
        user.setuPhone(getString(R.string.default_mobile));
        user.setuThumb(getString(R.string.default_thumb_image));

        mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    launchMainActivity();
                } else
                    Toast.makeText(RegisterActivity.this, getString(R.string.register_error), Toast.LENGTH_SHORT).show();
            }});
    }


    // change preference to go main activity directly
    private void changeSharedPreference() {
        pref = new SharedPreferenceStart(this);
        pref.changePref(getString(R.string.not_first_time));
    }


    // launch main activity
    private void launchMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    // setup text input layout error
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
        else if(view == nameEditText) checkTextNotEmpty(nameEditText,nameInputLayout);
    }
}
