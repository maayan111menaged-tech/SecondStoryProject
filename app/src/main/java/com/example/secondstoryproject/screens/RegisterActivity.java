package com.example.secondstoryproject.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.utils.Validator;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etUName, etFName, etLName, etEmail, etPhoneNumber, etPassword;
    private TextInputEditText etDate;
    private TextInputLayout usernameLayout, firstnameLayout, lastnameLayout,
            emailLayout, phoneLayout, passwordLayout, dateInputLayout;
    private Button btnRegister;
    @Override
    protected boolean hasSideMenu() {
        return false; // לא צריך Drawer
    }
    @Override
    protected boolean hasBottomMenu(){ return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        databaseService = DatabaseService.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textToLogin = findViewById(R.id.tv_register_to_login);
        textToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        etUName = findViewById(R.id.usernameInput);
        etFName = findViewById(R.id.firstnameInput);
        etLName = findViewById(R.id.lastnameInput);
        etEmail = findViewById(R.id.emailInput);
        etPhoneNumber = findViewById(R.id.phonenumberInput);
        etPassword = findViewById(R.id.passwordInput);
        btnRegister = findViewById(R.id.btn_register_toHome);
        etDate = findViewById(R.id.dateInput);

        usernameLayout = findViewById(R.id.usernameInput_layout);
        firstnameLayout = findViewById(R.id.firstnameInput_layout);
        lastnameLayout = findViewById(R.id.lastnameInput_layout);
        emailLayout = findViewById(R.id.emailInput_layout);
        phoneLayout = findViewById(R.id.phonenumberInput_layout);
        passwordLayout = findViewById(R.id.passwordInput_layout);
        dateInputLayout = findViewById(R.id.dateInput_layout);


        etDate.setFocusable(false);
        etDate.setClickable(true);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                etDate.setText(dateFormat.format(new Date(selection)));
                dateInputLayout.setError(null);
                dateInputLayout.setErrorEnabled(false);
            });

            datePicker.addOnNegativeButtonClickListener(v1 -> etDate.clearFocus());
            datePicker.addOnDismissListener(dialog -> etDate.clearFocus());
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        btnRegister.setOnClickListener(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) {
            Log.d(TAG, "onClick: Register button clicked");

            String uName = etUName.getText().toString();
            String fName = etFName.getText().toString();
            String lName = etLName.getText().toString();
            String email = etEmail.getText().toString();
            String date = etDate.getText().toString();
            String password = etPassword.getText().toString();
            String phone = etPhoneNumber.getText().toString();

            /// Validate input
            if (!checkInput(uName, fName, lName, email, phone, date, password)) {
                return;
            }

            registerUser(uName, password, fName, lName, email, phone, date);
        }
    }

    private boolean checkInput(String uName, String fName, String lName, String email, String phone, String date, String password) {
        clearErrors();

        if (!Validator.isUNameValid(uName)) {
            usernameLayout.setErrorEnabled(true);
            usernameLayout.setError("שם משתמש יכול להכיל אותיות באנגלית, מספרים, נקודה וקו תחתון");
            etUName.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(fName)) {
            firstnameLayout.setErrorEnabled(true);
            firstnameLayout.setError("שם פרטי חייב להכיל לפחות 3 תווים");
            etFName.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(lName)) {
            lastnameLayout.setErrorEnabled(true);
            lastnameLayout.setError("שם משפחה חייב להכיל לפחות 3 תווים");
            etLName.requestFocus();
            return false;
        }

        if (!Validator.isEmailValid(email)) {
            emailLayout.setErrorEnabled(true);
            emailLayout.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        }

        if (!Validator.isPhoneValid(phone)) {
            phoneLayout.setErrorEnabled(true);
            phoneLayout.setError("מספר טלפון חייב להכיל לפחות 10 ספרות");
            etPhoneNumber.requestFocus();
            return false;
        }

        if (!Validator.isBirthDateValid(date)) {
            dateInputLayout.setErrorEnabled(true);
            dateInputLayout.setError("נא לבחור תאריך לידה תקין");
            etDate.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String username, String password, String fName, String lName,
                              String email, String phoneNumber, String dateOfBirth) {

        String uid = databaseService.getUserService().generateId();
        User user = new User(
                uid,
                username,
                password,
                fName,
                lName,
                email,
                phoneNumber,
                dateOfBirth
        );

        databaseService.getUserService().checkIfUserNameExists(username, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                Log.d(TAG, "checkIfUserNameExists result: " + exists); // ← הוסיפי את זה
                if (exists) {
                    runOnUiThread(() -> {
                        usernameLayout.setErrorEnabled(true);
                        usernameLayout.setError("שם המשתמש קיים במערכת, בחרו שם משתמש אחר");
                        etUName.requestFocus();
                        usernameLayout.setErrorIconDrawable(null); // מניעת באג ויזואלי
                    });
                } else {
                    createUserInDatabase(user);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to check username", e);
                Toast.makeText(RegisterActivity.this, "שגיאה ברישום המשתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        databaseService.getUserService().create(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);

                DatabaseService.getInstance().getChatService()
                        .getOrCreateAdminChat(user.getId(),
                                new DatabaseService.DatabaseCallback<String>() {
                                    @Override
                                    public void onCompleted(String chatId) {
                                        // עוברים הלאה בכל מקרה
                                        sendAutoAdminMessage(user.getId(),
                                                "ברוכ/ה הבא/ה לסיפור שני! 🌸\nשמחים שהצטרפת לקהילה שלנו.\nאם יש שאלות או צורך בעזרה — אנחנו כאן!");
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                    }
                                    @Override
                                    public void onFailed(Exception e) {
                                        // לא קריטי — עוברים בכל מקרה
                                        sendAutoAdminMessage(user.getId(),
                                                "ברוכ/ה הבא/ה לסיפור שני! 🌸\nשמחים שהצטרפת לקהילה שלנו.\nאם יש שאלות או צורך בעזרה — אנחנו כאן!");
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                    }
                                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                Toast.makeText(RegisterActivity.this, "שגיאה ברישום המשתמש", Toast.LENGTH_SHORT).show();
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
            }
        });
    }

    private void clearErrors() {
        usernameLayout.setError(null);
        usernameLayout.setErrorEnabled(false);

        firstnameLayout.setError(null);
        firstnameLayout.setErrorEnabled(false);

        lastnameLayout.setError(null);
        lastnameLayout.setErrorEnabled(false);

        emailLayout.setError(null);
        emailLayout.setErrorEnabled(false);

        phoneLayout.setError(null);
        phoneLayout.setErrorEnabled(false);

        passwordLayout.setError(null);
        passwordLayout.setErrorEnabled(false);

        dateInputLayout.setError(null);
        dateInputLayout.setErrorEnabled(false);
    }
}