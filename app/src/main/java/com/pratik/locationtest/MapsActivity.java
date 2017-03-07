package com.pratik.locationtest;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener,
        ResultCallback<Status>{

    protected ArrayList<Geofence> mGeofenceList;

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    List<LatLng>polyline_points, signal_coords;
    Polyline polyline;
    PolylineOptions polylineOptions;
    Location mLastLocation;
    boolean setMarker;
    String id;
    Button startNavigation, stopNavigation;
    TextView tbtNavigation;
    boolean navigation_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

        SharedPrefs sharedPrefs = new SharedPrefs(this);
        id = sharedPrefs.getPrefs(SharedPrefs.USER_ID,null);

        setContentView(R.layout.activity_maps);
        startNavigation = (Button)findViewById(R.id.b_start_navigation);
        stopNavigation = (Button)findViewById(R.id.b_quit_navigation);
        tbtNavigation = (TextView) findViewById(R.id.tv_tbt_directions);
        startNavigation.setOnClickListener(this);
        stopNavigation.setOnClickListener(this);
        navigation_flag=false;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polyline_points=new ArrayList<LatLng>();
        signal_coords=new ArrayList<LatLng>();

        String response = getIntent().getStringExtra("Directions");
//        Log.i(TAG, response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray signals = jsonObject.getJSONArray("signals");
            for(int i=0; i<signals.length(); i++){
                JSONObject signal = signals.getJSONObject(i);
                JSONObject location = signal.getJSONObject("location");
                String signal_lat = location.getString("latitude");
                String signal_lon = location.getString("longitude");
                LatLng signal_loc = new LatLng(Double.parseDouble(signal_lat),Double.parseDouble(signal_lon));
                signal_coords.add(signal_loc);
            }
            String directionsString = jsonObject.getString("directions");
            JSONObject directions = new JSONObject(directionsString);
            JSONArray routes = directions.getJSONArray("routes");
            JSONObject overview = routes.getJSONObject(0).getJSONObject("overview_polyline");
            String points = overview.getString("points");
            polyline_points = PolyUtil.decode(points);

//            drawPolyLine(polyline_points);
//            JSONObject overview = jsonObject.getJSONObject("overview_polyline");
            Log.i(TAG, String.valueOf(signal_coords));

            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        drawPolyLine(polyline_points, signal_coords);
            //mMap.addMarker(new MarkerOptions().position(lastLoc).title("Current Location"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if(Build.VERSION.SDK_INT>=23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
           setMarker=true;
            LatLng lastLoc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLoc,15));
        }


        startUpdates();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates= result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    0x1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    private void startUpdates() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
        if(Build.VERSION.SDK_INT>=23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 0x1:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Networking.sendLocationUpdate(id,String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),String.valueOf(location.getBearing()));
        if(navigation_flag){
            RepositionCamera(location);
        }
    }

    private void drawPolyLine(List<LatLng> points, List<LatLng>signals) {
        polylineOptions = new PolylineOptions();
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setPoints(points);

        for(int i=0; i<signals.size(); i++){
            mMap.addMarker(new MarkerOptions().position(signals.get(i)).icon(BitmapDescriptorFactory.fromResource(R.drawable.traffic_signal)));
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("POINT_KEYS", (ArrayList<? extends Parcelable>) polyline_points);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        polyline_points=savedInstanceState.getParcelableArrayList("POINT_KEYS");
        polylineOptions = new PolylineOptions();
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setPoints(polyline_points);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.b_start_navigation){
            Toast.makeText(getApplicationContext(), "Starting Navigation", Toast.LENGTH_SHORT).show();
            startNavigation.setVisibility(View.GONE);
            tbtNavigation.setVisibility(View.VISIBLE);
            stopNavigation.setVisibility(View.VISIBLE);
            navigation_flag=true;
            mMap.getUiSettings().setAllGesturesEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            RepositionCamera(mLastLocation);
        }else if(view.getId() == R.id.b_quit_navigation){
            Toast.makeText(getApplicationContext(), "Stopping Navigation", Toast.LENGTH_SHORT).show();
            startNavigation.setVisibility(View.VISIBLE);
            tbtNavigation.setVisibility(View.GONE);
            stopNavigation.setVisibility(View.GONE);
            navigation_flag=false;
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    public void RepositionCamera(Location location){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(20)                   // Sets the zoom
                .bearing(location.getBearing())                // Sets the orientation of the camera to east
                .tilt(75)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }
}
