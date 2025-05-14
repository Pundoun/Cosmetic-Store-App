package com.project2.banhangmypham.admin.repository;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.project2.banhangmypham.model.ProductResponse;
import com.project2.banhangmypham.model.User;
import com.project2.banhangmypham.model.UserResponse;
import com.project2.banhangmypham.service.ApiClientService;
import com.project2.banhangmypham.service.ApiMethod;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UserRepositoryImpl implements IUserRepository{
    public static final String HOST = "http://10.131.141.214:5000/";
    public static final String TAG = "UserRepositoryImpl";
    public final ApiClientService apiClientService = new ApiClientService();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private List<User> usersList = new ArrayList<>();
    @SuppressLint("CheckResult")
    @Override
    public Single<UserResponse> getAllUsersList() {
        return Single.create(emitter -> {
            apiClientService.makeRequest(HOST+"user/getAllUsers", ApiMethod.GET, UserResponse.class, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(emitter::onSuccess, emitter::onError);
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public Single<UserResponse> deleteUserById(String id) {
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("_id", id);
        } catch (Exception e) {
            Log.d(TAG, "deleteUserById: ====> error = " + e.getMessage());
        }
        return Single.create(emitter -> apiClientService.makeRequest(HOST+"user/delete/", ApiMethod.DELETE, UserResponse.class,  RequestBody.create(jsonObject.toString(), JSON))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(emitter::onSuccess, emitter::onError));
    }

    @Override
    public List<User> filterByName(String data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return usersList.stream().filter(it -> it.getFull_name().contains(data)).collect(Collectors.toList());
        }else {
            List<User> userList = new ArrayList<>();
            for (User user : usersList){
                if (user.getFull_name().toLowerCase().contains(data.trim().toLowerCase())){
                    userList.add(user);
                }
            }
            return userList;
        }
    }

    @Override
    public List<User> filterByTypeAccount(boolean data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return usersList.stream().filter(it -> data == it.isIs_admin()).collect(Collectors.toList());
        }else {
            List<User> userList = new ArrayList<>();
            for (User user : usersList){
                if (data == user.isIs_admin()){
                    userList.add(user);
                }
            }
            return userList;
        }
    }

    @Override
    public List<User> filterByStateAccount(boolean data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return usersList.stream().filter(it -> data == it.isBanned()).collect(Collectors.toList());
        }else {
            List<User> userList = new ArrayList<>();
            for (User user : usersList){
                if (data == user.isBanned()){
                    userList.add(user);
                }
            }
            return userList;
        }
    }
}
