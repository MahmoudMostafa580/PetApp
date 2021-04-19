package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {
    FloatingActionButton add_pet_btn;
    RecyclerView mRecyclerView;
    PetsAdapter mPetsAdapter;
    ProgressBar mProgressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseAuth mAuth;
    DocumentReference documentReference;
    CollectionReference collectionReference;
    FirebaseFirestore db;
    StorageReference storageReference;
    FirebaseStorage mStorage;
    String userId;

    private List<Pet> pets;
    Pet selectedPet=new Pet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar=findViewById(R.id.progressBar);
        add_pet_btn=findViewById(R.id.add_pet_btn);
        mRecyclerView=findViewById(R.id.pets_recyclerView);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);

        mAuth=FirebaseAuth.getInstance();
        userId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db=FirebaseFirestore.getInstance();
        documentReference=db.collection("users").document(userId).collection("pets").document();
        collectionReference=db.collection("users").document(userId).collection("pets");
        storageReference=FirebaseStorage.getInstance().getReference("pets");
        mStorage=FirebaseStorage.getInstance();

        add_pet_btn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,AddPet.class)));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        pets=new ArrayList<>();
        mPetsAdapter=new PetsAdapter(MainActivity.this,pets);
        mRecyclerView.setAdapter(mPetsAdapter);
        mPetsAdapter.setOnItemClickListener(MainActivity.this);

        loadData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            mPetsAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadData() {
        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pets.clear();
                    for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                        Pet p=documentSnapshot.toObject(Pet.class);
                        p.setPetId(documentSnapshot.getId());
                        pets.add(p);
                    }
                    mPetsAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error while loading data !", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void OnViewDetailsClick(int position) {
        Intent viewIntent=new Intent(getApplicationContext(),ViewDetails.class);
        String name=pets.get(position).getName();
        String breed=pets.get(position).getBreed();
        String gender=pets.get(position).getGender();
        String weight=pets.get(position).getWeight();
        String imageUrl=pets.get(position).getImageUrl();
        viewIntent.putExtra("name",name);
        viewIntent.putExtra("breed",breed);
        viewIntent.putExtra("gender",gender);
        viewIntent.putExtra("weight",weight);
        viewIntent.putExtra("image",imageUrl);
        startActivity(viewIntent);
    }

    @Override
    public void OnEditClick(int position) {

        selectedPet=pets.get(position);
        String selectedKey=selectedPet.getPetId();
        Intent viewIntent=new Intent(getApplicationContext(),EditPet.class);
        String name=pets.get(position).getName();
        String breed=pets.get(position).getBreed();
        String gender=pets.get(position).getGender();
        String weight=pets.get(position).getWeight();
        String imageUrl=pets.get(position).getImageUrl();
        viewIntent.putExtra("name",name);
        viewIntent.putExtra("breed",breed);
        viewIntent.putExtra("gender",gender);
        viewIntent.putExtra("weight",weight);
        viewIntent.putExtra("imageUrl",imageUrl);
        viewIntent.putExtra("petId",selectedKey);
        startActivity(viewIntent);
    }

    @Override
    public void OnDeleteClick(int position) {
        selectedPet=pets.get(position);
        String selectedKey=selectedPet.getPetId();
        collectionReference.document(selectedKey).delete()
                .addOnSuccessListener(aVoid -> {
                    pets.remove(position);
                    mPetsAdapter.notifyItemRemoved(position);
                    StorageReference petsStorage=mStorage.getReferenceFromUrl(selectedPet.getImageUrl());
                    petsStorage.delete()
                            .addOnSuccessListener(aVoid1 -> Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        SearchView searchView=(SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search Pets");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPet(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPet(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(() -> {
            loadData();
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }
    private void searchPet(String text){
        collectionReference.whereEqualTo("name",text).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pets.clear();
                    for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                        Pet p=documentSnapshot.toObject(Pet.class);
                        p.setPetId(documentSnapshot.getId());
                        pets.add(p);
                    }
                    mPetsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                GoogleSignIn.getClient(this,new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                break;
            case R.id.profile:
                startActivity(new Intent(MainActivity.this,Profile.class));
                break;
        }
        return true;
    }
}