package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.DonationCategory;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.Validator;

// Step 2 of adding a donation — collects name, description and city
// Receives the selected category from PickCategoryActivity via Intent
public class AddDonationStep2Activity extends BaseActivity {

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

        imgCategory = findViewById(R.id.imgCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        etDonationName = findViewById(R.id.etDonationName);
        etDescription = findViewById(R.id.etDescription);
        actCity = findViewById(R.id.actCity);
        btnNextToUploadPic = findViewById(R.id.btnNextToUploadPic);

        // receives the category chosen in the previous screen and displays its icon and name
        String selectedCategoryNameFromIntent = getIntent().getStringExtra("selected_category");
        if (selectedCategoryNameFromIntent != null) {
            // converts the String back to the matching enum value
            selectedCategory = DonationCategory.fromString(selectedCategoryNameFromIntent);
            imgCategory.setImageResource(selectedCategory.getIconResId());
            tvCategoryName.setText(selectedCategory.getHebrewName());
        }

        setupCityDropdown();

        btnNextToUploadPic.setOnClickListener(v -> submitDonation());
    }

    // populates the city dropdown with all Israeli cities
    private void setupCityDropdown() {
        String[] cities = IsraelCity.getHebrewNames();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        cities);
        actCity.setAdapter(adapter);
    }

    // validates the fields and passes all collected data to step 3
    private void submitDonation() {
        String donationName = etDonationName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String city = actCity.getText().toString().trim();

        if (!checkInput(donationName, description, city)) {
            return;
        }

        // passes all data to the next step as Intent extras
        Intent intent = new Intent(AddDonationStep2Activity.this, AddDonationStep3Activity.class);
        intent.putExtra("donationName", donationName);
        intent.putExtra("description", description);
        intent.putExtra("city", city);
        // passes the enum name (e.g. "CLOTHES") so step 3 can reconstruct the category
        intent.putExtra("selected_category", selectedCategory.name());

        startActivity(intent);
    }

    // validates all fields — returns false and shows an error on the first invalid field
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

        // should never happen in normal flow, but guards against a null category
        if (selectedCategory == null) {
            Toast.makeText(this, "לא נבחרה קטגוריה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}