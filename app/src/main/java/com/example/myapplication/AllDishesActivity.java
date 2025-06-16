package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.List;

public class AllDishesActivity extends AppCompatActivity {
    private EditText searchEditText;
    private ChipGroup difficultyChipGroup;
    private RecyclerView dishRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyTextView;

    private DishAdapter dishAdapter;
    private List<Dish> allDishes = new ArrayList<>();
    private List<Dish> filteredDishes = new ArrayList<>();
    private int currentUserId;

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

        // Thêm back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        currentUserId = getIntent().getIntExtra("USER_ID", 0);
    }


    private void setupRecyclerView() {
        dishAdapter = new DishAdapter(filteredDishes, this::onDishClick, this::onFavoriteClick);
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
                showLoading(false);
                allDishes.clear();
                allDishes.addAll(dishes);
                applyFilters();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AllDishesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreDishes() {
        if (isLoading) return;

        isLoading = true;
        currentPage++;

        // Simulate loading more data (in real app, call API with page parameter)
        new android.os.Handler().postDelayed(() -> {
            // Add more filtered items to display
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

        // Reset pagination
        currentPage = 1;
        filteredDishes.clear();

        // Add first page of results
        int endIndex = Math.min(itemsPerPage, results.size());
        if (endIndex > 0) {
            filteredDishes.addAll(results.subList(0, endIndex));
        }

        dishAdapter.notifyDataSetChanged();

        // Show/hide empty state
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
        Toast.makeText(this, "Clicked: " + dish.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onFavoriteClick(Dish dish) {
        DishDAO.toggleFavorite(currentUserId, dish.getId(), new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AllDishesActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AllDishesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
