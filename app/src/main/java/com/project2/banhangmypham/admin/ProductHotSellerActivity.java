 package com.project2.banhangmypham.admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.R;
import com.project2.banhangmypham.adapter.GridSpacingItemDecoration;
import com.project2.banhangmypham.adapter.ProductHotSellerAdapter;
import com.project2.banhangmypham.admin.repository.IProductHotSellerImpl;
import com.project2.banhangmypham.admin.viewmodel.ProductHotSellerViewModel;
import com.project2.banhangmypham.databinding.ActivityProductHotSellerBinding;
import com.project2.banhangmypham.model.OrderRequestAdmin;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.model.ProductHotSeller;

import java.util.Calendar;
import java.util.List;

 public class ProductHotSellerActivity extends AppCompatActivity {

     public static final String TAG = "ProductHotSellerActivity";
    ActivityProductHotSellerBinding binding;
    ProductHotSellerViewModel productHotSellerViewModel ;
     DatePickerDialog datePickerDialogFrom ;
     DatePickerDialog datePickerDialogTo ;
     ProductHotSellerAdapter productHotSellerAdapter;
     Long timeFrom = 0L;
     Long timeTo = 0L ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductHotSellerBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        productHotSellerAdapter = new ProductHotSellerAdapter();
        productHotSellerViewModel = new ViewModelProvider(this).get(ProductHotSellerViewModel.class);
        productHotSellerViewModel.setIProductHotSeller(new IProductHotSellerImpl());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        binding.llTimeFrom.setOnClickListener(view -> {
            datePickerDialogFrom = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            binding.tvTimeFrom.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear , dayOfMonth);
                            timeFrom = calendar.getTimeInMillis();
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialogFrom.show();
        });

        binding.rcvOrder.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));

        productHotSellerViewModel.getProductSellerRespsonseLiveData().observe(this, result ->{
            if (result != null){
//                productHotSellerAdapter.submitList(result.getProductHotSellerList());
                result.getProductHotSellerList().forEach(it -> {
                    Log.d(TAG, "product = "+ it.getProduct());
                });
                showInfoOrderRequestIfValid(result.getProductHotSellerList());
            }
        });
        binding.llTimeTo.setOnClickListener(view -> {
            datePickerDialogTo =  new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            binding.tvTimeTo.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear , dayOfMonth);
                            timeTo = calendar.getTimeInMillis();
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialogTo.show();
        });
        binding.btnSearch.setOnClickListener(view -> {
            productHotSellerViewModel.getAllProductHotSeller(timeFrom, timeTo);
        });
        binding.ivBack.setOnClickListener(view -> finish());
        binding.rcvOrder.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rcvOrder.hasFixedSize();
        binding.rcvOrder.setAdapter(productHotSellerAdapter);
    }
     private void showInfoOrderRequestIfValid(List<ProductHotSeller> dataProductList) {
         if (dataProductList.isEmpty()) {
             binding.tvError.setVisibility(View.VISIBLE);
             binding.rcvOrder.setVisibility(View.GONE);
         }else {
             binding.tvError.setVisibility(View.GONE);
             binding.rcvOrder.setVisibility(View.VISIBLE);
             productHotSellerAdapter.submitList(dataProductList);
         }
     }
     @Override
     protected void onResume() {
         super.onResume();
         Calendar calendarFrom = Calendar.getInstance();
         calendarFrom.set(1990, 01 , 01);
         timeFrom = calendarFrom.getTimeInMillis();

         Calendar calendarTo = Calendar.getInstance();
         timeTo = calendarTo.getTimeInMillis();
         productHotSellerViewModel.getAllProductHotSeller(timeFrom, timeTo);
     }

     @Override
     protected void onStop() {
         super.onStop();
         productHotSellerViewModel.onClear();
     }
 }