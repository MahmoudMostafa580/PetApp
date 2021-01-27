package com.example.shelter;

import com.google.firebase.database.Exclude;

public class Pet {
    private String name;
    private String breed;
    private String gender;
    private String weight;
    private String imageUrl;
    private String mKey;

    public Pet() {
    }

    public Pet(String name, String breed, String gender, String weight, String imageUrl) {
        if (name.trim().equals("")){
            name="No Name";
        }
        this.name = name;
        this.breed = breed;
        this.gender = gender;
        this.weight = weight;
        this.imageUrl=imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String mKey) {
        this.mKey = mKey;
    }
}
