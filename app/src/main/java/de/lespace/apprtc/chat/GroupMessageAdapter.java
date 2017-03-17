package de.lespace.apprtc.chat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.lespace.apprtc.R;


public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {
  List<Message> messages;

  public GroupMessageAdapter(List<Message> messages) {
    this.messages = messages;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;
    TextView mTimeTv;

    public ViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
      mTimeTv = (TextView) itemView.findViewById(R.id.item_one_message_time_tv);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_each_message_owner, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Message message = messages.get(position);
    if (message.isOwner()) {
      holder.mContentTv.setTextColor(Color.WHITE);
      holder.mTimeTv.setTextColor(Color.WHITE);
      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
      layoutParams.setMargins(holder.itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.padding_owner_chat),
          holder.itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.padding_small), 0, 0);
    } else {
      holder.mContentTv.setTextColor(Color.BLACK);
      holder.mTimeTv.setTextColor(Color.BLACK);
    }
    holder.mContentTv.setText(message.getContent());
    holder.mTimeTv.setText(message.getTime());
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }
}
