package com.example.myapplication.entity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.entity.Dish;
import java.util.List;

public class FavoriteDishAdapter extends RecyclerView.Adapter<FavoriteDishAdapter.FavoriteDishViewHolder> {
    private List<Dish> favoriteDishes;
    private OnFavoriteDishClickListener listener;

    public interface OnFavoriteDishClickListener {
        void onFavoriteDishClick(Dish dish);
    }

    public FavoriteDishAdapter(List<Dish> favoriteDishes, OnFavoriteDishClickListener listener) {
        this.favoriteDishes = favoriteDishes;
        this.listener = listener;
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

        public void bind(Dish dish) {
            dishNameTextView.setText(dish.getName());
            dishDescriptionTextView.setText(dish.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteDishClick(dish);
                }
            });
        }
    }
}
