package com.example.shelter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class EditPet extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    EditText name_et;
    EditText breed_et;
    Spinner gender_sp;
    EditText weight_et;
    String mGender;
   // MyDatabase myDatabase=new MyDatabase(this);
    Pet p=new Pet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);
        name_et=findViewById(R.id.name_et);
        breed_et=findViewById(R.id.breed_et);
        gender_sp=findViewById(R.id.gender_sp);
        weight_et=findViewById(R.id.measurement_et);
        Intent intent=getIntent();
        int id=intent.getIntExtra("id",0);
        String name=intent.getStringExtra("name");
        String breed=intent.getStringExtra("breed");
        String gender=intent.getStringExtra("gender");
        String weight=intent.getStringExtra("weight");
        p.setId(id);
        p.setName(name);
        p.setBreed(breed);
        p.setGender(gender);
        p.setWeight(weight);
        name_et.setText(name);
        breed_et.setText(breed);
        //gender_sp.setSelection(getIndex(gender_sp,gender));

        String compareValue=gender;
        ArrayAdapter spinnerAdapter=ArrayAdapter.createFromResource(this,
                R.array.gender_spinner,android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender_sp.setAdapter(spinnerAdapter);
        if (compareValue!=null){
            int spinnerPosition=spinnerAdapter.getPosition(compareValue);
            gender_sp.setSelection(spinnerPosition);
        }
        gender_sp.setOnItemSelectedListener(this);

        weight_et.setText(weight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit,menu);
        return super.onCreateOptionsMenu(menu);
    }

   /* @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.save_edit:
                String name=name_et.getText().toString().trim();
                String breed=breed_et.getText().toString().trim();
                String gender=mGender;
                String weight=weight_et.getText().toString();
                p.setName(name);
                p.setBreed(breed);
                p.setGender(gender);
                p.setWeight(weight);
                if (myDatabase.updatePet(p)){
                    Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditPet.this,MainActivity.class));
                }else
                    Toast.makeText(this, "Error with updating pet !!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                if (myDatabase.deletePet(p)){
                    Toast.makeText(this, "Pet Deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditPet.this,MainActivity.class));
                }else
                    Toast.makeText(this, "Error with deleting pet !!", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mGender=adapterView.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /*private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }*/

}