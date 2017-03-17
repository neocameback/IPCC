package com.viettel.ipcclib.chat;

import com.viettel.ipcclib.R;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;


public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private final int TYPE_OWNER = 1;
  private final int TYPE_PARTNER = 0;
  List<GroupMessage> groupMessages;

  public ConversationAdapter(List<GroupMessage> groupMessages) {
    this.groupMessages = groupMessages;
  }

  public class ViewHolderOwner extends RecyclerView.ViewHolder {
    RecyclerView mGroupMessageRv;

    public ViewHolderOwner(View itemView) {
      super(itemView);
      mGroupMessageRv = (RecyclerView) itemView.findViewById(R.id.item_chat_group_owner_rv);
    }
  }

  public class ViewHolderPartner extends RecyclerView.ViewHolder {
    ImageView mAvatarImg;
    RecyclerView mGroupMessageRv;

    public ViewHolderPartner(View itemView) {
      super(itemView);
      mGroupMessageRv = (RecyclerView) itemView.findViewById(R.id.item_chat_group_message_rv);
      mAvatarImg = (ImageView) itemView.findViewById(R.id.item_chat_avatar_img);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_OWNER:
        return new ViewHolderOwner(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chat_owner, parent, false));
      case TYPE_PARTNER:
        return new ViewHolderPartner(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chat_partner, parent, false));
      default:
        return null;
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    GroupMessage groupMessage = groupMessages.get(position);
    switch (getItemViewType(position)) {
      case TYPE_OWNER:
        ((ViewHolderOwner) holder).mGroupMessageRv.setHasFixedSize(true);
        ((ViewHolderOwner) holder).mGroupMessageRv
            .setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        ((ViewHolderOwner) holder).mGroupMessageRv
            .setAdapter(new GroupMessageAdapter(groupMessage.getmMessages()));
        break;
      case TYPE_PARTNER:
        ((ViewHolderPartner) holder).mGroupMessageRv.setHasFixedSize(true);
        ((ViewHolderPartner) holder).mGroupMessageRv
            .setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        ((ViewHolderPartner) holder).mGroupMessageRv
            .setAdapter(new GroupMessageAdapter(groupMessage.getmMessages()));
        break;
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (groupMessages.get(position).isOwner())
      return TYPE_OWNER;//1 for owner
    return TYPE_PARTNER;
  }

  @Override
  public int getItemCount() {
    return groupMessages.size();
  }
}
