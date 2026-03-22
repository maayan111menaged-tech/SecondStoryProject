package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationCategory;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

public class AddDonationStep4Activity extends BaseActivity {
    private ImageView imgPreview;
    private TextView tvName, tvDescription, tvCity, tvCategory;
    private Button btnConfirm;
    private String donationName, description, city, categoryName;
    private String imageBase64;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation_step4);

        imgPreview = findViewById(R.id.imgPreview);
        tvName = findViewById(R.id.tvPreviewName);
        tvDescription = findViewById(R.id.tvPreviewDescription);
        tvCity = findViewById(R.id.tvPreviewCity);
        tvCategory = findViewById(R.id.tvPreviewCategory);
        btnConfirm = findViewById(R.id.btnConfirmDonation);

        // --- מקבלים מה-Step3 ---
        donationName = getIntent().getStringExtra("donationName");
        description = getIntent().getStringExtra("description");
        categoryName = getIntent().getStringExtra("selected_category");
        imageBase64 = getIntent().getStringExtra("imageBase64");
        city = getIntent().getStringExtra("city");


        // --- מציגים ---
        tvName.setText(donationName);
        tvDescription.setText(description);
        tvCity.setText(city);

        DonationCategory category = DonationCategory.fromString(categoryName);
        tvCategory.setText(category.getHebrewName());

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            imgPreview.setImageBitmap(ImageUtil.fromBase64(imageBase64));
        }

        btnConfirm.setOnClickListener(v -> publishDonation());
    }

    /// add the food to the database
    /// @see Donation
    private void publishDonation() {

        String donationId = databaseService.getDonationService().generateId();

        // שליפת המשתמש המחובר
        String currentUserId = SharedPreferencesUtil.getUserId(this);

        Donation donation = new Donation(
                donationId,
                donationName,
                description,
                DonationCategory.fromString(categoryName),
                DonationStatus.PENDING_APPROVAL,
                imageBase64,
                city,
                currentUserId,
                null
        );


        databaseService.getDonationService().create(donation, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddDonationStep4Activity.this,
                        "התרומה פורסמה בהצלחה!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AddDonationStep4Activity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddDonationStep4Activity.this,
                        "שגיאה בפרסום התרומה", Toast.LENGTH_SHORT).show();
            }
        });
    }

}