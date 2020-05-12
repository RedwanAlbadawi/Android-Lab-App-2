package com.example.save_food_and_reduce_hunger;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.save_food_and_reduce_hunger.Adapter.DonationAdapter;
import com.example.save_food_and_reduce_hunger.Model.Donation;
import com.example.save_food_and_reduce_hunger.Utilities.InternetConnectivityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class CharityActivity extends DonorActivity{
    public TextView name;
    public ImageView image;
    public LinearLayout linearLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    private DonationAdapter donationAdapter;
    private List<Donation> donationList;
    private Button getButton;
    ProgressDialog progressDialog;
    private ValueEventListener valueEventListener;
    private DatabaseReference donationNodeReference;

    public static boolean isNotificationAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charity);
        initViews();
        if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){

            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notifyThis("Alert","Donation Available");
                    if(isNotificationAvailable){
                        isNotificationAvailable = false;
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };


            donationNodeReference.addValueEventListener(valueEventListener);
            progressDialog.show();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userId = firebaseUser.getUid();
                    if(dataSnapshot.exists()){
                        for(final DataSnapshot snapshot:dataSnapshot.getChildren()){

                            if(!snapshot.getKey().equals(userId)){
                                DatabaseReference userNameReference = FirebaseDatabase.getInstance().getReference("users");
                                userNameReference.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        String username = (String) map.get("name");

                                        for(DataSnapshot postSnapShot: snapshot.getChildren()){
                                            Donation donation = postSnapShot.getValue(Donation.class);
                                            donation.setDonorName(username);
                                            donationList.add(donation);
                                        }
                                        DonationAdapter donationAdapter = new DonationAdapter(CharityActivity.this,donationList,linearLayout
                                                ,name,image,getButton);
                                        recyclerView.setAdapter(donationAdapter);
                                        donationAdapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CharityActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        progressDialog.dismiss();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(CharityActivity.this, "Record Not Found! ", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(CharityActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }

            });


        }
        else{
            Toast.makeText(this, getResources().getString(R.string.connectionProblemMessage), Toast.LENGTH_SHORT).show();
        }










    }

    @SuppressLint("RestrictedApi")
    private void initViews() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Donations");
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        Button get = (Button) findViewById(R.id.btnGet);
        name = (TextView) findViewById(R.id.name1);
        image = (ImageView) findViewById(R.id.getimg);
        getButton = findViewById(R.id.btnGet);
        linearLayout = (LinearLayout) findViewById(R.id.moreInfo);
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        // for recycler view
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        donationList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Donations");
        progressDialog.setMessage("Loading please wait...");
        progressDialog.setCancelable(false);

        donationNodeReference = FirebaseDatabase.getInstance().getReference("Donations");
    }



    public void notifyThis(String title, String message) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker("We have new donation available..check it out..")
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("INFO");

        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, b.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donationNodeReference.removeEventListener(valueEventListener);
    }
}
