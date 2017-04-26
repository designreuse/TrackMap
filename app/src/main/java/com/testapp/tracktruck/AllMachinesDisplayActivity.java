package com.testapp.tracktruck;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.testapp.tracktruck.data.DriverIDAdapter;
import com.testapp.tracktruck.data.MachinesAdapter;

import java.util.ArrayList;

public class AllMachinesDisplayActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseReference mDatabase;
    private ArrayList<String> myDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_machines_display);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        myDataset = new ArrayList<>();
        createDataset();
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view_machines);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);



        mAdapter = new MachinesAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void createDataset() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot user : dataSnapshot.getChildren()) {
                    for (DataSnapshot machine : user.getChildren()) {
                        if(machine.hasChild("currentLatitude")
                                && machine.hasChild("currentLongitude")
                                && machine.hasChild("engineStatus")) {
                            myDataset.add(machine.getKey());
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
