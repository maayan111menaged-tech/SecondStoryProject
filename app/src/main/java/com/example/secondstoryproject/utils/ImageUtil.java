package com.example.secondstoryproject.utils;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * Utility class for image-related operations.
 * Provides methods for:
 * - Requesting runtime permissions (camera & storage)
 * - Converting images to Base64 strings
 * - Converting Base64 strings back to Bitmap images
 */
public class ImageUtil {

    /**
     * Requests runtime permissions required for image handling.
     * Includes camera access and media storage permissions.
     * @param activity the activity used to request permissions
     */
    public static void requestPermission(@NotNull Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE, // ignored from API 29+
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, // ignored from API 33+
                        Manifest.permission.READ_MEDIA_IMAGES // required for API 33+

                }, 1);
    }

    /**
     * Converts an image from an ImageView into a Base64 encoded string.
     * The image is compressed using JPEG format before encoding.
     * @param postImage the ImageView containing the image
     * @return Base64 string representation of the image, or null if no image exists
     */
    public static @Nullable String toBase64(@NotNull final ImageView postImage) {
        if (postImage.getDrawable() == null) {
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Compress the bitmap into JPEG format with 100% quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Converts a Base64 encoded string back into a Bitmap image.
     * @param base64Code the Base64 string to decode
     * @return the decoded Bitmap, or null if the input is empty
     */
    public static @Nullable Bitmap fromBase64(@NotNull final String base64Code) {
        if (base64Code.isEmpty()) {
            return null;
        }
        byte[] decodedString = Base64.decode(base64Code, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}