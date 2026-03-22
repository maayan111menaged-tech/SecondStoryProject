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

public class ProfileDonationAdapter extends RecyclerView.Adapter<ProfileDonationAdapter.ViewHolder> {

    private Context context;
    private List<Donation> donations;

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

        holder.tvName.setText(donation.getName());

        DonationStatus status = donation.getStatus();
        holder.tvStatus.setText(status.getHebrewName());
        holder.imgStatus.setImageResource(status.getIconResId());

        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        }

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