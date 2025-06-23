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

public class EditProfileActivity extends AppCompatActivity {
    private EditText editFullName, editEmail, editBio, editUsername;
    private Button saveProfileButton;
    private ImageView editProfileImageView;
    private int userId;
    private User currentUser;
    private static final int REQUEST_PICK_IMAGE = 1001;
    private String avatarPath; // Đường dẫn ảnh local
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editUsername = findViewById(R.id.editUsername);
        editProfileImageView = findViewById(R.id.editProfileImageView);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editBio = findViewById(R.id.editBio);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        userId = getIntent().getIntExtra("USER_ID", 0);
        ImageView editAvatarButton = findViewById(R.id.editAvatarButton);
        editAvatarButton.setOnClickListener(v -> openImagePicker());
        UserDAO.getUserById(userId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                editUsername.setText(user.getUsername());
                editFullName.setText(user.getFullName());
                editEmail.setText(user.getEmail());
                editBio.setText(user.getBio());
                loadUserAvatar(user.getAvatarUrl()); // Add this line
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });


        saveProfileButton.setOnClickListener(v -> saveProfile());
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), REQUEST_PICK_IMAGE);
    }
    private void saveProfile() {
        String username = editUsername.getText().toString().trim();
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create updated user object
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setUsername(username);
        updatedUser.setPassword(currentUser.getPassword());
        updatedUser.setFullName(fullName);
        updatedUser.setEmail(email);
        updatedUser.setBio(bio);
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setCreatedAt(currentUser.getCreatedAt());

        // Handle avatar update
        if (avatarPath != null && !avatarPath.isEmpty()) {
            updatedUser.setAvatarUrl(avatarPath);
        } else {
            updatedUser.setAvatarUrl(currentUser.getAvatarUrl());
        }

        // Show loading state
        saveProfileButton.setEnabled(false);
        saveProfileButton.setText("Đang lưu...");

        UserDAO.updateUser(updatedUser, new UserDAO.UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                    saveProfileButton.setEnabled(true);
                    saveProfileButton.setText("Lưu thay đổi");
                });
            }
        });
    }

    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                // If it's a local file path
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        editProfileImageView.setImageBitmap(bitmap);
                    }
                }
                // If you're using network URLs, use Glide or Picasso here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editProfileImageView.setImageBitmap(bitmap);

                // Lưu ảnh vào internal storage và lấy đường dẫn
                avatarPath = saveProfileImage(bitmap, "avatar_user_" + userId + ".png");
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Lưu ảnh vào internal storage
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
