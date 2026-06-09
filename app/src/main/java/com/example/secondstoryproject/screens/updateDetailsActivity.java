package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.Validator;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


// Screen for editing the current user's profile details.
// No side drawer needed here.
public class updateDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "UpdateDetailsActivity";
    private EditText etUserName, etFirstName, etLastName, etEmail, etPhoneNumber, etDate, etPassword;
    private Button btnUpdateProfile;
    private User currentUser;
    private ImageView ivProfile;
    private ImageButton btnEditProfilePic;

    @Override
    protected boolean hasSideMenu() {
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_details);
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

        // both the profile image and the edit button navigate to the picture selection screen
        ivProfile = findViewById(R.id.imgProfile);
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(updateDetailsActivity.this, ProfilePicActivity.class);
                startActivity(intent);
            }
        });
        btnEditProfilePic = findViewById(R.id.btnEditProfilePic);
        btnEditProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(updateDetailsActivity.this, ProfilePicActivity.class);
                startActivity(intent);
            }
        });

        btnUpdateProfile = findViewById(R.id.btn_updateDetails_toHome);
        btnUpdateProfile.setOnClickListener(this);

        currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser != null) {
            // fills all fields with the current user's existing data
            showUserProfile();
        }

        // prevents manual typing in the date field — forces use of the date picker
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            // only allows selecting dates up to today
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                    .setValidator(DateValidatorPointBackward.now())
                    .build();

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("בחר תאריך לידה")
                    .setCalendarConstraints(constraints)
                    .build();

            // if the user already has a date, pre-selects it in the picker
            String existingDate = etDate.getText().toString();
            if (!existingDate.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date parsedDate = dateFormat.parse(existingDate);
                    if (parsedDate != null) {
                        datePicker = MaterialDatePicker.Builder.datePicker()
                                .setTitleText("בחר תאריך לידה")
                                .setCalendarConstraints(constraints)
                                .setSelection(parsedDate.getTime())
                                .build();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MaterialDatePicker<Long> finalDatePicker = datePicker;
            // when user confirms a date — formats it as dd/MM/yyyy and sets it in the field
            finalDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                etDate.setText(dateFormat.format(new Date(selection)));
            });

            finalDatePicker.addOnNegativeButtonClickListener(v1 -> etDate.clearFocus());
            finalDatePicker.addOnDismissListener(dialog -> etDate.clearFocus());
            finalDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

    }

    // populates all input fields with the current user's existing data
    private void showUserProfile() {
        etFirstName.setText(currentUser.getfName());
        etLastName.setText(currentUser.getlName());
        etEmail.setText(currentUser.getEmail());
        etPhoneNumber.setText(currentUser.getPhoneNumber());
        etDate.setText(currentUser.getDateOfBirth());
        etUserName.setText(currentUser.getUserName());
        etPassword.setText(currentUser.getPassword());

        String profileImageBase64 = currentUser.getProfilePic();

        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            ivProfile.setImageBitmap(ImageUtil.fromBase64(profileImageBase64));
        }

    }

    // called when the update button is tapped
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_updateDetails_toHome) {
            updateUserProfile();
        }
    }

    // reads all fields, validates them, then checks username uniqueness before saving
    private void updateUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String birthDate = etDate.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValid(firstName, lastName, phone, email, birthDate, userName, password)) {
            return;
        }

        // if the username changed — checks that the new one isn't already taken
        if (!userName.equals(currentUser.getUserName())) {

            databaseService.getUserService()
                    .checkIfUserNameExists(userName,
                            new DatabaseService.DatabaseCallback<Boolean>() {
                                @Override
                                public void onCompleted(Boolean exists) {
                                    if (exists) {
                                        Toast.makeText(updateDetailsActivity.this,
                                                "שם המשתמש כבר קיים במערכת",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        saveUpdatedUser(firstName, lastName, email, phone, birthDate, userName, password);
                                    }
                                }
                                @Override
                                public void onFailed(Exception e) {
                                    Toast.makeText(updateDetailsActivity.this,
                                            "שגיאה בבדיקת שם המשתמש",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

        } else {
            // username unchanged — skip the uniqueness check
            saveUpdatedUser(firstName, lastName, email, phone, birthDate, userName, password);
        }
    }

    // updates the user object, saves the changed fields to the DB, then navigates back to profile
    private void saveUpdatedUser(String firstName, String lastName,
                                 String email, String phone,
                                 String birthDate, String userName,
                                 String password) {

        currentUser.setfName(firstName);
        currentUser.setlName(lastName);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone);
        currentUser.setDateOfBirth(birthDate);
        currentUser.setUserName(userName);
        currentUser.setPassword(password);

        // updates only the changed fields — not the entire user object
        Map<String, Object> fields = new java.util.HashMap<>();
        fields.put("fName", firstName);
        fields.put("lName", lastName);
        fields.put("email", email);
        fields.put("phoneNumber", phone);
        fields.put("dateOfBirth", birthDate);
        fields.put("userName", userName);
        fields.put("password", password);

        databaseService.getUserService().updateUserFields(currentUser.getId(), fields,
                new IDatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void result) {
                        SharedPreferencesUtil.saveUser(updateDetailsActivity.this, currentUser );
                        Toast.makeText(updateDetailsActivity.this,
                                "פרטייך עודכנו בהצלחה!", Toast.LENGTH_LONG)
                                .show();
                        Intent intent = new Intent(updateDetailsActivity.this, UserProfileActivity.class );
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(updateDetailsActivity.this,"שגיאה בעדכון הפרטים",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // validates all fields — returns false and shows an error on the first invalid field
    private boolean isValid(String firstName, String lastName, String phone, String email,
                            String birthDate, String userName, String password) {
        if (!Validator.isNameValid(firstName)) {
            etFirstName.setError("שם פרטי חייב להכיל לפחות 3 תווים");
            etFirstName.requestFocus();
            return false;
        }
        if (!Validator.isNameValid(lastName)) {
            etLastName.setError("שם משפחה חייב להכיל לפחות 3 תווים");
            etLastName.requestFocus();
            return false;
        }
        if (!Validator.isPhoneValid(phone)) {
            etPhoneNumber.setError("מספר טלפון לא תקין");
            etPhoneNumber.requestFocus();
            return false;
        }
        if (!Validator.isEmailValid(email)) {
            etEmail.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        }
        if (!Validator.isBirthDateValid(birthDate)) {
            Log.e(TAG, "checkInput: Date cannot be empty");
            etDate.setError("נא לבחור תאריך לידה");
            etDate.requestFocus();
            return false;
        }
        else{
            Log.d(TAG, "dateCheck true");
        }

        if (!Validator.isUNameValid(userName)) {
            etUserName.setError("שם משתמש לא תקין");
            etUserName.requestFocus();
            return false;
        }
        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

}
