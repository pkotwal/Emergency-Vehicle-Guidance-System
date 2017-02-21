package com.pratik.locationtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Pratik on 17-01-2017.
 */

public class FlashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_screen);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {

                SharedPrefs sharedPrefs = new SharedPrefs(FlashScreen.this);
                String id = sharedPrefs.getPrefs(SharedPrefs.USER_ID, null);

                if(id == null){
                    startActivity(new Intent(getApplicationContext(),Login.class));
                    finish();
                }else{
                    startActivity(new Intent(getApplicationContext(),PickDestination.class));
                    finish();
                }


            }
        }, 3000);
    }
}
