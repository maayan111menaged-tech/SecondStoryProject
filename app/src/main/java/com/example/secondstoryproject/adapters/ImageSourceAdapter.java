package com.example.secondstoryproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.ImageSourceOption;

import java.util.List;

/**
 * Adapter for displaying image source options in a dialog.
 * Each item includes:
 * - Icon
 * - Title
 * - Description
 * Handles user selection via a callback listener.
 */
public class ImageSourceAdapter extends ArrayAdapter<ImageSourceOption> {

    /**
     * Listener for image source selection events.
     */
    public interface OnImageSourceSelectedListener {
        void onImageSourceSelected(ImageSourceOption option);
    }

    /** Layout inflater for creating item views */
    private final LayoutInflater inflater;

    /** List of image source options */
    private final List<ImageSourceOption> objects;

    /** Selection listener */
    private OnImageSourceSelectedListener listener;

    /**
     * Constructor.
     * @param context the application context
     * @param objects list of image source options
     * @param listener callback for item selection
     */
    public ImageSourceAdapter(@NonNull Context context, @NonNull List<ImageSourceOption> objects,
                              @NonNull OnImageSourceSelectedListener listener) {
        super(context, R.layout.row_image_source, objects);
        this.inflater = LayoutInflater.from(context);
        this.objects = objects;
        this.listener = listener;
    }

    /**
     * Returns the number of items in the adapter.
     */
    @Override
    public int getCount() {
        return objects.size();
    }

    /**
     * Returns the item at the specified position.
     */
    @Nullable
    @Override
    public ImageSourceOption getItem(int position) {
        return objects.get(position);
    }

    /**
     * Creates or reuses a view for displaying an item.
     * @param position item position
     * @param convertView recycled view (if available)
     * @param parent parent view group
     * @return configured item view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.row_image_source, parent, false);
        }

        // Get UI elements
        ImageView icon = convertView.findViewById(R.id.icon_dialog_item);
        TextView title = convertView.findViewById(R.id.text_dialog_item);
        TextView description = convertView.findViewById(R.id.text_dialog_item_description);

        // Get current item
        ImageSourceOption item = getItem(position);

        if (item != null) {
            // Bind data to views
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            icon.setImageResource(item.getIconResource());
        }

        // Handle item click
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageSourceSelected(item);
            }
        });

        return convertView;
    }
}