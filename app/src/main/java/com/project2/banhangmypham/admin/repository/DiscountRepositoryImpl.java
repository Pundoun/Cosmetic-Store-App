package com.project2.banhangmypham.admin.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.project2.banhangmypham.admin.CategoryResponse;
import com.project2.banhangmypham.model.Category;
import com.project2.banhangmypham.model.DiscountModel;
import com.project2.banhangmypham.model.DiscountResponse;
import com.project2.banhangmypham.model.MessageResponse;
import com.project2.banhangmypham.service.ApiClientService;
import com.project2.banhangmypham.service.ApiMethod;

import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DiscountRepositoryImpl implements IDiscountRepository{

    public static final String TAG = "DiscountRepositoryImpl";
    public final ApiClientService apiClientService = new ApiClientService();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    @SuppressLint("CheckResult")
    @Override
    public Single<MessageResponse> addDiscountModel(DiscountModel discountModel) {
        String jsonRequest = new Gson().toJson(discountModel);
        return Single.create(emitter -> {
            apiClientService.makeRequest(UserRepositoryImpl.HOST+"discount/add-new-discount", ApiMethod.POST, MessageResponse.class,
                            RequestBody.create(jsonRequest, JSON))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(emitter::onSuccess, emitter::onError);
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public Single<MessageResponse> updateDiscountModel(DiscountModel discountModel) {
        String jsonRequest = new Gson().toJson(discountModel);
        return Single.create(emitter -> {
            apiClientService.makeRequest(UserRepositoryImpl.HOST+"discount/update-discount", ApiMethod.PUT, MessageResponse.class,
                            RequestBody.create(jsonRequest, JSON))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(emitter::onSuccess, emitter::onError);
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public Single<MessageResponse> deleteDiscountModel(DiscountModel discountModel) {
        Log.d(TAG, "deleteDiscountModel: =====> id = " + discountModel.get_id());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("discountId", discountModel.get_id());
        }catch (Exception e){
            Log.d(TAG, "deleteDiscountModel: ===> error = " + e.getMessage());
        }
        return Single.create(emitter -> apiClientService.makeRequest(UserRepositoryImpl.HOST+"discount/delete-discount/"+ discountModel.get_id(), ApiMethod.DELETE, MessageResponse.class,
                        RequestBody.create(jsonObject.toString(), JSON))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(emitter::onSuccess, emitter::onError));
    }

    @SuppressLint("CheckResult")
    @Override
    public Single<DiscountResponse> getAllDiscountModelList() {
        return Single.create(emitter -> {
            apiClientService.makeRequest(UserRepositoryImpl.HOST+"discount/get-all-discounts", ApiMethod.GET, DiscountResponse.class, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(emitter::onSuccess, emitter::onError);
        });
    }
}
