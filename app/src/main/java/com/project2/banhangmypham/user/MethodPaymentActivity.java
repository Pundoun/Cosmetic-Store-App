package com.project2.banhangmypham.user;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project2.banhangmypham.R;
import com.project2.banhangmypham.databinding.ActivityMethodPaymentBinding;

public class MethodPaymentActivity extends AppCompatActivity {

    ActivityMethodPaymentBinding binding ;
    int selectIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMethodPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.llBankTransfer.setOnClickListener(view ->{
            CartProductActivity.setMethodPayment(2);
            selectIndex = 2;
            finish();
        });
        binding.llCard.setOnClickListener(view -> {
            CartProductActivity.setMethodPayment(1);
            selectIndex = 1 ;
            finish();
        });
        binding.llCashPayment.setOnClickListener(view -> {
            CartProductActivity.setMethodPayment(0);
            selectIndex = 0 ;
            finish();

        });
        binding.llZaloPay.setOnClickListener(view -> {
            CartProductActivity.setMethodPayment(3);
            selectIndex = 3 ;
            finish();
        });

        binding.ivBack.setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartProductActivity.setMethodPayment(selectIndex);
    }
}