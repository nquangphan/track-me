package com.phannhatquang.trackme.ui.maps.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.phannhatquang.trackme.R;
import com.phannhatquang.trackme.TrackMeApplication;
import com.phannhatquang.trackme.data.model.MyLocation;
import com.phannhatquang.trackme.data.model.TableSession;
import com.phannhatquang.trackme.services.LocationUpdatesService;
import com.phannhatquang.trackme.services.LocationUpdatesService_;
import com.phannhatquang.trackme.utils.AppState;
import com.phannhatquang.trackme.utils.PermissionUtils;
import com.phannhatquang.trackme.utils.SharePref_;
import com.phannhatquang.trackme.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@EActivity(R.layout.activity_maps)
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    @Extra
    boolean isHistory;

    @Extra
    String historySessionID;
    @Extra
    boolean isStartNewSession;

    private GoogleMap mMap;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;


    private Polyline polyline;

    private double distance = 0;
    private long time = 0;

    ArrayList<LatLng> _listLocation = new ArrayList<LatLng>();
    TableSession historySession;

    @ViewById(R.id.tvDistance)
    TextView tvDistance;
    @ViewById(R.id.tvSpeed)
    TextView tvSpeed;
    @ViewById(R.id.tvTime)
    TextView tvTime;

    @ViewById(R.id.btnStart)
    ImageButton btnStart;
    @ViewById(R.id.btnPause)
    ImageButton btnPause;
    @ViewById(R.id.btnRestart)
    ImageButton btnRestart;
    @ViewById(R.id.btnStop)
    ImageButton btnStop;
    @ViewById(R.id.lnPause)
    LinearLayout lnPause;


    @Pref
    SharePref_ mSharePref;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService_.LocalBinder binder = (LocationUpdatesService_.LocalBinder) service;
            mService = binder.getService();
            if (isStartNewSession) {
                mService.requestLocationUpdates(true);
            } else {
                mService.requestLocationUpdates();
            }

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @AfterViews
    protected void afterViews() {

        myReceiver = new MyReceiver();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupButtonState() {
        if (isHistory == true) {
            _enableStartButton();
            _loadRunningStateFromLocalDatabase();
            return;
        }

        switch (mSharePref.currentState().getOr(0)) {
            case AppState.IDLE:
                _enableStartButton();
                break;
            case AppState.RUNNING: {
                _enablePause();
                _loadRunningStateFromLocalDatabase();
                break;
            }
            case AppState.PAUSE: {
                _enablePauseSession();
                _loadRunningStateFromLocalDatabase();
                break;
            }
        }
    }

    private void _loadRunningStateFromLocalDatabase() {
        _listLocation.clear();
        new GetDataBaseTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService_.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isHistory) {
            LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                    new IntentFilter(LocationUpdatesService_.ACTION_BROADCAST));
            LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                                                                         @Override
                                                                         public void onReceive(Context context, Intent intent) {
                                                                             int timer = intent.getIntExtra(LocationUpdatesService_.EXTRA_TIME, 0);
                                                                             _setTimerUI(timer);
                                                                         }
                                                                     },
                    new IntentFilter(LocationUpdatesService_.ACTION_BROADCAST_TIME));
            if (isStartNewSession)
                onButtonStartClick();
        }
        _clearMap();
        setupButtonState();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (mSharePref.currentState().getOr(-1) == AppState.RUNNING) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want finish this session?")
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onButtonPauseClick();
                            finish();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            finish();
        }
    }

    private void _setTimerUI(long timer) {
        this.time = timer;
        int hours = (int) timer / 3600;
        int remainder = (int) timer - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        tvTime.setText(timeToString(hours) + ":" + timeToString(mins) + ":" + timeToString(secs));
    }

    String timeToString(int time) {
        return time > 9 ? String.valueOf(time) : "0" + time;
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
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

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService_.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MapsActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
                LatLng _myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (mSharePref.currentState().getOr(0) == AppState.RUNNING) {
                    _listLocation.add(_myLocation);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(_myLocation, 19));
                if (_listLocation.size() == 1) {

                    if (mSharePref.currentState().getOr(0) == AppState.RUNNING) {
                        mMap.addMarker(new MarkerOptions()
                                .position(_listLocation.get(0)).title("Start"));
                    } else {
                        mService.removeLocationUpdates();
                    }
                } else {
                    if (_listLocation.size() > 0) {

                        // A geodesic polyline that goes around the world.
                        polyline = mMap.addPolyline(new PolylineOptions()
                                .add(_listLocation.get(_listLocation.size() - 1), _listLocation.get(_listLocation.size() - 2))
                                .width(20)
                                .color(Color.BLUE)
                                .geodesic(true)
                                .clickable(false));
                        _calculateSpeed(location);
                        _calculateDistance();

                    }

                }
            }
        }
    }

    private void _calculateDistance() {
        distance += Utils.calculateDistance(_listLocation.get(_listLocation.size() - 1), _listLocation.get(_listLocation.size() - 2));
        tvDistance.setText(String.format("%.2f", distance) + " m");
    }

    private void _calculateSpeed(Location location) {
        int speed = (int) ((location.getSpeed() * 3600) / 1000);
        tvSpeed.setText(String.valueOf(speed) + " km/h");
    }

    private void _drawOldSession() {
        if (_listLocation.size() > 0) {
            mMap.addMarker(new MarkerOptions()
                    .position(_listLocation.get(0)).title("Start"));
            if (isHistory) {
                mMap.addMarker(new MarkerOptions()
                        .position(_listLocation.get(_listLocation.size() - 1)).title("End"));
                tvSpeed.setText(String.format("%.2f", historySession.speed) + " km/h");

                _setTimerUI(historySession.time);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(_listLocation.get(0), 19));
            }
            polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(_listLocation)
                    .width(20)
                    .color(Color.BLUE)
                    .geodesic(true)
                    .clickable(false));
        }

        tvDistance.setText(String.format("%.2f", distance) + " m");

    }


    void _enablePause() {
        btnStart.setVisibility(View.GONE);
        lnPause.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
    }

    void _enablePauseSession() {
        btnStart.setVisibility(View.GONE);
        lnPause.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    void _enableStartButton() {
        btnStart.setVisibility(View.VISIBLE);
        lnPause.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        tvDistance.setText("0.0 m");
        tvSpeed.setText("0 km/h");
        tvTime.setText("--:--:--");
    }

    void _clearMap() {
        if (mMap != null) {
            mMap.clear();
        }
        if (_listLocation != null) {
            _listLocation.clear();
        }
    }

    void startSession() {
        _clearMap();
        mSharePref.currentSessionID().put(String.valueOf(UUID.randomUUID()));
        mSharePref.currentState().put(AppState.RUNNING);
        if (mService != null) {
            mService.requestLocationUpdates(true);
            _enablePause();
        }
    }

    @Click(R.id.btnStart)
    void onButtonStartClick() {
        if (isHistory) {
            LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                    new IntentFilter(LocationUpdatesService_.ACTION_BROADCAST));
            LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                                                                         @Override
                                                                         public void onReceive(Context context, Intent intent) {
                                                                             int timer = intent.getIntExtra(LocationUpdatesService_.EXTRA_TIME, 0);
                                                                             _setTimerUI(timer);
                                                                         }
                                                                     },
                    new IntentFilter(LocationUpdatesService_.ACTION_BROADCAST_TIME));

            isHistory = false;
            historySessionID = "";
            _enableStartButton();
        }
        startSession();
    }

    @Click(R.id.btnRestart)
    void onButtonRestartClick() {
        _clearMap();
        startSession();
    }

    @Click(R.id.btnStop)
    void onButtonStopClick() {
        mSharePref.currentState().put(AppState.IDLE);
        _enableStartButton();
        _clearMap();
        finish();
    }

    @Click(R.id.btnPause)
    void onButtonPauseClick() {
        mSharePref.currentState().put(AppState.PAUSE);
        mService.removeLocationUpdates();
        final TableSession session = new TableSession();
        session.distance = distance;
        session.time = this.time;
        session.speed = (distance / 1000) / (this.time / 3600.0);
        session.id = mSharePref.currentSessionID().getOr("");
        long now = System.currentTimeMillis();
        session.stateDate = now - this.time;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TrackMeApplication.getInstance()
                        .getAppDatabase().sessionDAO().insertAll(session);
            }
        });
        mMap.addMarker(new MarkerOptions()
                .position(_listLocation.get(_listLocation.size() - 1)).title("End"));
        _enablePauseSession();
    }


    public class GetDataBaseTask<T> extends AsyncTask<Object, Void, T> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected T doInBackground(Object... objects) {
            //....
            String sessionID;
            if (isHistory) {
                sessionID = historySessionID;
                historySession = TrackMeApplication.getInstance()
                        .getAppDatabase()
                        .sessionDAO().loadSessionByID(sessionID);
            } else {
                sessionID = mSharePref.currentSessionID().getOr("");
            }
            List<MyLocation> lastLocations = TrackMeApplication.getInstance()
                    .getAppDatabase()
                    .locationDao()
                    .loadAllBySessionIds(sessionID);
            Log.d("TRACK_ME", lastLocations.toString());
            for (MyLocation location : lastLocations
            ) {
                _listLocation.add(new LatLng(location.latitude, location.longitude));
                if (_listLocation.size() > 1) {
                    Log.d("TRACK_ME", "DISTANCE " + Utils.calculateDistance(_listLocation.get(_listLocation.size() - 1), _listLocation.get(_listLocation.size() - 1)));
                    distance += Utils.calculateDistance(_listLocation.get(_listLocation.size() - 1), _listLocation.get(_listLocation.size() - 2));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(T result) {
            _drawOldSession();
        }
    }
}
