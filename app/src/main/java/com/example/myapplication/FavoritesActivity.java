package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.DishAdapter;
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
        // Truyền true để báo đây là màn hình favorites
        dishAdapter = new DishAdapter(favoritesList, this::onDishClick, this::onFavoriteClick, true);
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

                // Hiển thị thông báo nếu không có món yêu thích
                if (dishes.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Bạn chưa có món ăn yêu thích nào", Toast.LENGTH_SHORT).show();
                }
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
        // Hiển thị dialog xác nhận bỏ yêu thích
        showUnfavoriteDialog(dish);
    }

    private void showUnfavoriteDialog(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle("Bỏ yêu thích")
                .setMessage("Bạn có chắc muốn bỏ yêu thích món \"" + dish.getName() + "\" không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    removeFavorite(dish);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void removeFavorite(Dish dish) {
        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_SHORT).show();

                // Xóa món ăn khỏi danh sách hiển thị
                favoritesList.remove(dish);
                dishAdapter.notifyDataSetChanged();

                // Hiển thị thông báo nếu danh sách trống
                if (favoritesList.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FavoritesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
