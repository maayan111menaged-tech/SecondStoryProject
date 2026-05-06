package com.example.secondstoryproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Message;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT        = 1;
    private static final int VIEW_TYPE_RECEIVED    = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;
    private static final int VIEW_TYPE_SYSTEM      = 4;

    private static class ListItem {
        boolean isHeader;
        String headerText;
        Message message;

        static ListItem header(String text) {
            ListItem item = new ListItem();
            item.isHeader = true;
            item.headerText = text;
            return item;
        }

        static ListItem message(Message msg) {
            ListItem item = new ListItem();
            item.isHeader = false;
            item.message = msg;
            return item;
        }
    }

    public interface OnRateListener {
        void onRate(int stars, String comment);
    }

    private OnRateListener onRateListener;
    private String donationGiverId;
    private String donationId;

    public void setOnRateListener(OnRateListener listener) {
        this.onRateListener = listener;
    }

    public void setDonationGiverId(String giverId) {
        this.donationGiverId = giverId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    private final String currentUserId;
    private final boolean currentUserIsAdmin;
    private List<ListItem> items = new ArrayList<>();

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("d בMMMM", new Locale("he"));

    public MessageAdapter(String currentUserId, boolean currentUserIsAdmin) {
        this.currentUserId = currentUserId;
        this.currentUserIsAdmin = currentUserIsAdmin;
    }

    public void setMessages(List<Message> messages) {
        items = buildItemList(messages);
        notifyDataSetChanged();
    }

    private List<ListItem> buildItemList(List<Message> messages) {
        List<ListItem> result = new ArrayList<>();
        String lastDate = null;

        for (Message msg : messages) {
            String msgDate = dateFormat.format(new Date(msg.getTimestamp()));
            if (!msgDate.equals(lastDate)) {
                result.add(ListItem.header(msgDate));
                lastDate = msgDate;
            }
            result.add(ListItem.message(msg));
        }
        return result;
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = items.get(position);
        if (item.isHeader) return VIEW_TYPE_DATE_HEADER;

        if ("system".equals(item.message.getSenderId())) return VIEW_TYPE_SYSTEM;

        boolean isSent;
        if (currentUserIsAdmin) {
            isSent = item.message.isAdminSender();
        } else {
            isSent = item.message.getSenderId().equals(currentUserId);
        }

        return isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = inflater.inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_SYSTEM) {
            View view = inflater.inflate(R.layout.item_message_system, parent, false);
            return new SystemMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);

        if (item.isHeader) {
            ((DateHeaderViewHolder) holder).tvDate.setText(item.headerText);
            return;
        }

        if (holder instanceof SystemMessageViewHolder) {
            SystemMessageViewHolder vh = (SystemMessageViewHolder) holder;
            vh.tvText.setText(item.message.getText());

            // ✅ קודם כל איפוס מצב – חשוב למניעת מחזור שגוי
            vh.btnRate.setVisibility(View.GONE);
            vh.tvRated.setVisibility(View.GONE);
            vh.btnRate.setOnClickListener(null);

            if (item.message.isRatingRequest()) {
                boolean isReceiver = donationGiverId == null
                        || !currentUserId.equals(donationGiverId);

                if (isReceiver) {
                    // ✅ שמור reference לפוזיציה הנוכחית למניעת recycling bugs
                    final int currentPosition = position;

                    DatabaseService.getInstance().getRateService()
                            .hasRated(currentUserId, donationId,
                                    new IDatabaseService.DatabaseCallback<Boolean>() {
                                        @Override
                                        public void onCompleted(Boolean hasRated) {
                                            // ✅ בדוק שה-ViewHolder עדיין שייך לאותה פוזיציה
                                            if (vh.getAdapterPosition() != currentPosition) return;

                                            vh.btnRate.post(() -> {
                                                if (hasRated) {
                                                    vh.btnRate.setVisibility(View.GONE);
                                                    vh.tvRated.setVisibility(View.VISIBLE);
                                                } else {
                                                    vh.btnRate.setVisibility(View.VISIBLE);
                                                    vh.tvRated.setVisibility(View.GONE);
                                                    // ✅ ה-listener מוגדר כאן, לא בתוך async callback
                                                    vh.btnRate.setOnClickListener(v ->
                                                            showRatingDialog(v.getContext()));
                                                }
                                            });
                                        }
                                        @Override
                                        public void onFailed(Exception e) {
                                            if (vh.getAdapterPosition() != currentPosition) return;
                                            vh.btnRate.post(() -> {
                                                vh.btnRate.setVisibility(View.VISIBLE);
                                                vh.btnRate.setOnClickListener(v ->
                                                        showRatingDialog(v.getContext()));
                                            });
                                        }
                                    });
                }
            }
            return;
        }

        if (holder instanceof MessageViewHolder) {
            MessageViewHolder vh = (MessageViewHolder) holder;
            vh.tvText.setText(item.message.getText());
            vh.tvTime.setText(timeFormat.format(new Date(item.message.getTimestamp())));
        }
    }

    private void showRatingDialog(Context context) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_rate, null);

        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        EditText etComment  = dialogView.findViewById(R.id.et_comment);

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("דרג את התורם")
                .setView(dialogView)
                .setPositiveButton("שלח דירוג", (d, w) -> {
                    int stars      = (int) ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();
                    if (onRateListener != null) {
                        onRateListener.onRate(stars, comment);
                    }
                })
                .setNegativeButton("אחר כך", null)
                .show();
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_message_text);
            tvTime = itemView.findViewById(R.id.tv_message_time);
        }
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date_header);
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvRated;
        MaterialButton btnRate;
        SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText  = itemView.findViewById(R.id.tv_system_message);
            btnRate = itemView.findViewById(R.id.btn_rate);
            tvRated = itemView.findViewById(R.id.tv_already_rated);
        }
    }
}