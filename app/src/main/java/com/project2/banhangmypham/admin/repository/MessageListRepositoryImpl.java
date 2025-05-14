package com.project2.banhangmypham.admin.repository;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project2.banhangmypham.firebase.ChatManager;
import com.project2.banhangmypham.model.Message;
import com.project2.banhangmypham.model.OrderAdminResponse;
import com.project2.banhangmypham.model.User;
import com.project2.banhangmypham.model.UserResponse;
import com.project2.banhangmypham.service.ApiClientService;
import com.project2.banhangmypham.service.ApiMethod;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;

public class MessageListRepositoryImpl implements IMessageListRepository{
    public static final String TAG = "OrderAdminRepositoryImpl";
    public final ApiClientService apiClientService = new ApiClientService();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
    @SuppressLint("CheckResult")
    @Override
    public void getMessageUserList() {
    }
}
