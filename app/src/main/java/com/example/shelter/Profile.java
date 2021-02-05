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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    TextInputLayout mFullName,mEmail,mPassword,mPhone;
    AppCompatButton EditProfile,SaveChanges;
    ImageView verifyImageError,verifiedImage;
    CircleImageView profileImage;
    ImageView addPhoto;
    TextView pleaseVerifyText,clickHereText,verifiedText;

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
        verifyImageError=findViewById(R.id.verify_image_error);
        pleaseVerifyText=findViewById(R.id.please_verify_text);
        clickHereText=findViewById(R.id.click_here);
        SaveChanges=findViewById(R.id.saveChanges);
        addPhoto=findViewById(R.id.add_photo);
        verifiedImage=findViewById(R.id.verify_image);
        verifiedText=findViewById(R.id.verify_text);

        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        user=mAuth.getCurrentUser();

        userId=mAuth.getCurrentUser().getUid();

        //getting profile image from firebase
        StorageReference fileRef=storageReference.child("usersPicture/"+mAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Profile.this)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(profileImage);
            }
        });

        //pick profile image from gallery
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
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
        }
        else{
            verifyImageError.setVisibility(View.INVISIBLE);
            verifiedImage.setVisibility(View.VISIBLE);
            pleaseVerifyText.setVisibility(View.INVISIBLE);
            verifiedText.setVisibility(View.VISIBLE);
            clickHereText.setVisibility(View.GONE);

        }

        //retrieve user data from firebase
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

        //enable editTexts to edit it
        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto.setClickable(true);
                mEmail.setEnabled(true);
                mFullName.setEnabled(true);
                mPassword.setEnabled(true);
                mPhone.setEnabled(true);
                EditProfile.setVisibility(View.INVISIBLE);
                SaveChanges.setVisibility(View.VISIBLE);
            }
        });

        //save(update) changes button
        SaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFullName.getEditText().getText().toString().isEmpty() || mEmail.getEditText().getText().toString().isEmpty()
                    || mPassword.getEditText().getText().toString().isEmpty() || mPhone.getEditText().getText().toString().isEmpty()){
                    Toast.makeText(Profile.this, "Please fill all fields and try again", Toast.LENGTH_LONG).show();
                    return;
                }
                String email=mEmail.getEditText().getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference mDocumentReference=mFirestore.collection("users").document(user.getUid());
                        Map<String,Object> edited=new HashMap<>();
                        edited.put("email",email);
                        edited.put("fName",mFullName.getEditText().getText().toString());
                        edited.put("password",mPassword.getEditText().getText().toString());
                        edited.put("phone",mPhone.getEditText().getText().toString());
                        mDocumentReference.update(edited);
                        Toast.makeText(Profile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //pick image method
    private void openFileChooser() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    //set profile in it's place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null){
            Uri imageUri= data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    //upload image to firebase method
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef=storageReference.child("usersPicture/"+mAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Profile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(Profile.this).load(uri).placeholder(R.drawable.ic_profile).into(profileImage);
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