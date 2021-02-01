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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    TextInputEditText mFullNameEditText;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    TextInputEditText mPhoneEditText;
    AppCompatButton mRegisterBtn;
    ImageView profileImage,addPhoto;
    TextView mSignInText;
    ProgressBar mProgressBar;
    String userId;


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
        profileImage=findViewById(R.id.register_profile_image);
        addPhoto=findViewById(R.id.add_photo);

        mAuth=FirebaseAuth.getInstance();
        mFireStore=FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }





        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEditText.getText().toString().trim();
                String password=mPasswordEditText.getText().toString().trim();
                String fullName=mFullNameEditText.getText().toString().trim();
                String phone=mPhoneEditText.getText().toString().trim();

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

                            FirebaseUser fUser=mAuth.getCurrentUser();
                            fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "Verification has been sent \n Please check your mail",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Register.this, "Email not sent", Toast.LENGTH_SHORT).show();
                                }
                            });

                            userId=mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference=mFireStore.collection("users").document(userId);
                            Map<String,Object> user=new HashMap<>();
                            user.put("fName",fullName);
                            user.put("email",email);
                            user.put("password",password);
                            user.put("phone",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "User created & Data Saved", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
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

    private void openFileChooser() {
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null){
            Uri imageUri= data.getData();
            profileImage.setImageURI(imageUri);
            Picasso.with(this).load(imageUri).into(profileImage);
        }
    }
}