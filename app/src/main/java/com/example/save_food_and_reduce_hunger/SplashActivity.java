package com.example.save_food_and_reduce_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.save_food_and_reduce_hunger.Utilities.DialogManager;
import com.example.save_food_and_reduce_hunger.Utilities.InternetConnectivityManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // firebase classes
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // firebase initialization
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
    }

    private void updateUI(){

        // check for internet connectivity
        if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            // if user is already logged in go inside the application
            if(firebaseUser!=null){
                String userId =  firebaseUser.getUid();
                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        // get user information

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                String role = (String) map.get("role");
                                String username = (String) map.get("name");
                                Intent intent;
                                if(role.equals("donor")){
                                    intent = new Intent(getApplicationContext(),DonorActivity.class);
                                }
                                else{
                                    intent = new Intent(getApplicationContext(),CharityActivity.class);
                                }
                                intent.putExtra("name",username);
                                startActivity(intent);
                                finish();
                            }
                        },1000);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SplashActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },3000);
            }

        }
        else{
            DialogManager.showAlertDialog(SplashActivity.this,"No Internet Connection",
                    "You are not connected to internet. check your internet connection.....",
                    R.drawable.no_internet,MainActivity.class);
        }









    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }
}
