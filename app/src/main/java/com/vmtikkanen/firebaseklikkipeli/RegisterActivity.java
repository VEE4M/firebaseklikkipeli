package com.vmtikkanen.firebaseklikkipeli;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private EditText newUserNameEditText;
    private EditText newPasswordEditText;
    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        newUserNameEditText = findViewById(R.id.newUserNameEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        final EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        Button submitButton = findViewById(R.id.submitButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = (newUserNameEditText.getText().toString()+"@9870987f09dsdgsgf.com");
                String password = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                tryToRegister(email, password, confirmPassword);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    private void tryToRegister(String email, String password, String confirmPassword){
        if (!isEmailValid(email)){
            Log.d(TAG, "tryToRegister: invalid email address!");
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
        } else if(!isPasswordValid(password, confirmPassword)){
                Log.d(TAG, "tryToRegister: invalid password");
                Toast.makeText(this, "invalid password", Toast.LENGTH_SHORT).show();
        } else {
            createFirebaseUser();
        }

    }

    private boolean isEmailValid(String email){
        return email.contains("@");
    }

    private boolean isPasswordValid(String password, String confirmPassword){
        if (password.length() < 3){
            Log.d(TAG, "isPasswordValid: too short password");
        }
        if (!confirmPassword.equals(password)){
            Log.d(TAG, "isPasswordValid: passwords dont match");
        }
        return (password.length() > 3) && confirmPassword.equals(password);
    }

    private void createFirebaseUser(){
        //email = (newUserNameEditText.getText().toString()+"@9870987f09dsdgsgf.com");
        Log.d(TAG, "createFirebaseUser: "+email);
        String password = newPasswordEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "onComplete: " + task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.d(TAG, "onComplete: could not create new user");
                }else{
                    Log.d(TAG, "onComplete: new user registered");
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }
}


