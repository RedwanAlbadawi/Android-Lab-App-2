package com.example.save_food_and_reduce_hunger;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.save_food_and_reduce_hunger.Model.Donation;
import com.squareup.picasso.Picasso;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
public class DonorDetailActivity extends CharityActivity {
    Button locationButton;
    Button phoneButton, mailButton, messageButton;
    TextView totalItems,donorNameTextView,foodType;
    private ImageView imageView;
    Intent intent;
    Donation donation;
    private static final int MY_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_detail);
        initViews();
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first check if user lat lng were not provided then simple display message.
                if(donation.getLat() == null ||  donation.getLng() == null){
                    Toast.makeText(DonorDetailActivity.this, "Oopss..! we are unable to get user location... ", Toast.LENGTH_SHORT).show();
                }
                else{
                    // call map activity and pass donor lat,lng inside bundle...
                    Intent mpintent = new Intent(DonorDetailActivity.this, mapviewActivity.class);
                    Bundle b = new Bundle();
                    b.putDouble("latitude", donation.getLat());
                    b.putDouble("longitude", donation.getLng());
                    mpintent.putExtras(b);
                    startActivity(mpintent);
                }


            }
        });
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // perform call operation


                if (Build.VERSION.SDK_INT < 23) {
                    phoneCall();
                }else {

                    if (ActivityCompat.checkSelfPermission(DonorDetailActivity.this,
                            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        phoneCall();
                    }else {
                        final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                        //Asking request Permissions
                        ActivityCompat.requestPermissions(DonorDetailActivity.this, PERMISSIONS_STORAGE, 9);
                    }
                }

                /*Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + donation.getDonorPhone().replace("-","")));

                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                else
                startActivity(callIntent);*/
            }
        });
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // perform mail operation
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",donation.getDonorEmail(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call message operation;
                sendSMS();
            }
        });

    }

    private void sendSMS() {
        // send sms to a particular person,
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", donation.getDonorPhone().replace("-",""));
        startActivity(smsIntent);
    }


    @SuppressLint("RestrictedApi")
    private void initViews(){
        intent = getIntent();
        donation = (Donation) intent.getSerializableExtra("donationInformation");


        locationButton = findViewById(R.id.getLoc);
        phoneButton = findViewById(R.id.phone);
        mailButton = findViewById(R.id.mail);
        messageButton = findViewById(R.id.msg);
        totalItems = findViewById(R.id.row_bag_value);
        foodType = findViewById(R.id.row_food_value);
        imageView = findViewById(R.id.img);
        donorNameTextView = findViewById(R.id.donorName);
        donorNameTextView.setText(donation.getDonorName());
        totalItems.setText(donation.getQuantity());
        if(donation.isFruits()){
            foodType.setText("Fruits");
        }
        if(donation.isMeals()){
            foodType.setText(foodType.getText()+",Meals");
        }
        if(donation.isFastFood()){
            foodType.setText(foodType.getText()+",Fast Food");
        }

        Picasso.with(getApplicationContext())
                .load(donation.getImageUrl())
                .resize(350,500)
                .into(imageView);

        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawerOpen,R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }








    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = false;
        switch(requestCode){
            case 9:
                permissionGranted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(permissionGranted){
            phoneCall();
        }else {
            Toast.makeText(DonorDetailActivity.this, "You don't assign permission.", Toast.LENGTH_SHORT).show();
        }
    }

    // to call donor use this method.
    private void phoneCall(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+donation.getDonorPhone().replace("-","")));
            startActivity(callIntent);
        }else{
            Toast.makeText(DonorDetailActivity.this, "You don't assign permission.", Toast.LENGTH_SHORT).show();
        }
    }

}
