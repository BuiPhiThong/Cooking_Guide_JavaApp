package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.entity.CookingStepAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import com.example.myapplication.entity.CookingStep;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DishDetailActivity extends AppCompatActivity {
    private ImageView backButton, favoriteButton, dishImageView;
    private TextView dishNameTextView, dishDescriptionTextView;
    private TextView difficultyTextView, ingredientsTextView;
    private RecyclerView stepsRecyclerView;

    private CookingStepAdapter stepAdapter;
    private int dishId;
    private int currentUserId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        initViews();
        setupData();
        setupClickListeners();
        loadDishDetails();
        checkFavoriteStatus();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        dishImageView = findViewById(R.id.dishImageView);
        dishNameTextView = findViewById(R.id.dishNameTextView);
        dishDescriptionTextView = findViewById(R.id.dishDescriptionTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        ingredientsTextView = findViewById(R.id.ingredientsTextView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
    }

    private void setupData() {
        dishId = getIntent().getIntExtra("DISH_ID", 0);
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        Log.d("DishDetail", "Loading dish ID: " + dishId + " for user: " + currentUserId);

        // Setup RecyclerView
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    private void loadDishDetails() {
        DishDAO.getDishById(dishId, new DishDAO.DishDetailCallback() {
            @Override
            public void onSuccess(Dish dish) {
                Log.d("DishDetail", "Dish loaded: " + dish.getName());
                Log.d("DishDetail", "Cooking steps: " + dish.getCookingSteps());
                displayDishDetails(dish);
            }

            @Override
            public void onError(String error) {
                Log.e("DishDetail", "Error loading dish: " + error);
                Toast.makeText(DishDetailActivity.this, "Lỗi tải món ăn: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void checkFavoriteStatus() {
        DishDAO.checkIfFavorite(currentUserId, dishId, isFavorite -> {
            this.isFavorite = isFavorite;
            updateFavoriteButton();
        });
    }

    private void displayDishDetails(Dish dish) {
        // Hiển thị thông tin cơ bản
        dishNameTextView.setText(dish.getName());
        dishDescriptionTextView.setText(dish.getDescription());
        difficultyTextView.setText(getDifficultyText(dish.getDifficultyLevel()));

        // Format nguyên liệu
        String ingredients = formatIngredients(dish.getIngredient());
        ingredientsTextView.setText(ingredients);

        // Parse cooking_steps từ database và hiển thị
        List<CookingStep> steps = parseCookingStepsFromDatabase(dish.getCookingSteps());
        stepAdapter = new CookingStepAdapter(steps);
        stepsRecyclerView.setAdapter(stepAdapter);

        Log.d("DishDetail", "Parsed " + steps.size() + " cooking steps");
    }

    private String getDifficultyText(String difficulty) {
        if (difficulty == null) return "Chưa xác định";

        switch (difficulty.toLowerCase()) {
            case "easy": return "Dễ làm";
            case "medium": return "Trung bình";
            case "hard": return "Khó";
            default: return difficulty;
        }
    }

    private String formatIngredients(String ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "Chưa có thông tin nguyên liệu";
        }

        // Tách nguyên liệu bằng dấu phẩy
        String[] items = ingredients.split(",");
        StringBuilder formatted = new StringBuilder();

        for (String item : items) {
            formatted.append("• ").append(item.trim()).append("\n");
        }

        return formatted.toString().trim();
    }

    /**
     * Parse cooking_steps từ cột cooking_steps trong bảng Dishes
     * Format trong database: "1. Boil pasta\n2. Cook pancetta\n3. Mix with eggs and cheese"
     */
    /**
     * Parse cooking_steps từ cột cooking_steps trong bảng Dishes
     * Format trong database: "1. Butter bread\n2. Add cheese\n3. Grill until golden brown"
     */
    private List<CookingStep> parseCookingStepsFromDatabase(String cookingSteps) {
        List<CookingStep> steps = new ArrayList<>();

        if (cookingSteps == null || cookingSteps.trim().isEmpty()) {
            steps.add(new CookingStep(1, "Bước 1", "Chưa có hướng dẫn chi tiết"));
            return steps;
        }

        // Chuyển tất cả '\\n' thành xuống dòng thực sự '\n'
        cookingSteps = cookingSteps.replace("\\n", "\n");

        // Dùng regex để tách theo định dạng số thứ tự đầu dòng
        Pattern pattern = Pattern.compile("(?m)^(\\d+)\\.\\s*(.*?)(?=^\\d+\\.|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cookingSteps);

        int stepNumber = 1;
        while (matcher.find()) {
            String content = matcher.group(2).trim();
            if (!content.isEmpty()) {
                steps.add(new CookingStep(stepNumber, "Bước " + stepNumber, content));
                stepNumber++;
            }
        }

        // Nếu không match theo regex, fallback tách từng dòng
        if (steps.isEmpty()) {
            String[] lines = cookingSteps.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replaceFirst("^\\d+\\.\\s*", "").trim();
                if (!line.isEmpty()) {
                    steps.add(new CookingStep(i + 1, "Bước " + (i + 1), line));
                }
            }
        }

        return steps;
    }

    private void toggleFavorite() {
        DishDAO.toggleFavorite(currentUserId, dishId, new DishDAO.FavoriteCallback() {
            @Override
            public void onSuccess(String message) {
                isFavorite = !isFavorite;
                updateFavoriteButton();
                Toast.makeText(DishDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DishDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_bookmark_filled);
            favoriteButton.setColorFilter(getColor(android.R.color.holo_red_light));
        } else {
            favoriteButton.setImageResource(R.drawable.ic_bookmark_border);
            favoriteButton.setColorFilter(getColor(android.R.color.darker_gray));
        }
    }
}
