package com.example.save_food_and_reduce_hunger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {


    EditText email;
    Button sendEmail;
    ProgressBar mProgressBar;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        email=(EditText) findViewById(R.id.forgetemail);
        sendEmail=findViewById(R.id.btnSend);
        mProgressBar=findViewById(R.id.prg);
        mAuth=FirebaseAuth.getInstance();
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);
                String em = email.getText().toString().trim();
                if (em.matches("")) {
                    email.setError("Must be filled");
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(em).matches())
                {
                    email.setError("Please enter valid email");
                }

                else {
                    attemptVerify();
                }
            }
        });

    }
    public void attemptVerify()
    {
mAuth.sendPasswordResetEmail(email.getText().toString())
        .addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mProgressBar.setVisibility(View.GONE);
        if(task.isSuccessful())
        {
            Toast.makeText(PasswordResetActivity.this,"Password Reset Email Sent",Toast.LENGTH_SHORT).show();
            Intent i=new Intent(PasswordResetActivity.this,MainActivity.class);
            finish();
            startActivity(i);
        }
        else
        {
            Toast.makeText(PasswordResetActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();

        }
    }
});



    }
}
