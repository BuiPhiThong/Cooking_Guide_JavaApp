package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView usernameTextView, emailTextView, fullNameTextView, bioTextView;
    private Button editProfileButton, logoutButton;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupUserInfo();
        setupClickListeners();
        loadUserFromDatabase(); // THÊM DÒNG NÀY để load avatar ngay khi mở
    }

    private void initViews() {
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        bioTextView = findViewById(R.id.bioTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupUserInfo() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);

        String username = getIntent().getStringExtra("USERNAME");
        String email = getIntent().getStringExtra("EMAIL");
        String fullName = getIntent().getStringExtra("FULL_NAME");
        String bio = getIntent().getStringExtra("BIO");

        usernameTextView.setText("@" + username);
        emailTextView.setText(email);
        fullNameTextView.setText(fullName != null ? fullName : "Chưa cập nhật");
        bioTextView.setText(bio != null ? bio : "Chưa có mô tả");
    }

    // THÊM METHOD NÀY để load user từ database ngay khi mở activity
    private void loadUserFromDatabase() {
        UserDAO.getUserById(currentUserId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // Cập nhật tất cả thông tin từ database
                usernameTextView.setText("@" + user.getUsername());
                emailTextView.setText(user.getEmail());
                fullNameTextView.setText(user.getFullName() != null ? user.getFullName() : "Chưa cập nhật");
                bioTextView.setText(user.getBio() != null ? user.getBio() : "Chưa có mô tả");
                loadUserAvatar(user.getAvatarUrl());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // THÊM METHOD NÀY để load avatar
    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        profileImageView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivityForResult(intent, 100);
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            reloadUserInfo();
        }
    }

    private void reloadUserInfo() {
        UserDAO.getUserById(currentUserId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                usernameTextView.setText("@" + user.getUsername());
                emailTextView.setText(user.getEmail());
                fullNameTextView.setText(user.getFullName());
                bioTextView.setText(user.getBio());
                loadUserAvatar(user.getAvatarUrl()); // THÊM DÒNG NÀY
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải lại thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
