package com.project2.banhangmypham.localstorage;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class LocalStorageManager {
    private static LocalStorageManager _instance;
    Preferences.Key<String> TOKEN = PreferencesKeys.stringKey("TOKEN");
    Preferences.Key<String> USER = PreferencesKeys.stringKey("USER");
    Preferences.Key<String> ADDRESS_TRANSFER = PreferencesKeys.stringKey("ADDRESS_TRANSFER");
    Preferences.Key<Integer> INDEX = PreferencesKeys.intKey("INDEX");
    private static RxDataStore<Preferences> dataStore ;
    private static Context mContext ;
    public static void create(Context context) {
        mContext = context ;
        dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
    }
    public static LocalStorageManager getInstance() {
        if (_instance == null) {
            _instance = new LocalStorageManager();
        }
        return _instance;
    }

    public void saveToken(String token){
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(TOKEN,token);
            return Single.just(mutablePreferences);
        });
    }
    public void saveUserInfo(String data){
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(USER,data);
            return Single.just(mutablePreferences);
        });
    }

    public void saveAddressTransfer(String data, int index){
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(ADDRESS_TRANSFER,data);
            mutablePreferences.set(INDEX,index);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<String> tokenData = dataStore.data().map(prefs -> (prefs.get(TOKEN) != null) ? prefs.get(TOKEN) : "");
    public Flowable<String> userData = dataStore.data().map(prefs -> (prefs.get(USER) != null) ? prefs.get(USER) : "");
    public Flowable<String> transferData = dataStore.data().map(prefs -> (prefs.get(USER) != null) ? prefs.get(ADDRESS_TRANSFER) : "");
    public Flowable<Integer> indexData = dataStore.data().map(prefs -> (prefs.get(USER) != null) ? prefs.get(INDEX) : -1);
}
