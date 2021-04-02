package com.example.shelter;

public class screenItem {
    private String title,description;
    private int screenImage;

    public screenItem(String title, String description, int screenImage) {
        this.title = title;
        this.description = description;
        this.screenImage = screenImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getScreenImage() {
        return screenImage;
    }
}
