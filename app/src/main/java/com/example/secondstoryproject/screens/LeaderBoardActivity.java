package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.UserAdapter;
import com.example.secondstoryproject.adapters.UserAdapterLeaderBoard;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;

import java.util.List;

public class LeaderBoardActivity extends BaseActivity {

    private static final String TAG = "LeaderBoardActivity";
    private UserAdapterLeaderBoard userAdapterLeaderBoard;
    private TextView tvUserCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);



        RecyclerView usersList = findViewById(R.id.rv_users_list_leader_board);
        tvUserCount = findViewById(R.id.tv_user_count);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapterLeaderBoard = new UserAdapterLeaderBoard(new UserAdapterLeaderBoard.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Handle user click
                Log.d(TAG, "User clicked: " + user);
                Intent intent = new Intent(LeaderBoardActivity.this, updateDetailsActivity.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {
                // Handle long user click
                Log.d(TAG, "User long clicked: " + user);
            }
        });
        usersList.setAdapter(userAdapterLeaderBoard);
    }


    @Override
    protected void onResume(                                                                                                                                                                                                ) {
        super.onResume();
        DatabaseService.getInstance().getUserService().getAll(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapterLeaderBoard.setUserList(users);
                tvUserCount.setText("Total users: " + users.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }

}