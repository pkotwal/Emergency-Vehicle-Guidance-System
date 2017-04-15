package com.pratik.locationtest;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Pratik on 07-03-2017.
 */


public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 15;
    public static final float GEOFENCE_RADIUS_IN_METERS_LARGER = 50;

    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
}