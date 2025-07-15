package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.myapplication.adapter.DishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllDishesActivity extends AppCompatActivity implements DishAdapter.FavoriteChecker {
    private EditText searchEditText;
    private ChipGroup difficultyChipGroup;
    private RecyclerView dishRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyTextView;

    private DishAdapter dishAdapter;
    private List<Dish> allDishes = new ArrayList<>();
    private List<Dish> filteredDishes = new ArrayList<>();
    private int currentUserId;

    // THÊM FAVORITE CACHE
    private Map<Integer, Boolean> favoriteCache = new HashMap<>();

    // Pagination
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private boolean isLoading = false;

    // Filter
    private String searchQuery = "";
    private String selectedDifficulty = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_dishes);

        initViews();
        setupRecyclerView();
        setupFilters();
        loadDishes();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        difficultyChipGroup = findViewById(R.id.difficultyChipGroup);
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        currentUserId = getIntent().getIntExtra("USER_ID", 0);
        Log.d("AllDishes", "User ID: " + currentUserId);
    }

    private void setupRecyclerView() {
        // TRUYỀN this làm FavoriteChecker
        dishAdapter = new DishAdapter(filteredDishes, this::onDishClick, this::onFavoriteClick, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dishRecyclerView.setLayoutManager(layoutManager);
        dishRecyclerView.setAdapter(dishAdapter);

        // Pagination scroll listener
        dishRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= itemsPerPage) {
                        loadMoreDishes();
                    }
                }
            }
        });
    }

    private void setupFilters() {
        // Search filter
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Difficulty filter
        difficultyChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedDifficulty = "";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedDifficulty = selectedChip.getText().toString().toLowerCase();
            }
            applyFilters();
        });
    }

    private void loadDishes() {
        showLoading(true);
        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                Log.d("AllDishes", "Loaded " + dishes.size() + " dishes");
                allDishes.clear();
                allDishes.addAll(dishes);

                // LOAD FAVORITE STATUS SAU KHI CÓ DANH SÁCH
                loadFavoriteStatus();
            }

            @Override
            public void onError(String error) {
                Log.e("AllDishes", "Error loading dishes: " + error);
                showLoading(false);
                Toast.makeText(AllDishesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // THÊM METHOD LOAD FAVORITE STATUS
    private void loadFavoriteStatus() {
        if (allDishes.isEmpty()) {
            showLoading(false);
            applyFilters();
            return;
        }

        // Khởi tạo tất cả dishes là chưa favorite
        favoriteCache.clear();
        for (Dish dish : allDishes) {
            favoriteCache.put(dish.getId(), false);
        }

        // Đếm số món ăn đã kiểm tra
        final int[] checkedCount = {0};
        final int totalCount = allDishes.size();

        for (Dish dish : allDishes) {
            DishDAO.checkIfFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCheckCallback() {
                @Override
                public void onResult(boolean isFavorite) {
                    favoriteCache.put(dish.getId(), isFavorite);
                    checkedCount[0]++;

                    Log.d("AllDishes", "Dish " + dish.getId() + " favorite: " + isFavorite +
                            " (" + checkedCount[0] + "/" + totalCount + ")");

                    // Khi đã kiểm tra xong tất cả
                    if (checkedCount[0] == totalCount) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            applyFilters();
                            Log.d("AllDishes", "All favorite status loaded");
                        });
                    }
                }
            });
        }
    }

    // IMPLEMENT INTERFACE FavoriteChecker
    @Override
    public boolean isDishFavorite(int dishId) {
        boolean isFavorite = favoriteCache.getOrDefault(dishId, false);
        Log.d("AllDishes", "Checking favorite for dish " + dishId + ": " + isFavorite);
        return isFavorite;
    }

    private void loadMoreDishes() {
        if (isLoading) return;

        isLoading = true;
        currentPage++;

        new android.os.Handler().postDelayed(() -> {
            int startIndex = (currentPage - 1) * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, getFilteredResults().size());

            if (startIndex < getFilteredResults().size()) {
                List<Dish> newItems = getFilteredResults().subList(startIndex, endIndex);
                filteredDishes.addAll(newItems);
                dishAdapter.notifyItemRangeInserted(filteredDishes.size() - newItems.size(), newItems.size());
            }

            isLoading = false;
        }, 1000);
    }

    private void applyFilters() {
        List<Dish> results = getFilteredResults();

        currentPage = 1;
        filteredDishes.clear();

        int endIndex = Math.min(itemsPerPage, results.size());
        if (endIndex > 0) {
            filteredDishes.addAll(results.subList(0, endIndex));
        }

        dishAdapter.notifyDataSetChanged();

        if (filteredDishes.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            dishRecyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            dishRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private List<Dish> getFilteredResults() {
        List<Dish> results = new ArrayList<>();

        for (Dish dish : allDishes) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    dish.getName().toLowerCase().contains(searchQuery) ||
                    dish.getDescription().toLowerCase().contains(searchQuery);

            boolean matchesDifficulty = selectedDifficulty.isEmpty() ||
                    dish.getDifficultyLevel().toLowerCase().equals(selectedDifficulty);

            if (matchesSearch && matchesDifficulty) {
                results.add(dish);
            }
        }

        return results;
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        dishRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void onDishClick(Dish dish) {
        Intent intent = new Intent(this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", dish.getId());
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }

    private void onFavoriteClick(Dish dish) {
        // Cập nhật cache ngay lập tức
        boolean currentStatus = favoriteCache.getOrDefault(dish.getId(), false);
        favoriteCache.put(dish.getId(), !currentStatus);
        dishAdapter.notifyDataSetChanged();

        Log.d("AllDishes", "Toggling favorite for dish " + dish.getId() +
                " from " + currentStatus + " to " + !currentStatus);

        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("AllDishes", "Toggle favorite success: " + message);
                Toast.makeText(AllDishesActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e("AllDishes", "Toggle favorite error: " + error);
                // Revert cache nếu có lỗi
                favoriteCache.put(dish.getId(), currentStatus);
                dishAdapter.notifyDataSetChanged();
                Toast.makeText(AllDishesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorite status khi quay lại từ DishDetailActivity
        if (!allDishes.isEmpty()) {
            loadFavoriteStatus();
        }
    }
}
