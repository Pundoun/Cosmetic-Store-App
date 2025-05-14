package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project2.banhangmypham.R;
import com.project2.banhangmypham.databinding.ItemCategoryBinding;
import com.project2.banhangmypham.databinding.ItemProductBinding;
import com.project2.banhangmypham.model.Category;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.utils.Utils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ProductAdminViewHolder> {
    private static final String HOST = "http://172.16.3.8:5000/";
    private Context mContext;
    private final List<Product> productsList = new ArrayList<>();
    private IProductEvenListener callback ;
    public interface IProductEvenListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onClickItemProduct(Product product);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Product> data){
        productsList.clear();
        productsList.addAll(data);
        notifyDataSetChanged();
    }
    public ProductAdminAdapter(Context mContext , IProductEvenListener listener) {
        this.mContext = mContext;
        this.callback = listener ;
    }


    @NonNull
    @Override
    public ProductAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(mContext),parent,false);
        return new ProductAdminViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdminViewHolder holder, int position) {
            Product product = productsList.get(position);
            holder.setData(product, this.callback,mContext);
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public static class ProductAdminViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ProductAdminViewHolder(ItemProductBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        @SuppressLint("SetTextI18n")
        public void setData(Product product, IProductEvenListener iOnItemEvent, Context mContext) {
            if (product.getThumbnail().startsWith("http") || product.getThumbnail().startsWith("https")){
                Glide.with(mContext).load(product.getThumbnail()).into(binding.ivProduct);
            }else {
                Glide.with(mContext).load(HOST+product.getThumbnail()).placeholder(R.drawable.noimage).into(binding.ivProduct);
            }
            binding.tvNameProduct.setText(product.getName());
            binding.tvPriceProduct.setText(product.getPrice().toString());
            binding.tvNameCategory.setText(product.getCategory().getName());
            binding.tvFamous.setText("Nổi bật: "+ (product.isFamous() ? "Có" : "Không"));
            binding.btnDelete.setOnClickListener(view -> iOnItemEvent.onDelete(product));
            binding.btnEdit.setOnClickListener(view -> iOnItemEvent.onEdit(product));
            binding.cardview.setOnClickListener(view -> iOnItemEvent.onClickItemProduct(product));
        }
    }
}
