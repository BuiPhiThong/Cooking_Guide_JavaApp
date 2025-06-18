package com.example.myapplication;

import android.content.Intent;
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
        currentUserId = getIntent().getIntExtra("USER_ID", 0); // THÊM DÒNG NÀY

        String username = getIntent().getStringExtra("USERNAME");
        String email = getIntent().getStringExtra("EMAIL");
        String fullName = getIntent().getStringExtra("FULL_NAME");
        String bio = getIntent().getStringExtra("BIO");

        usernameTextView.setText("@" + username);
        emailTextView.setText(email);
        fullNameTextView.setText(fullName != null ? fullName : "Chưa cập nhật");
        bioTextView.setText(bio != null ? bio : "Chưa có mô tả");
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
            // Reload lại thông tin user từ database
            reloadUserInfo();
        }
    }

    private void reloadUserInfo() {
        // Gọi UserDAO.getUserById(currentUserId, ...) để lấy lại thông tin mới nhất
        UserDAO.getUserById(currentUserId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                usernameTextView.setText("@" + user.getUsername());
                emailTextView.setText(user.getEmail());
                fullNameTextView.setText(user.getFullName());
                bioTextView.setText(user.getBio());
                // Nếu có avatar thì load lại avatar
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải lại thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
