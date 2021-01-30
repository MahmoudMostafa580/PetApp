package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {
    FloatingActionButton add_pet_btn;
    RecyclerView mRecyclerView;
    PetsAdapter mPetsAdapter;
    ProgressBar mProgressBar;

    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;


    private List<Pet> pets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar=findViewById(R.id.progressBar);

        add_pet_btn=findViewById(R.id.add_pet_btn);
        add_pet_btn.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this,AddPet.class)));


        mRecyclerView=findViewById(R.id.pets_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pets=new ArrayList<>();

        mPetsAdapter=new PetsAdapter(MainActivity.this,pets);
        mRecyclerView.setAdapter(mPetsAdapter);
        mPetsAdapter.setOnItemClickListener(MainActivity.this);


        mStorage= FirebaseStorage.getInstance();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference("pets");
        mDBListener = mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pets.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    Pet pet=postSnapshot.getValue(Pet.class);
                    pet.setKey(postSnapshot.getKey());
                    pets.add(pet);
                }
                mPetsAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mDBListener);
    }

    @Override
    public void OnItemClick(int position) {
        Toast.makeText(this, "Normal click at position "+position , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnWhateverClick(int position) {
        Toast.makeText(this, "Whatever click at position "+position , Toast.LENGTH_SHORT).show();

    }

    @Override
    public void OnDeleteClick(int position) {
        Pet selectedPet=pets.get(position);
        String selectedKey=selectedPet.getKey();
        StorageReference imageRef=mStorage.getReferenceFromUrl(selectedPet.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseReference.child(selectedKey).removeValue();
                Toast.makeText(MainActivity.this, "Pet Deleted ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                //finish();
                break;
            case R.id.profile:
                startActivity(new Intent(MainActivity.this,Profile.class));
                break;
        }
        return true;
    }
}