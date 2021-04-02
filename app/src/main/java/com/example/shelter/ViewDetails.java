package com.example.shelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ViewDetails extends AppCompatActivity {
    ImageView img;
    TextView name_tv, breed_tv, gender_tv, weight_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details);

        img=findViewById(R.id.img);
        name_tv=findViewById(R.id.txt_name);
        breed_tv =findViewById(R.id.txt_breed);
        gender_tv =findViewById(R.id.txt_gender);
        weight_tv =findViewById(R.id.txt_weight);

        Intent intent=getIntent();
        String name=intent.getStringExtra("name");
        String breed=intent.getStringExtra("breed");
        String gender=intent.getStringExtra("gender");
        String weight=intent.getStringExtra("weight");
        String imageUrl=intent.getStringExtra("image");


        Glide.with(this).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.DATA).into(img);
        name_tv.setText("Name : "+name);
        breed_tv.setText("Breed : "+breed);
        gender_tv.setText("Gender : "+gender);
        weight_tv.setText("Weight : "+ weight+" Kg");


    }
}