package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project2.banhangmypham.R;
import com.project2.banhangmypham.databinding.ItemProductHotSellerBinding;
import com.project2.banhangmypham.model.ProductHotSeller;
import com.project2.banhangmypham.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProductHotSellerAdapter extends RecyclerView.Adapter<ProductHotSellerAdapter.ProductHotSellerViewHolder> {
    private static final String HOST = "http://172.16.3.8:5000/";
    public static final String TAG = "ProductHotSellerAdapter";
    private final List<ProductHotSeller> productHotSellerList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<ProductHotSeller> data){
        productHotSellerList.clear();
        productHotSellerList.addAll(data);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ProductHotSellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductHotSellerBinding binding = ItemProductHotSellerBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ProductHotSellerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHotSellerViewHolder holder, int position) {
        holder.setData(productHotSellerList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return productHotSellerList.size();
    }

    public static class ProductHotSellerViewHolder extends RecyclerView.ViewHolder {
        ItemProductHotSellerBinding binding ;
        public ProductHotSellerViewHolder(@NonNull ItemProductHotSellerBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void setData(ProductHotSeller data, int position){
            Log.d(TAG, "setData: ====> data = " + data.getProduct());
            if (data.getProduct().getThumbnail().startsWith("http") || data.getProduct().getThumbnail().startsWith("https")){
                Glide.with(itemView.getContext()).load(data.getProduct().getThumbnail()).placeholder(R.drawable.noimage).into(binding.ivProduct);
            }else {
                Glide.with(itemView.getContext()).load(HOST+data.getProduct().getThumbnail()).placeholder(R.drawable.noimage).into(binding.ivProduct);
            }
            binding.tvNameProduct.setText(data.getProduct().getName());
            binding.tvTypeProduct.setText(data.getProduct().getCategory().getName());
        }
    }
}
