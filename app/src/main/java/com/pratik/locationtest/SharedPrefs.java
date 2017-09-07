package com.pratik.locationtest;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Pratik on 21-01-2017.
 */

public class SharedPrefs {

    Context context;
    SharedPreferences pref;
    public static final String USER_ID="ID";
    public static final String NAME="NAME";
    public static final String VEHICLE_TYPE="VEHICLE_TYPE";
    public static final String VEHICLE_REGISTRATION="VEHICLE_REGISTRATION";
    public static final String FCM_ID="FCM_ID";

    public SharedPrefs(Context context){
        this.context=context;
        pref= context.getSharedPreferences("USER_DETAILS",context.MODE_PRIVATE);
    }

    public void addPrefs(String key,String value){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public void delPrefs(){
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public String getPrefs(String key,String def){
        String value=pref.getString(key,def);
        return value;
    }
}

