package com.testapp.tracktruck;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private static final String LOG = "MapsActivity";

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    private ArrayMap<String, Marker> allMarkers;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        allMarkers = new ArrayMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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
        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.3325152,30.2124584), 2));
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.w(LOG, "Accessing: " + dataSnapshot.getKey());
                for(DataSnapshot machine : dataSnapshot.getChildren()) {
                    Log.w(LOG, machine.getKey());
                    if(machine.hasChild("currentLatitude")
                            && machine.hasChild("currentLongitude")) {
                        Double lat = machine.child("currentLatitude").getValue(Double.class);
                        Double lng = machine.child("currentLongitude").getValue(Double.class);
                        if((boolean) machine.child("engineStatus").getValue()) {
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(lat,lng))
                                    .icon((BitmapDescriptorFactory.fromResource(R.drawable.marker_green)));
                            Marker marker = mMap.addMarker(markerOptions);
                            allMarkers.put(machine.getKey(), marker);
                        }
                        else {
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(lat,lng))
                                    .icon((BitmapDescriptorFactory.fromResource(R.drawable.marker_red)));
                            Marker marker = mMap.addMarker(markerOptions);
                            allMarkers.put(machine.getKey(), marker);
                        }
                }

                }
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(,lng), 4));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.w(LOG, "Updating: " + dataSnapshot.getKey());
                for(DataSnapshot machine : dataSnapshot.getChildren()) {
                    if(machine.hasChild("currentLatitude")
                            && machine.hasChild("currentLongitude")) {
                        MarkerOptions markerOptions;
                        Double lat = machine.child("currentLatitude").getValue(Double.class);
                        Double lng = machine.child("currentLongitude").getValue(Double.class);
                        if((boolean) machine.child("engineStatus").getValue()) {
                            markerOptions = new MarkerOptions()
                                    .position(new LatLng(lat,lng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                        }
                        else {
                            markerOptions = new MarkerOptions()
                                    .position(new LatLng(lat,lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
                        }
                        if(allMarkers.containsKey(machine.getKey())) {
                            Marker m = allMarkers.get(machine.getKey());
                            m.setIcon(markerOptions.getIcon());
                            m.setPosition(new LatLng(machine.child("currentLatitude")
                                    .getValue(Double.class),
                                    machine.child("currentLongitude").getValue(Double.class)));
                        }
                        else {
                            Marker marker = mMap.addMarker(markerOptions);
                            allMarkers.put(machine.getKey(), marker);
                        }

                    }

                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.w(LOG, "Removing: " + dataSnapshot.getKey());
                if(dataSnapshot.hasChild("currentLatitude")
                        && dataSnapshot.hasChild("currentLongitude")) {
                    Marker m = allMarkers.remove(dataSnapshot.getKey());
                    if(m != null) {
                        m.remove();
                    }

                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });



    }



    public void logout(MenuItem item) {
        mFirebaseAuth.signOut();
        finish();
    }

    public void showAllMachines(MenuItem item) {
        startActivity(new Intent(MapsActivity.this, AllMachinesDisplayActivity.class));

    }

    public void showAllDrivers(MenuItem item) {
        startActivity(new Intent(MapsActivity.this, AllDriversActivity.class));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(String key : allMarkers.keySet()) {
            if(marker.equals(allMarkers.get(key))) {
                Intent intent = new Intent(MapsActivity.this,
                        MachineInfoActivity.class);
                intent.putExtra("MachineID", key);
                startActivity(intent);
                return true;

            }
        }
        Toast.makeText(this, "Nothing to show!", Toast.LENGTH_SHORT).show();
        return false;
    }
}
