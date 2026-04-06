package com.example.secondstoryproject.adapters;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationCategory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.secondstoryproject.R;
import com.example.secondstoryproject.utils.ImageUtil;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    public interface OnDonationClickListener {
        void onDonationClick(Donation donation);
    }

    public interface OnFilterListener {
        void onFilterResult(int count);
    }

    private final List<Donation> donationList = new ArrayList<>();
    private final List<Donation> fullDonationList = new ArrayList<>();
    private OnDonationClickListener listener;
    private OnFilterListener onFilterListener;

    public DonationAdapter(OnDonationClickListener listener) {
        this.listener = listener;
    }

    public void setOnFilterListener(OnFilterListener listener) {
        this.onFilterListener = listener;
    }

    // ---- שיטה ישנה - נשמרת לתאימות לאחור לעמוד האדמין ----
    public void setDonations(List<Donation> donations) {
        fullDonationList.clear();
        fullDonationList.addAll(donations);
        donationList.clear();
        donationList.addAll(fullDonationList);
        notifyDataSetChanged();
    }

    // ---- שיטה חדשה לעמוד החיפוש ----
    public void setDonationList(List<Donation> donations) {
        setDonations(donations);
    }

    public void filter(String query, DonationCategory categoryFilter, String cityFilter) {
        donationList.clear();
        for (Donation d : fullDonationList) {
            boolean matchesName = query == null || query.isEmpty()
                    || d.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesCategory = categoryFilter == null
                    || d.getCategory() == categoryFilter;
            boolean matchesCity = cityFilter == null || cityFilter.isEmpty()
                    || d.getCity().equals(cityFilter);

            if (matchesName && matchesCategory && matchesCity) {
                donationList.add(d);
            }
        }
        notifyDataSetChanged();

        if (onFilterListener != null) {
            onFilterListener.onFilterResult(donationList.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Donation donation = donationList.get(position);

        holder.tvName.setText(donation.getName());
        holder.tvCategory.setText(donation.getCategory().getHebrewName());
        holder.imgCategory.setImageResource(donation.getCategory().getIconResId());

        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDonationClick(donation);
        });

        holder.btnMoreInfo.setOnClickListener(v -> {
            if (listener != null) listener.onDonationClick(donation);
        });
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory;
        ImageView imgDonation, imgCategory;
        Button btnMoreInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_donation_name);
            tvCategory = itemView.findViewById(R.id.tv_donation_category2);
            imgDonation = itemView.findViewById(R.id.img_donation);
            imgCategory = itemView.findViewById(R.id.img_category);
            btnMoreInfo = itemView.findViewById(R.id.btn_more_info);
        }
    }
}