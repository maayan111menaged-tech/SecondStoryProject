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

public class forgotPwActivity extends BaseActivity {

    private static final String TAG = "forgotPwActivity";
    private EditText etUName, etEmail, etPw, etConfirmPw;
    private Button btnConfirm;

    @Override
    protected boolean hasSideMenu() {
        return false; // לא צריך Drawer
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_forgot_pw);

        etUName = findViewById(R.id.usernameInput);
        etEmail = findViewById(R.id.emailInput);
        etPw = findViewById(R.id.passwordInput);
        etConfirmPw = findViewById(R.id.confirmPasswordInput);
        btnConfirm = findViewById(R.id.btn_changePw);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pw = etPw.getText().toString().trim();
                String cpw = etConfirmPw.getText().toString().trim();

                if (checkInput(username, email, pw, cpw)) {
                    Log.d(TAG, "Input valid, ready to change password");

                    Log.d(TAG, "Starting password reset for user: " + username);
                    Log.d(TAG , "searching user by the user name");

                    /// שלב 1: חיפוש יוזר לפי שם המשתמש
                    databaseService.findUserByUserName(username, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User user) {
                            if (user == null) {
                                Log.e(TAG, "Username does not exist: " + username);
                                etUName.setError("אין משתמש קיים עם השם משתמש הזה");
                                etUName.requestFocus();
                                return;
                            }
                            /// שלב 2: בדיקת התאמה של האימייל
                            if (!email.equals(user.getEmail())) {
                                Log.e(TAG, "Email does not match for user: " + username);
                                etEmail.setError("האימייל לא תואם לחשבון הזה");
                                etEmail.requestFocus();
                                return;
                            }
                            /// שלב 3: עידכון סיסמא
                            user.setPassword(cpw);

                            databaseService.writeUser(user, new DatabaseService.DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void object) {
                                    Log.d(TAG, "Password updated successfully for: " + username);
                                    Toast.makeText(forgotPwActivity.this, "הסיסמה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
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
    }

    /// Check if the input is valid
    /// @return true if valid, false otherwise
    private boolean checkInput(String username, String email, String pw, String cpw) {
        Log.d(TAG, "entered checkInput function");

        // validate user name
        if (!Validator.isUNameValid(username)) {
            Log.e(TAG, "checkInput: Invalid user name");
            etUName.setError("שם משתמש לא תקין");
            etUName.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isUNameValid true");
        }

        // validate email
        if (!Validator.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email");
            etEmail.setError("כתובת אימייל לא תקינה");
            etEmail.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isEmailValid true");
        }

        // validate password
        if (!Validator.isPasswordValid(pw)) {
            Log.e(TAG, "checkInput: Password must be at least 6 chars");
            etPw.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            etPw.requestFocus();
            return false;
        } else {
            Log.d(TAG, "isPasswordValid true");
        }

        // confirm password match
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
