package com.example.shelter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    TextInputLayout mFullName, mEmail;
    ImageView verifyImageError, verifiedImage;
    CircleImageView profileImage;
    TextView pleaseVerifyText, clickHereText, verifiedText;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    FirebaseUser user;
    String userId;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        mFullName = findViewById(R.id.fullName_layout);
        mEmail = findViewById(R.id.email_layout);
        verifyImageError = findViewById(R.id.verify_image_error);
        pleaseVerifyText = findViewById(R.id.please_verify_text);
        clickHereText = findViewById(R.id.click_here);
        verifiedImage = findViewById(R.id.verify_image);
        verifiedText = findViewById(R.id.verify_text);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = mAuth.getCurrentUser();

        userId = mAuth.getCurrentUser().getUid();

        Glide.with(this)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.ic_profile)
                .into(profileImage);

        mFullName.getEditText().setText(mAuth.getCurrentUser().getDisplayName());
        mEmail.getEditText().setText(mAuth.getCurrentUser().getEmail());

        //getting profile image from firebase

        StorageReference fileRef = storageReference.child("usersPicture/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Profile.this)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .placeholder(R.drawable.ic_profile)
                        .into(profileImage);
            }
        });


        //account validation
        if (!user.isEmailVerified()) {
            clickHereText.setOnClickListener(v ->
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Profile.this, "Verification has been sent \n Please check your mail",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile.this, "Email not sent", Toast.LENGTH_SHORT).show();
                        }
                    }));
        } else {
            verifyImageError.setVisibility(View.INVISIBLE);
            verifiedImage.setVisibility(View.VISIBLE);
            pleaseVerifyText.setVisibility(View.INVISIBLE);
            verifiedText.setVisibility(View.VISIBLE);
            clickHereText.setVisibility(View.GONE);

        }
    }
}