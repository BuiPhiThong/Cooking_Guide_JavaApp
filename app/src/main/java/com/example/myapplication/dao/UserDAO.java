package com.example.myapplication.dao;

import android.os.AsyncTask;

import com.example.myapplication.connnectDB.DatabaseConnection;
import com.example.myapplication.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(String error);
    }

    // Đăng nhập
    public static void login(String email, String password, LoginCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, email);
                        statement.setString(2, password); // Nên hash password

                        ResultSet resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            User user = new User();
                            user.setId(resultSet.getInt("id"));
                            user.setUsername(resultSet.getString("username"));
                            user.setEmail(resultSet.getString("email"));
                            user.setFullName(resultSet.getString("full_name"));
                            user.setBio(resultSet.getString("bio"));
                            user.setRole(resultSet.getString("role"));

                            connection.close();
                            return user;
                        } else {
                            connection.close();
                            return "Invalid email or password";
                        }
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof User) {
                    callback.onSuccess((User) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Đăng ký
    public static void register(String username, String email, String password, RegisterCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "INSERT INTO Users (username, email, password, bio, role) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, username);
                        statement.setString(2, email);
                        statement.setString(3, password); // Nên hash password
                        statement.setString(4, ""); // bio trống
                        statement.setString(5, "user"); // role mặc định

                        int result = statement.executeUpdate();
                        connection.close();

                        if (result > 0) {
                            return "SUCCESS";
                        } else {
                            return "Registration failed";
                        }
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if ("SUCCESS".equals(result)) {
                    callback.onSuccess();
                } else {
                    callback.onError(result);
                }
            }
        }.execute();
    }
}
