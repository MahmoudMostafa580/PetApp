package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    TextInputEditText mFullNameEditText;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    TextInputEditText mPhoneEditText;
    AppCompatButton mRegisterBtn;
    CircleImageView google_sign,facebook_sign;
    TextView mSignInText;
    ProgressBar mProgressBar;
    String userId;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;

    CallbackManager mCallbackManager;

    FirebaseAuth mAuth;
    FirebaseFirestore mFireStore;

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
        google_sign=findViewById(R.id.google_sign_btn);
        facebook_sign=findViewById(R.id.facebook_sign_btn);

        mAuth=FirebaseAuth.getInstance();
        mFireStore=FirebaseFirestore.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        mRegisterBtn.setOnClickListener(v -> {
            String email= Objects.requireNonNull(mEmailEditText.getText()).toString().trim();
            String password= Objects.requireNonNull(mPasswordEditText.getText()).toString().trim();
            String fullName= Objects.requireNonNull(mFullNameEditText.getText()).toString().trim();
            String phone= Objects.requireNonNull(mPhoneEditText.getText()).toString().trim();

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
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){

                    FirebaseUser fUser=mAuth.getCurrentUser();
                    fUser.sendEmailVerification().addOnSuccessListener(aVoid -> Toast.makeText(Register.this, "Verification has been sent \n Please check your mail",
                            Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(Register.this, "Email not sent", Toast.LENGTH_SHORT).show());

                    userId=mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference=mFireStore.collection("users").document(userId);
                    Map<String,Object> user=new HashMap<>();
                    user.put("fName",fullName);
                    user.put("email",email);
                    user.put("password",password);
                    user.put("phone",phone);
                    documentReference.set(user)
                            .addOnSuccessListener(aVoid ->
                            Toast.makeText(Register.this, "User created & Data Saved", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show());
                    startActivity(new Intent(Register.this,MainActivity.class));

                }else
                {
                    Toast.makeText(Register.this, "Error ! : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                }
            });

        });
        mSignInText.setOnClickListener(v -> startActivity(new Intent(Register.this,Login.class)));

        /*
         *Login with google account
         */
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount signInAccount=GoogleSignIn.getLastSignedInAccount(this);

        google_sign.setOnClickListener(v -> signIn());


        facebook_sign.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(Register.this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d("TAG", "facebook:onSuccess:" + loginResult);
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d("TAG", "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("TAG", "facebook:onError", error);
                }
            });
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show();
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Register.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        Toast.makeText(Register.this, "Facebook login successfully", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                        Toast.makeText(Register.this, "Facebook login failed", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user!=null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /*@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            updateUI(currentUser);
        }
    }*/
}