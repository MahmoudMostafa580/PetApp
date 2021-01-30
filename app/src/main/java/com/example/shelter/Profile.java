package com.example.shelter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Profile extends AppCompatActivity {
    AppCompatImageView profileImage;
    TextInputLayout mFullName,mEmail,mPassword,mPhone;
    AppCompatButton EditProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    String userId;

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

        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();

        userId=mAuth.getCurrentUser().getUid();

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
                mEmail.setEnabled(true);
                mPassword.setEnabled(true);
                mPhone.setEnabled(true);
                EditProfile.setText("Save Changes");
            }
        });

    }
}