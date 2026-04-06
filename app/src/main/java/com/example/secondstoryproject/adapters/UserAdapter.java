package com.example.secondstoryproject.adapters;
import static com.example.secondstoryproject.utils.SharedPreferencesUtil.getUserId;

import android.graphics.Bitmap;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>  {


    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
        void onMakeAdminClick(User user);
    }
    public interface OnFilterListener {
        void onFilterResult(int count);
    }

    private List<User> fullUserList = new ArrayList<>(); // הרשימה המלאה תמיד
    private String currentUserId = "";

    private final List<User> userList;
    private final OnUserClickListener onUserClickListener;
    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    private OnFilterListener onFilterListener;

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
        User user = userList.get(position);
        if (user == null) return;

        String currentUserId = SharedPreferencesUtil.getUserId(holder.itemView.getContext());
        if (user.getId().equals(currentUserId)) {
            holder.tvMeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvMeBadge.setVisibility(View.GONE);
        }

        holder.tvUserName.setText(user.getUserName());
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhoneNumber());

        String base64 = user.getProfilePhoneUrl();

        if (base64 != null && !base64.isEmpty()) {
            Bitmap bitmap = ImageUtil.fromBase64(base64);
            holder.ivProfilePic.setImageBitmap(bitmap);
        }

        // if user admin change the MakeAdmin button
        if (user.isAdmin()) {
            holder.btnMakeAdmin.setEnabled(false);
            holder.btnMakeAdmin.setText("Already Admin");
        } else {
            holder.btnMakeAdmin.setEnabled(true);
            holder.btnMakeAdmin.setText("Make Admin");
        }


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

            holder.btnMakeAdmin.setEnabled(false);
            holder.btnMakeAdmin.setText("Loading...");

            if (onUserClickListener != null) {
                onUserClickListener.onMakeAdminClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> users, String currentUserId) {
        this.currentUserId = currentUserId;

        fullUserList.clear();
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

    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }
    public void updateUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }
    public void updateUserById(User updatedUser) {
        // עדכון ב-fullUserList
        for (int i = 0; i < fullUserList.size(); i++) {
            if (fullUserList.get(i).getId().equals(updatedUser.getId())) {
                fullUserList.set(i, updatedUser);
                break;
            }
        }
        // עדכון ב-userList (המסוננת)
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(updatedUser.getId())) {
                userList.set(i, updatedUser);
                notifyItemChanged(i);
                return;
            }
        }
    }
    public void resetMakeAdminButton(User user) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {

                userList.get(i).setAdmin(false);
                notifyItemChanged(i);
                return;
            }
        }
    }
    public void removeUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }
    public void setLoadingState(Button button, boolean isLoading) {
        if (isLoading) {
            button.setEnabled(false);
            button.setText("Loading...");
        } else {
            button.setEnabled(true);
            button.setText("Make Admin");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName,tvName, tvEmail, tvPhone, tvMeBadge;
        ImageView ivProfilePic;
        Button btnMakeAdmin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_item_user_name);
            tvName = itemView.findViewById(R.id.tv_item_user_fullname);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            tvPhone = itemView.findViewById(R.id.tv_item_user_phone);
            ivProfilePic = itemView.findViewById(R.id.iv_user_profile_pic);
            btnMakeAdmin = itemView.findViewById(R.id.btn_make_admin);

            tvMeBadge = itemView.findViewById(R.id.tv_me_badge);

        }
    }
}
