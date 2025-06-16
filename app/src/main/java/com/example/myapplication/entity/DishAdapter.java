package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {
    private List<Dish> dishes;
    private OnDishClickListener dishClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private boolean isFavoriteScreen = false; // Để phân biệt màn hình

    public interface OnDishClickListener {
        void onDishClick(Dish dish);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Dish dish);
    }

    public DishAdapter(List<Dish> dishes, OnDishClickListener dishClickListener,
                       OnFavoriteClickListener favoriteClickListener) {
        this.dishes = dishes;
        this.dishClickListener = dishClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    // Constructor với flag để biết đang ở màn hình nào
    public DishAdapter(List<Dish> dishes, OnDishClickListener dishClickListener,
                       OnFavoriteClickListener favoriteClickListener, boolean isFavoriteScreen) {
        this.dishes = dishes;
        this.dishClickListener = dishClickListener;
        this.favoriteClickListener = favoriteClickListener;
        this.isFavoriteScreen = isFavoriteScreen;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private TextView dishNameTextView, dishDescriptionTextView, difficultyTextView;
        private ImageView favoriteImageView;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            dishNameTextView = itemView.findViewById(R.id.dishNameTextView);
            dishDescriptionTextView = itemView.findViewById(R.id.dishDescriptionTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
        }

        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());
            dishDescriptionTextView.setText(dish.getDescription());
            difficultyTextView.setText(dish.getDifficultyLevel());

            // Thay đổi icon dựa trên màn hình
            if (isFavoriteScreen) {
                favoriteImageView.setImageResource(R.drawable.ic_bookmark_filled);
                favoriteImageView.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_light));
            } else {
                favoriteImageView.setImageResource(R.drawable.ic_bookmark_border);
                favoriteImageView.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            itemView.setOnClickListener(v -> {
                if (dishClickListener != null) {
                    dishClickListener.onDishClick(dish);
                }
            });

            favoriteImageView.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(dish);
                }
            });
        }
    }
}
