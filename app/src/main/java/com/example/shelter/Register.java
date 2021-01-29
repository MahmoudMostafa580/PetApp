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

public class Register extends AppCompatActivity {

    TextInputEditText mFullNameEditText;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    TextInputEditText mPhoneEditText;
    AppCompatButton mRegisterBtn;
    TextView mSignInText;
    ProgressBar mProgressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullNameEditText=findViewById(R.id.fullName_editText);
        mEmailEditText=findViewById(R.id.email_editText);
        mPasswordEditText=findViewById(R.id.password_editText);
        mPhoneEditText=findViewById(R.id.phone_editText);
        mRegisterBtn=findViewById(R.id.register_btn);
        mSignInText=findViewById(R.id.signIn_text);
        mProgressBar=findViewById(R.id.progress_bar);

        mAuth=FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEditText.getText().toString().trim();
                String password=mPasswordEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    mEmailEditText.setError("Email required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPasswordEditText.setError("Password required");
                    return;
                }
                if (password.length()<8){
                    mPasswordEditText.setError("Password must be 8 chars or more");
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);

                //Registration process
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Register.this, "User created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this,MainActivity.class));
                        }else
                        {
                            Toast.makeText(Register.this, "Error ! : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });
        mSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });
    }
}