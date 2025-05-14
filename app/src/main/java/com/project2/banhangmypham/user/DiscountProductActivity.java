package com.project2.banhangmypham.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.R;
import com.project2.banhangmypham.adapter.DiscountAdapter;
import com.project2.banhangmypham.databinding.ActivityDiscountProductBinding;
import com.project2.banhangmypham.model.DiscountModel;
import com.project2.banhangmypham.repository.discount.DiscountRepositoryImpl;
import com.project2.banhangmypham.viewmodel.discount.DiscountViewModel;

import java.util.ArrayList;
import java.util.List;

public class DiscountProductActivity extends AppCompatActivity implements DiscountAdapter.IDiscountEvenListener {

    private static final String TAG = "DiscountProductActivity";
    ActivityDiscountProductBinding binding;
    DiscountAdapter discountAdapter;
    DiscountViewModel discountViewModel;
    String userId = "";
    private List<DiscountModel> usedDiscounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDiscountProductBinding.inflate(getLayoutInflater());
        discountAdapter = new DiscountAdapter(this, this, 0,0);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");

        binding.ivBack.setOnClickListener(view -> finish());
        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);
        discountViewModel.setIDiscountRepository(new DiscountRepositoryImpl());
        discountViewModel.getAllDiscountsList(userId);

        discountViewModel.getDiscountResponseLiveData().observe(this, result -> {
            if (result != null && result.getDiscountModelList() != null) {
                // Filter out used discounts
                List<DiscountModel> availableDiscounts = new ArrayList<>();
                for (DiscountModel discount : result.getDiscountModelList()) {
                    if (!isDiscountUsed(discount)) {
                        availableDiscounts.add(discount);
                    }
                }
                discountAdapter.submitList(availableDiscounts);
                if (availableDiscounts.isEmpty()) {
                    Toast.makeText(this, "Không có mã khuyến mãi khả dụng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.rcvDiscount.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvDiscount.hasFixedSize();
        binding.rcvDiscount.setAdapter(discountAdapter);
    }

    private boolean isDiscountUsed(DiscountModel discount) {
        // Check if the discount is already in the used discounts list
        for (DiscountModel usedDiscount : usedDiscounts) {
            if (usedDiscount.get_id().equals(discount.get_id())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClickItemDiscount(DiscountModel discountModel) {
        if (!isDiscountUsed(discountModel)) {
            CartProductActivity.setDiscountModel(discountModel);
            usedDiscounts.add(discountModel);
            finish();
        } else {
            Toast.makeText(this, "Mã khuyến mãi này đã được sử dụng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDelete(DiscountModel discountModel) {
        // Remove from used discounts if needed
        usedDiscounts.removeIf(discount -> discount.get_id().equals(discountModel.get_id()));
    }
}