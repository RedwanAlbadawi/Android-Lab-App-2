package com.example.save_food_and_reduce_hunger;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.save_food_and_reduce_hunger.Model.Donation;
import com.example.save_food_and_reduce_hunger.Utilities.DialogManager;
import com.example.save_food_and_reduce_hunger.Utilities.FileUtils;
import com.example.save_food_and_reduce_hunger.Utilities.InternetConnectivityManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;



// this activity is used for donation


public class DonationActivity extends DonorActivity implements AdapterView.OnItemSelectedListener {
    private CircleImageView ProfileImage;
    private static final int PICK_IMAGE = 1;
    Uri imageUri;
    Spinner spinner;
    double lat;
    double longi;
    private CheckBox check1, check2, check3;
    private Button button_loc;
    private Button button_submit;
    TextView addressTextview;
    Geocoder mGeocoder;
    List<Address> mAddresses;
    LocationManager locationManager;
    private Double longitude,latitude;
    ProgressDialog progressDialog;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private StorageTask uploadTask;
    private String downloadableImagePath;
    private Donation donation;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);



        // views initialization

        donation = new Donation();
        button_loc=(Button)findViewById(R.id.btnLoc);
        button_submit=(Button)findViewById(R.id.btnSubmit) ;
        addressTextview=(TextView)findViewById(R.id.addr);
        check1 = (CheckBox) findViewById(R.id.checkBox_meal);
        check2 = (CheckBox) findViewById(R.id.checkBox_fruits);
        check3 = (CheckBox) findViewById(R.id.checkBox_fast);



        drawerLayout = findViewById(R.id.drawer);


        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);


        // setup toolbar
        setSupportActionBar(toolbar);
        // to enable toolbar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        // to remove the title of the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // setup drawer layout and navigation view.

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawerOpen,R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // firebase initialization

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Donations");
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // show progress dialog

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Donations");
        progressDialog.setMessage("Sending your food for donation please wait...");
        progressDialog.setCancelable(false);
        // Location getting
        button_loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // check gps unable or not
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        OnGPS();
                    }
                    else{
                        // if gps is enabled get user current location.
                        getLocation();
                    }
                }
            });

        // Image getting
        ProfileImage = (CircleImageView) findViewById(R.id.pf);
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get image from gallery by calling an intent..
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
            }
        });

        //spinner
        spinner = findViewById(R.id.spinner2);

        // setup  array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.quantity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Check Boxes

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataProvided()){
                    if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){
                        // if data is provided and we have internet connection.. check for the storage permission
                        if(checkPermissionREAD_EXTERNAL_STORAGE(DonationActivity.this)) {

                            // if we have storage permission get selected file path and name..
                            String filePath = FileUtils.getPath(getApplicationContext(),imageUri);
                            String filename=filePath.substring(filePath.lastIndexOf("/")+1);
                            // upload image and then store record in db
                            progressDialog.show();
                            final StorageReference fileReference = storageReference.child(filename);
                            fileReference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return fileReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        downloadableImagePath = downloadUri.toString();
                                        getDonorInformation();
                                    } else {
                                        Toast.makeText(DonationActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    else{
                        DialogManager.showAlertDialog(DonationActivity.this,getResources().getString(R.string.connectionProblemTitle),
                                getResources().getString(R.string.connectionProblemMessage),R.drawable.no_internet,null);
                    }
                }
            }
        });
    }

    private void getDonorInformation(){
        // get donor email,phone and some other related information here.
        donation.setDonorEmail(firebaseUser.getEmail());
        DatabaseReference userPhoneReference =
                FirebaseDatabase.getInstance().getReference("users")
                .child(firebaseUser.getUid());
                userPhoneReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            donation.setDonorPhone(String.valueOf(map.get("phone")));
                            CharityActivity.isNotificationAvailable = true;
                            setDatabaseValues(downloadableImagePath);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        progressDialog.dismiss();
                        Toast.makeText(DonationActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // set up donation in firebase db
    private void setDatabaseValues(String imagePath) {

        donation.setQuantity(spinner.getSelectedItem().toString());

        // check for different food items..
        if(check1.isChecked()){
            donation.setMeals(true);

        }
        if(check2.isChecked()){
            donation.setFruits(true);
        }
        if(check3.isChecked()){
            donation.setFastFood(true);
        }
        donation.setLat(latitude);
        donation.setLng(longitude);
        donation.setImageUrl(imagePath);

        databaseReference.child(firebaseUser.getUid()).push().setValue(donation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {



                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            clearFormFields();
                            Toast.makeText(DonationActivity.this, "Thank You for you donation...:)", Toast.LENGTH_SHORT).show();
                            // Donation Successfull.
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(DonationActivity.this, "Exception: "+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    private void clearFormFields() {
        check1.setChecked(false);
        check2.setChecked(false);
        check3.setChecked(false);
    }


    @Override
        public void onItemSelected (AdapterView < ? > parent, View view,int position, long id){
            String quantity = parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected (AdapterView < ? > parent){

        }



        //Finction for gallery
        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
                imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ProfileImage.setImageBitmap(bitmap);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }




        // My defined Function for getting location
    // uses geocoder api to get the detailed address from latitude and longitude..
    public  void getAddress(Double lat1,Double longi1)
    {
        try {
            mGeocoder=new Geocoder(this, Locale.getDefault());
            mAddresses= mGeocoder.getFromLocation(lat1,longi1,1);

            String address= mAddresses.get(0).getAddressLine(0);
            String area = mAddresses.get(0).getLocality();
            String city = mAddresses.get(0).getAdminArea();
            String country =mAddresses.get(0).getCountryName();
            //String postal =mAddresses.get(0).getPostalCode();


            String fullAddress= address + "," + area + ", " + city + ", " + country;

            addressTextview.setText(fullAddress);

        }


        catch(Exception e)
        {



        }

    }


    public void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
    }

    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    public void getLocation()
    {

        if (ActivityCompat.checkSelfPermission(DonationActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DonationActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else

        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                lat=LocationGps.getLatitude();
                longi=LocationGps.getLongitude();
                getAddress(lat,longi);
                latitude=lat;
                longitude=longi;

                //t1.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);
            }
            else if (LocationNetwork !=null)
            {
                lat=LocationNetwork.getLatitude();
                longi=LocationNetwork.getLongitude();
                getAddress(lat,longi);
                latitude=lat;
                longitude=longi;

                //t1.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);
            }
            else if (LocationPassive !=null)
            {
                 lat=LocationPassive.getLatitude();
                 longi=LocationPassive.getLongitude();
                 getAddress(lat,longi);
                latitude=lat;
                longitude=longi;

                //t1.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);
            }
            else
            {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }


        }


    }

    // form data checks..
    private boolean isDataProvided(){

        if(imageUri == null){
            Toast.makeText(this, "Select Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(check1.isChecked() == false && check2.isChecked() == false && check3.isChecked() == false){
            Toast.makeText(this, "Please Select Food Type..", Toast.LENGTH_SHORT).show();
            return false;
        }

        // sometimes it does not get location thats why this part is commented...

        /*else if(longitude == null || latitude == null){
            Toast.makeText(this, "Please Select Your Location", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        /////////////////////////////////////////////////////////////////////////////////////

        return true;
    }


    private String getFileExtension(Uri uri){
        ContentResolver Cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(Cr.getType(uri));
    }


    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if(result == null) {
            result = "Not found";
        }


        Toast.makeText(context, "Result = "+result, Toast.LENGTH_SHORT).show();
        return result;
    }










    // FOR STORAGE PERMISSIONS


    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(DonationActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }


















}
