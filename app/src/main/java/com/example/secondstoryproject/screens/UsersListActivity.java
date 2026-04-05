package com.example.secondstoryproject.screens;

import static com.example.secondstoryproject.utils.SharedPreferencesUtil.getUserId;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.UserAdapter;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;

import java.util.List;

public class UsersListActivity extends BaseActivity {

    private static final String TAG = "UsersListActivity";
    private UserAdapter userAdapter;
    private TextView tvUserCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);


        RecyclerView usersList = findViewById(R.id.rv_users_list);
        tvUserCount = findViewById(R.id.tv_user_count);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Handle user click
                Log.d(TAG, "User clicked: " + user);
                Intent intent = new Intent(UsersListActivity.this, updateDetailsActivity.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {
                // Handle long user click
                Log.d(TAG, "User long clicked: " + user);
            }
            @Override
            public void onMakeAdminClick(User user) {

                String name = user.getUserName();

                new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                        .setTitle("Make Admin")
                        .setMessage("האם תרצה להפוך את " + name + " לאדמין?")
                        .setPositiveButton("כן", (dialog, which) -> {

                            // 👇 כאן "Loading" לוגי (לא UI ישיר)
                            user.setAdmin(true);

                            DatabaseService.getInstance().getUserService().update(
                                    user.getId(),
                                    oldUser -> user,
                                    new IDatabaseService.DatabaseCallback<User>() {

                                        @Override
                                        public void onCompleted(User result) {

                                            // ✔ זה מה שמתקן את הכפתור!
                                            userAdapter.updateUserById(result);

                                            Toast.makeText(UsersListActivity.this,
                                                    "המשתמש הפך לאדמין",
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailed(Exception e) {
                                            Toast.makeText(UsersListActivity.this,
                                                    "שגיאה בעדכון המשתמש",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        })
                        .setNegativeButton("לא", (dialog, which) ->{
                            userAdapter.resetMakeAdminButton(user);
                            dialog.dismiss();
                                })
                        .show();
            }
        });
        usersList.setAdapter(userAdapter);
    }


    @Override
    protected void onResume(                                                                                                                                                                                                ) {
        super.onResume();
        DatabaseService.getInstance().getUserService().getAll(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
                tvUserCount.setText("Total users: " + users.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }

}