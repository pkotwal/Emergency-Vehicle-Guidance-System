package com.pratik.locationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pratik on 17-01-2017.
 */

public class Login extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "Login Tag";
    EditText name, last_name, reg1, reg2, reg3, reg4;
    RadioGroup radioGroup;
    RadioButton radioButton;
    Button submit;
    String n, reg, type;
    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TODO: Verify Name and Vehicle number
        // TODO: Switch Focus
        name = (EditText)findViewById(R.id.et_login_name);
        last_name = (EditText)findViewById(R.id.et_login_last_name);
        reg1 = (EditText)findViewById(R.id.et_login_vehicle_reg1);
        reg2 = (EditText)findViewById(R.id.et_login_vehicle_reg2);
        reg3 = (EditText)findViewById(R.id.et_login_vehicle_reg3);
        reg4 = (EditText)findViewById(R.id.et_login_vehicle_reg4);

        radioGroup = (RadioGroup)findViewById(R.id.rg_login_rg);
        submit = (Button)findViewById(R.id.b_login_submit);

        dialog=new ProgressDialog(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG,"Token is:"+ token);
        n = name.getText().toString() + " " + last_name.getText().toString();
        String r1 = reg1.getText().toString();
        String r2 = reg2.getText().toString();
        String r3 = reg3.getText().toString();
        String r4 = reg4.getText().toString();
        final int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radioButton = (RadioButton) findViewById(selectedId);
        reg = r1+"-"+r2+"-"+r3+"-"+r4;
        type = radioButton.getText().toString();

        if(n.length()>0 && r1.length()>0 && r2.length()>0 && r3.length()>0 && r4.length()>0){
            dialog.displayDialog("Signing In...");
            StringRequest request = new StringRequest(Request.Method.POST, ApiDetails.connect_site+"/login",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG,response);
                            try {
                                dialog.dismissDialog();
                                JSONObject jsonResponse = new JSONObject(response);
                                String status = jsonResponse.getString("state");
                                if(status.contentEquals("SUCCESS")){
                                    JSONObject user = jsonResponse.getJSONObject("user");
                                    String name = user.getString("name");
                                    String id = user.getString("_id");
                                    String type = user.getString("vehicle_type");
                                    String registration = user.getString("vehicle_registration");

                                    SharedPrefs sharedPrefs = new SharedPrefs(getApplicationContext());
                                    sharedPrefs.addPrefs(SharedPrefs.USER_ID,id);
                                    sharedPrefs.addPrefs(SharedPrefs.NAME,name);
                                    sharedPrefs.addPrefs(SharedPrefs.VEHICLE_REGISTRATION,registration);
                                    sharedPrefs.addPrefs(SharedPrefs.VEHICLE_TYPE,type);
                                    sharedPrefs.addPrefs(SharedPrefs.FCM_ID, token);
                                    startActivity(new Intent(getApplicationContext(), PickDestination.class));
                                    finish();
                                }else{
                                    String message = jsonResponse.getString("message");
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:
                    params.put("name", n);
                    params.put("registration", reg);
                    params.put("type", type);
                    params.put("fcm", token);
                    return params;
                }
            };
            AppController.getInstance().addToRequestQueue(request);
        }else {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
        }
    }
}
