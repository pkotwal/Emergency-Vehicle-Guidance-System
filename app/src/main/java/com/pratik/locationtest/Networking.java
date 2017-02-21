package com.pratik.locationtest;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pratik on 26-01-2017.
 */

public class Networking {

    public static void sendLocationUpdate(final String user_id, final String latitude, final String longitude, final String bearing){
        StringRequest request = new StringRequest(Request.Method.POST, ApiDetails.connect_site+"/locationUpdate",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

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
                params.put("user_id",user_id);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("bearing", bearing);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(request);
    }
}
