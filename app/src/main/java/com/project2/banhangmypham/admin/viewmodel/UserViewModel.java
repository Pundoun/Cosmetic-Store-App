package com.project2.banhangmypham.admin.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project2.banhangmypham.admin.repository.ICategoryRepository;
import com.project2.banhangmypham.admin.repository.IProductRepository;
import com.project2.banhangmypham.admin.repository.IUserRepository;
import com.project2.banhangmypham.model.ProductResponse;
import com.project2.banhangmypham.model.UserResponse;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserViewModel extends ViewModel {
    public static final String TAG = "UserViewModel";
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IUserRepository iUserRepository ;
    private MutableLiveData<UserResponse> _usersListLiveData = new MutableLiveData<>();
    LiveData<UserResponse> usersListLiveData = _usersListLiveData;

    private MutableLiveData<UserResponse> _deleteUserLiveData = new MutableLiveData<>();
    LiveData<UserResponse> deleteUserLiveData = _deleteUserLiveData;
    public void setUserRepository(IUserRepository data){
        iUserRepository = data;
    }

    public LiveData<UserResponse> getUsersListLiveData() {
        return usersListLiveData;
    }

    public LiveData<UserResponse> getDeleteUserLiveData() {
        return deleteUserLiveData;
    }

    public void getAllUsersList(){
        Disposable disposable = iUserRepository.getAllUsersList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( result -> {
                            _usersListLiveData.postValue(result);
                        },
                        error -> {
//                            _categoryListLiveData.postValue(new CategoryResponse(new ArrayList<>(), false, error.getMessage()));
//                            Log.d(TAG, "getAllProductsList: ====> error = " + error.getMessage());
                        });

        compositeDisposable.add(disposable);
    }

    public void deleteUserById(String id) {
        Disposable disposable = iUserRepository.deleteUserById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( result -> {
                            _deleteUserLiveData.postValue(result);
                        },
                        error -> {
//                            _categoryListLiveData.postValue(new CategoryResponse(new ArrayList<>(), false, error.getMessage()));
                            Log.d(TAG, "deleteUserById: ====> error = " + error.getMessage());
                        });

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
