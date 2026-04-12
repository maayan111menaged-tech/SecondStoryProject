package com.example.secondstoryproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.screens.DonationDetailActivity;
import com.example.secondstoryproject.screens.MainActivity;
import com.example.secondstoryproject.utils.ImageUtil;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a user's donations in their profile.
 * Each item shows:
 * - Donation name
 * - Donation status (text + icon)
 * - Donation image (if available)
 * Clicking on an item opens the donation details screen.
 */
public class ProfileDonationAdapter extends RecyclerView.Adapter<ProfileDonationAdapter.ViewHolder> {

    /** Application context used for inflating views and navigation */
    private Context context;

    /** List of donations displayed in the profile */
    private List<Donation> donations;

    /**
     * Constructor.
     * @param context the application context
     * @param donations list of donations to display
     */
    public ProfileDonationAdapter(Context context, List<Donation> donations) {
        this.context = context;
        this.donations = donations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_donation_profile, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        // Set donation name
        holder.tvName.setText(donation.getName());

        // Set donation status (text + icon)
        DonationStatus status = donation.getStatus();
        holder.tvStatus.setText(status.getHebrewName());
        holder.imgStatus.setImageResource(status.getIconResId());

        // Load donation image from Base64 if available
        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        }

        // Open donation details on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    /**
     * ViewHolder for donation item in profile.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgDonation;
        ImageView imgStatus;
        TextView tvName;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgDonation = itemView.findViewById(R.id.imgDonation);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvName = itemView.findViewById(R.id.tvDonationName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}