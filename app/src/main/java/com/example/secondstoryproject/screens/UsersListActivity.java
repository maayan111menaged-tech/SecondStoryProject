package com.example.secondstoryproject.screens;

import static com.example.secondstoryproject.utils.SharedPreferencesUtil.getUserId;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.UserAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.secondstoryproject.utils.Validator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

// Admin user management screen — lists all users with search by username and admin/non-admin filter.
// Supports adding new users, promoting to admin, deactivating, reactivating, and permanent deletion.
public class UsersListActivity extends BaseActivity {

    private static final String TAG = "UsersListActivity";
    private UserAdapter userAdapter;
    private TextView tvUserCount;

    private String searchQuery = "";
    // null = show all, true = admins only, false = non-admins only
    private Boolean adminFilter = null;

    private LinearLayout layoutEmpty;
    private RecyclerView usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        layoutEmpty = findViewById(R.id.layout_empty);
        usersList = findViewById(R.id.rv_users_list);
        tvUserCount = findViewById(R.id.tv_user_count);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            // opens the user's profile screen
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {}

            // info icon also opens the profile screen
            @Override
            public void onInfoClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, UserProfileActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
            }

            // promotes a user to admin after confirming they have no active donations
            @Override
            public void onMakeAdminClick(User user) {
                new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this, R.style.DialogTheme)
                        .setTitle("הפיכה לאדמין")
                        .setMessage("האם תרצה להפוך את " + user.getUserName() + " לאדמין?")
                        .setPositiveButton("כן", (dialog, which) -> {
                            databaseService.getDonationService()
                                    .getByGiverId(user.getId(),
                                            new IDatabaseService.DatabaseCallback<List<Donation>>() {
                                                @Override
                                                public void onCompleted(List<Donation> donations) {
                                                    // blocks promotion if the user has pending or available donations
                                                    boolean hasActiveDonations = false;
                                                    for (Donation d : donations) {
                                                        DonationStatus s = d.getStatus();
                                                        if (s == DonationStatus.PENDING_APPROVAL ||
                                                                s == DonationStatus.APPROVED_AVAILABLE) {
                                                            hasActiveDonations = true;
                                                            break;
                                                        }
                                                    }
                                                    if (hasActiveDonations) {
                                                        new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this, R.style.DialogTheme)
                                                                .setTitle("לא ניתן להפוך לאדמין")
                                                                .setMessage("למשתמש יש תרומות פעילות.\nיש לסגור אותן לפני הפיכתו לאדמין.")
                                                                .setPositiveButton("הבנתי", null)
                                                                .show();
                                                        userAdapter.resetMakeAdminButton(user);
                                                        return;
                                                    }
                                                    // updates the admin field in the DB and deletes the user's admin chat
                                                    databaseService.getUserService()
                                                            .updateUserFields(user.getId(), Map.of("admin", true),
                                                                    new IDatabaseService.DatabaseCallback<Void>() {
                                                                        @Override
                                                                        public void onCompleted(Void unused) {
                                                                            user.setAdmin(true);
                                                                            userAdapter.updateUserById(user);
                                                                            databaseService.getChatService()
                                                                                    .deleteAdminChat(user.getId(),
                                                                                            new IDatabaseService.DatabaseCallback<Void>() {
                                                                                                @Override public void onCompleted(Void u) {}
                                                                                                @Override public void onFailed(Exception e) {
                                                                                                    Log.e(TAG, "שגיאה במחיקת צאט", e);
                                                                                                }
                                                                                            });
                                                                            Toast.makeText(UsersListActivity.this,
                                                                                    "המשתמש הפך לאדמין", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        @Override
                                                                        public void onFailed(Exception e) {
                                                                            Toast.makeText(UsersListActivity.this,
                                                                                    "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                }
                                                @Override
                                                public void onFailed(Exception e) {
                                                    Toast.makeText(UsersListActivity.this,
                                                            "שגיאה בבדיקת תרומות", Toast.LENGTH_SHORT).show();
                                                    userAdapter.resetMakeAdminButton(user);
                                                }
                                            });
                        })
                        .setNegativeButton("לא", (dialog, which) -> {
                            userAdapter.resetMakeAdminButton(user);
                            dialog.dismiss();
                        })
                        .show();
            }
            // deactivated users get a reactivate dialog; active users get a deactivate/delete choice
            @Override
            public void onToggleActiveClick(User user) {
                if (!user.isActive()) {
                    new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this, R.style.DialogTheme)
                            .setTitle("הפעל משתמש")
                            .setMessage("האם להפעיל מחדש את " + user.getUserName() + "?")
                            .setPositiveButton("הפעל", (d, w) -> reactivateUser(user))
                            .setNegativeButton("ביטול", null)
                            .show();
                    return;
                }

                new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this, R.style.DialogTheme)
                        .setTitle("פעולה על משתמש")
                        .setMessage("מה ברצונך לעשות עם " + user.getUserName() + "?")
                        .setPositiveButton("⛔ השבת", (dialog, which) -> showDeactivateConfirmDialog(user))
                        .setNeutralButton("🗑 מחק לצמיתות", (dialog, which) -> showDeleteConfirmDialog(user))
                        .setNegativeButton("ביטול", null)
                        .show();
            }

            // opens an admin chat with the selected user, skips if the user is the admin themselves
            @Override
            public void onChatClick(User user) {
                String currentUserId = SharedPreferencesUtil.getUserId(UsersListActivity.this);
                if (user.getId().equals(currentUserId)) return;


                Intent intent = new Intent(UsersListActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_ID", "admin_" + user.getId());
                intent.putExtra("OTHER_USER_NAME", user.getUserName());
                intent.putExtra("OTHER_USER_ID", user.getId());
                startActivity(intent);
            }
        });

        usersList.setAdapter(userAdapter);

        // FAB opens the add-user dialog
        findViewById(R.id.fab_add_user).setOnClickListener(v -> showAddUserDialog());

        // search filter — updates the adapter on every keystroke
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                userAdapter.filter(searchQuery, adminFilter);
            }
        });

        // chip group toggles between showing all users, admins only, or non-admins only
        ChipGroup chipGroup = findViewById(R.id.chip_group_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_admin)) adminFilter = true;
            else if (checkedIds.contains(R.id.chip_not_admin)) adminFilter = false;
            else adminFilter = null;
            userAdapter.filter(searchQuery, adminFilter);
        });

        userAdapter.setOnFilterListener(count ->
                tvUserCount.setText("סה״כ: " + count));
    }

    // inflates and shows the add-user dialog with full validation and username uniqueness check
    private void showAddUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);

        TextInputEditText etUsername  = dialogView.findViewById(R.id.et_username);
        TextInputEditText etFirstname = dialogView.findViewById(R.id.et_firstname);
        TextInputEditText etLastname  = dialogView.findViewById(R.id.et_lastname);
        TextInputEditText etEmail     = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone     = dialogView.findViewById(R.id.et_phone);
        TextInputEditText etDate      = dialogView.findViewById(R.id.et_date);
        TextInputEditText etPassword  = dialogView.findViewById(R.id.et_password);

        TextInputLayout layoutUsername  = dialogView.findViewById(R.id.layout_username);
        TextInputLayout layoutFirstname = dialogView.findViewById(R.id.layout_firstname);
        TextInputLayout layoutLastname  = dialogView.findViewById(R.id.layout_lastname);
        TextInputLayout layoutEmail     = dialogView.findViewById(R.id.layout_email);
        TextInputLayout layoutPhone     = dialogView.findViewById(R.id.layout_phone);
        TextInputLayout layoutDate      = dialogView.findViewById(R.id.layout_date);
        TextInputLayout layoutPassword  = dialogView.findViewById(R.id.layout_password);

        // date field opens a date picker instead of a keyboard
        etDate.setOnClickListener(v -> {
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                    .setValidator(DateValidatorPointBackward.now())
                    .build();
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("בחר תאריך לידה")
                    .setCalendarConstraints(constraints)
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                etDate.setText(sdf.format(new Date(selection)));
            });
            datePicker.show(getSupportFragmentManager(), "ADD_USER_DATE_PICKER");
        });

        // positive button is set to null in the builder so we can control dismiss manually
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("הוספת משתמש חדש")
                .setView(dialogView)
                .setPositiveButton("הוסף", null)
                .setNegativeButton("ביטול", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                // clears all previous field errors before re-validating
                layoutUsername.setError(null);
                layoutFirstname.setError(null);
                layoutLastname.setError(null);
                layoutEmail.setError(null);
                layoutPhone.setError(null);
                layoutDate.setError(null);
                layoutPassword.setError(null);

                String username  = etUsername.getText().toString().trim();
                String firstname = etFirstname.getText().toString().trim();
                String lastname  = etLastname.getText().toString().trim();
                String email     = etEmail.getText().toString().trim();
                String phone     = etPhone.getText().toString().trim();
                String date      = etDate.getText().toString().trim();
                String password  = etPassword.getText().toString().trim();

                // validates each field in order and stops at the first error
                if (!Validator.isUNameValid(username)) {
                    layoutUsername.setError("שם משתמש לא תקין (אותיות, מספרים, נקודה, קו תחתון)");
                    return;
                }
                if (!Validator.isNameValid(firstname)) {
                    layoutFirstname.setError("שם פרטי חייב להכיל לפחות 3 תווים");
                    return;
                }
                if (!Validator.isNameValid(lastname)) {
                    layoutLastname.setError("שם משפחה חייב להכיל לפחות 3 תווים");
                    return;
                }
                if (!Validator.isEmailValid(email)) {
                    layoutEmail.setError("כתובת אימייל לא תקינה");
                    return;
                }
                if (!Validator.isPhoneValid(phone)) {
                    layoutPhone.setError("מספר טלפון חייב להכיל לפחות 10 ספרות");
                    return;
                }
                if (!Validator.isBirthDateValid(date)) {
                    layoutDate.setError("יש לבחור תאריך לידה תקין");
                    return;
                }
                if (!Validator.isPasswordValid(password)) {
                    layoutPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
                    return;
                }

                // checks uniqueness in the DB before creating the user
                databaseService.getUserService()
                        .checkIfUserNameExists(username, new IDatabaseService.DatabaseCallback<Boolean>() {
                            @Override
                            public void onCompleted(Boolean exists) {
                                if (exists) {
                                    layoutUsername.setError("שם המשתמש כבר קיים במערכת");
                                    return;
                                }
                                // generates a new ID and creates the user in the DB
                                String uid = databaseService.getUserService().generateId();
                                User newUser = new User(uid, username, password,
                                        firstname, lastname, email, phone, date);

                                databaseService.getUserService()
                                        .create(newUser, new IDatabaseService.DatabaseCallback<Void>() {
                                            @Override
                                            public void onCompleted(Void unused) {
                                                // automatically creates an admin chat for the new user
                                                databaseService.getChatService()
                                                        .getOrCreateAdminChat(uid, new IDatabaseService.DatabaseCallback<String>() {
                                                            @Override public void onCompleted(String chatId) {}
                                                            @Override public void onFailed(Exception e) {}
                                                        });
                                                // sends a welcome message from the admin
                                                sendAutoAdminMessage(uid,
                                                        "ברוכ/ה הבא/ה לסיפור שני! 🌸\nשמחים שהצטרפת לקהילה שלנו.\nאם יש שאלות או צורך בעזרה — אנחנו כאן!");
                                                userAdapter.addUser(newUser);
                                                tvUserCount.setText("סה״כ: " + userAdapter.getItemCount());
                                                dialog.dismiss();
                                                Toast.makeText(UsersListActivity.this,
                                                        "המשתמש נוסף בהצלחה ✅", Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onFailed(Exception e) {
                                                Toast.makeText(UsersListActivity.this,
                                                        "שגיאה ביצירת המשתמש", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(UsersListActivity.this,
                                        "שגיאה בבדיקת שם משתמש", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        });

        dialog.show();
    }

    // soft delete — marks the user as inactive without removing data
    private void showDeactivateConfirmDialog(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("השבתת משתמש")
                .setMessage(
                        "האם אתה בטוח שברצונך להשבית את " + user.getUserName() + "?\n\n" +
                                "• המשתמש לא יוכל להתחבר למערכת\n" +
                                "• הצ'אטים שלו יוצגו כחסומים\n" +
                                "• תרומותיו לא יוצגו בחיפוש\n" +
                                "• ניתן לשחזר בכל עת ✅"
                )
                .setPositiveButton("השבת", (d, w) -> {
                    databaseService.getUserService()
                            .updateUserFields(user.getId(), Map.of("active", false),
                                    new IDatabaseService.DatabaseCallback<Void>() {
                                        @Override
                                        public void onCompleted(Void unused) {
                                            user.setActive(false);
                                            userAdapter.updateUserById(user);
                                            Toast.makeText(UsersListActivity.this,
                                                    "המשתמש הושבת ⛔", Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onFailed(Exception e) {
                                            Toast.makeText(UsersListActivity.this,
                                                    "שגיאה בהשבתת המשתמש", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // permanent deletion — marks donations as DONOR_DELETED, flags chats, then removes the user
    private void showDeleteConfirmDialog(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("מחיקה לצמיתות")
                .setMessage(
                        "האם אתה בטוח שברצונך למחוק לצמיתות את " + user.getUserName() + "?\n\n" +
                                "• הצ'אטים שלו יוצגו כ\"משתמש נמחק\" לצד השני\n" +
                                "• תרומותיו יוסרו מהחיפוש (הרשומות נשמרות לתיעוד)\n" +
                                "• לא ניתן לשחזר פעולה זו\n\n" +
                                "⚠️ פעולה זו היא בלתי הפיכה!"
                )
                .setPositiveButton("מחק לצמיתות", (d, w) -> performFullDelete(user))
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Full permanent delete in 3 steps:
     * 1. marks all donations → DONOR_DELETED
     * 2. flags all chats as donorDeleted so the other side sees a banner
     * 3. deletes the user record from the DB
     */
    private void performFullDelete(User user) {
        databaseService.getDonationService()
                .getByGiverId(user.getId(), new IDatabaseService.DatabaseCallback<List<Donation>>() {
                    @Override
                    public void onCompleted(List<Donation> donations) {
                        // step 1 — update each active donation to DONOR_DELETED
                        for (Donation donation : donations) {
                            if (donation.getStatus() != DonationStatus.DONOR_DELETED) {
                                donation.updateStatus(DonationStatus.DONOR_DELETED, "תורם נמחק מהמערכת");
                                databaseService.getDonationService()
                                        .update(donation.getId(), old -> donation, null);
                            }
                        }

                        // step 2 — flag chats (not deleted, so the other user sees the "deleted" banner)
                        databaseService.getChatService()
                                .markUserAsDeleted(user.getId(), new IDatabaseService.DatabaseCallback<Void>() {
                                    @Override
                                    public void onCompleted(Void unused) {
                                        // step 3 — delete the user record
                                        databaseService.getUserService()
                                                .delete(user.getId(), new IDatabaseService.DatabaseCallback<Void>() {
                                                    @Override
                                                    public void onCompleted(Void unused2) {
                                                        userAdapter.removeUser(user);
                                                        Toast.makeText(UsersListActivity.this,
                                                                "המשתמש נמחק לצמיתות 🗑", Toast.LENGTH_SHORT).show();
                                                    }
                                                    @Override
                                                    public void onFailed(Exception e) {
                                                        Toast.makeText(UsersListActivity.this,
                                                                "שגיאה במחיקת המשתמש", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                    @Override
                                    public void onFailed(Exception e) {
                                        Log.e(TAG, "שגיאה במחיקת צ'אטים", e);
                                        // continues with user deletion even if chat flagging failed
                                        databaseService.getUserService()
                                                .delete(user.getId(), new IDatabaseService.DatabaseCallback<Void>() {
                                                    @Override
                                                    public void onCompleted(Void unused) {
                                                        userAdapter.removeUser(user);
                                                        Toast.makeText(UsersListActivity.this,
                                                                "המשתמש נמחק 🗑 (שגיאה בצ'אטים)", Toast.LENGTH_SHORT).show();
                                                    }
                                                    @Override
                                                    public void onFailed(Exception e2) {
                                                        Toast.makeText(UsersListActivity.this,
                                                                "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "שגיאה בטעינת תרומות", e);
                        Toast.makeText(UsersListActivity.this,
                                "שגיאה בטעינת תרומות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // restores a deactivated user's access
    private void reactivateUser(User user) {
        databaseService.getUserService()
                .updateUserFields(user.getId(), Map.of("active", true),
                        new IDatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void unused) {
                                user.setActive(true);
                                userAdapter.updateUserById(user);
                                Toast.makeText(UsersListActivity.this,
                                        "המשתמש הופעל מחדש ✅", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(UsersListActivity.this,
                                        "שגיאה בהפעלת המשתמש", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // reloads the full user list every time the screen becomes visible
    @Override
    protected void onResume() {
        super.onResume();
        databaseService.getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        String currentUserId = SharedPreferencesUtil.getUserId(UsersListActivity.this);
                        userAdapter.setUserList(users, currentUserId);
                        tvUserCount.setText("סה״כ: " + users.size());
                        if (users.isEmpty()) {
                            usersList.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        } else {
                            usersList.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get users list", e);
                    }
                });
    }
}