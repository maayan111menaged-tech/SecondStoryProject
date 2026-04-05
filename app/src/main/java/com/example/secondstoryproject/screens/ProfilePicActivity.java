package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.ImageSourceAdapter;
import com.example.secondstoryproject.models.ImageSourceOption;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class ProfilePicActivity extends BaseActivity {
    private User currentUser;

    private ImageView ivProfile;

    private ImageButton pic1, pic2, pic3, pic4, pic5, pic6, btnAddImage;
    private Button btnSubmit;

    private ActivityResultLauncher<Intent> selectImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;

    private Bitmap selectedBitmap;
    private boolean isBitmap = false;

    private ImageButton selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_pic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivProfile = findViewById(R.id.imgProfile);

        pic1 = findViewById(R.id.pic1);
        pic2 = findViewById(R.id.pic2);
        pic3 = findViewById(R.id.pic3);
        pic4 = findViewById(R.id.pic4);
        pic5 = findViewById(R.id.pic5);
        pic6 = findViewById(R.id.pic6);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        ImageUtil.requestPermission(this);

        setupPickers();
        setupClicks();

        // קבלת המשתמש מהSharedPreferences
        currentUser = SharedPreferencesUtil.getUser(this);

        // הצגת הפרטים בשדות
        if (currentUser != null ) {
            showUserProfilePic();
        }

    }

    private void showUserProfilePic() {

        String base64 = currentUser.getProfilePhoneUrl();

        if (base64 != null && !base64.isEmpty()) {
            ivProfile.setImageBitmap(ImageUtil.fromBase64(base64));
        }

        isBitmap = false;
        selectedBitmap = null;
    }
    private void showImageSourceDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_source, null);
        bottomSheetDialog.setContentView(view);

        ArrayList<ImageSourceOption> options = new ArrayList<>();
        options.add(new ImageSourceOption("Gallery", "Choose from gallery", R.drawable.gallery_thumbnail));
        options.add(new ImageSourceOption("Camera", "Take a photo", R.drawable.photo_camera));

        ListView listView = view.findViewById(R.id.list_view_image_sources);

        ImageSourceAdapter adapter = new ImageSourceAdapter(this, options, option -> {
            bottomSheetDialog.dismiss();

            if (option.getTitle().equals("Gallery")) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                selectImageLauncher.launch(intent);
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureImageLauncher.launch(intent);
            }
        });

        listView.setAdapter(adapter);
        bottomSheetDialog.show();
    }
    private void saveProfileImage() {

        if (currentUser == null) return;

        String base64 = ImageUtil.toBase64(ivProfile);

        currentUser.setProfilePhoneUrl(base64);

        DatabaseService.getInstance().getUserService().update(
                currentUser.getId(),
                oldUser -> currentUser,
                new IDatabaseService.DatabaseCallback<User>() {

                    @Override
                    public void onCompleted(User result) {

                        SharedPreferencesUtil.saveUser(ProfilePicActivity.this, currentUser);

                        Toast.makeText(ProfilePicActivity.this,
                                "Profile updated!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(ProfilePicActivity.this, UserProfileActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(ProfilePicActivity.this,
                                "Update failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void setupClicks() {

        ImageButton[] buttons = {pic1, pic2, pic3, pic4, pic5, pic6};

        View.OnClickListener listener = v -> {

            for (ImageButton btn : buttons) {
                btn.setBackgroundResource(R.drawable.profile_unselected);
            }

            ImageButton clicked = (ImageButton) v;
            clicked.setBackgroundResource(R.drawable.profile_selected);

            ivProfile.setImageDrawable(clicked.getDrawable());

            selectedButton = clicked;
            isBitmap = false;
            selectedBitmap = null;
        };

        for (ImageButton btn : buttons) {
            btn.setOnClickListener(listener);
        }

        btnAddImage.setOnClickListener(v -> {
            resetSelection();
            showImageSourceDialog();
        });
        btnSubmit.setOnClickListener(v -> saveProfileImage());
    }
    private void resetSelection() {
        if (selectedButton != null) {
            selectedButton.setBackgroundResource(R.drawable.profile_unselected);
            selectedButton = null;
        }
    }
    private void setupPickers() {

        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Uri uri = result.getData().getData();
                        ivProfile.setImageURI(uri);

                        isBitmap = false;
                        selectedBitmap = null;
                    }
                });

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        ivProfile.setImageBitmap(bitmap);

                        selectedBitmap = bitmap;
                        isBitmap = true;
                    }
                });
    }
}