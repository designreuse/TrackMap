package com.testapp.tracktruck;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MachineInfoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String machineID;
    private String location;
    private String distanceTravelled;
    private String currentSpeed;
    private String fuelConsumption;
    private String batteryAlertCount;
    private String engineStatus;
    private String workingTime;
    private DatabaseReference mDatabase;
    private String userID;

    private List<LatLng> allLatLng;

    private TextView id;
    private TextView coordinates;
    private TextView distTravelled;
    private TextView speed;
    private TextView fuelConsumpt;
    private TextView alertCount;
    private TextView engineOnOff;
    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_info);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_detail);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        machineID = extras.getString("MachineID");
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        id = (TextView) findViewById(R.id.textView_machine_id);
        coordinates = (TextView) findViewById(R.id.textView_coordinates);
        distTravelled = (TextView) findViewById(R.id.textView_distance_travelled);
        speed = (TextView) findViewById(R.id.textView_speed);
        fuelConsumpt = (TextView) findViewById(R.id.textView_fuel);
        alertCount = (TextView) findViewById(R.id.textView_alertCount);
        engineOnOff = (TextView) findViewById(R.id.textView_engine);
        time = (TextView) findViewById(R.id.textView_working_hours);

        final ScrollView mainScrollView = (ScrollView) findViewById(R.id.scrollView2);
        ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);
        // allows scrolling of google map
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
        displayData();
    }

    private void displayData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w("MachineInfoActivity", dataSnapshot.getKey());
                LatLng origin = null;
                LatLng dest = null;
                for(DataSnapshot user : dataSnapshot.getChildren()) {
                    for(DataSnapshot machine : user.getChildren()) {
                        if(machine.getKey().equals(machineID)) {
                            userID = user.getKey();

                            location = String.valueOf(machine.child("currentLatitude").getValue(Double.class))+
                                    ", " + String.valueOf(machine.child("currentLongitude").getValue(Double.class));
                            currentSpeed = String.valueOf(machine.child("currentSpeed")
                                    .getValue(Double.class)) + " km/h";
                            distanceTravelled = String.valueOf(machine.child("distanceTravelled")
                                    .getValue(Double.class));
                            if(machine.child("engineStatus").getValue(Boolean.class)) {
                                engineStatus = "On";
                            }
                            else {
                                engineStatus = "Off";
                            }
                            int millis = machine.child("workingTimeMillis").getValue(Integer.class);
                            workingTime = String.format("%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                            batteryAlertCount = String.valueOf(machine.child("batteryAlertCount")
                                    .getValue(Integer.class));
                            fuelConsumption = machine.child("fuelConsumption").getValue(Double.class)
                                    .toString() + " liters per km";
                            batteryAlertCount = String.valueOf(machine.child("batteryAlertCount")
                                    .getValue(Integer.class)) + " alerts";
                            Iterator<DataSnapshot> log = machine.child("locationLog").getChildren()
                                    .iterator();
                            DataSnapshot first = log.next();
                            origin = new LatLng(first.child("latitude").getValue(Double.class),
                                    first.child("longitude").getValue(Double.class));
                            allLatLng = new ArrayList<LatLng>();
                            allLatLng.add(origin);
                            while (log.hasNext()) {
                                DataSnapshot last = log.next();
                                dest = new LatLng(last.child("latitude").getValue(Double.class),
                                        last.child("longitude").getValue(Double.class));
                                allLatLng.add(dest);
                            }


                        }

                    }

                }
                if(origin != null && dest != null) {
                    if(mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 8));
                        PolylineOptions lineOptions = new PolylineOptions();
                        lineOptions.addAll(allLatLng);
                        lineOptions.width(8f);
                        lineOptions.color(R.color.wallet_holo_blue_light);
                        mMap.addPolyline(lineOptions);
                    }

                    float[] results = new float[1];
                    Location.distanceBetween(
                            origin.latitude,
                            origin.longitude,
                            dest.latitude,
                            dest.longitude,
                            results);
                    String res = String.valueOf(results[0]/1000);
                    res += " km";
                    distTravelled.setText(res);
                }
                id.setText(machineID);
                coordinates.setText(location);
                speed.setText(currentSpeed);

                engineOnOff.setText(engineStatus);
                time.setText(workingTime);
                alertCount.setText(batteryAlertCount);
                fuelConsumpt.setText(fuelConsumption);
                if(distTravelled.getText().toString().isEmpty()) {
                    String error = "null";
                    distTravelled.setText(error);
                }






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void resetTime(View view) {
        mDatabase.child(machineID).child("workingTimeMillis").setValue(0);
        time.setText("00:00:00");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

}
