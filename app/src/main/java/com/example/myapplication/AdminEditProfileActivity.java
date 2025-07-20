package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;
import java.io.FileOutputStream;

public class AdminEditProfileActivity extends AppCompatActivity {
    private EditText editFullName, editEmail, editBio, editUsername;
    private Button saveProfileButton, cancelButton;
    private ImageView editProfileImageView;
    private int adminId;
    private User currentAdmin;
    private static final int REQUEST_PICK_IMAGE = 1001;
    private String avatarPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        initViews();
        setupAdminData();
        setupClickListeners();
    }

    private void initViews() {
        editUsername = findViewById(R.id.editUsername);
        editProfileImageView = findViewById(R.id.editProfileImageView);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editBio = findViewById(R.id.editBio);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupAdminData() {
        adminId = getIntent().getIntExtra("ADMIN_ID", 0);

        if (adminId == 0) {
            adminId = getIntent().getIntExtra("USER_ID", 0);
        }

        if (adminId != 0) {
            loadAdminData();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin admin", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAdminData() {
        UserDAO.getUserById(adminId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User admin) {
                currentAdmin = admin;

                // Điền thông tin vào các EditText
                editUsername.setText(admin.getUsername());
                editFullName.setText(admin.getFullName() != null ? admin.getFullName() : "");
                editEmail.setText(admin.getEmail());
                editBio.setText(admin.getBio() != null ? admin.getBio() : "");

                // Load avatar
                loadAdminAvatar(admin.getAvatarUrl());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminEditProfileActivity.this,
                        "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadAdminAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        editProfileImageView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupClickListeners() {
        ImageView editAvatarButton = findViewById(R.id.editAvatarButton);
        editAvatarButton.setOnClickListener(v -> openImagePicker());

        saveProfileButton.setOnClickListener(v -> saveAdminProfile());

        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), REQUEST_PICK_IMAGE);
    }

    private void saveAdminProfile() {
        String username = editUsername.getText().toString().trim();
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create updated admin object
        User updatedAdmin = new User();
        updatedAdmin.setId(currentAdmin.getId());
        updatedAdmin.setUsername(username);
        updatedAdmin.setPassword(currentAdmin.getPassword());
        updatedAdmin.setFullName(fullName);
        updatedAdmin.setEmail(email);
        updatedAdmin.setBio(bio);
        updatedAdmin.setRole(currentAdmin.getRole()); // Keep admin role
        updatedAdmin.setCreatedAt(currentAdmin.getCreatedAt());

        // Handle avatar update
        if (avatarPath != null && !avatarPath.isEmpty()) {
            updatedAdmin.setAvatarUrl(avatarPath);
        } else {
            updatedAdmin.setAvatarUrl(currentAdmin.getAvatarUrl());
        }

        // Show loading state
        saveProfileButton.setEnabled(false);
        saveProfileButton.setText("Đang lưu...");

        UserDAO.updateUser(updatedAdmin, new UserDAO.UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AdminEditProfileActivity.this,
                            "Cập nhật thông tin admin thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminEditProfileActivity.this,
                            "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                    saveProfileButton.setEnabled(true);
                    saveProfileButton.setText("Lưu thông tin");
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editProfileImageView.setImageBitmap(bitmap);

                // Lưu ảnh vào internal storage với tên riêng cho admin
                avatarPath = saveProfileImage(bitmap, "avatar_admin_" + adminId + ".png");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveProfileImage(Bitmap bitmap, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return getFilesDir() + "/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
