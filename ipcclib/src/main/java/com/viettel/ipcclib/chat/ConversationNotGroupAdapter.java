package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.R;
import com.viettel.ipcclib.util.Utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ConversationNotGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  List<Message> messages;

  public ConversationNotGroupAdapter(List<Message> messages) {
    this.messages = messages;
  }

  public class SupporterViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;
    TextView mTimeTv;

    public SupporterViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
      mTimeTv = (TextView) itemView.findViewById(R.id.time_tv);
    }
  }

  public class CustomerViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;
    TextView mTimeTv;

    public CustomerViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
      mTimeTv = (TextView) itemView.findViewById(R.id.time_tv);
    }
  }

  public class NoticeVH extends RecyclerView.ViewHolder {
    TextView mNoticeTv;

    public NoticeVH(View itemView) {
      super(itemView);
      mNoticeTv = (TextView) itemView.findViewById(R.id.notice_tv);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == Message.Type.MINE.ordinal()) {
      return new CustomerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_each_message_owner, parent, false));
    } else if (viewType == Message.Type.OTHER.ordinal()) {
      return new SupporterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_each_message_supporter, parent, false));
    } else {
      return new NoticeVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_notice, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    int viewType = getItemViewType(position);
    Message message = messages.get(position);

    if (viewType == Message.Type.MINE.ordinal()) {
      ((CustomerViewHolder) holder).mContentTv.setText(message.getContent());
      ((CustomerViewHolder) holder).mTimeTv.setText(Utils.formatTime(message.getTime()));
    } else if (viewType == Message.Type.OTHER.ordinal()){
      ((SupporterViewHolder) holder).mContentTv.setText(messages.get(position).getContent());
      ((SupporterViewHolder) holder).mTimeTv.setText(Utils.formatTime(messages.get(position).getTime()));
    } else {
      ((NoticeVH) holder).mNoticeTv.setText(message.getContent());
    }
  }

  @Override
  public int getItemViewType(int position) {
    return messages.get(position).getType().ordinal();
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }
}
