package com.example.secondstoryproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    // פריט פנימי — הודעה או header תאריך
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

    private final String currentUserId;
    private List<ListItem> items = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("d בMMMM", new Locale("he"));

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> messages) {
        items = buildItemList(messages);
        notifyDataSetChanged();
    }

    // בונה רשימה עם headers של תאריך
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
        return item.message.getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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
        } else {
            MessageViewHolder vh = (MessageViewHolder) holder;
            vh.tvText.setText(item.message.getText());
            vh.tvTime.setText(timeFormat.format(new Date(item.message.getTimestamp())));
        }
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
}