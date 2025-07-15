package com.example.myapplication.entity;

public class RecentDish {
    private String name;
    private String timeAgo;
    private int imageResource;
    private int dishId; // Thêm field này

    // Constructor mới
    public RecentDish(String name, String timeAgo, int imageResource, int dishId) {
        this.name = name;
        this.timeAgo = timeAgo;
        this.imageResource = imageResource;
        this.dishId = dishId;
    }

    // Constructor cũ (để tương thích)
    public RecentDish(String name, String timeAgo, int imageResource) {
        this.name = name;
        this.timeAgo = timeAgo;
        this.imageResource = imageResource;
        this.dishId = 0; // Giá trị mặc định
    }

    // Getter cho dishId
    public int getDishId() {
        return dishId;
    }

    // Setter cho dishId
    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    // Các getter/setter khác giữ nguyên...
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
}


