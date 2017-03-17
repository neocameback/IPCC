package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.R;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import me.himanshusoni.chatmessageview.ChatMessageView;


public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {
  List<Message> messages;

  public GroupMessageAdapter(List<Message> messages) {
    this.messages = messages;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    TextView mContentTv;
    TextView mTimeTv;
    ChatMessageView mChatView;

    public ViewHolder(View itemView) {
      super(itemView);
      mContentTv = (TextView) itemView.findViewById(R.id.item_one_message_content_tv);
      mTimeTv = (TextView) itemView.findViewById(R.id.item_one_message_time_tv);
      mChatView = (ChatMessageView) itemView.findViewById(R.id.item_each_message_bb);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_each_message, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Message message = messages.get(position);
    if (message.isOwner()) {
      holder.mChatView.setArrowGravity(ChatMessageView.ArrowGravity.END);
      holder.mChatView.setArrowPosition(ChatMessageView.ArrowPosition.RIGHT);
      holder.mContentTv.setTextColor(Color.WHITE);
      holder.mTimeTv.setTextColor(Color.WHITE);
      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
      layoutParams.setMargins(holder.itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.padding_owner_chat),
          holder.itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.padding_small),0,0);
      holder.mChatView.setLayoutParams(layoutParams);
      holder.mChatView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.message_owner
      ), ContextCompat.getColor(holder.itemView.getContext(), R.color.message_owner));
    } else {
      holder.mChatView.setArrowPosition(ChatMessageView.ArrowPosition.LEFT);
      holder.mChatView.setArrowGravity(ChatMessageView.ArrowGravity.START);
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
