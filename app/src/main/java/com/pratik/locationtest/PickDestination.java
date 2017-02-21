package com.pratik.locationtest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class PickDestination extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final String TAG = "Place Picker";
    String id;
    int sourseSet, destinationSet;
    Location mLastLocation, sLocation, dLocation;
    private GoogleApiClient mGoogleApiClient;
    final int PLACE_PICKER_REQUEST = 2;
    Button selectLocation, getDirections;
    TextView selectedSource, selectedSourceAddr, selectedDestination, selectedDestinationAddr;
    private LocationRequest mLocationRequest;
    ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_destination);
        selectedDestination = (TextView) findViewById(R.id.tv_pick_destination_selected_destination);
        selectedSource = (TextView) findViewById(R.id.tv_pick_destination_selected_source);
        selectedSourceAddr = (TextView) findViewById(R.id.tv_pick_destination_selected_source_addr);
        selectedDestinationAddr = (TextView) findViewById(R.id.tv_pick_destination_selected_destination_addr);
        getDirections = (Button) findViewById(R.id.b_pick_destination_get_directions);

        selectLocation = (Button) findViewById(R.id.b_pick_destination_select);
        selectLocation.setOnClickListener(this);
        getDirections.setOnClickListener(this);
        dialog=new ProgressDialog(this);

        sourseSet=0;
        destinationSet=0;

        SharedPrefs sharedPrefs = new SharedPrefs(this);
        id = sharedPrefs.getPrefs(SharedPrefs.USER_ID, null);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.b_pick_destination_select){
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }else if(view.getId() == R.id.b_pick_destination_get_directions){
            if(sourseSet==0){
                Toast.makeText(PickDestination.this, "Location not yet found", Toast.LENGTH_SHORT).show();
            }else if (destinationSet==0){
                Toast.makeText(PickDestination.this, "Destination not yet set", Toast.LENGTH_SHORT).show();
            }else{
                dialog.displayDialog("Getting Directions...");
                Log.i(TAG,"Source: "+sLocation.getLatitude()+" , "+sLocation.getLongitude());
                Log.i(TAG,"Destination: "+dLocation.getLatitude()+" , "+dLocation.getLongitude());
                StringRequest request = new StringRequest(Request.Method.POST, ApiDetails.connect_site+"/directionRequest",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

//                                Log.i(TAG,response);
                                dialog.dismissDialog();
                                Intent intent = new Intent(PickDestination.this, MapsActivity.class);
                                intent.putExtra("Directions", response);
                                startActivity(intent);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        // the POST parameters:
                        params.put("slat", String.valueOf(sLocation.getLatitude()));
                        params.put("slong", String.valueOf(sLocation.getLongitude()));
                        params.put("dlat", String.valueOf(dLocation.getLatitude()));
                        params.put("dlong", String.valueOf(dLocation.getLongitude()));
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(request);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
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
                                    PickDestination.this,
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setSmallestDisplacement(100);
        mLocationRequest.setFastestInterval(3000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, this);
                    selectedDestination.setText(place.getName());
                    selectedDestinationAddr.setText(place.getAddress());
                    selectLocation.setText("Change Destination");
                    destinationSet=1;

                    dLocation = new Location(LocationManager.GPS_PROVIDER);
                    dLocation.setLatitude(place.getLatLng().latitude);
                    dLocation.setLongitude(place.getLatLng().longitude);
                }
                break;

            case 0x1:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "GPS is needed for application to run", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            sLocation = new Location(LocationManager.GPS_PROVIDER);
            sLocation.setLatitude(mLastLocation.getLatitude());
            sLocation.setLongitude(mLastLocation.getLongitude());

            Log.i(TAG,String.valueOf(mLastLocation.getLatitude()));
            Log.i(TAG,String.valueOf(mLastLocation.getLongitude()));
            selectedSource.setText(String.valueOf(mLastLocation.getLatitude())+" , "+ String.valueOf(mLastLocation.getLongitude()));
            sourseSet=1;
            Networking.sendLocationUpdate(id, String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()), String.valueOf(location.getBearing()));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            PendingResult<PlaceLikelihoodBuffer> result2 = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result2.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    if(likelyPlaces.getCount()>0){
                        Log.i(TAG, String.format("Place '%s'",
                                likelyPlaces.get(0).getPlace().getAddress()));
                        selectedSourceAddr.setText(likelyPlaces.get(0).getPlace().getAddress());
                    }
                    likelyPlaces.release();
                }
            });
        }
    }
}
