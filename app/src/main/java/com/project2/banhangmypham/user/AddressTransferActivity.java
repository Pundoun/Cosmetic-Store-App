package com.project2.banhangmypham.user;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.project2.banhangmypham.adapter.AddressTransferAdapter;
import com.project2.banhangmypham.databinding.ActivityAddressTransferBinding;
import com.project2.banhangmypham.databinding.DialogAddAddressBinding;
import com.project2.banhangmypham.localstorage.LocalStorageManager;
import com.project2.banhangmypham.model.AddressTransfer;
import com.project2.banhangmypham.repository.address_transfer.AddressTransferRepository;
import com.project2.banhangmypham.viewmodel.address.AddressViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddressTransferActivity extends AppCompatActivity implements AddressTransferAdapter.IAddressTransfer {

    private static final String TAG = "AddressTransferActivity";
    ActivityAddressTransferBinding binding;
    AddressTransferAdapter addressTransferAdapter;
    List<AddressTransfer> addressTransferList = new ArrayList<>();
    AddressViewModel addressViewModel;
    String userId;
    AddressTransfer addressTransferCurrent;
    int indexSelected = 0;
    private LocalStorageManager localStorageManager;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @SuppressLint({"CheckResult", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddressTransferBinding.inflate(getLayoutInflater());
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        addressViewModel.setAddressTransferRepository(new AddressTransferRepository());
        localStorageManager = LocalStorageManager.getInstance();
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(view -> finish());
        userId = getIntent().getStringExtra("userId");
        addressTransferAdapter = new AddressTransferAdapter(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Subscribe to saved index
        compositeDisposable.add(
            localStorageManager.indexData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    data -> {
                        if (data != null && data != -1) {
                            addressTransferAdapter.setSelectedIndex(data);
                            indexSelected = data;
                            addressTransferAdapter.notifyDataSetChanged();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error getting saved index: " + error.getMessage());
                        // Set default index if error occurs
                        addressTransferAdapter.setSelectedIndex(0);
                        indexSelected = 0;
                        addressTransferAdapter.notifyDataSetChanged();
                    }
                )
        );

        addressViewModel.getAllAddressTransferLiveData().observe(this, result -> {
            if (result != null && result.getAddressResponseList() != null) {
                addressTransferList.clear();
                addressTransferList.addAll(result.getAddressResponseList());
                if (!result.getAddressResponseList().isEmpty()) {
                    // Get saved index and set address
                    compositeDisposable.add(
                        localStorageManager.indexData
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                index -> {
                                    if (index != null && index != -1 && index < addressTransferList.size()) {
                                        CartProductActivity.setAddressTransfer(addressTransferList.get(index));
                                    } else {
                                        CartProductActivity.setAddressTransfer(addressTransferList.get(0));
                                    }
                                },
                                error -> {
                                    Log.e(TAG, "Error getting saved index for address: " + error.getMessage());
                                    // Set first address as default if error occurs
                                    CartProductActivity.setAddressTransfer(addressTransferList.get(0));
                                }
                            )
                    );
                }
                showInfoAddressTransferIfValid(result.getAddressResponseList());
            }
        });

        addressViewModel.getAddAddressTransferLiveData().observe(this, result -> {
            if (result != null) {
                Log.d(TAG, "addressViewModel: ===> result = " + result.getCode());
                if (result.getCode() == 200) {
                    runOnUiThread(() -> {
                        if (addressTransferCurrent != null) {
                            addressTransferList.add(addressTransferCurrent);
                            addressTransferAdapter.submitList(addressTransferList);
                        }
                    });
                }
            }
        });

        binding.rcvAddressTransfer.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvAddressTransfer.hasFixedSize();
        binding.rcvAddressTransfer.setAdapter(addressTransferAdapter);
        binding.btnAddAddress.setOnClickListener(view -> showDialog());
    }

    void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogAddAddressBinding binding1 = DialogAddAddressBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding1.getRoot());
        Window window = getWindow();
        if (window == null) return;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        window.setGravity(Gravity.BOTTOM);
        binding1.btnAdd.setOnClickListener(view -> {
            String name = binding1.edtName.getText().toString().trim();
            String phone = binding1.edtPhone.getText().toString().trim();
            String address = binding1.edtAddress.getText().toString().trim();
            
            if (!name.isEmpty() && !phone.isEmpty() && !address.isEmpty()) {
                AddressTransfer addressTransfer = new AddressTransfer(name, phone, address);
                addressTransfer.setUid(userId);
                addressViewModel.addAddressTransfer(addressTransfer);
                addressTransferCurrent = addressTransfer;
                dialog.dismiss();
            }
        });

        binding1.btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void showInfoAddressTransferIfValid(List<AddressTransfer> addressTransferList) {
        if (addressTransferList == null || addressTransferList.isEmpty()) {
            binding.tvError.setVisibility(View.VISIBLE);
            binding.rcvAddressTransfer.setVisibility(View.GONE);
        } else {
            binding.tvError.setVisibility(View.GONE);
            binding.rcvAddressTransfer.setVisibility(View.VISIBLE);
            addressTransferAdapter.submitList(addressTransferList);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClickItem(int position) {
        if (position >= 0 && position < addressTransferList.size()) {
            indexSelected = position;
            AddressTransfer selectedAddress = addressTransferList.get(position);
            if (selectedAddress != null) {
                CartProductActivity.setAddressTransfer(selectedAddress);
                addressTransferAdapter.setSelectedIndex(position);
                addressTransferAdapter.notifyDataSetChanged();
                
                // Save selected address and index
                String addressJson = new Gson().toJson(selectedAddress);
                localStorageManager.saveAddressTransfer(addressJson, position);
            }
        }
    }

    @Override
    protected void onResume() {
        if (userId != null && !userId.isEmpty()) {
            addressViewModel.getAllAddressTransferList(userId);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (!addressTransferList.isEmpty() && indexSelected >= 0 && indexSelected < addressTransferList.size()) {
            AddressTransfer selectedAddress = addressTransferList.get(indexSelected);
            if (selectedAddress != null) {
                CartProductActivity.setAddressTransfer(selectedAddress);
                // Save final state
                String addressJson = new Gson().toJson(selectedAddress);
                localStorageManager.saveAddressTransfer(addressJson, indexSelected);
            }
        }
        compositeDisposable.clear();
        super.onDestroy();
    }
}