package com.testapp.tracktruck.data;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.testapp.tracktruck.AllDriversActivity;
import com.testapp.tracktruck.AllMachinesDisplayActivity;
import com.testapp.tracktruck.R;

import java.util.ArrayList;
import java.util.HashMap;


public class DriverIDAdapter extends RecyclerView.Adapter<DriverIDAdapter.ViewHolder> {

    private ArrayList<String> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public CardView mCardView;
        public Button mEditButton;
        public Button mMachinesButton;
        public Button mDeleteButton;
        public TextView mDriverId;
        public EditText newDriverId;
        public Button mSaveButton;
        public Button mDiscardButton;
        public ConstraintLayout mEditLayout;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
            mDriverId = (TextView) v.findViewById(R.id.driver_id);
            mEditButton = (Button) v.findViewById(R.id.button_edit);
            mMachinesButton = (Button) v.findViewById(R.id.button_all_machines);
            mDeleteButton = (Button) v.findViewById(R.id.button_delete);
            mEditLayout = (ConstraintLayout) v.findViewById(R.id.edit_driver_name);
            mSaveButton = (Button) v.findViewById(R.id.save_new_name);
            mDiscardButton = (Button) v.findViewById(R.id.discard_change);
            newDriverId = (EditText) v.findViewById(R.id.edit_name);


            mEditButton.setOnClickListener(this);
            mMachinesButton.setOnClickListener(this);
            mDeleteButton.setOnClickListener(this);
            mSaveButton.setOnClickListener(this);
            mDiscardButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if(mEditButton.getId() == viewId) {
                mEditLayout.setVisibility(View.VISIBLE);
            }
            else if(mSaveButton.getId() == viewId) {
                final String oldName = mDriverId.getText().toString();
                final String newName = newDriverId.getText().toString();
                final DatabaseReference mDatabase = FirebaseDatabase
                        .getInstance()
                        .getReference("users");
                final DatabaseReference usrRef = mDatabase
                        .child(oldName);
                final HashMap<String, Object> usrInfo = new HashMap<>();

                usrRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            usrInfo.put(ds.getKey(), ds.getValue());
                        }
                        if(!newName.isEmpty()) {
                            HashMap<String, Object> newId = new HashMap<>();
                            newId.put(newName, " ");
                            mDatabase.updateChildren(newId);
                            mDatabase.child(newName).setValue(usrInfo);
                            mDatabase.child(oldName).removeValue();
                            mDriverId.setText(newName);
                            mEditLayout.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }
            else if(mDiscardButton.getId() == viewId) {
                mEditLayout.setVisibility(View.GONE);
            }
            else if (mMachinesButton.getId() == viewId) {
                Intent intent = new Intent(mMachinesButton.getContext(),
                        AllMachinesDisplayActivity.class);
                mMachinesButton.getContext().startActivity(intent);
            }
            else if (mDeleteButton.getId() == viewId) {
                final DatabaseReference mDatabase = FirebaseDatabase
                        .getInstance()
                        .getReference("users");
                mDatabase.child(mDriverId.getText().toString()).removeValue();
                mCardView.removeAllViews();

            }
        }

    }


    public DriverIDAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DriverIDAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.driver_viewholder, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(DriverIDAdapter.ViewHolder holder, int position) {
        holder.mDriverId.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
