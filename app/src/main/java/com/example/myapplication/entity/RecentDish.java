package com.example.myapplication.entity;

public class RecentDish {
    private String name;
    private String timeAgo;
    private int imageResId;

    public RecentDish(String name, String timeAgo, int imageResId) {
        this.name = name;
        this.timeAgo = timeAgo;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getTimeAgo() { return timeAgo; }
    public int getImageResId() { return imageResId; }
}

