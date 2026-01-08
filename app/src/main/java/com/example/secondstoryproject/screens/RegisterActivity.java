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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etUName, etFName, etLName, etEmail, etPhoneNumber, etPassword;
    private TextInputEditText etDate;
    private Button btnRegister;

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

        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
            datePickerBuilder.setTitleText("בחר תאריך");

            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds());
            datePickerBuilder.setCalendarConstraints(constraintsBuilder.build());

            String existingDate = etDate.getText().toString();
            if (!existingDate.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date parsedDate = dateFormat.parse(existingDate);
                    if (parsedDate != null) {
                        datePickerBuilder.setSelection(parsedDate.getTime());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MaterialDatePicker<Long> datePicker = datePickerBuilder.build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formattedDate = dateFormat.format(new Date(selection));
                etDate.setText(formattedDate);
            });

            datePicker.addOnNegativeButtonClickListener(v1 -> etDate.clearFocus());
            datePicker.addOnDismissListener(dialog -> etDate.clearFocus());

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        btnRegister.setOnClickListener(this);
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
        if (!Validator.isUNameValid(uName)) {
            etUName.setError("User name can include letters, numbers, dot and underscore");
            etEmail.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(fName)) {
            etFName.setError("First name must be at least 3 characters long");
            etFName.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(lName)) {
            etLName.setError("Last name must be at least 3 characters long");
            etLName.requestFocus();
            return false;
        }

        if (!Validator.isEmailValid(email)) {
            etEmail.setError("Invalid email address");
            etEmail.requestFocus();
            return false;
        }

        if (date == null || date.trim().isEmpty()) {
            etDate.setError("Please select a date");
            etDate.requestFocus();
            return false;
        }

        if (!Validator.isPhoneValid(phone)) {
            etPhoneNumber.setError("Phone number must be at least 10 characters long");
            etPhoneNumber.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("Password must be at least 6 characters long");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String username, String password, String fName, String lName,
                              String email, String phoneNumber, String dateOfBirth) {

        String uid = databaseService.generateUserId();

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

        databaseService.checkIfUserNameExists(username, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    Toast.makeText(RegisterActivity.this, "User name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    createUserInDatabase(user);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to check username", e);
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        databaseService.writeUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);
                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
            }
        });
    }
}
