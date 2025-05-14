package com.project2.banhangmypham.viewmodel.product;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project2.banhangmypham.model.CartDeleteRequest;
import com.project2.banhangmypham.model.CartProductRequest;
import com.project2.banhangmypham.model.CartProductResponse;
import com.project2.banhangmypham.model.DetailProductResponse;
import com.project2.banhangmypham.model.FavoriteRequest;
import com.project2.banhangmypham.model.FavoriteResponse;
import com.project2.banhangmypham.model.MessageResponse;
import com.project2.banhangmypham.model.OrderRequest;
import com.project2.banhangmypham.model.OrderRequestAdmin;
import com.project2.banhangmypham.model.OrderResponse;
import com.project2.banhangmypham.repository.detail_product.IDetailProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DetailProductViewModel extends ViewModel {

    public static final String TAG = "DetailProductViewModel";
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<MessageResponse> _detailProductLiveData = new MutableLiveData<>();
    private LiveData<MessageResponse> detailProductLiveData = _detailProductLiveData;
    private MutableLiveData<MessageResponse> _updateProductLiveData = new MutableLiveData<>();
    private LiveData<MessageResponse> updateProductLiveData = _updateProductLiveData;
    private MutableLiveData<CartProductResponse> _listProductLiveData = new MutableLiveData<>();
    private final LiveData<CartProductResponse> listProductLiveData = _listProductLiveData;
    private IDetailProductRepository iDetailProductRepository ;
    private MutableLiveData<MessageResponse> _createOrderLiveData = new MutableLiveData<>();
    private LiveData<MessageResponse> createOrderLiveData = _createOrderLiveData;

    private MutableLiveData<List<OrderRequest>> _orderCompletingResponseLiveData = new MutableLiveData<>();
    private LiveData<List<OrderRequest>> orderCompletingResponseLiveData = _orderCompletingResponseLiveData;

    private MutableLiveData<List<OrderRequest>> _orderExecutingResponseLiveData = new MutableLiveData<>();
    private LiveData<List<OrderRequest>> orderExecutingResponseLiveData = _orderExecutingResponseLiveData;

    public CompositeDisposable getCompositeDisposable() {
        return compositeDisposable;
    }

    public LiveData<List<OrderRequest>> getOrderExecutingResponseLiveData() {
        return orderExecutingResponseLiveData;
    }

    public LiveData<List<OrderRequest>> getOrderCompletingResponseLiveData() {
        return orderCompletingResponseLiveData;
    }

    private MutableLiveData<MessageResponse> _updateFavoriteLiveData = new MutableLiveData<>();
    private LiveData<MessageResponse> updateFavoriteLiveData = _updateFavoriteLiveData;

    private MutableLiveData<MessageResponse> _oneProductFavoriteLiveData = new MutableLiveData<>();
    private LiveData<MessageResponse> oneProductFavoriteLiveData = _oneProductFavoriteLiveData;

    private MutableLiveData<FavoriteResponse> _allProductFavoriteLiveData = new MutableLiveData<>();
    private LiveData<FavoriteResponse> allProductFavoriteLiveData = _allProductFavoriteLiveData;

    public LiveData<MessageResponse> getOneProductFavoriteLiveData() {
        return oneProductFavoriteLiveData;
    }

    public LiveData<FavoriteResponse> getAllProductFavoriteLiveData() {
        return allProductFavoriteLiveData;
    }

    public LiveData<MessageResponse> getUpdateFavoriteLiveData() {
        return updateFavoriteLiveData;
    }

    public void setIDetailProductRepository(IDetailProductRepository data) {
        iDetailProductRepository = data;
    }

    public LiveData<MessageResponse> getCreateOrderLiveData() {
        return createOrderLiveData;
    }

    public LiveData<MessageResponse> getDetailProductLiveData() {
        return detailProductLiveData;
    }

    public LiveData<CartProductResponse> getListProductLiveData() {
        return listProductLiveData;
    }

    public LiveData<MessageResponse> getUpdateProductLiveData() {
        return updateProductLiveData;
    }

    public void addCartProduct(CartProductRequest cartProductRequest){
        Disposable disposable = iDetailProductRepository.addCartProduct(cartProductRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _detailProductLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }

    public void getAllCartProductList(String uid ){
        Disposable disposable = iDetailProductRepository.getAllCartProductList(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _listProductLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }

    public void updateAllCartProductList(CartDeleteRequest data){
        Disposable disposable = iDetailProductRepository.updateListCartProduct(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _updateProductLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }
    public void createOrderProductList(OrderRequest data){
        Disposable disposable = iDetailProductRepository.createOrderForProductList(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _createOrderLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }
    public void getAllOrderProductList(String uid) {
        Log.d(TAG, "getAllOrderProductList: Starting to fetch orders for user: " + uid);
        Disposable disposable = iDetailProductRepository.getAllOrderForProductList(uid)
                .flatMap(it -> Single.create(emitter -> {
                    Log.d(TAG, "getAllOrderProductList: Received response from server: " + (it != null ? "not null" : "null"));
                    if (it != null && it.getData() != null) {
                        Log.d(TAG, "getAllOrderProductList: Number of orders received: " + it.getData().size());
                        // Nhóm dữ liệu theo trạng thái của OrderRequest
                        it.getData().forEach(order ->{
                            Log.d(TAG, "getAllOrderProductList: Order details - ID: " + order.get_id() 
                                + ", Status: " + order.getStatus() 
                                + ", MethodPayment: " + order.getMethodPayment());
                        });
                        
                        // Create new lists to store the orders
                        List<OrderRequest> executingOrders = new ArrayList<>();
                        List<OrderRequest> completingOrders = new ArrayList<>();
                        
                        // Process each order and maintain all its properties
                        for (OrderRequest order : it.getData()) {
                            if (order.getStatus() == 0 || order.getStatus() == 1) {
                                executingOrders.add(order);
                            } else if (order.getStatus() == 2) {
                                completingOrders.add(order);
                            }
                        }

                        Log.d(TAG, "getAllOrderProductList: Executing orders count: " + executingOrders.size());
                        Log.d(TAG, "getAllOrderProductList: Completing orders count: " + completingOrders.size());
                        
                        _orderExecutingResponseLiveData.postValue(executingOrders);
                        _orderCompletingResponseLiveData.postValue(completingOrders);

                        emitter.onSuccess(new MessageResponse(200, ""));
                    } else {
                        Log.e(TAG, "getAllOrderProductList: Data is null or empty");
                        emitter.onError(new Throwable("Data is null"));
                    }
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        Log.d(TAG, "getAllOrderProductList: Successfully processed orders");
                    },
                    error -> {
                        Log.e(TAG, "getAllOrderProductList: Error processing orders: " + error.getMessage());
                    }
                );
        compositeDisposable.add(disposable);
    }
    public void updateFavoriteProduct(FavoriteRequest favoriteRequest) {
        Disposable disposable = iDetailProductRepository.updateFavorite(favoriteRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _updateFavoriteLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }

    public void getFavoriteById(String productId, String userId) {
        Disposable disposable = iDetailProductRepository.getFavoriteById(productId,userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _oneProductFavoriteLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }
    public void getAllFavoriteProductsList(String userId) {
        Disposable disposable = iDetailProductRepository.getAllFavoriteProductByIdUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> _allProductFavoriteLiveData.postValue(result), error -> Log.d(TAG, "getDataHome: ====> error = "+ error.getMessage()));
        compositeDisposable.add(disposable);
    }
    public void clear(){
        compositeDisposable.clear();
    }
    @Override
    protected void onCleared() {
        compositeDisposable.clear();
        super.onCleared();
    }
}
