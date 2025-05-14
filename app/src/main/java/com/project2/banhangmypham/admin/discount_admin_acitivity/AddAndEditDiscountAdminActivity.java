package com.project2.banhangmypham.admin.discount_admin_acitivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.project2.banhangmypham.admin.repository.DiscountRepositoryImpl;
import com.project2.banhangmypham.admin.viewmodel.DiscountViewModel;
import com.project2.banhangmypham.databinding.ActivityAddDiscountAdminBinding;
import com.project2.banhangmypham.model.DiscountModel;

import java.util.Objects;

public class AddAndEditDiscountAdminActivity extends AppCompatActivity {

    public static final String TAG = "AddAndEditDiscountAdminActivity";
    ActivityAddDiscountAdminBinding binding ;
    DiscountViewModel discountViewModel ;
    DiscountModel currentDiscountModel ;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddDiscountAdminBinding.inflate(getLayoutInflater());
        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);
        discountViewModel.setIDiscountRepository(new DiscountRepositoryImpl());

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        discountViewModel.getAddDiscountLiveData().observe(this, result ->{
            Log.d(TAG, "onCreate: ====> result = " + result.getMessage());
            if (result != null){
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        discountViewModel.getEditDiscountLiveData().observe(this, result ->{
            if (result != null){
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentDiscountModel = bundle.getParcelable("discount", DiscountModel.class);
            }else {
                currentDiscountModel = bundle.getParcelable("discount");
            }
        }

        if (currentDiscountModel != null) {
            binding.tvTitle.setText("Chỉnh sửa khuyến mại");
            binding.edtTitleDiscount.setText(currentDiscountModel.getTitleDiscount());
            binding.edtContentDiscount.setText(currentDiscountModel.getSubTitleDiscount());
            binding.edtValueDiscount.setText(currentDiscountModel.getValue() + "");
            binding.edtNumberDiscount.setText(currentDiscountModel.getQuantity() +"");
            binding.edtValueNumberDiscount.setText(currentDiscountModel.getCondition()+"");
        }else {
            binding.tvTitle.setText("Thêm mới khuyến mại");
        }

        binding.ivBack.setOnClickListener(view -> finish());
        boolean isAdd = getIntent().getBooleanExtra("isAdd", true);

        binding.btnAddDiscount.setOnClickListener(view -> {
            if (isAdd) {
                String title = binding.edtTitleDiscount.getText().toString().trim();
                String content = binding.edtContentDiscount.getText().toString().trim();
                int value = Integer.parseInt(binding.edtValueDiscount.getText().toString().trim());
                int numberDiscount = Integer.parseInt(binding.edtNumberDiscount.getText().toString().trim());
                int condition = Integer.parseInt(binding.edtValueNumberDiscount.getText().toString().trim());

                DiscountModel discountModel = new DiscountModel(title, content, value, condition, numberDiscount);
                discountViewModel.addDiscountModel(discountModel);
            }else {
                String title = binding.edtTitleDiscount.getText().toString().trim();
                String content = binding.edtContentDiscount.getText().toString().trim();
                int value = Integer.parseInt(binding.edtValueDiscount.getText().toString().trim());
                int numberDiscount = Integer.parseInt(binding.edtNumberDiscount.getText().toString().trim());
                int condition = Integer.parseInt(binding.edtValueNumberDiscount.getText().toString().trim());
                DiscountModel discountModel = new DiscountModel(title, content, value, condition, numberDiscount);
                discountModel.set_id(currentDiscountModel.get_id());
                discountViewModel.updateDiscountModel(discountModel);
            }
        });

    }
}