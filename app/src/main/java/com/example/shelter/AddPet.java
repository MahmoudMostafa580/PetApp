package com.example.shelter;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPet extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    TextInputLayout name_et;
    TextInputLayout breed_et;
    AutoCompleteTextView gender_sp;
    TextInputLayout weight_et;
    String mGender;
    ProgressBar mProgressBar;
    ImageView mImageView;
    private Uri mImageUri;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    StorageReference mStorageReference;
    String userId;
    private StorageTask mUploadTask;

    String[] genderSpinner={"Unknown","Male","Female"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        name_et = findViewById(R.id.name_et);
        breed_et = findViewById(R.id.breed_et);
        gender_sp = findViewById(R.id.gender_sp);
        weight_et = findViewById(R.id.measurement_et);
        mImageView = findViewById(R.id.pet_image);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<>(this,android.R.layout.select_dialog_item,genderSpinner);
        gender_sp.setAdapter(spinnerAdapter);
        gender_sp.setOnItemClickListener(this);

        mImageView.setOnClickListener(v -> openFileChooser());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference("pets");
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
            Picasso.with(this).load(mImageUri).fit().centerInside().into(mImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            if (mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(AddPet.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadPet();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadPet() {
        if (mImageUri != null) {

            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Handler handler = new Handler();
                                handler.postDelayed(() -> mProgressBar.setProgress(0), 500);

                                String name = Objects.requireNonNull(name_et.getEditText()).getText().toString().trim();
                                String breed = Objects.requireNonNull(breed_et.getEditText()).getText().toString().trim();
                                String gender = mGender;
                                String weight = Objects.requireNonNull(weight_et.getEditText()).getText().toString();
                                Pet p=new Pet(name,breed,gender,weight,uri.toString());

                                userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                CollectionReference collectionReference = mFirestore.collection("users").document(userId).collection("pets");
                                collectionReference.add(p)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(AddPet.this, "Data Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AddPet.this,MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(AddPet.this, e.toString(), Toast.LENGTH_SHORT).show());
                            }))
                    .addOnFailureListener(e -> Toast.makeText(AddPet.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(snapshot -> {
                        mProgressBar.setVisibility(View.VISIBLE);
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    });
        }
        else
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    { mGender = parent.getItemAtPosition(position).toString();}


    @Override
    protected void onResume() {
        super.onResume();
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<>(this,android.R.layout.select_dialog_item,genderSpinner);
        gender_sp.setAdapter(spinnerAdapter);
        gender_sp.setOnItemClickListener(this);
    }
}