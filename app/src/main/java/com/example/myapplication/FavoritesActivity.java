package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.entity.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView favoritesRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> favoritesList;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupData();
        loadFavorites();
    }

    private void initViews() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
    }

    private void setupData() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        favoritesList = new ArrayList<>();
        dishAdapter = new DishAdapter(favoritesList, this::onDishClick, this::onFavoriteClick);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(dishAdapter);
    }

    private void loadFavorites() {
        DishDAO.getFavoriteDishes(currentUserId, new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                favoritesList.clear();
                favoritesList.addAll(dishes);
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FavoritesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDishClick(Dish dish) {
        Toast.makeText(this, "Clicked: " + dish.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onFavoriteClick(Dish dish) {
        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_SHORT).show();
                loadFavorites(); // Reload danh sách
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FavoritesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
