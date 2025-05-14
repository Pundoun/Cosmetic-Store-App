package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project2.banhangmypham.R;
import com.project2.banhangmypham.databinding.ItemMessageLeftBinding;
import com.project2.banhangmypham.databinding.ItemMessageSenderBinding;
import com.project2.banhangmypham.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String HOST = "http://172.16.3.8:5000/";
    private final List<Message> messageList = new ArrayList<>();
    private String currentUserId = "";
    public static final int MYSELF = 0 ;
    public static final int OTHER = 1;
    private  Context mContext;
    public MessageAdapter(Context context){
        mContext = context ;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getIdSender().equals(currentUserId)) return MYSELF;
        else return OTHER;
    }
    public void setCurrentUserId(String data) {
        this.currentUserId = data;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Message> data){
        messageList.clear();
        messageList.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == OTHER) {
            ItemMessageLeftBinding bindingLeft = ItemMessageLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MessageReceiverViewHolder(bindingLeft);
        }
        ItemMessageSenderBinding bindingDefault = ItemMessageSenderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageSenderViewHolder(bindingDefault);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageReceiverViewHolder) {
            ((MessageReceiverViewHolder) holder).setData(messageList.get(position), mContext);
        }
        if (holder instanceof MessageSenderViewHolder) {
            ((MessageSenderViewHolder) holder).setData(messageList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageSenderViewHolder extends RecyclerView.ViewHolder{
        ItemMessageSenderBinding binding ;
        public MessageSenderViewHolder(@NonNull ItemMessageSenderBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
        public void setData(Message message) {
            binding.tvMessage.setText(message.getMessage());
        }
    }

    public static class MessageReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemMessageLeftBinding binding ;
        public MessageReceiverViewHolder(@NonNull ItemMessageLeftBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
        public void setData(Message message, Context context) {
            binding.tvMessage.setText(message.getMessage());
            if (message.getUserUrlImage().startsWith("http") || message.getUserUrlImage().startsWith("https")){
//                    Glide.with(mContext).load(product.getThumbnail()).into(binding.ivProduct);
                Glide.with(context).load(message.getUserUrlImage()).error(R.drawable.noimage).into(binding.ivAvatar);
            }else {
//                    Glide.with(mContext).load(HOST+product.getThumbnail()).into(binding.ivProduct);
                Glide.with(context).load(HOST+message.getUserUrlImage())
                        .placeholder(R.drawable.noimage).into(binding.ivAvatar);
            }
        }
    }
}
