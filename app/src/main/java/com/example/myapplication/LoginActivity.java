package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;
import com.example.myapplication.connnectDB.TestConnectionActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button signInButton, testConnectionButton;
    private TextView signUpLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        testConnectionButton = findViewById(R.id.testConnectionButton);
        signUpLinkTextView = findViewById(R.id.signUpLinkTextView);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> performSignIn());
        testConnectionButton.setOnClickListener(v -> openTestConnection());
        signUpLinkTextView.setOnClickListener(v -> goToSignUp());
    }

    private void openTestConnection() {
        Intent intent = new Intent(this, TestConnectionActivity.class);
        startActivity(intent);
    }

    private void performSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        signInButton.setEnabled(false);
        signInButton.setText("Signing in...");

        UserDAO.login(email, password, new UserDAO.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");

                // Chuyển sang màn hình thành công với thông tin user
                Intent intent = new Intent(LoginActivity.this, LoginSuccessActivity.class);
                intent.putExtra("USER_ID", user.getId());
                intent.putExtra("USERNAME", user.getUsername());
                intent.putExtra("EMAIL", user.getEmail());
                intent.putExtra("FULL_NAME", user.getFullName());
                intent.putExtra("ROLE", user.getRole());
                intent.putExtra("BIO", user.getBio());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");
                Toast.makeText(LoginActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}
