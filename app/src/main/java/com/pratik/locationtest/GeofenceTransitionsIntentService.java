package com.pratik.locationtest;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

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
    public static String DIRECTION_TEXT = "directions";
    public static String DURATION_TEXT = "duration";
    public static String DISTANCE_TEXT = "distance";
    public static String ACTION_LOCATION_BROADCAST = GeofenceTransitionsIntentService.class.getName() + "LocationBroadcast";

    boolean destination_reached = false;

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
        String userID = prefs.getPrefs(SharedPrefs.USER_ID, null);

        if (destination_reached == false) {
            if (description.contains("End Directions")) {
                if (description.contains("Dwelling")) {
                    Log.i(TAG, "End Direcions here");
                    destination_reached = true;
//                Log.i(TAG+" d", destination_reached+"");
                    Intent dialogIntent = new Intent(getBaseContext(), ExitNavigationDialog.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(dialogIntent);
                }
            } else if (description.contains("Signal") && description.contains("Entering")) {
                Log.i(TAG, "Entered Area Message not sent");
            } else if (description.contains("Signal") && description.contains("Exiting")) {
                Log.i(TAG, "Exited Area Message sent");
                try {
                    if (Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Signal ", "")) < MapsActivity.SIGNAL_LENGTH - 1)
                        Networking.sendSignalChangeMessage(userID,
                                MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Signal ", "")))
                                        .getString("signalGroup"),
                                MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Signal ", "")) + 1)
                                        .getString("_id"),
                                MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Signal ", "")) + 1)
                                        .getString("signalGroup")
                        );
                    else
                        Networking.sendSignalChangeMessage(userID,
                                MapsActivity.signals.getJSONObject(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Signal ", "")))
                                        .getString("signalGroup"), "-1", "-1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (description.contains("Direction") && description.contains("Entering")) {
                Log.i(TAG, MapsActivity.directions.get(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Direction ", ""))));
                sendBroadcastMessage(MapsActivity.directions.get(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Direction ", ""))),
                        MapsActivity.dists.get(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Direction ", ""))),
                        MapsActivity.times.get(Integer.parseInt(event.getTriggeringGeofences().get(0).getRequestId().replace("Direction ", ""))));
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
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
            status = "Dwelling ";
        return status + TextUtils.join(", ", triggeringIDs);
    }

    private void sendBroadcastMessage(String route, String dist, String time) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(DIRECTION_TEXT, route);
        intent.putExtra(DURATION_TEXT, time);
        intent.putExtra(DISTANCE_TEXT, dist);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}