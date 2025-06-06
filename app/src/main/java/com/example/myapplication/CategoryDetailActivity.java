package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.entity.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.List;

public class CategoryDetailActivity extends AppCompatActivity {
    private TextView categoryTitleTextView;
    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> dishList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        initViews();
        setupData();
        loadDishes();
    }

    private void initViews() {
        categoryTitleTextView = findViewById(R.id.categoryTitleTextView);
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
    }

    private void setupData() {
        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        categoryTitleTextView.setText(categoryName);

        dishList = new ArrayList<>();
        dishAdapter = new DishAdapter(dishList, this::onDishClick, this::onFavoriteClick);
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishRecyclerView.setAdapter(dishAdapter);
    }

    private void loadDishes() {
        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                dishList.clear();
                dishList.addAll(dishes);
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CategoryDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDishClick(Dish dish) {
        Toast.makeText(this, "Clicked: " + dish.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onFavoriteClick(Dish dish) {
        Toast.makeText(this, "Favorite: " + dish.getName(), Toast.LENGTH_SHORT).show();
    }
}
