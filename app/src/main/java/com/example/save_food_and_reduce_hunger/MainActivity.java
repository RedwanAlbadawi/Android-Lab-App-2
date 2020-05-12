package com.example.save_food_and_reduce_hunger;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save_food_and_reduce_hunger.Utilities.DialogManager;
import com.example.save_food_and_reduce_hunger.Utilities.InternetConnectivityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText userEmail;
    EditText userPassword;
    Button loginButton;
    TextView register;
    TextView forget;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init views

        userEmail=(EditText)findViewById(R.id.email);
        userPassword=(EditText)findViewById(R.id.pass);
        loginButton=(Button)findViewById(R.id.btnLogin);
        register=(TextView)findViewById(R.id.register);
        forget=(TextView)findViewById(R.id.forget);

        // progress dialog initialization

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Signing in please wait....");
        progressDialog.setCancelable(false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");


        // buttons callback registration
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start RegisterActivity
                Intent IntentRegister=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(IntentRegister);
            }
        });
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IntentRegister=new Intent(MainActivity.this,PasswordResetActivity.class);
                startActivity(IntentRegister);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCredentialsProvided()){


                    if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){

                        // if credentials are provided and we have internet connection try to login using firebase.

                        progressDialog.show();

                        firebaseAuth.signInWithEmailAndPassword(userEmail.getText().toString().trim(),
                                userPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){

                                            // once the login is successfull make sure that user is verified or not
                                            // it user is verified get user details from database otherwise error dialog
                                            // is displayed to the user and verification link is sent to the user again..

                                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                String userId = firebaseUser.getUid();

                                                // if it is verified user get user information

                                                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){


                                                            // Store user information in map and then get the values from map using their key

                                                            Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                                            String role = String.valueOf(map.get("role"));
                                                            String username = String.valueOf(map.get("name"));
                                                            String phone = String.valueOf(map.get("phone"));
                                                            Intent intent;

                                                            if(role.equals(R.string.roleDonor)){
                                                                intent = new Intent(getApplicationContext(),DonorActivity.class);
                                                            }
                                                            else{
                                                                intent = new Intent(getApplicationContext(),CharityActivity.class);
                                                            }
                                                            progressDialog.dismiss();
                                                            intent.putExtra("name",username);
                                                            intent.putExtra("phone",phone);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else{
                                                            progressDialog.dismiss();
                                                            Toast.makeText(MainActivity.this, "User Information does not exist..", Toast.LENGTH_SHORT).show();
                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(MainActivity.this, "Exception: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                            }
                                            else{

                                                // if email is not verified.

                                                progressDialog.dismiss();
                                                userEmail.setText("");
                                                userPassword.setText("");
                                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                                                builder.setTitle("Alert");
                                                builder.setMessage("Looks like your email is not yet verified. Check your email account for verification link.");
                                                builder.setCancelable(true);
                                                builder.setPositiveButton(
                                                        "Ok",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Toast.makeText(MainActivity.this, "Verification Link Sent.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        else{
                                                                            Toast.makeText(MainActivity.this, "Error Sending Verification Link.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });


                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            }
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                                Toast.makeText(MainActivity.this, "this email is not registered", Toast.LENGTH_SHORT).show();
                                            }
                                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                                Toast.makeText(MainActivity.this, "Your password is wrong", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });



                    }
                    else{
                        DialogManager.showAlertDialog(MainActivity.this,getResources().getString(R.string.connectionProblemTitle),
                                getResources().getString(R.string.connectionProblemMessage),R.drawable.no_internet,null);
                    }

                }

            }
        });

    }


    // form fields check..
    private boolean isCredentialsProvided(){
        if(TextUtils.isEmpty(userEmail.getText())){
            userEmail.setError("Please Enter Email");
            return false;
        }
        else if(TextUtils.isEmpty(userPassword.getText())){
            userPassword.setError("Please Enter Password");
            return false;
        }
        return true;
    }



}
