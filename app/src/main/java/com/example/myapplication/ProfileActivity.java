package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView usernameTextView, emailTextView, fullNameTextView, bioTextView;
    private Button editProfileButton, logoutButton;

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
            // TODO: Implement edit profile
            Toast.makeText(this, "Chức năng sẽ được cập nhật", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
