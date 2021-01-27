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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AddPet extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final int PICK_IMAGE_REQUEST = 1;
    EditText name_et;
    EditText breed_et;
    Spinner gender_sp;
    EditText weight_et;
    String mGender;
    ProgressBar mProgressBar;
    ImageView mImageView;
    private Uri mImageUri;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private StorageTask mUploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        name_et=findViewById(R.id.name_et);
        breed_et=findViewById(R.id.breed_et);
        gender_sp=findViewById(R.id.gender_sp);
        weight_et=findViewById(R.id.measurement_et);
        mImageView=findViewById(R.id.pet_image);
        mProgressBar=findViewById(R.id.progressBar);

        ArrayAdapter spinnerAdapter=ArrayAdapter.createFromResource(this,
                R.array.gender_spinner,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) ;
        gender_sp.setAdapter(spinnerAdapter);
        gender_sp.setOnItemSelectedListener(this);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        mStorageReference= FirebaseStorage.getInstance().getReference("pets");
        mDatabaseReference= FirebaseDatabase.getInstance().getReference("pets");
    }

    private void openFileChooser() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            mImageUri= data.getData();
            mImageView.setImageURI(mImageUri);
            Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(AddPet.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadPet();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getFileExtension(Uri uri){
        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadPet() {
        if (mImageUri!=null){
            StorageReference fileReference=mStorageReference.child(System.currentTimeMillis  ()+"."+getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Handler handler=new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgressBar.setProgress(0);
                                        }
                                    },500);

                                    Toast.makeText(AddPet.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                    String name=name_et.getText().toString().trim();
                                    String breed=breed_et.getText().toString().trim();
                                    String gender=mGender;
                                    String weight=weight_et.getText().toString();
                                    Pet pet=new Pet(name,breed,gender,weight,uri.toString());
                                    String uploadId=mDatabaseReference.push().getKey();
                                    mDatabaseReference.child(uploadId).setValue(pet);

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddPet.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress=(100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                            mProgressBar.setProgress((int)progress);
                        }
                    });

        }else
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mGender=adapterView.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}