package com.example.save_food_and_reduce_hunger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save_food_and_reduce_hunger.Model.User;
import com.example.save_food_and_reduce_hunger.Utilities.DialogManager;
import com.example.save_food_and_reduce_hunger.Utilities.InternetConnectivityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {
    static final String NAME = "";
    static final String DISPLAY_NAME_KEY = "username";
    Button btnRegister;
    EditText userName;
    EditText userEmail;
    EditText password;
    EditText confirmPassword;
    EditText phoneNumber;
    Spinner spinner;
    String userID;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();



        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataProvided()){

                    if(InternetConnectivityManager.isNetworkConnected(getApplicationContext())){

                        // when the data is provided and we have internet connection signup user with email and password
                        progressDialog.show();
                        firebaseAuth.createUserWithEmailAndPassword(
                                userEmail.getText().toString(),
                                password.getText().toString()
                        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    // when signup is successfull send user a verification link

                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                // store user profile information so that we can use it later on
                                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                String userId = firebaseUser.getUid();
                                                //Toast.makeText(RegisterActivity.this, "User id = "+userId, Toast.LENGTH_SHORT).show();
                                                String name = userName.getText().toString();
                                                String phone = phoneNumber.getText().toString();
                                                final String role = spinner.getSelectedItem().toString();
                                                User user = new User(name,phone,role);
                                                databaseReference.child(userId).setValue(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                                                builder.setTitle("Registration Successful!");
                                                                builder.setMessage("An email with verification link send to your email account.");
                                                                builder.setCancelable(true);
                                                                builder.setPositiveButton(
                                                                        "Ok",
                                                                        new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                dialog.cancel();
                                                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        });


                                                                AlertDialog alertDialog = builder.create();
                                                                alertDialog.show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RegisterActivity.this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                });

                                            }
                                            else{
                                                Toast.makeText(RegisterActivity.this, "Exception: "+task.getException(), Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    });

                                }
                                else{
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                        Toast.makeText(RegisterActivity.this,R.string.errorEmailExists, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                    else{
                        DialogManager.showAlertDialog(RegisterActivity.this,getResources().getString(R.string.connectionProblemTitle),
                                getResources().getString(R.string.connectionProblemMessage),R.drawable.no_internet,null);
                    }







                }
            }
        });
    }


    // views initialization

    private void initViews() {
        //Getting id's from xml
        btnRegister=(Button)findViewById(R.id.btnRegister);
        userName=(EditText)findViewById(R.id.name);
        userEmail=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.pass);
        confirmPassword=(EditText)findViewById(R.id.cnf_pass);
        phoneNumber=findViewById(R.id.phone);
        //sppiner
        spinner=findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.type,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(this.getResources().getString(R.string.signUpDialogTitle));
        progressDialog.setMessage(this.getResources().getString(R.string.signUpDialogMessage));
        progressDialog.setCancelable(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String type=parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* To check password validation  */
    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }




    // sign up form checks
    public boolean isDataProvided(){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(TextUtils.isEmpty(userName.getText())){
            userName.setError(this.getResources().getString(R.string.usernameRequired));
            return false;
        }
        else if(TextUtils.isEmpty(userEmail.getText())){
            userEmail.setError(this.getResources().getString(R.string.emailRequired));
            return false;
        }
        else if(!userEmail.getText().toString().trim().matches(emailPattern)) {
            userEmail.setError(this.getResources().getString(R.string.emailValidityFailed));
            return false;
        }
        else if(TextUtils.isEmpty(password.getText())){
            password.setError(this.getResources().getString(R.string.passwordRequired));
            return false;
        }
        /*else if(! isValidPassword(password.getText().toString())){
            password.setError(this.getResources().getString(R.string.passwordValidityFailed));
            return false;
        }*/
        else if(TextUtils.isEmpty(confirmPassword.getText())){
            confirmPassword.setError(this.getResources().getString(R.string.confirmPasswordRequired));
            return false;
        }
        else if(! password.getText().toString().equals(confirmPassword.getText().toString())){
            Toast.makeText(this,R.string.passwordMatchFailed, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}

