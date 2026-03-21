package com.example.secondstoryproject.adapters;

import com.example.secondstoryproject.models.Donation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.secondstoryproject.R;
import com.example.secondstoryproject.utils.ImageUtil;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    private List<Donation> donations;
    private OnDonationClickListener listener;

    public interface OnDonationClickListener {
        void onDonationClick(Donation donation);
    }

    public DonationAdapter(OnDonationClickListener listener) {
        this.listener = listener;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        holder.tvName.setText(donation.getName());
        holder.tvCategory.setText(donation.getCategory().getHebrewName());
        holder.imgCategory.setImageResource(donation.getCategory().getIconResId());
        if (donation.getPhotoUrl() != null && !donation.getPhotoUrl().isEmpty()) {
            holder.imgDonation.setImageBitmap(ImageUtil.fromBase64(donation.getPhotoUrl()));
        }

        // לחיצה על כל הכרטיס
        holder.itemView.setOnClickListener(v ->
                listener.onDonationClick(donation));

        // לחיצה על כפתור +
        holder.btnMoreInfo.setOnClickListener(v ->
                listener.onDonationClick(donation));


        // 👉 בעתיד: תמונה
        // Glide.with(holder.itemView.getContext())
        //      .load(donation.getPhotoUrl())
        //      .into(holder.imgDonation);
    }

    @Override
    public int getItemCount() {
        return donations == null ? 0 : donations.size();
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