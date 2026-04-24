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
import java.util.TimeZone;

public class UsersListActivity extends BaseActivity {

    private static final String TAG = "UsersListActivity";
    private UserAdapter userAdapter;
    private TextView tvUserCount;

    private String searchQuery = "";
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
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, updateDetailsActivity.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(User user) {}

            @Override
            public void onInfoClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, updateDetailsActivity.class);
                intent.putExtra("USER_UID", user.getId());
                intent.putExtra("VIEW_ONLY", true);
                startActivity(intent);
            }

            @Override
            public void onMakeAdminClick(User user) {
                new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                        .setTitle("Make Admin")
                        .setMessage("האם תרצה להפוך את " + user.getUserName() + " לאדמין?")
                        .setPositiveButton("כן", (dialog, which) -> {
                            DatabaseService.getInstance().getDonationService()
                                    .getByGiverId(user.getId(),
                                            new IDatabaseService.DatabaseCallback<List<Donation>>() {
                                                @Override
                                                public void onCompleted(List<Donation> donations) {
                                                    boolean hasActiveDonations = false;
                                                    for (Donation d : donations) {
                                                        DonationStatus s = d.getStatus();
                                                        if (s == DonationStatus.PENDING_APPROVAL ||
                                                                s == DonationStatus.APPROVED_AVAILABLE ||
                                                                s == DonationStatus.MATCHED) {
                                                            hasActiveDonations = true;
                                                            break;
                                                        }
                                                    }
                                                    if (hasActiveDonations) {
                                                        new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                                                                .setTitle("לא ניתן להפוך לאדמין")
                                                                .setMessage("למשתמש יש תרומות פעילות.\nיש לסגור אותן לפני הפיכתו לאדמין.")
                                                                .setPositiveButton("הבנתי", null)
                                                                .show();
                                                        userAdapter.resetMakeAdminButton(user);
                                                        return;
                                                    }
                                                    user.setAdmin(true);
                                                    DatabaseService.getInstance().getUserService().update(
                                                            user.getId(), oldUser -> user,
                                                            new IDatabaseService.DatabaseCallback<User>() {
                                                                @Override
                                                                public void onCompleted(User result) {
                                                                    userAdapter.updateUserById(result);
                                                                    DatabaseService.getInstance().getChatService()
                                                                            .deleteAdminChat(user.getId(), new IDatabaseService.DatabaseCallback<Void>() {
                                                                                @Override public void onCompleted(Void unused) {}
                                                                                @Override public void onFailed(Exception e) { Log.e(TAG, "שגיאה במחיקת צאט", e); }
                                                                            });
                                                                    Toast.makeText(UsersListActivity.this, "המשתמש הפך לאדמין", Toast.LENGTH_SHORT).show();
                                                                }
                                                                @Override
                                                                public void onFailed(Exception e) {
                                                                    Toast.makeText(UsersListActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                                @Override
                                                public void onFailed(Exception e) {
                                                    Toast.makeText(UsersListActivity.this, "שגיאה בבדיקת תרומות", Toast.LENGTH_SHORT).show();
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

            @Override
            public void onToggleActiveClick(User user) {
                if (!user.isActive()) {
                    // משתמש מושבת – הפעל מחדש בלבד
                    new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                            .setTitle("הפעל משתמש")
                            .setMessage("האם להפעיל מחדש את " + user.getUserName() + "?")
                            .setPositiveButton("הפעל", (d, w) -> reactivateUser(user))
                            .setNegativeButton("ביטול", null)
                            .show();
                    return;
                }

                // משתמש פעיל – דיאלוג בחירה
                new androidx.appcompat.app.AlertDialog.Builder(UsersListActivity.this)
                        .setTitle("פעולה על משתמש")
                        .setMessage("מה ברצונך לעשות עם " + user.getUserName() + "?")
                        .setPositiveButton("⛔ השבת", (dialog, which) -> showDeactivateConfirmDialog(user))
                        .setNeutralButton("🗑 מחק לצמיתות", (dialog, which) -> showDeleteConfirmDialog(user))
                        .setNegativeButton("ביטול", null)
                        .show();
            }

            @Override
            public void onChatClick(User user) {
                Intent intent = new Intent(UsersListActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_ID", "admin_" + user.getId());
                intent.putExtra("OTHER_USER_NAME", user.getUserName());
                intent.putExtra("OTHER_USER_ID", user.getId());
                startActivity(intent);
            }
        });

        usersList.setAdapter(userAdapter);

        findViewById(R.id.fab_add_user).setOnClickListener(v -> showAddUserDialog());

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

        ChipGroup chipGroup = findViewById(R.id.chip_group_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_admin)) adminFilter = true;
            else if (checkedIds.contains(R.id.chip_not_admin)) adminFilter = false;
            else adminFilter = null;
            userAdapter.filter(searchQuery, adminFilter);
        });

        userAdapter.setOnFilterListener(count ->
                tvUserCount.setText("Total users: " + count));
    }

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

        // פתיחת date picker בלחיצה על השדה
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

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("➕ הוספת משתמש חדש")
                .setView(dialogView)
                .setPositiveButton("הוסף", null) // null – נטפל ידנית
                .setNegativeButton("ביטול", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                // ניקוי שגיאות קודמות
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

                // Validation
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

                // בדיקת קיום שם משתמש
                DatabaseService.getInstance().getUserService()
                        .checkIfUserNameExists(username, new IDatabaseService.DatabaseCallback<Boolean>() {
                            @Override
                            public void onCompleted(Boolean exists) {
                                if (exists) {
                                    layoutUsername.setError("שם המשתמש כבר קיים במערכת");
                                    return;
                                }
                                // יצירת המשתמש
                                String uid = DatabaseService.getInstance().getUserService().generateId();
                                User newUser = new User(uid, username, password,
                                        firstname, lastname, email, phone, date);

                                DatabaseService.getInstance().getUserService()
                                        .create(newUser, new IDatabaseService.DatabaseCallback<Void>() {
                                            @Override
                                            public void onCompleted(Void unused) {
                                                // יצירת צ'אט אדמין אוטומטי
                                                DatabaseService.getInstance().getChatService()
                                                        .getOrCreateAdminChat(uid, new IDatabaseService.DatabaseCallback<String>() {
                                                            @Override public void onCompleted(String chatId) {}
                                                            @Override public void onFailed(Exception e) {}
                                                        });
                                                userAdapter.addUser(newUser);
                                                tvUserCount.setText("Total users: " + userAdapter.getItemCount());
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

    // ─────────────────────────────────────────────
    // השבתה לוגית
    // ─────────────────────────────────────────────
    private void showDeactivateConfirmDialog(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⛔ השבתת משתמש")
                .setMessage(
                        "האם אתה בטוח שברצונך להשבית את " + user.getUserName() + "?\n\n" +
                                "• המשתמש לא יוכל להתחבר למערכת\n" +
                                "• הצ'אטים שלו יוצגו כחסומים\n" +
                                "• תרומותיו לא יוצגו בחיפוש\n" +
                                "• ניתן לשחזר בכל עת ✅"
                )
                .setPositiveButton("השבת", (d, w) -> {
                    user.setActive(false);
                    DatabaseService.getInstance().getUserService().update(
                            user.getId(), oldUser -> user,
                            new IDatabaseService.DatabaseCallback<User>() {
                                @Override
                                public void onCompleted(User result) {
                                    // ✅ updateUserById מפעיל notifyItemChanged → שקיפות מיידית
                                    userAdapter.updateUserById(result);
                                    Toast.makeText(UsersListActivity.this,
                                            "המשתמש הושבת ⛔", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onFailed(Exception e) {
                                    user.setActive(true);
                                    Toast.makeText(UsersListActivity.this,
                                            "שגיאה בהשבתת המשתמש", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // ─────────────────────────────────────────────
    // מחיקה פיזית – צ'אטים + תרומות + משתמש
    // ─────────────────────────────────────────────
    private void showDeleteConfirmDialog(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🗑 מחיקה לצמיתות")
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
     * ✅ מחיקה פיזית מלאה:
     * 1. עדכון סטטוס תרומות → DONOR_DELETED
     * 2. מחיקת כל הצ'אטים
     * 3. מחיקת המשתמש מה-DB
     */
    private void performFullDelete(User user) {
        // שלב 1: עדכון תרומות לסטטוס DONOR_DELETED
        DatabaseService.getInstance().getDonationService()
                .getByGiverId(user.getId(), new IDatabaseService.DatabaseCallback<List<Donation>>() {
                    @Override
                    public void onCompleted(List<Donation> donations) {
                        for (Donation donation : donations) {
                            if (donation.getStatus() != DonationStatus.DONOR_DELETED) {
                                donation.updateStatus(DonationStatus.DONOR_DELETED, "תורם נמחק מהמערכת");
                                DatabaseService.getInstance().getDonationService()
                                        .update(donation.getId(), old -> donation, null);
                            }
                        }

                        // שלב 2: סימון הצ'אטים כ-donorDeleted (לא נמחקים – הצד השני יראה באנר)
                        DatabaseService.getInstance().getChatService()
                                .markUserAsDeleted(user.getId(), new IDatabaseService.DatabaseCallback<Void>() {
                                    @Override
                                    public void onCompleted(Void unused) {
                                        // שלב 3: מחיקת המשתמש עצמו
                                        DatabaseService.getInstance().getUserService()
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
                                        // ממשיכים למחיקת המשתמש גם אם הצ'אטים נכשלו
                                        DatabaseService.getInstance().getUserService()
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

    // ─────────────────────────────────────────────
    // הפעלה מחדש
    // ─────────────────────────────────────────────
    private void reactivateUser(User user) {
        user.setActive(true);
        DatabaseService.getInstance().getUserService().update(
                user.getId(), oldUser -> user,
                new IDatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User result) {
                        userAdapter.updateUserById(result);
                        Toast.makeText(UsersListActivity.this,
                                "המשתמש הופעל מחדש ✅", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailed(Exception e) {
                        user.setActive(false);
                        Toast.makeText(UsersListActivity.this,
                                "שגיאה בהפעלת המשתמש", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        String currentUserId = SharedPreferencesUtil.getUserId(UsersListActivity.this);
                        userAdapter.setUserList(users, currentUserId);
                        tvUserCount.setText("Total users: " + users.size());
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