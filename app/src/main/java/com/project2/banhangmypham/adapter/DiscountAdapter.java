package com.project2.banhangmypham.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.databinding.ItemDiscountProductBinding;
import com.project2.banhangmypham.model.DiscountModel;
import com.project2.banhangmypham.model.Product;

import java.util.ArrayList;
import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

    private Context mContext;
    private final List<DiscountModel> discountModelList = new ArrayList<>();
    private IDiscountEvenListener callback;
    private long totalMoney = 0;
    private int role = 0 ; // 0 là user , 1 la admin
    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiscountProductBinding binding = ItemDiscountProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DiscountViewHolder(binding);
    }

    public DiscountAdapter(IDiscountEvenListener callback, Context mContext, long totalMoney, int role ) {
        this.callback = callback;
        this.mContext = mContext;
        this.totalMoney = totalMoney;
        this.role = role;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<DiscountModel> data){
        discountModelList.clear();
        discountModelList.addAll(data);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        DiscountModel discountModel = discountModelList.get(position);
        holder.setData(discountModel,callback,totalMoney, role);
    }

    @Override
    public int getItemCount() {
        return discountModelList.size();
    }

    public interface IDiscountEvenListener {
        void onClickItemDiscount(DiscountModel discountModel);
        void onDelete(DiscountModel discountModel);
    }

    public static class DiscountViewHolder extends RecyclerView.ViewHolder {
        ItemDiscountProductBinding binding;

        public DiscountViewHolder(ItemDiscountProductBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        @SuppressLint("SetTextI18n")
        public void setData(DiscountModel discountModel, IDiscountEvenListener iOnItemEvent, Long total, int role ) {
            binding.titleDiscount.setText(discountModel.getTitleDiscount());
            binding.subTitleDiscount.setText(discountModel.getSubTitleDiscount());

            if (discountModel.isIs_used()) {
                // Đã sử dụng - màu xám và không cho click
                binding.titleDiscount.setTextColor(Color.GRAY);
                binding.subTitleDiscount.setTextColor(Color.GRAY);
                binding.cardItemDiscount.setEnabled(false);
                binding.cardItemDiscount.setAlpha(0.7f);
                binding.ivDelete.setVisibility(View.GONE);
            } else {
                // Chưa sử dụng - màu xanh lá và cho phép click
                binding.titleDiscount.setTextColor(Color.rgb(0, 128, 0)); // Màu xanh lá đậm
                binding.subTitleDiscount.setTextColor(Color.rgb(0, 128, 0));
                binding.cardItemDiscount.setEnabled(true);
                binding.cardItemDiscount.setAlpha(1.0f);
                binding.ivDelete.setVisibility(View.VISIBLE);
            }

            binding.cardItemDiscount.setOnClickListener(view -> {
                if (!discountModel.isIs_used()) {
                    iOnItemEvent.onClickItemDiscount(discountModel);
                }
            });
            if (role == 0) {
                binding.ivDelete.setVisibility(View.GONE);
            }else {
                binding.ivDelete.setVisibility(View.VISIBLE);

            }
            binding.ivDelete.setOnClickListener(view ->{
                AlertDialog alertDialog = new AlertDialog.Builder(view.getContext())
                        .setTitle("Thông báo")
                        .setMessage("Bạn có muốn xóa  "+ discountModel.getTitleDiscount())
                        .setNegativeButton("Có", (dialog, which) -> {
                            iOnItemEvent.onDelete(discountModel); ;
                            dialog.dismiss();
                        })
                        .setPositiveButton("Không",(dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create();
                alertDialog.show();

            });

        }
    }
}

