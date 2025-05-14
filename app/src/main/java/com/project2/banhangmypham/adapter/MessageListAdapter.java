package com.project2.banhangmypham.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.databinding.ItemMessageListBinding;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>{

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageListBinding binding = ItemMessageListBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ItemMessageListBinding binding ;
        public MessageViewHolder(@NonNull ItemMessageListBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void setData() {

        }
    }
}
