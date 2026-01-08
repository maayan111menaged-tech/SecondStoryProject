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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserAdapterLeaderBoard extends RecyclerView.Adapter<UserAdapterLeaderBoard.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
    }

    private final List<User> userList = new ArrayList<>();
    private final OnUserClickListener listener;

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

        // מקום בדירוג (מתחיל מ־1)
        holder.tvPlace.setText(String.valueOf(position + 1));

        // שם מלא
        holder.tvName.setText(user.getFullName());

        // רמה
        holder.tvLevel.setText(user.getLevel().getLabel());
        holder.ivLevelIcon.setImageResource(user.getLevel().getIconRes());

        // מספר תרומות
        holder.tvDonationCount.setText(user.getDonationCounter() + " תרומות");

        // ראשי תיבות
        holder.tvInitials.setText(getInitials(user));


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


    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);

        // מיון: הכי הרבה תרומות למעלה
        Collections.sort(userList, (u1, u2) ->
                Integer.compare(u2.getDonationCounter(), u1.getDonationCounter())
        );

        notifyDataSetChanged();
    }

    private String getInitials(User user) {
        String initials = "";
        if (user.getfName() != null && !user.getfName().isEmpty()) {
            initials += user.getfName().charAt(0);
        }
        if (user.getlName() != null && !user.getlName().isEmpty()) {
            initials += user.getlName().charAt(0);
        }
        return initials.toUpperCase();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlace, tvInitials, tvName, tvLevel, tvDonationCount;
        ImageView ivLevelIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlace = itemView.findViewById(R.id.tv_user_place);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            tvName = itemView.findViewById(R.id.tv_item_user_name);
            tvLevel = itemView.findViewById(R.id.tv_item_user_level);
            tvDonationCount = itemView.findViewById(R.id.tv_item_user_donation_counter);
            ivLevelIcon = itemView.findViewById(R.id.imageView2);
        }
    }
}
