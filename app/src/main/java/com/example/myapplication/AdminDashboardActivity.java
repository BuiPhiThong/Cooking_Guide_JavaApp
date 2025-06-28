package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    private LinearLayout manageUsersLayout, addDishLayout, manageDishesLayout;
    private Button logoutButton;
    private int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminId = getIntent().getIntExtra("USER_ID", 0);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        manageUsersLayout = findViewById(R.id.manageUsersLayout);
        addDishLayout = findViewById(R.id.addDishLayout);
        manageDishesLayout = findViewById(R.id.manageDishesLayout);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupClickListeners() {
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
            Intent intent = new Intent(this, ManageDishesActivity.class);
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
}
