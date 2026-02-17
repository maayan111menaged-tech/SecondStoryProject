package com.example.secondstoryproject.screens;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.Validator;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class updateDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "UpdateDetailsActivity";
    private EditText etUserName, etFirstName, etLastName, etEmail, etPhoneNumber, etDate, etPassword;
    /// private date
    private Button btnUpdateProfile;
    private User currentUser;

    @Override
    protected boolean hasSideMenu() {
        return false; // לא צריך Drawer
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentLayout(R.layout.activity_update_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFirstName = findViewById(R.id.firstnameInput);
        etLastName = findViewById(R.id.lastnameInput);
        etEmail = findViewById(R.id.emailInput);
        etPhoneNumber = findViewById(R.id.phonenumberInput);
        etDate = findViewById(R.id.dateInput);
        etUserName = findViewById(R.id.usernameInput);
        etPassword = findViewById(R.id.passwordInput);

        // אתחול הכפתור והגדרת OnClickListener
        btnUpdateProfile = findViewById(R.id.btn_updateDetails_toHome);
        btnUpdateProfile.setOnClickListener(this);

        // קבלת המשתמש מהSharedPreferences
        currentUser = SharedPreferencesUtil.getUser(this);

        // הצגת הפרטים בשדות
        if (currentUser != null) {
            showUserProfile();
        }


        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// צור את ה-DatePicker
                MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
                datePickerBuilder.setTitleText("בחר תאריך");

                /// הגבלת תאריכים לעבר
                CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                constraintsBuilder.setEnd(MaterialDatePicker.todayInUtcMilliseconds()); // עד היום
                datePickerBuilder.setCalendarConstraints(constraintsBuilder.build());

                /// אם יש כבר תאריך בשדה, הגדר אותו בתור תאריך התחלתי
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
        btnUpdateProfile.setOnClickListener(this);
    } // סוגר הסופי של onCreate

    private void showUserProfile() {
        etFirstName.setText(currentUser.getfName());
        etLastName.setText(currentUser.getlName());
        etEmail.setText(currentUser.getEmail());
        etPhoneNumber.setText(currentUser.getPhoneNumber());
        etDate.setText(currentUser.getDateOfBirth());
        etUserName.setText(currentUser.getUserName());
        etPassword.setText(currentUser.getPassword());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_updateDetails_toHome) {
            updateUserProfile();
        }
    }

    private void updateUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
            return;
        }

        // קריאת הערכים מהשדות
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String birthDate = etDate.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // בדיקה שהקלט תקין
        if (!isValid(firstName, lastName, phone, email,birthDate, userName, password)) {
            return;
        }

        // עדכון האובייקט
        currentUser.setfName(firstName);
        currentUser.setlName(lastName);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone);
        currentUser.setDateOfBirth(birthDate);
        currentUser.setUserName(userName);
        currentUser.setPassword(password);

        // עדכון במסד הנתונים
        DatabaseService.getInstance().writeUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Toast.makeText(updateDetailsActivity.this, "פרטייך עודכנו בהצלחה!", Toast.LENGTH_LONG).show();
                showUserProfile(); // ריענון השדות
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(updateDetailsActivity.this, "שגיאה בעדכון הפרטים", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating user", e);
            }
        });
    } // סוגר הסופי של updateUserProfile

    private boolean isValid(String firstName, String lastName, String phone, String email,
                            String birthDate, String userName, String password) {
        if (!Validator.isNameValid(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }
        if (!Validator.isNameValid(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }
        if (!Validator.isPhoneValid(phone)) {
            etPhoneNumber.setError("Phone number is required");
            etPhoneNumber.requestFocus();
            return false;
        }
        if (!Validator.isEmailValid(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (birthDate == null || birthDate.trim().isEmpty()) {
            Log.e(TAG, "checkInput: Date cannot be empty");
            etDate.setError("Please select a date");
            etDate.requestFocus();
            return false;
        }
        else{
            Log.d(TAG, "dateCheck true");
        }

        if (!Validator.isUNameValid(userName)) {
            etUserName.setError("User Name is required");
            etUserName.requestFocus();
            return false;
        }
        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

} // סוגר הסופי של המחלקה
