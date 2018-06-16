package com.ibra.chatappdemo.preferenceManage;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceStart {

    public static final String IS_FIRST_TIME_PREF = "is this first time pref";
    public static final String IS_FIRST_TIME_EDITOR = "is this first time editor";
    private SharedPreferences pref;
    private Context context;

    public SharedPreferenceStart(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(IS_FIRST_TIME_PREF,Context.MODE_PRIVATE);
    }

    public void changePref(String state){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(IS_FIRST_TIME_EDITOR,state);
        editor.commit();
    }

    public boolean isFirstTime_app(){
        boolean status = false;

        if (pref.getString(IS_FIRST_TIME_EDITOR,"null").equals("null")) {

            status = false;
        }else {
            status = true;
        }
        return status;
    }

    public void clearpref(){
        if(isFirstTime_app()){
            changePref("null");
        }
    }
}
