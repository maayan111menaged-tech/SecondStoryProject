package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationCategory;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.utils.Validator;

public class AddDonationStep2Activity extends BaseActivity {

    private static final String TAG = "AddDonationStep2";

    private EditText etDonationName;
    private EditText etDescription;
    private AutoCompleteTextView actCity;
    private Button btnNextToUploadPic;

    private ImageView imgCategory;
    private TextView tvCategoryName;

    private DonationCategory selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation_step2);

        databaseService = DatabaseService.getInstance();

        imgCategory = findViewById(R.id.imgCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        etDonationName = findViewById(R.id.etDonationName);
        etDescription = findViewById(R.id.etDescription);
        actCity = findViewById(R.id.actCity);
        btnNextToUploadPic = findViewById(R.id.btnNextToUploadPic);

        String selectedCategoryName = getIntent().getStringExtra("selected_category");
        if (selectedCategoryName != null) {
            selectedCategory = DonationCategory.fromString(selectedCategoryName);
            imgCategory.setImageResource(selectedCategory.getIconResId());
            tvCategoryName.setText(selectedCategory.getHebrewName());
        }

        setupCityDropdown();
        btnNextToUploadPic.setOnClickListener(v -> submitDonation());
    }

    private void setupCityDropdown() {

        String[] cities = getResources().getStringArray(R.array.israel_cities);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        cities);

        actCity.setAdapter(adapter);
    }

    private void submitDonation() {

        String donationName = etDonationName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String city = actCity.getText().toString().trim();

        if (!checkInput(donationName, description, city)) {
            return;
        }

        createDonation(donationName, description, city);
    }

    private boolean checkInput(String donationName, String description, String city) {

        if (!Validator.isDonationNameValid(donationName)) {
            etDonationName.setError("נא להזין שם לתרומה");
            etDonationName.requestFocus();
            return false;
        }

        if (!Validator.isDescriptionValid(description)) {
            etDescription.setError("נא להוסיף תיאור מפורט יותר (לפחות 20 תווים)");
            etDescription.requestFocus();
            return false;
        }

        if (!Validator.isCityValid(city) || !Validator.isCityInList(this, city)) {
            actCity.setError("נא לבחור עיר מהרשימה");
            actCity.requestFocus();
            return false;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "לא נבחרה קטגוריה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createDonation(String name, String description, String city) {

        String donationId = databaseService.getDonationService().generateId();

        // שליפת המשתמש המחובר
        String currentUserID = SharedPreferencesUtil.getUserId(this);

        Donation donation = new Donation(
                donationId,
                name,
                description,
                selectedCategory, // enum אמיתי
                Donation.DonationStatus.AVAILABLE, // סטטוס התחלתי
                "", // photoUrl ריק בשלב הזה
                city,
                currentUserID, // giver
                null // receiver בהתחלה אין
        );

        databaseService.getDonationService().create(donation,
                new DatabaseService.DatabaseCallback<Void>() {

                    @Override
                    public void onCompleted(Void object) {

                        Toast.makeText(AddDonationStep2Activity.this,
                                "התרומה פורסמה בהצלחה!",
                                Toast.LENGTH_SHORT).show();

                        finish(); // חזרה למסך קודם
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(AddDonationStep2Activity.this,
                                "שגיאה בפרסום התרומה",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}