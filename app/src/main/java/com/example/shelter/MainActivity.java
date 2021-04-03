package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {
    FloatingActionButton add_pet_btn;
    RecyclerView mRecyclerView;
    PetsAdapter mPetsAdapter;
    ProgressBar mProgressBar;
    FirebaseAuth mAuth;
    DocumentReference documentReference;
    CollectionReference collectionReference;
    FirebaseFirestore db;
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

        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        db=FirebaseFirestore.getInstance();
        documentReference=db.collection("users").document(userId).collection("pets").document();
        collectionReference=db.collection("users").document(userId).collection("pets");

        add_pet_btn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,AddPet.class)));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        pets=new ArrayList<>();
        mPetsAdapter=new PetsAdapter(MainActivity.this,pets);
        mRecyclerView.setAdapter(mPetsAdapter);
        mPetsAdapter.setOnItemClickListener(MainActivity.this);

        loadData();
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
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error while loading data !", Toast.LENGTH_SHORT).show();
                    }
                });
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pets.remove(position);
                mPetsAdapter.notifyItemRemoved(position);
                Toast.makeText(MainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                //String search=query.toLowerCase();
                collectionReference.whereEqualTo("name",query).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                pets.clear();
                                for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                                    Pet p=documentSnapshot.toObject(Pet.class);
                                    p.setPetId(documentSnapshot.getId());
                                    pets.add(p);
                                }
                                mPetsAdapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //String search=newText.toLowerCase();
                collectionReference.whereEqualTo("name",newText).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                pets.clear();
                                for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                                    Pet p=documentSnapshot.toObject(Pet.class);
                                    p.setPetId(documentSnapshot.getId());
                                    pets.add(p);
                                }
                                mPetsAdapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

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