package com.example.secondstoryproject.screens;

import static android.opengl.ETC1.isValid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.ImageSourceAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationCategory;
import com.example.secondstoryproject.models.ImageSourceOption;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.example.secondstoryproject.utils.Validator;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class AddDonationStep3Activity extends BaseActivity implements View.OnClickListener {

    /// tag for logging
    private static final String TAG = "AddDonationStep3Activity";

    private Button addDonationButton;
    private ImageView DonationImageView;

    private String selectedDonationName;
    private String selectedDescription;
    private String selectedCity;
    private String selectedCategoryName;

    /// Activity result launcher for selecting image from gallery
    private ActivityResultLauncher<Intent> selectImageLauncher;
    /// Activity result launcher for capturing image from camera
    private ActivityResultLauncher<Intent> captureImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /// set the layout for the activity
        setContentView(R.layout.activity_add_donation_step3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// getting extra from AddDonationStep2Activity
        selectedDonationName = getIntent().getStringExtra("donationName");
        selectedDescription = getIntent().getStringExtra("description");
        selectedCity = getIntent().getStringExtra("city");
        selectedCategoryName = getIntent().getStringExtra("selected_category");




        /// request permission for the camera and storage
        ImageUtil.requestPermission(this);

        /// get the views
        addDonationButton = findViewById(R.id.add_donation_button);
        DonationImageView = findViewById(R.id.donation_image);

        /// set the tag for the image view
        /// to check if the image was changed from app:srcCompat="@drawable/image"
        DonationImageView.setTag(R.drawable.image);

        /// set the on click listeners
        DonationImageView.setOnClickListener(this);
        addDonationButton.setOnClickListener(this);

        /// register the activity result launcher for selecting image from gallery
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        DonationImageView.setImageURI(selectedImage);
                        /// set the tag for the image view to null
                        DonationImageView.setTag(null);
                    }
                });

        /// register the activity result launcher for capturing image from camera
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        DonationImageView.setImageBitmap(bitmap);
                        /// set the tag for the image view to null
                        DonationImageView.setTag(null);
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == addDonationButton.getId()) {
            Log.d(TAG, "Add donation button clicked");
            addDonationToDatabase();
            return;
        }
        if (v.getId() == DonationImageView.getId()) {
            Log.d(TAG, "Select image button clicked");
            showImageSourceDialog();
            return;
        }
    }

    /// show the image source dialog
    /// this dialog will show the options to select image from gallery or capture image from camera
    /// @see ImageSourceOption
    /// @see ImageSourceAdapter
    /// @see BottomSheetDialog
    private void showImageSourceDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_source, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        final ArrayList<ImageSourceOption> options = new ArrayList<>();
        options.add(new ImageSourceOption(getString(R.string.gallery_title), getString(R.string.gallery_description), R.drawable.gallery_thumbnail));
        options.add(new ImageSourceOption(getString(R.string.camera_title), getString(R.string.camera_description), R.drawable.photo_camera));

        ListView listView = bottomSheetView.findViewById(R.id.list_view_image_sources);
        ImageSourceAdapter adapter = new ImageSourceAdapter(this, options, option -> {
            bottomSheetDialog.dismiss();
            if (option.getTitle().equals(getString(R.string.gallery_title))) {
                selectImageFromGallery();
            } else if (option.getTitle().equals(getString(R.string.camera_title))) {
                captureImageFromCamera();
            }
        });
        listView.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    /// add the food to the database
    /// @see Donation
    private void addDonationToDatabase() {
        String donationId = databaseService.getDonationService().generateId();

        // שליפת המשתמש המחובר
        String currentUserID = SharedPreferencesUtil.getUserId(this);

        DonationCategory selectedCategory = DonationCategory.fromString(selectedCategoryName);

        if (!checkInput(DonationImageView)) return;

        String imageBase64 = ImageUtil.toBase64(DonationImageView);

        Donation donation = new Donation(
                donationId,
                selectedCategoryName,
                selectedDescription,
                selectedCategory,
                Donation.DonationStatus.AVAILABLE, // סטטוס התחלתי
                imageBase64,
                selectedCity,
                currentUserID, // giver
                null // receiver בהתחלה אין
        );

        databaseService.getDonationService().create(donation,
                new DatabaseService.DatabaseCallback<Void>() {

                    @Override
                    public void onCompleted(Void object) {

                        Toast.makeText(AddDonationStep3Activity.this,
                                "התרומה פורסמה בהצלחה!",
                                Toast.LENGTH_SHORT).show();

                        finish(); // חזרה למסך קודם
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(AddDonationStep3Activity.this,
                                "שגיאה בפרסום התרומה",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /// select image from gallery
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    /// capture image from camera
    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }


    /// validate the input
    private boolean checkInput(ImageView donationImageView) {
        if (donationImageView.getTag() != null) {
             Log.e(TAG, "Image is required");
             Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
             return false;
        }

        return true;
    }

}