package com.example.myapplication.dao;

import android.os.AsyncTask;
import com.example.myapplication.connnectDB.DatabaseConnection;
import com.example.myapplication.entity.Dish;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {

    // Interface cho callback khi lấy danh sách món ăn
    public interface DishCallback {
        void onSuccess(List<Dish> dishes);
        void onError(String error);
    }

    // Interface cho callback khi thêm/xóa yêu thích
    public interface FavoriteCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Lấy tất cả món ăn
    public static void getAllDishes(DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT * FROM Dishes ORDER BY created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Lấy món ăn gần đây (2 món mới nhất)
    public static void getRecentDishes(DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT TOP 2 * FROM Dishes ORDER BY created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Lấy món ăn yêu thích của user
    public static void getFavoriteDishes(int userId, DishCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        String query = "SELECT d.* FROM Dishes d " +
                                "INNER JOIN User_Favorites uf ON d.id = uf.dishes_id " +
                                "WHERE uf.user_id = ? ORDER BY uf.created_at DESC";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setInt(1, userId);
                        ResultSet resultSet = statement.executeQuery();

                        List<Dish> dishes = new ArrayList<>();
                        while (resultSet.next()) {
                            Dish dish = new Dish();
                            dish.setId(resultSet.getInt("id"));
                            dish.setName(resultSet.getString("name"));
                            dish.setDescription(resultSet.getString("description"));
                            dish.setUserId(resultSet.getInt("user_id"));
                            dish.setImageUrl(resultSet.getString("image_url"));
                            dish.setCookingSteps(resultSet.getString("cooking_steps"));
                            dish.setIngredient(resultSet.getString("ingredient"));
                            dish.setDifficultyLevel(resultSet.getString("difficulty_level"));
                            dish.setCreatedAt(resultSet.getString("created_at"));
                            dishes.add(dish);
                        }

                        connection.close();
                        return dishes;
                    } else {
                        return "Connection failed";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof List) {
                    callback.onSuccess((List<Dish>) result);
                } else {
                    callback.onError(result.toString());
                }
            }
        }.execute();
    }

    // Thêm/xóa món ăn yêu thích
    public static void toggleFavorite(int userId, int dishId, FavoriteCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Connection connection = DatabaseConnection.getConnection();
                    if (connection != null) {
                        // Kiểm tra xem đã yêu thích chưa
                        String checkQuery = "SELECT COUNT(*) FROM User_Favorites WHERE user_id = ? AND dishes_id = ?";
                        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                        checkStatement.setInt(1, userId);
                        checkStatement.setInt(2, dishId);
                        ResultSet resultSet = checkStatement.executeQuery();

                        resultSet.next();
                        int count = resultSet.getInt(1);

                        if (count > 0) {
                            // Xóa khỏi yêu thích
                            String deleteQuery = "DELETE FROM User_Favorites WHERE user_id = ? AND dishes_id = ?";
                            PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                            deleteStatement.setInt(1, userId);
                            deleteStatement.setInt(2, dishId);
                            deleteStatement.executeUpdate();
                            connection.close();
                            return "Đã xóa khỏi yêu thích";
                        } else {
                            // Thêm vào yêu thích
                            String insertQuery = "INSERT INTO User_Favorites (user_id, dishes_id) VALUES (?, ?)";
                            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                            insertStatement.setInt(1, userId);
                            insertStatement.setInt(2, dishId);
                            insertStatement.executeUpdate();
                            connection.close();
                            return "Đã thêm vào yêu thích";
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
                if (result.contains("thêm") || result.contains("xóa")) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(result);
                }
            }
        }.execute();
    }
}
