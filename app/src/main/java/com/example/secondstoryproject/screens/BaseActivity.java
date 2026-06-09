package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Chat;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

// Abstract base activity for the entire app — all screens extend this class.
// Cannot be instantiated directly;
// Implements NavigationView.OnNavigationItemSelectedListener to handle
// side drawer menu item clicks via onNavigationItemSelected().
public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected IDatabaseService databaseService;
    protected DrawerLayout drawerLayout;
    BottomNavigationView bottomNav;

    // list of listeners that track unread message count per chat
    private final List<com.google.firebase.database.ValueEventListener> unreadListeners = new ArrayList<>();
    // list of Firebase references (paths) for unread count per chat — kept to allow listener removal
    private final List<com.google.firebase.database.DatabaseReference> unreadRefs = new ArrayList<>();

    // Returns true if the current logged-in user is admin
    protected boolean isAdmin() {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) { return false; }
        return currentUser.isAdmin();
    }
    // Override to false for hiding the side drawer in this activity
    protected boolean hasSideMenu() {
        return true;
    }
    // Override to false for hiding the bottom navigation bar in this activity
    protected boolean hasBottomMenu(){
        return true;
    }
    // Override to return the R.id of the bottom nav item to mark as selected. Default: none (-1).
    protected int getSelectedBottomNavItem() {
        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseService = DatabaseService.getInstance();

        // inflates the base XML layout that wraps all activities
        super.setContentView(R.layout.activity_base);

        // registers the toolbar as the official action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // finds the drawer and sets this activity to listen to its menu clicks
        drawerLayout = findViewById(R.id.nav_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // adding to the drawer header the current user's name, username and profile picture
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderName);
        TextView navHeaderUsername = headerView.findViewById(R.id.navHeaderUsername);
        ShapeableImageView navHeaderAvatar = headerView.findViewById(R.id.navHeaderAvatar);

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null) {
            navHeaderName.setText(currentUser.getFullName());
            navHeaderUsername.setText("@" + currentUser.getUserName());

            String photo = currentUser.getProfilePic();
            if (photo != null && !photo.isEmpty()) {
                navHeaderAvatar.setImageBitmap(ImageUtil.fromBase64(photo));
            }
        }

        bottomNav = findViewById(R.id.bottom_nav);

        // loads the correct menu XML depending on whether the user is admin
        if (isAdmin()) {
            navigationView.inflateMenu(R.menu.nav_menu_admin);
        } else {
            navigationView.inflateMenu(R.menu.nav_menu);
        }

        // sets up the hamburger icon and toggle behavior for the side drawer
        if (hasSideMenu()) {

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setTitle("");
                Drawable menuIcon = ContextCompat.getDrawable(this, R.drawable.baseline_menu_24);
                menuIcon.setTint(ContextCompat.getColor(this, R.color.light_lavender));
                getSupportActionBar().setHomeAsUpIndicator(menuIcon);
            }

            // toggles the drawer open/closed on toolbar icon click
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });

        } else{
            // locks and hides the drawer for activities that don't need it
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            navigationView.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("");
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeAsUpIndicator(null);
            }
        }

        if(!hasBottomMenu()){
            bottomNav.setVisibility(View.GONE);
        }
        else {
            // navigates to the correct screen based on which bottom nav item was clicked
            bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.menu_chat) {
                        navigateTo(ChatsListActivity.class);
                    }
                    if (item.getItemId() == R.id.menu_home) {
                        if(isAdmin())   navigateTo(AdminMainActivity.class);
                        else navigateTo(MainActivity.class);
                    }
                    if (item.getItemId() == R.id.menu_profile) {
                        navigateTo(UserProfileActivity.class);

                    }
                    return true;
                }
            });

            // marks the correct item as selected, or clears selection if none specified
            int selectedItem = getSelectedBottomNavItem();
            if (selectedItem != -1) {
                bottomNav.setSelectedItemId(selectedItem);
            }else{
                for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                    bottomNav.getMenu().getItem(i).setChecked(false);
                }
            }
        }

        if (hasBottomMenu()) {
            listenToTotalUnread();
        }
    }

    // overrides setContentView so child activities inject their layout into the base frame
    @Override
    public void setContentView(int layoutResID) {
        setContentLayout(layoutResID);
    }

    // inflates the child activity's layout into the FrameLayout inside activity_base.xml
    protected void setContentLayout(int layoutResId) {
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResId, contentFrame, true);
    }

    // navigates to the target activity and finishes the current one to avoid stacking screens
    // is used in the sideMenu navigation function
    protected void navigateTo(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            startActivity(intent);
            finish();
        } else if (targetActivity.equals(UserProfileActivity.class)) {
            // allows re-opening the profile screen even if already on it
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    // handles clicks on side drawer menu items and navigates accordingly
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            navigateTo(MainActivity.class);
        } else if(id == R.id.nav_home_admin){
            navigateTo(AdminMainActivity.class);
        } else if (id == R.id.nav_user_profile) {
            navigateTo(UserProfileActivity.class);
        } else if(id == R.id.nav_admin_profile){
            navigateTo(UserProfileActivity.class);
        } else if (id == R.id.nav_add_donation) {
            navigateTo(PickCatergoryActivity.class);
        } else if(id == R.id.nav_accept_donation){
            navigateTo(AcceptDonationActivity.class);
        } else if (id == R.id.nav_search_donation) {
            navigateTo(SearchDonationsActivity.class);
        } else if (id == R.id.nav_donation_list) {
            navigateTo(DonationsListActivity.class);
        } else if (id == R.id.nav_leaders_board) {
            navigateTo(LeaderBoardActivity.class);
        } else if(id == R.id.nav_users_list){
            navigateTo(UsersListActivity.class);
        } else if (id == R.id.nav_signOut) {
            drawerLayout.closeDrawer(GravityCompat.START);
            showLogoutDialog();
        }
        return true;
    }

    // shows a confirmation dialog before signing the user out
    private void showLogoutDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this , R.style.DialogTheme)
                .setTitle("התנתקות")
                .setMessage("את/ה בטוח/ה שאת/ה רוצה להתנתק?")
                .setPositiveButton("כן", (dialog, which) -> {
                    SharedPreferencesUtil.signOutUser(this);
                    // clears the back stack so the user cannot navigate back after logout
                    Intent intent = new Intent(this, LandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("לא", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // closes the drawer on back press if open, otherwise performs default back behavior
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // attaches a real-time Firebase listener per chat to track total unread message count
    private void listenToTotalUnread() {
        // detaches any existing listeners before re-attaching to avoid duplicates
        for (int i = 0; i < unreadListeners.size(); i++) {
            unreadRefs.get(i).removeEventListener(unreadListeners.get(i));
        }
        unreadListeners.clear();
        unreadRefs.clear();

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) return;

        com.google.firebase.database.DatabaseReference chatsRef =
                databaseService.getChatService().getChatsRef();

        if (!isAdmin()) {
            databaseService.getChatService()
                    .getUserChats(currentUser.getId(),
                            new IDatabaseService.DatabaseCallback<List<Chat>>() {
                                @Override
                                public void onCompleted(List<Chat> chats) {
                                    if (chats.isEmpty()) return;
                                    final int[] total = {0};
                                    final int[] count = {0};
                                    String userId = currentUser.getId();

                                    for (Chat chat : chats) {
                                        com.google.firebase.database.DatabaseReference ref =
                                                chatsRef.child(chat.getId())
                                                        .child("metadata")
                                                        .child("unread_" + userId);

                                        com.google.firebase.database.ValueEventListener listener =
                                                new com.google.firebase.database.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                                                        Integer unread = snapshot.getValue(Integer.class);
                                                        total[0] += (unread != null ? unread : 0);
                                                        count[0]++;
                                                        // once all chats were counted, update the badge and reset counters
                                                        if (count[0] == chats.size()) {
                                                            updateChatBadge(total[0]);
                                                            total[0] = 0;
                                                            count[0] = 0;
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(com.google.firebase.database.DatabaseError error) {}
                                                };

                                        ref.addValueEventListener(listener);
                                        unreadListeners.add(listener);
                                        unreadRefs.add(ref);
                                    }
                                }
                                @Override
                                public void onFailed(Exception e) {}
                            });
        } else {
            // admin listens to unread_admin field across all chats
            databaseService.getChatService()
                    .getAllAdminChats(new IDatabaseService.DatabaseCallback<List<Chat>>() {
                        @Override
                        public void onCompleted(List<Chat> chats) {
                            if (chats.isEmpty()) return;
                            final int[] total = {0};
                            final int[] count = {0};

                            for (Chat chat : chats) {
                                com.google.firebase.database.DatabaseReference ref =
                                        chatsRef.child(chat.getId())
                                                .child("metadata")
                                                .child("unread_admin");

                                com.google.firebase.database.ValueEventListener listener =
                                        new com.google.firebase.database.ValueEventListener() {
                                            @Override
                                            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                                                Integer unread = snapshot.getValue(Integer.class);
                                                total[0] += (unread != null ? unread : 0);
                                                count[0]++;
                                                // once all chats were counted, update the badge and reset counters
                                                if (count[0] == chats.size()) {
                                                    updateChatBadge(total[0]);
                                                    total[0] = 0;
                                                    count[0] = 0;
                                                }
                                            }
                                            @Override
                                            public void onCancelled(com.google.firebase.database.DatabaseError error) {}
                                        };

                                ref.addValueEventListener(listener);
                                unreadListeners.add(listener);
                                unreadRefs.add(ref);
                            }
                        }
                        @Override
                        public void onFailed(Exception e) {}
                    });
        }
    }

    // detaches all Firebase listeners when the activity is paused to prevent memory leaks
    @Override
    protected void onPause() {
        super.onPause();
        for (int i = 0; i < unreadListeners.size(); i++) {
            unreadRefs.get(i).removeEventListener(unreadListeners.get(i));
        }
        unreadListeners.clear();
        unreadRefs.clear();
    }

    // updates the chat badge on the bottom nav with the total unread count
    private void updateChatBadge(int count) {
        runOnUiThread(() -> {
            com.google.android.material.badge.BadgeDrawable badge =
                    bottomNav.getOrCreateBadge(R.id.menu_chat);
            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });
    }

    // sends an automated message from admin to the given user's chat
    protected void sendAutoAdminMessage(String userId, String text) {
        databaseService.getChatService()
                .getOrCreateAdminChat(userId, new IDatabaseService.DatabaseCallback<String>() {
                    @Override
                    public void onCompleted(String chatId) {
                        databaseService.getChatService()
                                .sendMessage(chatId, "admin", text, true, new IDatabaseService.DatabaseCallback<Void>() {
                                    @Override
                                    public void onCompleted(Void unused) {}
                                    @Override
                                    public void onFailed(Exception e) {}
                                });
                    }
                    @Override
                    public void onFailed(Exception e) {}
                });
    }

    // re-attaches unread listeners when returning to the activity
    @Override
    protected void onResume() {
        super.onResume();
        if (hasBottomMenu()) {
            listenToTotalUnread();
        }
    }
}
