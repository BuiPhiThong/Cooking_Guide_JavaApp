package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

public class AdminDashboardActivity extends AppCompatActivity {
    private LinearLayout manageUsersLayout, addDishLayout, manageDishesLayout;
    private Button logoutButton;
    private CardView adminProfileCard;
    private ImageView adminAvatarImageView;
    private TextView adminNameTextView, adminUsernameTextView, adminEmailTextView;
    private int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminId = getIntent().getIntExtra("USER_ID", 0);

        initViews();
        setupClickListeners();
        loadAdminProfileData(); // Load admin data for profile card
    }

    private void initViews() {
        manageUsersLayout = findViewById(R.id.manageUsersLayout);
        addDishLayout = findViewById(R.id.addDishLayout);
        manageDishesLayout = findViewById(R.id.manageDishesLayout);
        logoutButton = findViewById(R.id.logoutButton);

        // Admin profile card views
        adminProfileCard = findViewById(R.id.adminProfileCard);
        adminAvatarImageView = findViewById(R.id.adminAvatarImageView);
        adminNameTextView = findViewById(R.id.adminNameTextView);
        adminUsernameTextView = findViewById(R.id.adminUsernameTextView);
        adminEmailTextView = findViewById(R.id.adminEmailTextView);
    }

    private void loadAdminProfileData() {
        if (adminId != 0) {
            UserDAO.getUserById(adminId, new UserDAO.UserCallback() {
                @Override
                public void onSuccess(User admin) {
                    // Update admin profile card
                    adminNameTextView.setText(admin.getFullName() != null ?
                            admin.getFullName() : "Administrator");
                    adminUsernameTextView.setText("@" + admin.getUsername());
                    adminEmailTextView.setText(admin.getEmail());

                    // Load admin avatar
                    loadAdminAvatar(admin.getAvatarUrl());
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Lỗi tải thông tin admin: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadAdminAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (bitmap != null) {
                        adminAvatarImageView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupClickListeners() {
        // Admin profile card click
        adminProfileCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        manageUsersLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageUsersActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        addDishLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDishActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        manageDishesLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.myapplication.ManageDishesActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload admin data when returning to dashboard
        loadAdminProfileData();
    }
}
