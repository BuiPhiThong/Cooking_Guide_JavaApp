package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import java.io.FileOutputStream;
import java.util.List;

public class EditDishActivity extends AppCompatActivity {
    private EditText dishNameEditText, dishDescriptionEditText, ingredientsEditText, cookingStepsEditText;
    private Spinner difficultySpinner;
    private ImageView dishImageView, backButton;
    private Button selectImageButton, saveDishButton;
    private String dishImagePath;
    private int dishId;
    private Dish currentDish;
    private static final int REQUEST_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dish);

        dishId = getIntent().getIntExtra("DISH_ID", 0);

        initViews();
        setupClickListeners();
        loadDishData();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        dishNameEditText = findViewById(R.id.dishNameEditText);
        dishDescriptionEditText = findViewById(R.id.dishDescriptionEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        cookingStepsEditText = findViewById(R.id.cookingStepsEditText);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        dishImageView = findViewById(R.id.dishImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveDishButton = findViewById(R.id.saveDishButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveDishButton.setOnClickListener(v -> saveDish());
    }


    private void loadDishData() {
        if (dishId <= 0) {
            Toast.makeText(this, "ID món ăn không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DishDAO.getDishById(dishId, new DishDAO.DishDetailCallback() {
            @Override
            public void onSuccess(Dish dish) {
                if (dish != null) {
                    currentDish = dish;
                    dishNameEditText.setText(dish.getName() != null ? dish.getName() : "");
                    dishDescriptionEditText.setText(dish.getDescription() != null ? dish.getDescription() : "");
                    ingredientsEditText.setText(dish.getIngredient() != null ? dish.getIngredient() : "");
                    cookingStepsEditText.setText(dish.getCookingSteps() != null ? dish.getCookingSteps() : "");

                    // Set difficulty spinner
                    String[] difficulties = {"easy", "medium", "hard"};
                    String currentDifficulty = dish.getDifficultyLevel();
                    if (currentDifficulty != null) {
                        for (int i = 0; i < difficulties.length; i++) {
                            if (difficulties[i].equalsIgnoreCase(currentDifficulty)) {
                                difficultySpinner.setSelection(i);
                                break;
                            }
                        }
                    }

                    // Load image
                    if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
                        try {
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(dish.getImageUrl());
                            if (bitmap != null) {
                                dishImageView.setImageBitmap(bitmap);
                            } else {
                                dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                            }
                        } catch (Exception e) {
                            dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                        }
                    } else {
                        dishImageView.setImageResource(R.drawable.ic_dish_placeholder);
                    }
                } else {
                    Toast.makeText(EditDishActivity.this, "Không tìm thấy thông tin món ăn", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditDishActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh món ăn"), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                dishImageView.setImageBitmap(bitmap);
                dishImagePath = saveDishImage(bitmap, "dish_" + dishId + "_" + System.currentTimeMillis() + ".png");
                Toast.makeText(this, "Đã chọn ảnh mới", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDish() {
        String name = dishNameEditText.getText().toString().trim();
        String description = dishDescriptionEditText.getText().toString().trim();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String cookingSteps = cookingStepsEditText.getText().toString().trim();
        String difficulty = difficultySpinner.getSelectedItem().toString();

        if (name.isEmpty() || description.isEmpty() || ingredients.isEmpty() || cookingSteps.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Dish updatedDish = new Dish();
        updatedDish.setId(currentDish.getId());
        updatedDish.setName(name);
        updatedDish.setDescription(description);
        updatedDish.setIngredient(ingredients);
        updatedDish.setCookingSteps(cookingSteps);
        updatedDish.setDifficultyLevel(difficulty);
        updatedDish.setUserId(currentDish.getUserId());
        updatedDish.setCreatedAt(currentDish.getCreatedAt());

        // Xử lý ảnh
        if (dishImagePath != null && !dishImagePath.isEmpty()) {
            updatedDish.setImageUrl(dishImagePath);
        } else {
            updatedDish.setImageUrl(currentDish.getImageUrl());
        }

        DishDAO.updateDish(updatedDish, new DishDAO.AddDishCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditDishActivity.this, "Cập nhật món ăn thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditDishActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String saveDishImage(Bitmap bitmap, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return getFilesDir() + "/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
