package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextInputEditText mEmailEditText;
    TextInputEditText mPassEditText;
    AppCompatButton mLoginButton;
    TextView mRegisterText;
    ProgressBar mProgressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailEditText=findViewById(R.id.email_editText);
        mPassEditText=findViewById(R.id.password_editText);
        mRegisterText=findViewById(R.id.register_text);
        mLoginButton=findViewById(R.id.login_btn);
        mProgressBar=findViewById(R.id.progress_bar);
        mAuth=FirebaseAuth.getInstance();
        mRegisterText.setOnClickListener(v -> startActivity(new Intent(Login.this,Register.class)));
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEditText.getText().toString().trim();
                String password=mPassEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    mEmailEditText.setError("Email required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPassEditText.setError("Password required");
                    return;
                }
                if (password.length()<8){
                    mPassEditText.setError("Password must be 8 chars or more");
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(Login.this, "Login successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }else{
                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}