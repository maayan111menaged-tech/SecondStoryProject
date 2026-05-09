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
import com.example.secondstoryproject.screens.DonationDetailActivity;
import com.example.secondstoryproject.utils.ImageUtil;

import java.util.List;

public class CarouselDonationAdapter extends RecyclerView.Adapter<CarouselDonationAdapter.ViewHolder> {

    private final Context context;
    private final List<Donation> donations;

    public CarouselDonationAdapter(Context context, List<Donation> donations) {
        this.context = context;
        this.donations = donations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_donation_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        holder.tvName.setText(donation.getName());
        holder.tvCity.setText(donation.getCity() != null ? donation.getCity() : "");

        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        } else {
            holder.imgDonation.setImageResource(R.drawable.ic_profile);
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
        TextView tvName, tvCity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDonation = itemView.findViewById(R.id.imgCarouselDonation);
            tvName      = itemView.findViewById(R.id.tvCarouselName);
            tvCity      = itemView.findViewById(R.id.tvCarouselCity);
        }
    }
}