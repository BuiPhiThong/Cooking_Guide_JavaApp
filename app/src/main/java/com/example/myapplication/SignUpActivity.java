package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private CheckBox termsCheckBox;
    private Button continueButton;
    private TextView signInLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        continueButton = findViewById(R.id.continueButton);
        signInLinkTextView = findViewById(R.id.signInLinkTextView);
    }

    private void setupClickListeners() {
        continueButton.setOnClickListener(v -> performSignUp());
        signInLinkTextView.setOnClickListener(v -> goToSignIn());
    }

    private void performSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "Please agree to Terms of Service", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý logic đăng ký
    }

    private void goToSignIn() {
        finish(); // Quay lại màn hình đăng nhập
    }
}
