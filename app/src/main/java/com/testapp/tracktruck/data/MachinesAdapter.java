package com.testapp.tracktruck.data;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.testapp.tracktruck.MachineInfoActivity;
import com.testapp.tracktruck.R;

import java.util.ArrayList;

/**
 * Created by paul on 22.04.17.
 */

public class MachinesAdapter extends RecyclerView.Adapter<MachinesAdapter.ViewHolder> {

    private ArrayList<String> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView mCardView;
        public TextView mMacineID;
        public Button mDetails;
        public Button mDeleteButton;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mMacineID = (TextView) v.findViewById(R.id.machine_id);
            mDetails = (Button) v.findViewById(R.id.button_details_machine);
            mDeleteButton = (Button) v.findViewById(R.id.button_delete_macine);

            mDetails.setOnClickListener(this);
            mDeleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if(mDetails.getId() == viewId) {
                String machineId = mMacineID.getText().toString();
                Intent intent = new Intent(mDetails.getContext(), MachineInfoActivity.class);
                intent.putExtra("MachineID", machineId);
                mDetails.getContext().startActivity(intent);
            }
            else if(mDeleteButton.getId() == viewId) {
                final DatabaseReference mDatabase = FirebaseDatabase
                        .getInstance()
                        .getReference("users");
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String machineId = mMacineID.getText().toString();
                        DatabaseReference machineRef = null;
                        for(DataSnapshot user : dataSnapshot.getChildren()) {
                            for(DataSnapshot machine : user.getChildren()) {
                                if(machineId.equals(machine.getKey())) {
                                    machineRef = machine.getRef();
                                }
                            }
                        }
                        if(machineRef != null) {
                            machineRef.removeValue();
                            mCardView.removeAllViews();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public MachinesAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }


    @Override
    public MachinesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.machine_viewholder, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MachinesAdapter.ViewHolder holder, int position) {
        holder.mMacineID.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
