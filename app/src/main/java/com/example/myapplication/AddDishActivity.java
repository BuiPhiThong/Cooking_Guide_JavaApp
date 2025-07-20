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

public class AddDishActivity extends AppCompatActivity {
    private EditText dishNameEditText, dishDescriptionEditText, ingredientsEditText, cookingStepsEditText;
    private Spinner difficultySpinner;
    private ImageView dishImageView;
    private Button selectImageButton, saveDishButton;
    private String dishImagePath;
    private int adminId;
    private static final int REQUEST_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        adminId = getIntent().getIntExtra("ADMIN_ID", 0);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
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
        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveDishButton.setOnClickListener(v -> saveDish());
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
                dishImagePath = saveDishImage(bitmap, "dish_" + System.currentTimeMillis() + ".png");
                Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void saveDish() {
//        String name = dishNameEditText.getText().toString().trim();
//        String description = dishDescriptionEditText.getText().toString().trim();
//        String ingredients = ingredientsEditText.getText().toString().trim();
//        String cookingSteps = cookingStepsEditText.getText().toString().trim();
//        String difficulty = difficultySpinner.getSelectedItem().toString();
//
//        if (name.isEmpty() || description.isEmpty() || ingredients.isEmpty() || cookingSteps.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Dish newDish = new Dish();
//        newDish.setName(name);
//        newDish.setDescription(description);
//        newDish.setIngredient(ingredients);
//        newDish.setCookingSteps(cookingSteps);
//        newDish.setDifficultyLevel(difficulty);
//        newDish.setUserId(adminId);
//        newDish.setImageUrl(dishImagePath);
//
//        DishDAO.addDish(newDish, new DishDAO.AddDishCallback() {
//            @Override
//            public void onSuccess() {
//                Toast.makeText(AddDishActivity.this, "Đã thêm món ăn thành công", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//
//            @Override
//            public void onError(String error) {
//                Toast.makeText(AddDishActivity.this, "Lỗi thêm món ăn: " + error, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

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

        // TỰ ĐỘNG FORMAT COOKING STEPS TỪ XUỐNG DÒNG
        String formattedCookingSteps = formatCookingStepsFromNewLines(cookingSteps);

        Dish newDish = new Dish();
        newDish.setName(name);
        newDish.setDescription(description);
        newDish.setIngredient(ingredients);
        newDish.setCookingSteps(formattedCookingSteps);
        newDish.setDifficultyLevel(difficulty);
        newDish.setUserId(adminId);
        newDish.setImageUrl(dishImagePath);

        DishDAO.addDish(newDish, new DishDAO.AddDishCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddDishActivity.this, "Đã thêm món ăn thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddDishActivity.this, "Lỗi thêm món ăn: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String formatCookingStepsFromNewLines(String rawSteps) {
        if (rawSteps == null || rawSteps.trim().isEmpty()) {
            return rawSteps;
        }

        // Tách các bước bằng xuống dòng
        String[] steps = rawSteps.split("\\n");
        StringBuilder formattedSteps = new StringBuilder();

        int stepNumber = 1;
        for (String step : steps) {
            String trimmedStep = step.trim();
            if (!trimmedStep.isEmpty()) {
                // Loại bỏ số thứ tự cũ nếu có
                trimmedStep = trimmedStep.replaceFirst("^\\d+[\\.\\)]\\s*", "");

                formattedSteps.append(stepNumber).append(". ").append(trimmedStep);

                // Thêm \\n để lưu database (trừ bước cuối)
                if (stepNumber < countNonEmptySteps(steps)) {
                    formattedSteps.append("\\n");
                }
                stepNumber++;
            }
        }

        return formattedSteps.toString();
    }


    private int countNonEmptySteps(String[] steps) {
        int count = 0;
        for (String step : steps) {
            if (!step.trim().isEmpty()) {
                count++;
            }
        }
        return count;
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
