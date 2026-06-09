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

// Registration screen — handles new user sign-up. No drawer or bottom nav needed
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etUName, etFName, etLName, etEmail, etPhoneNumber, etPassword;
    private TextInputEditText etDate;
    // TextInputLayout wraps each field and allows showing error messages below it
    private TextInputLayout usernameLayout, firstnameLayout, lastnameLayout,
            emailLayout, phoneLayout, passwordLayout, dateInputLayout;
    private Button btnRegister;
    @Override
    protected boolean hasSideMenu() { return false; }
    @Override
    protected boolean hasBottomMenu(){ return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // navigates to the login screen
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

        // prevents typing in the date field — forces the user to use the date picker
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            // only allows selecting dates up to today (no future dates)
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                    .setValidator(DateValidatorPointBackward.now())
                    .build();

            // builds and shows a Material date picker dialog
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("בחר תאריך לידה")
                    .setCalendarConstraints(constraints)
                    .build();

            // when user confirms a date — formats it as dd/MM/yyyy and sets it in the field
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                etDate.setText(dateFormat.format(new Date(selection)));
                dateInputLayout.setError(null);
                dateInputLayout.setErrorEnabled(false);
            });

            // clears focus if the user cancels or dismisses the picker
            datePicker.addOnNegativeButtonClickListener(v1 -> etDate.clearFocus());
            datePicker.addOnDismissListener(dialog -> etDate.clearFocus());
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        // this activity handles the register button click (see onClick below)
        btnRegister.setOnClickListener(this);

        // closes this screen and returns to LandingActivity
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }

    // called when the register button is tapped
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

            // validates all fields before attempting registration
            if (!checkInput(uName, fName, lName, email, phone, date, password)) {
                return;
            }

            registerUser(uName, password, fName, lName, email, phone, date);
        }
    }

    // validates all registration fields — shows an error on the first invalid field and returns false
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

    // checks if the username is already taken, then creates the user if not
    private void registerUser(String username, String password, String fName, String lName,
                              String email, String phoneNumber, String dateOfBirth) {
        // generates a unique ID for the new user
        String uid = databaseService.getUserService().generateId();
        User user = new User( uid, username, password, fName, lName, email, phoneNumber, dateOfBirth );

        databaseService.getUserService().checkIfUserNameExists(username, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    // username already taken — show error on the username field
                    runOnUiThread(() -> {
                        usernameLayout.setErrorEnabled(true);
                        usernameLayout.setError("שם המשתמש קיים במערכת, בחרו שם משתמש אחר");
                        etUName.requestFocus();
                        usernameLayout.setErrorIconDrawable(null);
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

    // saves the new user to the DB, creates an admin chat, sends a welcome message, then navigates to MainActivity
    private void createUserInDatabase(User user) {
        databaseService.getUserService().create(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                // saves the user locally so the app remembers it
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);

                databaseService.getChatService()
                        .getOrCreateAdminChat(user.getId(),
                                new DatabaseService.DatabaseCallback<String>() {
                                    @Override
                                    public void onCompleted(String chatId) {
                                        sendAutoAdminMessage(user.getId(),
                                                "ברוכ/ה הבא/ה לסיפור שני! 🌸\nשמחים שהצטרפת לקהילה שלנו.\nאם יש שאלות או צורך בעזרה — אנחנו כאן!");
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                    }
                                    @Override
                                    public void onFailed(Exception e) {
                                        // chat creation failed but registration succeeded — continue anyway
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

    // clears all error messages from all input fields
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