package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {
    TextInputEditText mEmailEditText;
    TextInputEditText mPassEditText;
    AppCompatButton mLoginButton,googleSignIn;
    TextView mRegisterText,mForgetPassword;
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
        mForgetPassword=findViewById(R.id.forgetPass_text);
        googleSignIn=findViewById(R.id.google_sign_btn);

        mAuth=FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        /*
         *Go to register activity
         */
        mRegisterText.setOnClickListener(v -> startActivity(new Intent(Login.this,Register.class)));

        /*
         * *Login with email and password
         */
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


        /*
         *Implement forget password button
         */
        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail=new EditText(v.getContext());
                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                builder.setTitle("Reset Password");
                builder.setMessage("Enter email to receive reset link");
                builder.setView(resetMail);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail=resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset Link Sent To Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error with sending reset link !!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                builder.create().show();
            }
        });

    }
}