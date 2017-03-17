package de.lespace.apprtc.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.lespace.apprtc.R;


public class ConversationNotGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int TYPE_SUPPORTER = 1;
  private static final int TYPE_CUSTOMER = 2;
  List<Message> messages;

  public ConversationNotGroupAdapter(List<Message> messages) {
    this.messages = messages;
  }

  public class SupporterViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;

    public SupporterViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
    }
  }

  public class CustomerViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;

    public CustomerViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == TYPE_CUSTOMER)
      return new CustomerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_each_message_owner, parent, false));
    return new SupporterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_each_message_supporter, parent, false));
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) == TYPE_CUSTOMER) {
      ((CustomerViewHolder) holder).mContentTv.setText(messages.get(position).getContent());
    } else {
      ((SupporterViewHolder) holder).mContentTv.setText(messages.get(position).getContent());
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (messages.get(position).isOwner())
      return TYPE_CUSTOMER;
    return TYPE_SUPPORTER;
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }
}
