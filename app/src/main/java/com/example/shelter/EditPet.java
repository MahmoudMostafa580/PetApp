package com.example.shelter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditPet extends AppCompatActivity implements AdapterView.OnItemClickListener{
    ImageView pet_image;
    TextInputLayout name_et;
    TextInputLayout breed_et;
    AutoCompleteTextView gender_sp;
    TextInputLayout weight_et;
    String mGender;
    Pet p=new Pet();

    String name,breed,gender,weight,imageUrl,petId;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    StorageReference mStorageReference;
    String userId;
    CollectionReference collectionReference;

    String[] genderSpinner={"Unknown","Male","Female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        name_et=findViewById(R.id.name_et);
        breed_et=findViewById(R.id.breed_et);
        gender_sp=findViewById(R.id.gender_sp);
        weight_et=findViewById(R.id.measurement_et);
        pet_image=findViewById(R.id.pet_image);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference("pets");
        userId = mAuth.getCurrentUser().getUid();
        collectionReference=mFirestore.collection("users").document(userId).collection("pets");

        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<>(this, android.R.layout.select_dialog_item, genderSpinner);
        gender_sp.setAdapter(spinnerAdapter);

        Intent intent=getIntent();
        name=intent.getStringExtra("name");
        breed=intent.getStringExtra("breed");
        gender=intent.getStringExtra("gender");
        weight=intent.getStringExtra("weight");
        imageUrl=intent.getStringExtra("imageUrl");
        petId=intent.getStringExtra("petId");


        p.setName(name);
        p.setBreed(breed);
        p.setGender(gender);
        p.setWeight(weight);
        p.setImageUrl(imageUrl);
        p.setPetId(petId);

        name_et.getEditText().setText(name);
        breed_et.getEditText().setText(breed);
        weight_et.getEditText().setText(weight);
        gender_sp.setText(gender);

        /*if (p.getGender() !=null){
            int spinnerPosition=spinnerAdapter.getPosition(p.getGender());
            gender_sp.setText(gender);
            gender_sp.setSelection(spinnerPosition);
        }*/
        gender_sp.setOnItemClickListener(this);

        Glide.with(this).load(imageUrl).into(pet_image);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_edit:
                if (name_et.getEditText().getText()==null || breed_et.getEditText().getText()==null || weight_et.getEditText().getText()==null
                    ||imageUrl.isEmpty() ||gender==null){
                    Toast.makeText(this, "Please fill all fields first !", Toast.LENGTH_SHORT).show();
                }else{
                    name=name_et.getEditText().getText().toString();
                    breed=breed_et.getEditText().getText().toString();
                    gender=gender_sp.getText().toString();
                    weight=weight_et.getEditText().getText().toString();

                    Map<String,Object> pet=new HashMap<>();
                    pet.put("name",name);
                    pet.put("breed",breed);
                    pet.put("gender",gender);
                    pet.put("weight",weight);
                    pet.put("imageUrl",imageUrl);
                    pet.put("petId",petId);

                    collectionReference.document(p.getPetId()).update(pet)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditPet.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditPet.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mGender=parent.getItemAtPosition(position).toString();
        gender_sp.setText(mGender);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<>(this,android.R.layout.select_dialog_item,genderSpinner);
        gender_sp.setAdapter(spinnerAdapter);
        gender_sp.setOnItemClickListener(this);
    }

}