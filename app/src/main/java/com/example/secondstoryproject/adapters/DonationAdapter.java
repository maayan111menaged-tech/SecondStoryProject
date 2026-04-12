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
import java.util.Set;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.utils.ImageUtil;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

/**
 * RecyclerView Adapter for displaying donation items.
 * Supports:
 * - Displaying donation details (name, category, image)
 * - Marking user's own donations
 * - Filtering (search, category, city, status)
 * - Admin-specific filtering
 * Maintains two lists:
 * - fullDonationList: original data
 * - donationList: filtered data
 */
public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    /**
     * Listener for donation click events.
     */
    public interface OnDonationClickListener {
        void onDonationClick(Donation donation);
    }

    /**
     * Listener for filter result updates.
     */
    public interface OnFilterListener {
        void onFilterResult(int count);
    }

    /** Filtered list displayed in RecyclerView */
    private final List<Donation> donationList = new ArrayList<>();

    /** Full list used as source for filtering */
    private final List<Donation> fullDonationList = new ArrayList<>();

    /** Click listener */
    private OnDonationClickListener listener;

    /** Filter result listener */
    private OnFilterListener onFilterListener;

    /**
     * Constructor.
     * @param listener click listener for donation items
     */
    public DonationAdapter(OnDonationClickListener listener) {
        this.listener = listener;
    }

    /**
     * Sets filter result listener.
     */
    public void setOnFilterListener(OnFilterListener listener) {
        this.onFilterListener = listener;
    }

    // ---- שיטה ישנה - נשמרת לתאימות לאחור לעמוד האדמין ----
    /**
     * Sets donation list (used in admin screen).
     * @param donations list of donations
     */
    public void setDonations(List<Donation> donations) {
        fullDonationList.clear();
        fullDonationList.addAll(donations);
        donationList.clear();
        donationList.addAll(fullDonationList);
        notifyDataSetChanged();
    }

    /**
     * Sets donation list (used in search screen).
     *
     * @param donations list of donations
     */
    public void setDonationList(List<Donation> donations) {
        setDonations(donations);
    }

    /**
     * Filters donations for regular users.
     * @param query search query (by name)
     * @param categoryFilter selected category
     * @param cityFilter selected city
     */
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

    /**
     * Filters donations for admin users.
     * @param query search query
     * @param statusFilter set of selected statuses
     * @param categoryFilter selected category
     * @param cityFilter selected city
     */
    public void filterAdmin(String query, Set<DonationStatus> statusFilter,
                            DonationCategory categoryFilter, String cityFilter) {
        donationList.clear();
        for (Donation d : fullDonationList) {
            boolean matchesName = query == null || query.isEmpty()
                    || d.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesStatus = statusFilter == null || statusFilter.isEmpty()
                    || statusFilter.contains(d.getStatus());
            boolean matchesCategory = categoryFilter == null
                    || d.getCategory() == categoryFilter;
            boolean matchesCity = cityFilter == null || cityFilter.isEmpty()
                    || d.getCity().equals(cityFilter);
            if (matchesName && matchesStatus && matchesCategory && matchesCity) {
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

        // Set donation name
        holder.tvName.setText(donation.getName());

        // Set category name and icon
        holder.tvCategory.setText(donation.getCategory().getHebrewName());
        holder.imgCategory.setImageResource(donation.getCategory().getIconResId());

        // Load donation image if exists
        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        }

        // Show "Mine" badge if donation belongs to current user
        String currentUserId = SharedPreferencesUtil.getUserId(holder.itemView.getContext());
        if (donation.getGiverID().equals(currentUserId)) {
            holder.tvMineBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvMineBadge.setVisibility(View.GONE);
        }

        // Click listeners
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

    /**
     * ViewHolder for donation item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory , tvMineBadge;
        ImageView imgDonation, imgCategory;
        Button btnMoreInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_donation_name);
            tvCategory = itemView.findViewById(R.id.tv_donation_category2);
            imgDonation = itemView.findViewById(R.id.img_donation);
            imgCategory = itemView.findViewById(R.id.img_category);
            btnMoreInfo = itemView.findViewById(R.id.btn_more_info);
            tvMineBadge = itemView.findViewById(R.id.tv_mine_badge);
        }
    }
}