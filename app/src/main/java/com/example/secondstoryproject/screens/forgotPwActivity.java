package com.example.secondstoryproject.screens;

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
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.Validator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Forgot password screen — resets a user's password by verifying username and email
// No drawer or bottom nav needed
public class forgotPwActivity extends BaseActivity {

    private static final String TAG = "forgotPwActivity";
    private EditText etUName, etEmail, etPw, etConfirmPw;
    private Button btnConfirm;

    @Override
    protected boolean hasSideMenu() {
        return false;
    }
    @Override
    protected boolean hasBottomMenu(){ return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pw);

        etUName = findViewById(R.id.usernameInput);
        etEmail = findViewById(R.id.emailInput);
        etPw = findViewById(R.id.passwordInput);
        etConfirmPw = findViewById(R.id.confirmPasswordInput);
        btnConfirm = findViewById(R.id.btn_changePw);

        // called when btnConfirm is clicked
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // trim() removes spaces from the start & end of the input
                String username = etUName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pw = etPw.getText().toString().trim();
                String cpw = etConfirmPw.getText().toString().trim();

                if (checkInput(username, email, pw, cpw)) {
                    // step 1: find the user in the DB by username
                    databaseService.getUserService().findUserByUserName(username, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User user) {
                            if (user == null) {
                                Log.e(TAG, "Username does not exist: " + username);
                                etUName.setError("אין משתמש קיים עם השם משתמש הזה");
                                etUName.requestFocus();
                                return;
                            }
                            // step 2: verify that the email matches the account
                            if (!email.equals(user.getEmail())) {
                                Log.e(TAG, "Email does not match for user: " + username);
                                etEmail.setError("האימייל לא תואם לחשבון זה");
                                etEmail.requestFocus();
                                return;
                            }

                            // step 3: update the password in the DB
                            user.setPassword(cpw);
                            databaseService.getUserService().update(user.getId(), u -> {
                                u.setPassword(user.getPassword());
                                return u;
                            }, new DatabaseService.DatabaseCallback<User>() {
                                @Override
                                public void onCompleted(User updatedUser) {
                                    Toast.makeText(forgotPwActivity.this, "הסיסמה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                                    // closes the screen and returns to login
                                    finish();
                                }
                                @Override
                                public void onFailed(Exception e) {
                                    Log.e(TAG, "Failed to update password", e);
                                    Toast.makeText(forgotPwActivity.this, "שגיאה בעדכון הסיסמה", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "Database error while searching for user", e);
                            Toast.makeText(forgotPwActivity.this, "שגיאה בגישה לשרת", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // closes this screen and returns to login
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // validates all fields — returns false and shows an error on the first invalid field
    private boolean checkInput(String username, String email, String pw, String cpw) {
        Log.d(TAG, "entered checkInput function");
        if (!Validator.isUNameValid(username)) {
            Log.e(TAG, "checkInput: Invalid user name");
            etUName.setError("שם משתמש לא תקין");
            etUName.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isUNameValid true");
        }
        if (!Validator.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email");
            etEmail.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isEmailValid true");
        }
        if (!Validator.isPasswordValid(pw)) {
            Log.e(TAG, "checkInput: Password must be at least 6 chars");
            etPw.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            etPw.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isPasswordValid true");
        }
        // confirms both password fields match
        if (!pw.equals(cpw)) {
            Log.e(TAG, "checkInput: Passwords do not match");
            etConfirmPw.setError("הסיסמאות אינן תואמות");
            etConfirmPw.requestFocus();
            return false;
        } else {
            Log.d(TAG, "password match true");
        }

        Log.d(TAG, "checkInput: Input is valid");
        return true;
    }
}
