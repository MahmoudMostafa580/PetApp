package com.example.shelter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewDetails extends AppCompatActivity {
    AppCompatImageView img;
    TextView name_tv, breed_tv, gender_tv, weight_tv;

    @SuppressLint("SetTextI18n")
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
        name_tv.setText(name);
        breed_tv.setText(breed);
        gender_tv.setText(gender);
        weight_tv.setText(weight+" Kg");


    }
}