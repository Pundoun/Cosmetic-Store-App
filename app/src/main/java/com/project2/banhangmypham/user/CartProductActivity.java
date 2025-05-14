package com.project2.banhangmypham.user;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paypal.android.cardpayments.CardClient;
import com.paypal.android.corepayments.CoreConfig;
import com.project2.banhangmypham.adapter.ProductCartAdapter;
import com.project2.banhangmypham.databinding.ActivityCartProductBinding;
import com.project2.banhangmypham.localstorage.LocalStorageManager;
import com.project2.banhangmypham.model.AddressTransfer;
import com.project2.banhangmypham.model.CartDeleteRequest;
import com.project2.banhangmypham.model.CartProductRequest;
import com.project2.banhangmypham.model.CreateOrder;
import com.project2.banhangmypham.model.DiscountModel;
import com.project2.banhangmypham.model.OrderRequest;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.repository.detail_product.DetailProductRepositoryImpl;
import com.project2.banhangmypham.utils.PaypalUtils;
import com.project2.banhangmypham.utils.Utils;
import com.project2.banhangmypham.viewmodel.product.DetailProductViewModel;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.project2.banhangmypham.utils.PaypalManager;
import com.google.gson.Gson;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class CartProductActivity extends AppCompatActivity implements ProductCartAdapter.ICartProductEventListener {

    public static final String TAG = "CartProductActivity";
    ActivityCartProductBinding binding ;
    DetailProductViewModel detailProductViewModel ;
    String userId = "";
    ProductCartAdapter productCartAdapter ;
    List<CartProductRequest> cartProductRequestList = new ArrayList<>();
    private static DiscountModel discountModel;
    private static AddressTransfer addressTransfer ;
    private static int methodPayment = -1;

    private long totalCart = 0;
    public static void setMethodPayment(int data){
        methodPayment = data ;
    }

    public static void setDiscountModel(DiscountModel data){
        discountModel = data ;
    }

    public static void setAddressTransfer(AddressTransfer data){
        addressTransfer = data;
    }

    private long amountMoneyWithoutDiscount = 0 ;
    private PaypalManager paypalManager;
    private String tokenPayment = "";
    private String orderIdPayment = "";
    private LocalStorageManager localStorageManager;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @SuppressLint({"SetTextI18n", "UnsafeIntentLaunch"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCartProductBinding.inflate(getLayoutInflater());
        productCartAdapter = new ProductCartAdapter(this, this);
        detailProductViewModel = new ViewModelProvider(this).get(DetailProductViewModel.class);
        detailProductViewModel.setIDetailProductRepository(new DetailProductRepositoryImpl());
        paypalManager = new PaypalManager();
        localStorageManager = LocalStorageManager.getInstance();
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        paypalManager.fetchAccessToken();
        paypalManager.getTokenLiveData().observe(this, result -> {
            tokenPayment = result;
        });

        paypalManager.getCreateOrderLiveData().observe(this, result -> {
            if (result != null && !result.isEmpty()) {
                orderIdPayment = result;
                paypalManager.handleOrderID(CartProductActivity.this, result);
            }
        });

        paypalManager.getHandleOrderLiveData().observe(this, result -> {
            if (result != null && result) {
                paypalManager.captureOrder(orderIdPayment, tokenPayment);
            }
        });

        paypalManager.getHandleCaptureLiveData().observe(this, result -> {
            if (result != null && result) {

            }
        });
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        detailProductViewModel.getAllCartProductList(userId);
        detailProductViewModel.getListProductLiveData().observe(this, result -> {
            if (result != null && result.getData() != null){
                cartProductRequestList.clear();
                result.getData().getCartProductRequestList().forEach(it -> it.setUserId(userId));
                cartProductRequestList.addAll(result.getData().getCartProductRequestList());
                showInfoProductIfValid(cartProductRequestList);
                binding.tvTotalMoney.setText(Utils.convertToMoneyVN(result.getData().getTotalCart()));
                totalCart = result.getData().getTotalCart();
                binding.tvNumberProductForPrice.setText("("+cartProductRequestList.size() + " sản phẩm)");
                binding.tvPriceTotal.setText(Utils.convertToMoneyVN(result.getData().getTotalCart()));
                amountMoneyWithoutDiscount = totalCart;
            }
        });

        detailProductViewModel.getCreateOrderLiveData().observe(this, result -> {
            Log.d(TAG, "onCreate: result = " + result);
            if (result != null) {
                if (result.getCode() == 200){
                    Toast.makeText(this, "methodPayment = "+ methodPayment, Toast.LENGTH_SHORT).show();

                    if (methodPayment == 0) {
                        cartProductRequestList.clear();
                        productCartAdapter.submitList(cartProductRequestList);
                        binding.tvTotalMoney.setText(Utils.convertToMoneyVN(0));
                        binding.tvPriceTotal.setText(Utils.convertToMoneyVN(0));
                        binding.tvDiscountProduct.setText(Utils.convertToMoneyVN(0));
                        binding.tvTotalMoney.setText(Utils.convertToMoneyVN(0));

//                    finish();
                    }else if (methodPayment == 2) {
                        cartProductRequestList.clear();
                        productCartAdapter.submitList(cartProductRequestList);
                        binding.tvTotalMoney.setText(Utils.convertToMoneyVN(0));
                        binding.tvPriceTotal.setText(Utils.convertToMoneyVN(0));
                        binding.tvDiscountProduct.setText(Utils.convertToMoneyVN(0));
                        binding.tvTotalMoney.setText(Utils.convertToMoneyVN(0));
                    }
                }
            }
        });
        binding.rcvCart.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvCart.hasFixedSize();
        binding.rcvCart.setAdapter(productCartAdapter);
        binding.btnAddToCart.setOnClickListener(view ->{
            // go to home
            finish();
        });

        binding.ivBack.setOnClickListener(view -> finish());
        binding.btnPaymentScreen.setOnClickListener(view -> {
            // go to payment methoid
            Intent intentMethod = new Intent(this, MethodPaymentActivity.class);
            startActivity(intentMethod);
        });

        binding.btnGoToAddress.setOnClickListener(view -> {
            Intent intentAddress = new Intent(CartProductActivity.this, AddressTransferActivity.class);
            intentAddress.putExtra("userId", userId);
            startActivity(intentAddress);
        });
        binding.btnGoToDiscount.setOnClickListener(view -> {
            // go to discount screen
            Intent intentDiscount = new Intent(CartProductActivity.this, DiscountProductActivity.class);
            intentDiscount.putExtra("userId", userId);
            startActivity(intentDiscount);
        });
        binding.btnOrder.setOnClickListener(view -> {
            if (methodPayment == 0) {
                OrderRequest orderRequest = new OrderRequest(userId, cartProductRequestList, methodPayment, addressTransfer, discountModel, amountMoneyWithoutDiscount);
                detailProductViewModel.createOrderProductList(orderRequest);
            } else if (methodPayment == 2) {
                if (!tokenPayment.isEmpty()) {
                    paypalManager.createOrder(this, tokenPayment, totalCart);
                } else {
                    Toast.makeText(this, "Token payment is empty", Toast.LENGTH_SHORT).show();
                    paypalManager.fetchAccessToken();
                }
            }
        });

        // Load cached address
        compositeDisposable.add(
            localStorageManager.transferData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    addressJson -> {
                        if (addressJson != null && !addressJson.isEmpty()) {
                            try {
                                AddressTransfer cachedAddress = new Gson().fromJson(addressJson, AddressTransfer.class);
                                if (cachedAddress != null) {
                                    addressTransfer = cachedAddress;
                                    binding.tvNameAddress.setText(cachedAddress.getAddressReceiver());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing cached address: " + e.getMessage());
                            }
                        }
                    },
                    error -> Log.e(TAG, "Error loading cached address: " + error.getMessage())
                )
        );
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onIncrease(int number, int position) {
        productCartAdapter.notifyItemChanged(position);
        String data = Utils.convertToMoneyVN(productCartAdapter.getTotalMoney());
        binding.tvTotalMoney.setText(data);
        binding.tvPriceTotal.setText(data);
        totalCart = productCartAdapter.getTotalMoney();
        amountMoneyWithoutDiscount = totalCart;

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDecrease(int number, int position) {
        productCartAdapter.notifyItemChanged(position);
        String data = Utils.convertToMoneyVN(productCartAdapter.getTotalMoney());
        binding.tvTotalMoney.setText(data);
        binding.tvPriceTotal.setText(data);
        totalCart = productCartAdapter.getTotalMoney();
        amountMoneyWithoutDiscount = totalCart;

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDelete(Product product, int position) {
        Log.d(TAG, "onDelete: ====> before = " + cartProductRequestList.size());
        cartProductRequestList.removeIf(it -> it.getProduct().get_id().equals(product.get_id()));
        Log.d(TAG, "onDelete: ====> after = " + cartProductRequestList.size());
        productCartAdapter.submitList(cartProductRequestList);
        showInfoProductIfValid(cartProductRequestList);
        String data = Utils.convertToMoneyVN(productCartAdapter.getTotalMoney());
        binding.tvTotalMoney.setText(data);
        binding.tvPriceTotal.setText(data);
        totalCart = productCartAdapter.getTotalMoney();
        binding.tvNumberProductForPrice.setText("("+cartProductRequestList.size() + " sản phẩm)");
        amountMoneyWithoutDiscount = totalCart;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        if (discountModel != null){
            binding.tvNameDiscount.setText(discountModel.getTitleDiscount());
        }
        if (addressTransfer != null) {
            binding.tvNameAddress.setText(addressTransfer.getAddressReceiver());
        }
        if (methodPayment != -1 ) {
            switch (methodPayment){
                case 0 : {
                    binding.tvNamePayment.setText("Thanh toan tien mat");
                    break;
                }
                case 1 : {
                    binding.tvNamePayment.setText("Credit or debit card");
                    break;
                }
                case 2 : {
                    binding.tvNamePayment.setText("Chuyen khoan ngan hang");
                    break;
                }
                case 3 : {
                    binding.tvNamePayment.setText("ZaloPay");
                    break;
                }
            }
        }
        if (discountModel != null) {
            long moneyDiscount = totalCart * discountModel.getValue()/100;
            binding.tvContentDiscount.setText(discountModel.getSubTitleDiscount());
            binding.tvDiscountProduct.setText("-"+ Utils.convertToMoneyVN(moneyDiscount));
            binding.tvTotalMoney.setText(Utils.convertToMoneyVN(totalCart - moneyDiscount) );
        }
    }
    private void showInfoProductIfValid(List<CartProductRequest> dataProductList) {
        if (dataProductList.isEmpty()) {
            binding.tvError.setVisibility(View.VISIBLE);
            binding.rcvCart.setVisibility(View.GONE);
        }else {
            binding.tvError.setVisibility(View.GONE);
            binding.rcvCart.setVisibility(View.VISIBLE);
            productCartAdapter.submitList(dataProductList);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        detailProductViewModel.updateAllCartProductList(new CartDeleteRequest(userId, cartProductRequestList));
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ====> intent result");
        Log.d(TAG, "onNewIntent: intent = " + intent.getData());
        if (intent.getData() != null && Objects.equals(intent.getData().getQueryParameter("opType"), "payment")) {
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
            OrderRequest orderRequest = new OrderRequest(userId, cartProductRequestList, methodPayment, addressTransfer, discountModel, amountMoneyWithoutDiscount);
            detailProductViewModel.createOrderProductList(orderRequest);
        } else if (intent.getData() != null && Objects.equals(intent.getData().getQueryParameter("opType"), "cancel")) {
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (paypalManager != null) {
            paypalManager.release();
        }
        detailProductViewModel.updateAllCartProductList(new CartDeleteRequest(userId, cartProductRequestList));
    }
}