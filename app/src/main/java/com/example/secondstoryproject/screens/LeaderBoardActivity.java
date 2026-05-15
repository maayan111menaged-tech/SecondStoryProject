package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.UserAdapterLeaderBoard;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;

public class LeaderBoardActivity extends BaseActivity {

    private static final String TAG = "LeaderBoardActivity";
    private UserAdapterLeaderBoard userAdapterLeaderBoard;
    private TextView tvUserCount;

    // פודיום
    private ImageView ivPodium1, ivPodium2, ivPodium3;
    private TextView tvPodium1Name, tvPodium1Count;
    private TextView tvPodium2Name, tvPodium2Count;
    private TextView tvPodium3Name, tvPodium3Count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        tvUserCount = findViewById(R.id.tv_user_count);

        // פודיום
        ivPodium1 = findViewById(R.id.iv_podium_1);
        ivPodium2 = findViewById(R.id.iv_podium_2);
        ivPodium3 = findViewById(R.id.iv_podium_3);
        tvPodium1Name  = findViewById(R.id.tv_podium_1_name);
        tvPodium1Count = findViewById(R.id.tv_podium_1_count);
        tvPodium2Name  = findViewById(R.id.tv_podium_2_name);
        tvPodium2Count = findViewById(R.id.tv_podium_2_count);
        tvPodium3Name  = findViewById(R.id.tv_podium_3_name);
        tvPodium3Count = findViewById(R.id.tv_podium_3_count);

        RecyclerView usersList = findViewById(R.id.rv_users_list_leader_board);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        userAdapterLeaderBoard = new UserAdapterLeaderBoard(new UserAdapterLeaderBoard.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(LeaderBoardActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
            }
            @Override
            public void onLongUserClick(User user) {}
        });
        usersList.setAdapter(userAdapterLeaderBoard);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        // מיון לפי תרומות
                        List<User> sorted = new ArrayList<>(users);
                        sorted.sort((a, b) ->
                                Integer.compare(b.getDonationCounter(), a.getDonationCounter()));

                        runOnUiThread(() -> {
                            tvUserCount.setText(sorted.size() + " תורמים");
                            updatePodium(sorted);

                            // הרשימה מקבלת רק מקום 4 ומעלה
                            List<User> rest = sorted.size() > 3
                                    ? sorted.subList(3, sorted.size())
                                    : new ArrayList<>();
                            userAdapterLeaderBoard.setUserList(rest);
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get users list", e);
                    }
                });
    }

    private void updatePodium(List<User> sorted) {
        if (sorted.size() >= 1) {
            User u = sorted.get(0);
            tvPodium1Name.setText(u.getUserName());
            tvPodium1Count.setText(u.getDonationCounter() + " תרומות");
            if (u.getProfilePic() != null && !u.getProfilePic().isEmpty()) {
                ivPodium1.setImageBitmap(ImageUtil.fromBase64(u.getProfilePic()));
                ivPodium1.setColorFilter(null); // מסיר את ה-tint
            }
            findViewById(R.id.podium_item_1).setOnClickListener(v -> navigateToProfile(u));
        }
        if (sorted.size() >= 2) {
            User u = sorted.get(1);
            tvPodium2Name.setText(u.getUserName());
            tvPodium2Count.setText(u.getDonationCounter() + " תרומות");
            if (u.getProfilePic() != null && !u.getProfilePic().isEmpty()) {
                ivPodium2.setImageBitmap(ImageUtil.fromBase64(u.getProfilePic()));
                ivPodium2.setColorFilter(null);
            }
            findViewById(R.id.podium_item_2).setOnClickListener(v -> navigateToProfile(u));
        }
        if (sorted.size() >= 3) {
            User u = sorted.get(2);
            tvPodium3Name.setText(u.getUserName());
            tvPodium3Count.setText(u.getDonationCounter() + " תרומות");
            if (u.getProfilePic() != null && !u.getProfilePic().isEmpty()) {
                ivPodium3.setImageBitmap(ImageUtil.fromBase64(u.getProfilePic()));
                ivPodium3.setColorFilter(null);
            }
            findViewById(R.id.podium_item_3).setOnClickListener(v -> navigateToProfile(u));
        }
    }
    private void navigateToProfile(User user) {
        Intent intent = new Intent(LeaderBoardActivity.this, UserProfileActivity.class);
        intent.putExtra("USER_ID", user.getId());
        startActivity(intent);
    }
}