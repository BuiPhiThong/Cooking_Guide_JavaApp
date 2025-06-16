package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.entity.RecentDish;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.entity.CategoryAdapter;
import com.example.myapplication.entity.RecentDishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import com.example.myapplication.entity.Category;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private TextView userNameTextView;
    private ImageView profileImageView, notificationImageView;
    private EditText searchEditText;
    private RecyclerView categoryRecyclerView, recentDishRecyclerView;
    private BottomNavigationView bottomNavigationView;

    private CategoryAdapter categoryAdapter;
    private RecentDishAdapter recentDishAdapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupUserInfo();
        setupCategories();
        loadRecentDishes();
        setupBottomNavigation();
    }

    private void initViews() {
        userNameTextView = findViewById(R.id.userNameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        notificationImageView = findViewById(R.id.notificationImageView);
        searchEditText = findViewById(R.id.searchEditText);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        recentDishRecyclerView = findViewById(R.id.recentDishRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupUserInfo() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);
        String username = getIntent().getStringExtra("USERNAME");

        userNameTextView.setText("Tìm kiếm");

        profileImageView.setOnClickListener(v -> openProfile());
        notificationImageView.setOnClickListener(v -> openNotifications());
    }

    private void setupCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Khoai lang", R.drawable.ic_potato));
        categories.add(new Category("Gà rán", R.drawable.ic_chicken));
        categories.add(new Category("Mỳ xào", R.drawable.ic_noodles));
        categories.add(new Category("Thịt rang cháy cạnh", R.drawable.ic_meat));
        categories.add(new Category("Bún bò", R.drawable.ic_bowl));
        categories.add(new Category("Phở", R.drawable.ic_pho));

        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Thêm click listener cho "Tất cả các món ăn"
        TextView allDishesTextView = findViewById(R.id.allDishesTextView); // ID của TextView "Tất cả các món ăn"
        allDishesTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllDishesActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
    }

    private void loadRecentDishes() {
        DishDAO.getRecentDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                List<RecentDish> recentDishes = new ArrayList<>();
                for (Dish dish : dishes) {
                    String timeAgo = calculateTimeAgo(dish.getCreatedAt());
                    recentDishes.add(new RecentDish(dish.getName(), timeAgo, R.drawable.ic_dish_placeholder));
                }

                recentDishAdapter = new RecentDishAdapter(recentDishes, HomeActivity.this::onRecentDishClick);
                recentDishRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                recentDishRecyclerView.setAdapter(recentDishAdapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_favorites) {
                openFavorites();
                return true;
            }
            return false;
        });
    }

    private void onCategoryClick(Category category) {
        Intent intent = new Intent(this, CategoryDetailActivity.class);
        intent.putExtra("CATEGORY_NAME", category.getName());
        startActivity(intent);
    }

    private void onRecentDishClick(RecentDish recentDish) {
        Toast.makeText(this, "Clicked: " + recentDish.getName(), Toast.LENGTH_SHORT).show();
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    private void openNotifications() {
        Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
    }

    private void openFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private String calculateTimeAgo(String createdAt) {
        // Tính toán thời gian đã qua (đơn giản hóa)
        return "Cách đây 5 ngày";
    }
}
