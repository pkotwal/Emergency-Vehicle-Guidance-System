package com.pratik.locationtest;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pratik on 07-03-2017.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
            return;
        }
        String description = getGeofenceTransitionDetails(event);
//        Log.i(TAG,"Handle Intent "+description);
//        Toast.makeText(MapsActivity.this,description,Toast.LENGTH_SHORT).show();
        SharedPrefs prefs = new SharedPrefs(getApplicationContext());
        String userID = prefs.getPrefs(SharedPrefs.USER_ID,null);

        if(description.contains("Entering")){
            Log.i(TAG,"Entered Area Message not sent");
        }else if(description.contains("Exiting")){
            Log.i(TAG,"Exited Area Message sent");
            try {
                if(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId()) < MapsActivity.SIGNAL_LENGTH-1)
                    Networking.sendSignalChangeMessage(userID,
                            MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId()))
                                    .getString("signalGroup"),
                            MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId())+1)
                                    .getString("_id"),
                            MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId())+1)
                                    .getString("signalGroup")
                            );
                else
                    Networking.sendSignalChangeMessage(userID,
                            MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId()))
                                    .getString("signalGroup"),"-1","-1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString =
                GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        int geoFenceTransition = event.getGeofenceTransition();

        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringIDs);
    }
}