package com.pratik.locationtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by Pratik on 17-03-2017.
 */

public class MyDialog extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Destination Reached");
        alertDialog.setMessage("Do you want to exit navigation?");
        //alertDialog.setIcon(R.drawable.icon);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
//                Toast.makeText(getApplicationContext(),"User clicked OK button",Toast.LENGTH_SHORT ).show();
                Intent i = new Intent(MapsActivity.FINISH_ALERT);
                sendBroadcast(i);
                finish();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                Toast.makeText(getApplicationContext(),"User clicked No button",Toast.LENGTH_SHORT ).show();
            }
        });
        alertDialog.show();
    }
}