package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class temporeryActivity extends AppCompatActivity {

    private static final String TAG = "temporeryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_temporery);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // 🔥 בדיקת חיבור ל-Firebase והדפסת כל המשתמשים
        DatabaseReference ref = FirebaseDatabase.getInstance(
                "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("users");

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Success! Users count: " + task.getResult().getChildrenCount());

                for (DataSnapshot ds : task.getResult().getChildren()) {
                    String username = ds.child("userName").getValue(String.class);
                    String email = ds.child("email").getValue(String.class);
                    Log.d(TAG, "User: " + username + ", Email: " + email);
                }

            } else {
                Log.e(TAG, "Failed to connect to Firebase", task.getException());
            }
        });
    }
}
