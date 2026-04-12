package com.example.secondstoryproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.utils.ImageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * RecyclerView Adapter for displaying a leaderboard of users.
 * Users are ranked based on their donation count (highest first).
 * Displays user ranking, name, level, donation count, and initials.
 */
public class UserAdapterLeaderBoard extends RecyclerView.Adapter<UserAdapterLeaderBoard.ViewHolder> {

    /**
     * Listener for user interactions.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
    }

    /** List of users displayed in the leaderboard */
    private final List<User> userList = new ArrayList<>();

    /** Click listener */
    private final OnUserClickListener listener;

    /**
     * Constructor.
     * @param listener listener for user interactions
     */
    public UserAdapterLeaderBoard(@Nullable OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_leader_board, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Display ranking position (starting from 1)
        holder.tvPlace.setText(String.valueOf(position + 1));

        // Display user full name
        holder.tvName.setText(user.getFullName());

        // Display user level and icon
        holder.tvLevel.setText(user.getLevel().getLabel());
        holder.ivLevelIcon.setImageResource(user.getLevel().getIconRes());

        // Display donation count
        holder.tvDonationCount.setText(user.getDonationCounter() + " תרומות");

        // Display user profile picture
        if (user.getProfilePhoneUrl() != null && !user.getProfilePhoneUrl().isEmpty()) {
            holder.ivProfilePic.setImageBitmap(ImageUtil.fromBase64(user.getProfilePhoneUrl()));
        }

        // Click events
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongUserClick(user);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets and sorts the user list for the leaderboard.
     * Users are sorted in descending order by donation count.
     * @param users list of users to display
     */
    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);

        // Sort users by donation count (highest first)
        Collections.sort(userList, (u1, u2) ->
                Integer.compare(u2.getDonationCounter(), u1.getDonationCounter())
        );

        notifyDataSetChanged();
    }

    /**
     * ViewHolder for leaderboard user item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlace, tvName, tvLevel, tvDonationCount;
        ImageView ivLevelIcon,ivProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlace = itemView.findViewById(R.id.tv_user_place);
            ivProfilePic = itemView.findViewById(R.id.tv_user_profile_pic);
            tvName = itemView.findViewById(R.id.tv_item_user_name);
            tvLevel = itemView.findViewById(R.id.tv_item_user_level);
            tvDonationCount = itemView.findViewById(R.id.tv_item_user_donation_counter);
            ivLevelIcon = itemView.findViewById(R.id.imageView2);
        }
    }
}
