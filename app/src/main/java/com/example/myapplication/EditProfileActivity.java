package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.User;

public class EditProfileActivity extends AppCompatActivity {
    private EditText editFullName, editEmail, editBio;
    private Button saveProfileButton;
    private ImageView editProfileImageView;
    private int userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileImageView = findViewById(R.id.editProfileImageView);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editBio = findViewById(R.id.editBio);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        userId = getIntent().getIntExtra("USER_ID", 0);
        UserDAO.getUserById(userId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                editFullName.setText(user.getFullName());
                editEmail.setText(user.getEmail());
                editBio.setText(user.getBio());
                // Nếu có avatar thì load vào editProfileImageView (dùng Glide/Picasso)
            }
            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });


        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật thông tin user
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setUsername(currentUser.getUsername());
        updatedUser.setPassword(currentUser.getPassword());
        updatedUser.setFullName(fullName);
        updatedUser.setEmail(email);
        updatedUser.setBio(bio);
        updatedUser.setAvatarUrl(currentUser.getAvatarUrl());
        updatedUser.setRole(currentUser.getRole());
        updatedUser.setCreatedAt(currentUser.getCreatedAt());

        UserDAO.updateUser(updatedUser, new UserDAO.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
