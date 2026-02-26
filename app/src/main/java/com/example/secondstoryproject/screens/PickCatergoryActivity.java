package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.CategoryAdapter;
import com.example.secondstoryproject.models.DonationCategory;

public class PickCatergoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge (לא חובה אבל יפה)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pick_catergory);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ----------------------------
        // RecyclerView setup
        // ----------------------------
        RecyclerView rvCategories = findViewById(R.id.rvCategories);

        // Grid עם 3 ריבועים בשורה
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvCategories.setLayoutManager(gridLayoutManager);

        // Adapter עם callback ללחיצה
        CategoryAdapter adapter = new CategoryAdapter(category -> {
            // מה קורה כשנלחץ ריבוע
            // נעבור לשלב הבא של התרומה
            Intent intent = new Intent(PickCatergoryActivity.this, AddDonationStep2Activity.class);
            intent.putExtra("selected_category", category.name());
            startActivity(intent);
        });

        rvCategories.setAdapter(adapter);
    }
}
