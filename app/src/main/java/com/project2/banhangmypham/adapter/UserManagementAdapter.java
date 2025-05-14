package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project2.banhangmypham.R;
import com.project2.banhangmypham.databinding.ItemProductBinding;
import com.project2.banhangmypham.databinding.ItemUserBinding;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.model.User;
import com.project2.banhangmypham.utils.Utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    public static final String HOST = "http://172.16.3.8:5000/";

    private Context mContext ;
    private List<User> usersList = new ArrayList<>();

    private IUserEventItem iUserEventItem;

    public UserManagementAdapter(Context context, IUserEventItem data){
        mContext = context;
        iUserEventItem = data;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.setData(user,iUserEventItem,mContext);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public interface IUserEventItem{
        void onClickItem(User user);
        void onDeleteItem(User user);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<User> data){
        usersList.clear();
        usersList.addAll(data);
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;
        public UserViewHolder(ItemUserBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        @SuppressLint("SetTextI18n")
        public void setData(User user, IUserEventItem iOnItemEvent, Context mContext) {
//            Bitmap bitmap ;
//            byte[] dataImage = Base64.getDecoder().decode(user.getProfile_image());
//            bitmap = Utils.convertFromByteArrayToBitMap(dataImage);
            if (user.getProfile_image().startsWith("http") || user.getProfile_image().startsWith("https")){
//                    Glide.with(mContext).load(product.getThumbnail()).into(binding.ivProduct);
                Glide.with(mContext).load(user.getProfile_image()).error(R.drawable.noimage).into(binding.ivUser);
            }else {
//                    Glide.with(mContext).load(HOST+product.getThumbnail()).into(binding.ivProduct);
                Glide.with(mContext).load(HOST+user.getProfile_image())
                        .placeholder(R.drawable.noimage).into(binding.ivUser);
            }

            binding.tvUsername.setText(user.getUsername());
            binding.tvPhone.setText(user.getPhone());
            binding.tvAddress.setText(user.getAddress());
            binding.cardItem.setOnClickListener(view -> iOnItemEvent.onClickItem(user));
            binding.cardItem.setOnLongClickListener(view -> {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                        .setTitle("Thông báo")
                        .setMessage("Bạn có muốn xóa tài khoản "+ user.getFull_name())
                        .setNegativeButton("Có", (dialog, which) -> {
                            iOnItemEvent.onDeleteItem(user) ;
                            dialog.dismiss();
                        })
                        .setPositiveButton("Không",(dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create();
                alertDialog.show();
                return true;
            });
        }
    }
}
