package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.entity.FavoriteDishAdapter;
import com.example.myapplication.entity.RecentDish;
import com.example.myapplication.entity.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.entity.CategoryAdapter;
import com.example.myapplication.entity.RecentDishAdapter;
import com.example.myapplication.dao.DishDAO;
import com.example.myapplication.entity.Dish;
import com.example.myapplication.entity.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {
    private TextView userNameTextView;
    private ImageView profileImageView, notificationImageView;
    private EditText searchEditText;
    private RecyclerView favoriteDishRecyclerView, recentDishRecyclerView; // Đổi tên categoryRecyclerView
    private BottomNavigationView bottomNavigationView;

    private FavoriteDishAdapter favoriteDishAdapter; // Thay CategoryAdapter
    private RecentDishAdapter recentDishAdapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupUserInfo();
        loadMostFavoriteDishes(); // Thay setupCategories()
        loadRecentDishes();
        setupBottomNavigation();
        setupViewAllClick();
    }

    private void initViews() {
        userNameTextView = findViewById(R.id.userNameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        notificationImageView = findViewById(R.id.notificationImageView);
        searchEditText = findViewById(R.id.searchEditText);
        favoriteDishRecyclerView = findViewById(R.id.categoryRecyclerView); // Sử dụng lại RecyclerView cũ
        recentDishRecyclerView = findViewById(R.id.recentDishRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupUserInfo() {
        currentUserId = getIntent().getIntExtra("USER_ID", 0);
        String username = getIntent().getStringExtra("USERNAME");

        userNameTextView.setText("Tìm kiếm");

        // Load avatar từ database
        loadUserAvatarFromDatabase();

        profileImageView.setOnClickListener(v -> openProfile());
        notificationImageView.setOnClickListener(v -> openNotifications());
    }

    private void loadUserAvatar(String avatarUrl, ImageView imageView) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("/")) {
                    Bitmap originalBitmap = BitmapFactory.decodeFile(avatarUrl);
                    if (originalBitmap != null) {
                        // Tạo bitmap tròn
                        Bitmap circularBitmap = getCircularBitmap(originalBitmap);
                        imageView.setImageBitmap(circularBitmap);
                    } else {
                        // Sử dụng ảnh mặc định
                        imageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    // Thêm method tạo bitmap tròn
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Vẽ hình tròn
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Cắt ảnh theo hình tròn
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));

        // Tính toán để crop từ giữa ảnh
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        canvas.drawBitmap(bitmap, -x, -y, paint);

        return output;
    }
    private void loadUserAvatarFromDatabase() {
        UserDAO.getUserById(currentUserId, new UserDAO.UserCallback() {
            @Override
            public void onSuccess(User user) {
                loadUserAvatar(user.getAvatarUrl(), profileImageView);
            }

            @Override
            public void onError(String error) {
                Log.e("HomeActivity", "Error loading user avatar: " + error);
            }
        });
    }




    // Trong HomeActivity.java
    private void loadMostFavoriteDishes() {
        Log.d("HomeActivity", "Loading most favorite dishes...");

        DishDAO.getMostFavoriteDishes(6, new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                Log.d("HomeActivity", "Received " + dishes.size() + " favorite dishes");

                if (dishes.size() > 0) {
                    favoriteDishAdapter = new FavoriteDishAdapter(dishes, HomeActivity.this, currentUserId);
                    favoriteDishRecyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                    favoriteDishRecyclerView.setAdapter(favoriteDishAdapter);
                    Log.d("HomeActivity", "Adapter set successfully");
                } else {
                    Log.w("HomeActivity", "No favorite dishes found, loading all dishes as fallback");
                    loadAllDishesAsFallback();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("HomeActivity", "Error loading favorite dishes: " + error);
                Toast.makeText(HomeActivity.this, "Lỗi tải món yêu thích: " + error, Toast.LENGTH_SHORT).show();
                // Fallback: load all dishes
                loadAllDishesAsFallback();
            }
        });
    }

    private void loadAllDishesAsFallback() {
        Log.d("HomeActivity", "Loading all dishes as fallback");

        DishDAO.getAllDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                Log.d("HomeActivity", "Fallback: Loaded " + dishes.size() + " dishes");

                // Lấy 6 món đầu tiên
                List<Dish> firstSix = dishes.subList(0, Math.min(6, dishes.size()));

                favoriteDishAdapter = new FavoriteDishAdapter(firstSix, HomeActivity.this, currentUserId);
                favoriteDishRecyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                favoriteDishRecyclerView.setAdapter(favoriteDishAdapter);

                Toast.makeText(HomeActivity.this, "Hiển thị " + firstSix.size() + " món ăn", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e("HomeActivity", "Fallback also failed: " + error);
                Toast.makeText(HomeActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Thêm method fallback
    // Sửa method loadAllDishesAsFallback trong HomeActivity.java


    // Thêm method fallback


    private void loadRecentDishes() {
        DishDAO.getRecentDishes(new DishDAO.DishCallback() {
            @Override
            public void onSuccess(List<Dish> dishes) {
                List<RecentDish> recentDishes = new ArrayList<>();
                for (Dish dish : dishes) {
                    String timeAgo = calculateTimeAgo(dish.getCreatedAt());
                    // Truyền thêm imageUrl từ dish
                    recentDishes.add(new RecentDish(
                            dish.getName(),
                            timeAgo,
                            R.drawable.ic_dish_placeholder,
                            dish.getId(),
                            dish.getImageUrl() // Thêm imageUrl
                    ));
                }

                recentDishAdapter = new RecentDishAdapter(recentDishes, HomeActivity.this::onRecentDishClick);
                recentDishRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                recentDishRecyclerView.setAdapter(recentDishAdapter);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_favorites) {
                openFavorites();
                return true;
            }
            return false;
        });
    }

    private void setupViewAllClick() {
        TextView allDishesTextView = findViewById(R.id.allDishesTextView);
        allDishesTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllDishesActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });
    }

    private void onFavoriteDishClick(Dish dish) {
        Toast.makeText(this, "Món được yêu thích: " + dish.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Chuyển sang màn hình chi tiết món ăn
    }

    private void onRecentDishClick(RecentDish recentDish) {
        // Sử dụng dishId từ RecentDish để chuyển sang DishDetailActivity
        Intent intent = new Intent(HomeActivity.this, DishDetailActivity.class);
        intent.putExtra("DISH_ID", recentDish.getDishId());
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }


    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    private void openNotifications() {
        Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
    }

    private void openFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserAvatarFromDatabase(); // Refresh avatar mỗi khi quay lại HomeActivity
    }
    private String calculateTimeAgo(String createdAt) {
        try {
            // Giả sử createdAt có format: "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdDate = sdf.parse(createdAt);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - createdDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInDays == 0) {
                return "Hôm nay";
            } else if (diffInDays == 1) {
                return "Hôm qua";
            } else if (diffInDays < 7) {
                return "Cách đây " + diffInDays + " ngày";
            } else {
                return "Cách đây " + (diffInDays / 7) + " tuần";
            }
        } catch (Exception e) {
            return "Cách đây vài ngày";
        }
    }

}
