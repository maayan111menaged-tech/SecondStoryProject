package com.example.secondstoryproject.adapters;
import static com.example.secondstoryproject.utils.SharedPreferencesUtil.getUserId;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying and managing a list of users.
 * Supports:
 * - Click and long-click actions
 * - Admin promotion actions
 * - Filtering (by name and admin status)
 * - Dynamic updates (add, update, remove)
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>  {

    /**
     * Listener for user item interactions.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
        void onMakeAdminClick(User user);
        void onToggleActiveClick(User user);
        void onInfoClick(User user);
        void onChatClick(User user);
    }
    /**
     * Listener for filter results.
     */
    public interface OnFilterListener {
        void onFilterResult(int count);
    }

    /** Full list of users (unfiltered) */
    private List<User> fullUserList = new ArrayList<>();

    /** ID of the currently logged-in user */
    private String currentUserId = "";

    /** Displayed (filtered) user list */
    private final List<User> userList;

    /** Click listener */
    private final OnUserClickListener onUserClickListener;

    /** Filter result listener */
    private OnFilterListener onFilterListener;

    /**
     * Constructor.
     * @param onUserClickListener listener for user interactions
     */
    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    /**
     * Sets the filter listener.
     *
     * @param listener the listener to receive filter results
     */
    public void setOnFilterListener(OnFilterListener listener) {
        this.onFilterListener = listener;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentUserId = SharedPreferencesUtil.getUserId(holder.itemView.getContext());

        User user = userList.get(position);
        if (user == null) return;

        // טיפול בפעיל לא פעיל
        if (!user.isActive()) {
            holder.itemView.setAlpha(0.7f);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("⛔ לא פעיל");
            holder.tvStatus.setTextColor(Color.parseColor("#E53935"));

            holder.btnToggleActive.setText("+");
            holder.btnToggleActive.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#43A047")));
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvStatus.setVisibility(View.GONE);

            holder.btnToggleActive.setText("-");
            holder.btnToggleActive.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
        }

        // טיפול בהשבתה עצמית או השבתת אדמין אחר
        if(user.getId().equals(currentUserId) || user.isAdmin()){
            holder.btnToggleActive.setAlpha(0.5f);
            holder.btnToggleActive.setEnabled(false);

        }

        // Show "Me" badge if this is the logged-in user
        if (user.getId().equals(currentUserId)) {
            holder.tvMeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvMeBadge.setVisibility(View.GONE);
        }

        // Bind user data
        holder.tvUserName.setText(user.getUserName());
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhoneNumber());

        // Load profile image from Base64
        String base64 = user.getProfilePhoneUrl();
        if (base64 != null && !base64.isEmpty()) {
            Bitmap bitmap = ImageUtil.fromBase64(base64);
            holder.ivProfilePic.setImageBitmap(bitmap);
        }

        // כפתור Make Admin – disabled אם כבר אדמין או אם לא פעיל
        if (user.isAdmin()) {
            holder.btnMakeAdmin.setEnabled(false);
            holder.btnMakeAdmin.setText("Already Admin");
            holder.btnMakeAdmin.setAlpha(0.5f);
        } else if (!user.isActive()) {
            holder.btnMakeAdmin.setEnabled(false);
            holder.btnMakeAdmin.setText("Make Admin");
            holder.btnMakeAdmin.setAlpha(0.5f);
        } else {
            holder.btnMakeAdmin.setEnabled(true);
            holder.btnMakeAdmin.setText("Make Admin");
            holder.btnMakeAdmin.setAlpha(1.0f);
        }

        // Click events
        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongUserClick(user);
            }
            return true;
        });
        holder.btnMakeAdmin.setOnClickListener(v -> {
            // Set temporary loading state
            holder.btnMakeAdmin.setEnabled(false);
            holder.btnMakeAdmin.setText("Loading...");

            if (onUserClickListener != null) {
                onUserClickListener.onMakeAdminClick(user);
            }
        });
        holder.btnToggleActive.setOnClickListener(v -> {
            if (onUserClickListener != null) onUserClickListener.onToggleActiveClick(user);
        });

        // ✅ כפתור INFO
        holder.btnInfoUser.setOnClickListener(v -> {
            if (onUserClickListener != null) onUserClickListener.onInfoClick(user);
        });
        holder.btnChatUser.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onChatClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets the user list and sorts it so the current user appears first.
     * @param users list of users
     * @param currentUserId ID of the logged-in user
     */
    public void setUserList(List<User> users, String currentUserId) {
        this.currentUserId = currentUserId;
        fullUserList.clear();

        // Sort: current user appears first
        List<User> sorted = new ArrayList<>(users);
        sorted.sort((a, b) -> {
            boolean aIsMe = a.getId().equals(currentUserId);
            boolean bIsMe = b.getId().equals(currentUserId);
            return Boolean.compare(!aIsMe, !bIsMe);
        });
        fullUserList.addAll(sorted);

        userList.clear();
        userList.addAll(fullUserList);
        notifyDataSetChanged();
    }

    /**
     * Filters users based on search query and admin status.
     * @param query search text (username)
     * @param adminFilter true = only admins, false = only non-admins, null = all
     */
    public void filter(String query, Boolean adminFilter) {
        userList.clear();
        for (User user : fullUserList) {
            boolean matchesSearch = query == null || query.isEmpty()
                    || user.getUserName().toLowerCase().contains(query.toLowerCase());
            boolean matchesAdmin = adminFilter == null
                    || user.isAdmin() == adminFilter;
            if (matchesSearch && matchesAdmin) {
                userList.add(user);
            }
        }
        notifyDataSetChanged();

        if (onFilterListener != null) {
            onFilterListener.onFilterResult(userList.size());
        }
    }

    /**
     * Adds a new user to the list.
     */
    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    /**
     * Updates an existing user (by object reference).
     */
    public void updateUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }

    /**
     * Updates a user by ID in both full and filtered lists.
     */
    public void updateUserById(User updatedUser) {
        // Update full list
        for (int i = 0; i < fullUserList.size(); i++) {
            if (fullUserList.get(i).getId().equals(updatedUser.getId())) {
                fullUserList.set(i, updatedUser);
                break;
            }
        }
        // Update visible list
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(updatedUser.getId())) {
                userList.set(i, updatedUser);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * Resets the "Make Admin" button state for a user.
     */
    public void resetMakeAdminButton(User user) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {

                userList.get(i).setAdmin(false);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * Removes a user from the list.
     */
    public void removeUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }

    /**
     * Sets loading state for a button.
     */
    public void setLoadingState(Button button, boolean isLoading) {
        if (isLoading) {
            button.setEnabled(false);
            button.setText("Loading...");
        } else {
            button.setEnabled(true);
            button.setText("Make Admin");
        }
    }

    /**
     * ViewHolder for user item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvName, tvEmail, tvPhone, tvMeBadge , tvStatus;
        ImageView ivProfilePic;
        Button btnMakeAdmin, btnToggleActive, btnInfoUser, btnChatUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_item_user_name);
            tvName = itemView.findViewById(R.id.tv_item_user_fullname);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            tvPhone = itemView.findViewById(R.id.tv_item_user_phone);
            ivProfilePic = itemView.findViewById(R.id.iv_user_profile_pic);
            btnMakeAdmin = itemView.findViewById(R.id.btn_make_admin);
            btnToggleActive = itemView.findViewById(R.id.btn_delete_user);
            btnInfoUser   = itemView.findViewById(R.id.btn_Info_user);
            btnChatUser = itemView.findViewById(R.id.btn_chat_user);
            tvMeBadge = itemView.findViewById(R.id.tv_me_badge);
            tvStatus      = itemView.findViewById(R.id.tv_user_status);
        }
    }
}
