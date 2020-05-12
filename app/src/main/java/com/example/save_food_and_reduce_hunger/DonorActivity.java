package com.example.save_food_and_reduce_hunger;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class DonorActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected Toolbar toolbar;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;
    Button make_donation;
    Intent intent;
    String username;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor);

        intent = getIntent();
        username = intent.getStringExtra("name");


        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);

        // To set username in the navigation header.........

        // TODO Bug need to fix here...
        /*View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.userloggedin);
        Toast.makeText(this, "header name = "+headerName.getText(), Toast.LENGTH_SHORT).show();
        headerName.setText(username);*/

        /////////////////////////////////////////////////////////////////

        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        make_donation = (Button) findViewById(R.id.make_donation);
        make_donation = (Button) findViewById(R.id.make_donation);
        make_donation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(DonorActivity.this, DonationActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_donate:
                Intent i = new Intent(DonorActivity.this, DonorActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.nav_home:
                //Do some thing here
                Intent ihome = new Intent(DonorActivity.this, CharityActivity.class);
                startActivity(ihome);
                finish();
                break;
            case R.id.nav_tools:
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent ilogout = new Intent(DonorActivity.this, MainActivity.class);
                startActivity(ilogout);
                finish();
                break;
            case R.id.nav_share:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Save Food And Reduce Hunger");
                    String shareMessage= "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    Toast.makeText(this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                 }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure to exit the application?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }



}
