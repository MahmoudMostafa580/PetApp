package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    TextInputLayout mFullName,mEmail,mPassword,mPhone;
    AppCompatButton EditProfile,SaveChanges;
    ImageView verifyImage;
    ImageView profileImage,addPhoto;
    TextView verifyText,clickHereText;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    FirebaseUser user;
    String userId;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage=findViewById(R.id.profile_image);
        mFullName=findViewById(R.id.fullName_layout);
        mEmail=findViewById(R.id.email_layout);
        mPassword=findViewById(R.id.password_layout);
        mPhone=findViewById(R.id.phone_layout);
        EditProfile=findViewById(R.id.editProfile_btn);
        verifyImage=findViewById(R.id.verify_image);
        verifyText=findViewById(R.id.verify_text);
        clickHereText=findViewById(R.id.click_here);
        SaveChanges=findViewById(R.id.saveChanges);
        addPhoto=findViewById(R.id.add_photo);

        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        userId=mAuth.getCurrentUser().getUid();

        StorageReference fileRef=storageReference.child("usersPicture/"+mAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(Profile.this).load(uri).into(profileImage);
            }
        });

        user=mAuth.getCurrentUser();
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
        }else{
            verifyImage.setImageResource(R.drawable.ic_verified_user);
            verifyText.setText("Account verified");
            clickHereText.setVisibility(View.GONE);
        }

        DocumentReference documentReference=mFirestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                mFullName.getEditText().setText(value.getString("fName"));
                mEmail.getEditText().setText(value.getString("email"));
                mPassword.getEditText().setText(value.getString("password"));
                mPhone.getEditText().setText(value.getString("phone"));
            }
        });

        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullName.setEnabled(true);
                mPassword.setEnabled(true);
                mPhone.setEnabled(true);
                EditProfile.setVisibility(View.GONE);
                SaveChanges.setVisibility(View.VISIBLE);
            }
        });
        SaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

    }
    private void openFileChooser() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null){
            Uri imageUri= data.getData();
            //profileImage.setImageURI(imageUri);
            //Picasso.with(this).load(imageUri).fit().centerCrop().into(profileImage);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef=storageReference.child("usersPicture/"+mAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Profile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(Profile.this).load(uri).into(profileImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

}