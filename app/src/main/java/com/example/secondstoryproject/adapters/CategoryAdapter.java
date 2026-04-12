package com.example.secondstoryproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.DonationCategory;

/**
 * RecyclerView Adapter for displaying donation categories.
 * Each item includes:
 * - Category name
 * - Category icon
 * Clicking an item triggers a category selection event.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    /** Array of all available donation categories */
    private final DonationCategory[] categories;

    /** Click listener for category selection */
    private final OnCategoryClickListener listener;

    /**
     * Listener for category click events.
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(DonationCategory category);
    }

    /**
     * Constructor.
     * Initializes the adapter with all enum values.
     * @param listener callback for category clicks
     */
    public CategoryAdapter(OnCategoryClickListener listener) {
        this.categories = DonationCategory.values();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        DonationCategory category = categories[position];

        // Set category name and icon
        holder.tvName.setText(category.getHebrewName());
        holder.imgIcon.setImageResource(category.getIconResId());

        // Handle click
        holder.itemView.setOnClickListener(v ->
                listener.onCategoryClick(category)
        );
    }

    @Override
    public int getItemCount() {
        return categories.length;
    }

    /**
     * ViewHolder for category item.
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}

