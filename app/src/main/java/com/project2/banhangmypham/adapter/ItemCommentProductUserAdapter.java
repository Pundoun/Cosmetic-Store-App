package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.databinding.ItemCommentProductUserBinding;
import com.project2.banhangmypham.model.EvaluationRequestModel;

public class ItemCommentProductUserAdapter extends ListAdapter<EvaluationRequestModel, ItemCommentProductUserAdapter.CommentProductUserViewHolder> {
    public static final String TAG = "CommentAdminAdapter";

    public ItemCommentProductUserAdapter() {
        super(new DiffUtil.ItemCallback<EvaluationRequestModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull EvaluationRequestModel oldItem, @NonNull EvaluationRequestModel newItem) {
                return oldItem.getUser().get_id().equals(newItem.getUser().get_id());
            }

            @SuppressLint("DiffUtilEquals")
            @Override
            public boolean areContentsTheSame(@NonNull EvaluationRequestModel oldItem, @NonNull EvaluationRequestModel newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public CommentProductUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentProductUserBinding binding = ItemCommentProductUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentProductUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentProductUserViewHolder holder, int position) {
        EvaluationRequestModel data = getItem(position);
        if (data != null) {
            Log.d(TAG, "onBindViewHolder: ====> data = " + data);
            holder.setData(data);
        }
    }

    public static class CommentProductUserViewHolder extends RecyclerView.ViewHolder {
        ItemCommentProductUserBinding binding;

        public CommentProductUserViewHolder(ItemCommentProductUserBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        @SuppressLint("SetTextI18n")
        public void setData(EvaluationRequestModel data) {
            if (data == null) return;
            
            Log.d(TAG, "setData: ===> data= " + data);
            binding.tvRating.setText(String.valueOf(data.getRating()));
            if (data.getUser() != null) {
                binding.tvNameUser.setText(data.getUser().getUsername());
            }
            binding.tvMessage.setText(data.getMessage());
            binding.tvTimeComment.setText(data.getCreatedDate());
        }
    }
}
