package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.utils.Validator;

// Login screen — handles username/password login. No drawer or bottom nav needed.
// implements View.OnClickListener — this activity handles its own button clicks via onClick()
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    // TAG is used to identify this activity's log messages in Logcat
    private static final String TAG = "LoginActivity";

    private EditText etUName, etPassword;
    private Button btnLogin;


    @Override
    protected boolean hasSideMenu() { return false; }
    @Override
    protected boolean hasBottomMenu(){ return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // navigates to the registration screen
        TextView textToRegister = findViewById(R.id.tv_login_to_register);
        textToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // navigates to the forgot password screen
        TextView textToForgotPw = findViewById(R.id.tv_login_to_resetpw);
        textToForgotPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, forgotPwActivity.class);
                startActivity(intent);
            }
        });

        etUName = findViewById(R.id.usernameInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btn_login_toHome);

        // this activity handles the login button click (see onClick below)
        btnLogin.setOnClickListener(this);

        // closes this screen and returns to LandingActivity
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // called when btnLogin is clicked
    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            Log.d(TAG, "onClick: Login button clicked");

            String uname = etUName.getText().toString();
            String password = etPassword.getText().toString();

            // validates the input before attempting login
            if (!checkInput(uname, password)) { return; }

            loginUser(uname, password);
        }
    }

    // validates username and password format — shows an error on the field if invalid
    private boolean checkInput(String uname, String password) {
        if (!Validator.isUNameValid(uname)) {
            etUName.setError("שם משתמש לא תקין");
            etUName.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            etPassword.setError("סיסמה חייבת להכיל לפחות 6 תווים");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    // looks up the user in the DB by username and password, then navigates to the correct screen
    private void loginUser(String uname, String password) {
        Log.d(TAG,"in function loginUser");
        databaseService.getUserService().getUserByUserNameAndPassword(uname, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                // user not found in the DB
                if (user == null) {
                    Log.d(TAG,"got null as a user, USER DOES NOT EXIST");
                    Toast.makeText(LoginActivity.this,
                            "שם משתמש או סיסמה אינם נכונים",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // user exists but is blocked
                if (!user.isActive()) {
                    Toast.makeText(LoginActivity.this,
                            "החשבון שלך חסום. אי אפשר להתחבר אליו עד לשחרור החסימה.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // saves the logged-in user locally so the app remembers it
                SharedPreferencesUtil.saveUser(LoginActivity.this, user);

                // sends admin to AdminMainActivity, regular user to MainActivity
                Class<?> targetActivity;
                if (user.isAdmin()) {
                    targetActivity = AdminMainActivity.class;
                } else {
                    targetActivity = MainActivity.class;
                }

                // clears the back stack so the user can't navigate back to the login screen
                Intent mainIntent = new Intent(LoginActivity.this, targetActivity);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                // DB call failed — show a generic error and sign out for safety
                Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                Toast.makeText(LoginActivity.this,
                        "שגיאה טכנית, נסה שוב מאוחר יותר",
                        Toast.LENGTH_LONG).show();
                SharedPreferencesUtil.signOutUser(LoginActivity.this);
            }
        });
    }
}
