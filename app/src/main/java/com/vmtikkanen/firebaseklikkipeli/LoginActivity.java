package com.vmtikkanen.firebaseklikkipeli;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private AutoCompleteTextView userNameView;
    private EditText passwordView;
    private static final String TAG = "LoginActivity";

    private DatabaseReference mUserData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        userNameView = findViewById(R.id.userNameView);
        passwordView = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        MainActivity.setCurrentUser("sds");
        mUserData = FirebaseDatabase.getInstance().getReference().child("userdata");

        mAuth = FirebaseAuth.getInstance();



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

    private void getUserData(){
        mUserData = FirebaseDatabase.getInstance().getReference().child("userdata");
        UserData userData = new UserData();

        userData.setCurrentPoints(1);
    }

    private void tryToLogin(){
        final String userName = userNameView.getText().toString();
        String email = (userNameView.getText().toString()+"@9870987f09dsdgsgf.com");
        String password = passwordView.getText().toString();
        if ((email.equals("")) || (password.equals(""))){
            return;
        }else{
            Toast.makeText(this, "Logging in..", Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "onComplete: " + task.isSuccessful());
                    
                    if(!task.isSuccessful()){
                        Log.d(TAG, "onComplete: login failed " +task.getException());
                    }else{
                        Log.d(TAG, "onComplete: login successful!");
                        MainActivity.setCurrentUser(userName);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
            });
        }
    }

}
