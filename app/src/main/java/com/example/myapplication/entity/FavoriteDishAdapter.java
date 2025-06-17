package com.example.myapplication.entity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.DishDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class FavoriteDishAdapter extends RecyclerView.Adapter<FavoriteDishAdapter.FavoriteDishViewHolder> {
    private List<Dish> favoriteDishes;
    private Context context;
    private int currentUserId;

    public FavoriteDishAdapter(List<Dish> favoriteDishes, Context context, int currentUserId) {
        this.favoriteDishes = favoriteDishes;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public FavoriteDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_dish, parent, false);
        return new FavoriteDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteDishViewHolder holder, int position) {
        Dish dish = favoriteDishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return favoriteDishes.size();
    }

    class FavoriteDishViewHolder extends RecyclerView.ViewHolder {
        private TextView dishNameTextView, dishDescriptionTextView;

        public FavoriteDishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
            dishDescriptionTextView = itemView.findViewById(R.id.dishDescriptionTextView);
        }

        // Trong FavoriteDishAdapter.java
        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());
            dishDescriptionTextView.setText(dish.getDescription());

            itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(context, DishDetailActivity.class);
                    intent.putExtra("DISH_ID", dish.getId());
                    intent.putExtra("USER_ID", currentUserId);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("FavoriteDishAdapter", "Error opening DishDetailActivity: " + e.getMessage());
                    Toast.makeText(context, "Lỗi mở chi tiết món ăn", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
