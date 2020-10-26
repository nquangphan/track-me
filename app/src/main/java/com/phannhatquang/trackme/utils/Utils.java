package com.phannhatquang.trackme.utils;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.phannhatquang.trackme.R;

import java.text.DateFormat;
import java.util.Date;

public class Utils {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static double calculateDistance(LatLng a, LatLng b) {
        Location locationA = new Location("point A");
        locationA.setLatitude(a.latitude);
        locationA.setLongitude(a.longitude);
        Location locationB = new Location("point B");
        locationB.setLatitude(b.latitude);
        locationB.setLongitude(b.longitude);
        Log.d("TRACK_ME", "DISTANCE: " + String.valueOf(locationA.distanceTo(locationB)));
        return locationA.distanceTo(locationB);
    }

    public static float distance (LatLng a, LatLng b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(b.latitude -a.latitude);
        double lngDiff = Math.toRadians(b.longitude-a.longitude);
        double _a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(_a), Math.sqrt(1-_a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        Log.d("TRACK_ME", "DISTANCE: " +new Float(distance * meterConversion).floatValue());
        return new Float(distance * meterConversion).floatValue();
    }
}

