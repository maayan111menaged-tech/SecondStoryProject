package com.example.secondstoryproject.screens;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
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

/// Activity for registering the user
/// This activity is used to register the user
/// It contains fields for the user to enter their information
/// It also contains a button to register the user
/// When the user is registered, they are redirected to the main activity

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText etUName, etFName, etLName, etEmail, etPhoneNumber, etPassword;
    private TextInputEditText etDate;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /// set the layout for the activity
        setContentView(R.layout.activity_register);
        /// ///////////////////////////////////////////////////////////////////////////////////////////
        databaseService = DatabaseService.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        /// get the views
        etUName = findViewById(R.id.usernameInput);
        etFName = findViewById(R.id.firstnameInput);
        etLName = findViewById(R.id.lastnameInput);
        etEmail = findViewById(R.id.emailInput);
        etPhoneNumber = findViewById(R.id.phonenumberInput);
        etDate = findViewById(R.id.dateInput);
        etPassword = findViewById(R.id.passwordInput);
        btnRegister = findViewById(R.id.btn_register_toHome);

        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // צור את ה-DatePicker
                MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
                datePickerBuilder.setTitleText("בחר תאריך");

                // הגבלת תאריכים לעבר
                CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds()); // עד היום
                datePickerBuilder.setCalendarConstraints(constraintsBuilder.build());

                // אם יש כבר תאריך בשדה, הגדר אותו בתור תאריך התחלתי
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

                // כאשר המשתמש בוחר תאריך
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String formattedDate = dateFormat.format(new Date(selection));
                        etDate.setText(formattedDate);
                    }
                });

                // ביטול או סגירה - רק מאפס את הפוקוס
                datePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        etDate.clearFocus();
                    }
                });
                datePicker.addOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        etDate.clearFocus();
                    }
                });

                // הצג את ה-DatePicker
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        /// set the click listener
        btnRegister.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == btnRegister.getId()) {
            Log.d(TAG, "onClick: Register button clicked");

            /// get the input from the user
            String uName = etUName.getText().toString();
            String fName = etFName.getText().toString();
            String lName = etLName.getText().toString();
            String email = etEmail.getText().toString();
            String date = etDate.getText().toString();
            String password = etPassword.getText().toString();
            String phone = etPhoneNumber.getText().toString();

            /// log the input
            Log.d(TAG, "onClick: User Name: " + uName);
            Log.d(TAG, "onClick: First Name: " + fName);
            Log.d(TAG, "onClick: Last Name: " + lName);
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Date: " + date);
            Log.d(TAG, "onClick: Password: " + password);
            Log.d(TAG, "onClick: Phone: " + phone);


            /// Validate input
            Log.d(TAG, "onClick: Validating input...");
            if (!checkInput(uName, fName, lName, email, phone, date, password)) {
                return;
            }

            Log.d(TAG, "onClick: Registering user...");

            /// Register user
            registerUser(uName, password, fName, lName, email, phone, date,false);

        }
        }


    /// Check if the input is valid
    /// @return true if the input is valid, false otherwise
    /// @see Validator
    private boolean checkInput(String uName, String fName, String lName, String email, String phone,String date, String password) {

        if (!Validator.isUNameValid(uName)) {
            Log.e(TAG, "checkInput: User name can include letters, numbers, dot and underscore");
            /// show error message to user
            etUName.setError("User name can include letters, numbers, dot and underscore");
            /// set focus to user name field
            etEmail.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(fName)) {
            Log.e(TAG, "checkInput: First name must be at least 3 characters long");
            /// show error message to user
            etFName.setError("First name must be at least 3 characters long");
            /// set focus to first name field
            etFName.requestFocus();
            return false;
        }

        if (!Validator.isNameValid(lName)) {
            Log.e(TAG, "checkInput: Last name must be at least 3 characters long");
            /// show error message to user
            etLName.setError("Last name must be at least 3 characters long");
            /// set focus to last name field
            etLName.requestFocus();
            return false;
        }

        if (!Validator.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email address");
            /// show error message to user
            etEmail.setError("Invalid email address");
            /// set focus to email field
            etEmail.requestFocus();
            return false;
        }
        if (date == null || date.trim().isEmpty()) {
            Log.e(TAG, "checkInput: Date cannot be empty");
            etDate.setError("Please select a date");
            etDate.requestFocus();
            return false;
        }

        if (!Validator.isPhoneValid(phone)) {
            Log.e(TAG, "checkInput: Phone number must be at least 10 characters long");
            /// show error message to user
            etPhoneNumber.setError("Phone number must be at least 10 characters long");
            /// set focus to phone field
            etPhoneNumber.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            Log.e(TAG, "checkInput: Password must be at least 6 characters long");
            /// show error message to user
            etPassword.setError("Password must be at least 6 characters long");
            /// set focus to password field
            etPassword.requestFocus();
            return false;
        }

        Log.d(TAG, "checkInput: Input is valid");
        return true;
    }

    /// Register the user
    private void registerUser(String username, String password, String fName, String lName, String email, String phoneNumber,String dateOfBirth,boolean isAdmin) {
        Log.d(TAG, "registerUser: Registering user...");

        String uid = databaseService.generateUserId();

        /// create a new user object
        User user = new User( uid, username, password, fName, lName, email,
                phoneNumber,dateOfBirth,0, "default",
                false);

        databaseService.checkIfUserNameExists(username, new DatabaseService.DatabaseCallback<Boolean>() {
            @Override
            public void onCompleted(Boolean exists) {
                Log.d(TAG, "checkIfUserNameExists completed: " + exists);
                if (exists) {
                    Log.e(TAG, "onCompleted: User name already exists");
                    Toast.makeText(RegisterActivity.this, "User name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    createUserInDatabase(user);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "checkIfUserNameExists failed", e);
                Log.e(TAG, "onFailed: Failed to check username", e);
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d(TAG, "createUserInDatabase: User created successfully");
                /// save the user to shared preferences
                SharedPreferencesUtil.saveUser(RegisterActivity.this, user);
                Log.d(TAG, "createUserInDatabase: Redirecting to MainActivity");
                /// Redirect to MainActivity and clear back stack to prevent user from going back to register screen
                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                /// clear the back stack (clear history) and start the MainActivity
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                /// show error message to user
                Toast.makeText(RegisterActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                /// sign out the user if failed to register
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
            }
        });
    }
}