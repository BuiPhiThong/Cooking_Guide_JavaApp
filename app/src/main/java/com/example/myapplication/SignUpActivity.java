package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.dao.UserDAO;

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
    private boolean validateInput(String email, String password) {
        boolean valid = true;
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            valid = false;
        } else {
            emailEditText.setError(null);
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }
        return valid;
    }

    private void performSignUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "Bạn phải đồng ý điều khoản!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateInput(email, password)) {
            return;
        }

        // Tạo username mặc định từ email (hoặc thêm trường nhập username nếu muốn)
        String username = email.split("@")[0];

        // Gọi UserDAO để đăng ký
        UserDAO.register(username, email, password, new UserDAO.RegisterCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình đăng nhập
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SignUpActivity.this, "Lỗi đăng ký: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void goToSignIn() {
        finish(); // Quay lại màn hình đăng nhập
    }
}
