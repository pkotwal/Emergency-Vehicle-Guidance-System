package com.pratik.locationtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Pratik on 17-01-2017.
 */

public class FlashScreen extends AppCompatActivity {
    String response;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_flash_screen);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            response = extras.getString("Data");
            if(response!=null)
                Log.i("Flash Screen",response);
        }
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {

                SharedPrefs sharedPrefs = new SharedPrefs(FlashScreen.this);
                String id = sharedPrefs.getPrefs(SharedPrefs.USER_ID, null);

                if(id == null){
                    startActivity(new Intent(getApplicationContext(),Login.class));
                    finish();
                }else{
                    Intent i =new Intent(getApplicationContext(),PickDestination.class);
                    if(response!=null){
                        i.putExtra("Data", response);
                    }
                    startActivity(i);
                    finish();
                }


            }
        }, 3000);
    }
}
