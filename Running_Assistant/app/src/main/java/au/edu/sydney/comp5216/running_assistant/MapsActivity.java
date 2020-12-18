package au.edu.sydney.comp5216.running_assistant;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    //The GoogleMap variable and camera position
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // A default location (Sydney, Australia) and default zoom to use when no location permission
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    // The last-known location retrieved by the Fused Location Provider. And the penultimate location
    private Location mLastKnownLocation = new Location("");
    private Location PreviousLocation = new Location("");
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private long start_time;
    private long end_time;
    //Save distance and running_time
    private long Distance;
    private long running_time;
    //First means first time to get current location and add start point on polyline
    private boolean first = true;
    // Start for determining whether the tracker is started
    private boolean start = false;

    //Database, Polyline on map and TextView showing current tracking state
    RunDataDB db;
    RunDataDao Datadao;

    PolylineOptions rectOptions = new PolylineOptions();

    TextView Tracking = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve location and camera position
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_maps);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Set SupportMapFragment when the map is ready.
        setUpMapIfNeeded();
        //TextView showing current tracking state
        Tracking = findViewById(R.id.Tracking);
        db = RunDataDB.getDatabase(this.getApplication().getApplicationContext());
        Datadao = db.RunDataDao();

    }
    //Set SupportMapFragment when the map is ready.
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Ask for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.(same function can be used to add points on polyline)
        getDeviceLocation();
    }

    //Start tracking the location and drawing the points on polyline
    public void onStartrun(View view) {
        if (start == false) {
            start = true;
            first = true;
            mMap.clear();
            updateLocationUI();
            //Reset the last polyline
            rectOptions = new PolylineOptions();
            //Start the timer
            start_time = System.nanoTime();
            Distance = 0;
            //Loop tracking
            mHanlder.postDelayed(task, 1000);
            //Show the tracking starts
            Tracking.setText("Tracking");
        }

    }

    public void onFinish(View view) {
        if (start) {
            start = false;
            //Stop Loop
            mHanlder.removeCallbacks(task);
            //Stop timer
            end_time = System.nanoTime();
            running_time = end_time - start_time;
            //Save all data in database
            saveItemsToDatabase();
            //Show the tracking stops
            Tracking.setText("Not Tracking");
        }
    }

    //Back to mainActivity
    public void onBack(View view) {
        //Save the data if tracking hasn't stopped
        if (start) {
            mHanlder.removeCallbacks(task);
            end_time = System.nanoTime();
            running_time = end_time - start_time;
            saveItemsToDatabase();
        }
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        this.setResult(1, intent);
        this.finish();
    }

    private void getLocationPermission() {
        //Request location permission, results will be handled in onRequestPermissionsResult
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

   // Handles the result of the request for location permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    //Updates the map's UI settings based on whether the user has granted location permission.
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true); // false to disable my location button
                mMap.getUiSettings().setZoomControlsEnabled(true); // false to disable zoom controls
                mMap.getUiSettings().setCompassEnabled(true); // false to disable compass
                mMap.getUiSettings().setRotateGesturesEnabled(true); // false to disable rotate gesture
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //Gets the current location, positions the map's camera and add points of polyline. The main function of tracker
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Obtain the current location of the device
                            mLastKnownLocation = task.getResult();
                            String currentOrDefault = "Current";
                            //Position the camera
                            if (mLastKnownLocation != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                //Set the PreviousLocation and the first point of polyline in first loop
                                if (first) {
                                    rectOptions.add(new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()));
                                    PreviousLocation = mLastKnownLocation;
                                    first = false;
                                }
                                //Only add points when displacement is bigger than 1m
                                if (mLastKnownLocation.distanceTo(PreviousLocation) >= 1) {
                                    rectOptions.add(new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()));
                                    Distance += mLastKnownLocation.distanceTo(PreviousLocation);
                                    Log.i("distance", Distance + rectOptions.getPoints().toString());
                                    PreviousLocation = mLastKnownLocation;
                                }
                            } else {
                                Log.d("1", "Current location is null. Using defaults.");
                                currentOrDefault = "Default";
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                                // Set current location to the default location
                                mLastKnownLocation = new Location("");
                                mLastKnownLocation.setLatitude(mDefaultLocation.latitude);
                                mLastKnownLocation.setLongitude(mDefaultLocation.longitude);
                            }
                        } else {
                            Log.d("1", "Current location is null. Using defaults.");
                            Log.e("1", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //Set a handler for loop the tracking
    private Handler mHanlder = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            getDeviceLocation();
            mMap.clear();
            mMap.addPolyline(rectOptions);
            mHanlder.sendEmptyMessage(1);
            // 1 sec as cycle
            mHanlder.postDelayed(this, 1 * 1000);
        }
    };
    //Save all data to database
    private void saveItemsToDatabase() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                long current = System.currentTimeMillis();
                //Current date and time
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(current);
                //Distance and running time
                String distance = Distance + "";
                String run_time = running_time / 1000000000 + "";
                //Speed and pace
                double speed = (double) Distance * 1000000000 / running_time;
                double pace;
                //Week of the year
                int week = Getweek(time);
                // Avoid pace as infinity when distance is 0
                if (Distance == 0) {
                    pace = 0;
                } else {
                    pace = (double) running_time / (Distance * 1000000000);
                }
                String date = time;
                RunData run_statistics = new RunData(distance, run_time, pace, speed, date, week);
                Datadao.insert(run_statistics);
                Log.i("Save run statistics", "distance:" + distance + ",and run_time is " + run_time + ",and Pace is " + pace + ",and speed is " + speed + "and the week is" + week);
                return null;
            }
        }.execute();
    }
    //Get the week of the year
    private int Getweek(String date) {
        Date time = null;
        try {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(time);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
}
