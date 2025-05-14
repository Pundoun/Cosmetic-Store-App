package com.project2.banhangmypham;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.user.CartProductActivity;
import com.project2.banhangmypham.user.DetailProductActivity;
import com.project2.banhangmypham.user.SearchActivity;
import com.project2.banhangmypham.adapter.CategoryUserAdapter;
import com.project2.banhangmypham.adapter.ProductUserAdapter;
import com.project2.banhangmypham.databinding.FragmentHomeBinding;
import com.project2.banhangmypham.model.Category;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.repository.home.HomeRepositoryImpl;
import com.project2.banhangmypham.viewmodel.HomeViewModel;
import com.project2.banhangmypham.viewmodel.account.AccountViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment implements View.OnClickListener,
        ProductUserAdapter.IProductEvenListener, CategoryUserAdapter.IOnItemEvent {
    List<Category> categoryList = new ArrayList<>();
    List<Product> productsList = new ArrayList<>();
    Intent intent;
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 1500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.
    FragmentHomeBinding binding;
    ProductUserAdapter productPopularUserAdapter;
    ProductUserAdapter productNewUserAdapter;
    ProductUserAdapter productDiscountUserAdapter;
    CategoryUserAdapter categoryUserAdapter;
    private HomeViewModel homeViewModel;
    private AccountViewModel accountViewModel;
    private final HashMap<String,List<Product>> productMap = new HashMap<>();
    private Category categoryCurrent ;
    public static final String TAG = "HomeFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        homeViewModel.setiHomeRepository(new HomeRepositoryImpl());

        productDiscountUserAdapter = new ProductUserAdapter(requireContext(), this);
        productNewUserAdapter = new ProductUserAdapter(requireContext(), this);
        productPopularUserAdapter = new ProductUserAdapter(requireContext(), this);
        categoryUserAdapter = new CategoryUserAdapter(this);

        binding.rcvCategory.setAdapter(categoryUserAdapter);
        binding.rcvCategory.setLayoutManager(new LinearLayoutManager(requireContext() ,RecyclerView.HORIZONTAL, false));

        binding.rcvDiscountProduct.setAdapter(productDiscountUserAdapter);
        binding.rcvDiscountProduct.setLayoutManager(new LinearLayoutManager(requireContext() ,RecyclerView.HORIZONTAL, false));
        binding.rcvNewProduct.setAdapter(productNewUserAdapter);
        binding.rcvNewProduct.setLayoutManager(new LinearLayoutManager(requireContext() ,RecyclerView.HORIZONTAL, false));
        binding.rcvPopularProduct.setAdapter(productPopularUserAdapter);
        binding.rcvPopularProduct.setLayoutManager(new LinearLayoutManager(requireContext() ,RecyclerView.HORIZONTAL, false));

        homeViewModel.getDataHome();
        homeViewModel.getHomeResponseLiveData().observe(requireActivity(), result -> {
            if (result != null) {
//                Toast.makeText(requireContext(), "result = " + result.getProductFamousList().size(), Toast.LENGTH_LONG).show();
                productsList.addAll(result.getProductsList());
                categoryList.addAll(result.getCategoryList());
                if (!categoryList.isEmpty()){
                    categoryUserAdapter.submitList(result.getCategoryList());
                    List<Product> dataProductList = result.getProductMap().getOrDefault(categoryList.get(0).getId(), new ArrayList<>());
                    assert dataProductList != null;

                    List<Product> discountProductList = dataProductList.stream().filter(it -> (it.getDiscount() > 0)).collect(Collectors.toList());
                    List<Product> newProductList = dataProductList.stream().filter(Product::isNew).collect(Collectors.toList());
                    List<Product> popularProductList = dataProductList.stream().filter(Product::isFamous).collect(Collectors.toList());

                    showInfoDiscountProductIfValid(discountProductList);
                    showInfoNewProductIfValid(newProductList);
                    showInfoPopularProductIfValid(popularProductList);
                    productMap.putAll(result.getProductMap());
                    categoryCurrent = categoryList.get(0);
                }
            }
        });
        binding.homeSearch.setOnClickListener(this);
        binding.btnCart.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), CartProductActivity.class);
            intent.putExtra("userId", Objects.requireNonNull(accountViewModel.getUserInfoLiveData().getValue()).get_id());
            startActivity(intent);
        });

        binding.btnSeeAll.setOnClickListener(view -> {
            intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("search_state" , 2);
            startActivity(intent);
        });

        binding.btnSeeNewAll.setOnClickListener(view -> {
            intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("search_state" , 3);
            startActivity(intent);
        });
        setLoadMoreAction();


        return binding.getRoot();
    }


    private void setLoadMoreAction() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.home_search) {
            intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("search_state" , 1);
            startActivity(intent);
        }
    }

    @Override
    public void onClickItemProduct(Product product) {
        Intent intent = new Intent(requireActivity(), DetailProductActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("product", product);
        bundle.putString("userId", Objects.requireNonNull(accountViewModel.getUserInfoLiveData().getValue()).get_id());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClickItem(String id, int position) {
        categoryUserAdapter.setSelectedIndex(position);
        categoryUserAdapter.notifyDataSetChanged();
        List<Product> tmpProductList = Objects.requireNonNull(productMap.getOrDefault(id, new ArrayList<>()));
        Log.d(TAG, "onClickItem: ====> product = "+ tmpProductList);
        List<Product> dataDiscountProductList = tmpProductList.stream().filter(it -> !(it.getDiscount() <= 0)).collect(Collectors.toList());
        showInfoDiscountProductIfValid(dataDiscountProductList);
        List<Product> dataNewProductList = tmpProductList.stream().filter(Product::isNew).collect(Collectors.toList());
        showInfoNewProductIfValid(dataNewProductList);
        List<Product> dataPopularProductList = tmpProductList.stream().filter(Product::isFamous).collect(Collectors.toList());
        showInfoPopularProductIfValid(dataPopularProductList);

        categoryCurrent = categoryList.get(position);
    }


    private void showInfoDiscountProductIfValid(List<Product> dataDiscountProductList) {
        if (dataDiscountProductList.isEmpty()) {
            binding.llDiscountProduct.setVisibility(View.GONE);
        }else {
            binding.llDiscountProduct.setVisibility(View.VISIBLE);
            productDiscountUserAdapter.submitList(dataDiscountProductList);
        }
    }

    private void showInfoPopularProductIfValid(List<Product> dataPopularProductList) {
        if (dataPopularProductList.isEmpty()) {
            binding.llFamousProduct.setVisibility(View.GONE);
        }else {
            binding.llFamousProduct.setVisibility(View.VISIBLE);
            productPopularUserAdapter.submitList(dataPopularProductList);
        }
    }
    private void showInfoNewProductIfValid(List<Product> dataNewProductList) {
        if (dataNewProductList.isEmpty()) {
            binding.llNewProduct.setVisibility(View.GONE);
        }else {
            binding.llNewProduct.setVisibility(View.VISIBLE);
            productNewUserAdapter.submitList(dataNewProductList);
        }
    }

    @Override
    public void onStop() {
        homeViewModel.clear();
        super.onStop();
    }
}
