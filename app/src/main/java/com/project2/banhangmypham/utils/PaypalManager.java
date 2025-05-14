package com.project2.banhangmypham.utils;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.paypal.android.corepayments.CoreConfig;
import com.paypal.android.corepayments.Environment;
import com.paypal.android.corepayments.PayPalSDKError;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult;
import com.project2.banhangmypham.model.TokenModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaypalManager {
    private static final String TAG = "PaypalManager";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String CLIENT_ID = "AQfUp9XfSj1VR4pRVs57bDiTSvoig4PTpqzdXTVtyojn8mORHZIUPvhf7P1KwZ980JJnR_Z4DaE2ScwY";
    private static final String SECRET_KEY = "EFD4qIQpKcun4OsvvzgZp2ds9vSbTTR59nTO9ltFJIK7dm-H-z5U1Y3vIi0F5-M3M59Dg5C5TzeqTSBS";
    private static final String RETURN_URL = "myapp://return_url";

    private final MutableLiveData<String> tokenLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> createOrderLiveData = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> handleOrderLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> handleCaptureLiveData = new MutableLiveData<>();
    private PayPalWebCheckoutClient payPalWebCheckoutClient;

    public LiveData<String> getTokenLiveData() {
        return tokenLiveData;
    }

    public LiveData<String> getCreateOrderLiveData() {
        return createOrderLiveData;
    }

    public LiveData<Boolean> getHandleOrderLiveData() {
        return handleOrderLiveData;
    }
    public LiveData<Boolean> getHandleCaptureLiveData() {
        return handleCaptureLiveData;
    }

    public void fetchAccessToken() {
        String url = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        String credentials = Credentials.basic(CLIENT_ID, SECRET_KEY);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching token: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    TokenModel tokenModel = new Gson().fromJson(response.body().string(), TokenModel.class);
                    tokenLiveData.postValue(tokenModel.getAccess_token());
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                }
            }
        });
    }

    public void createOrder(FragmentActivity activity, String token, long totalCart) {
        try {
            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("intent", "CAPTURE");
            
            JSONArray purchaseUnits = new JSONArray();
            JSONObject purchaseUnit = new JSONObject();
            JSONObject amount = new JSONObject();
            amount.put("currency_code", "USD");
            amount.put("value", String.valueOf(convertVNDToUSD(totalCart, 25000)));
            purchaseUnit.put("amount", amount);
            purchaseUnits.put(purchaseUnit);
            orderRequestJson.put("purchase_units", purchaseUnits);

            JSONObject experienceContext = new JSONObject();
            experienceContext.put("return_url", RETURN_URL);
            experienceContext.put("cancel_url", "https://example.com/cancelUrl");
            orderRequestJson.put("application_context", experienceContext);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api-m.sandbox.paypal.com/v2/checkout/orders/")
                    .header("Authorization", "Bearer " + token)
                    .post(RequestBody.create(orderRequestJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error creating order: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            createOrderLiveData.postValue(jsonObject.getString("id"));
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing order response: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating order JSON: " + e.getMessage());
        }
    }

    public void handleOrderID(FragmentActivity activity, String orderID) {
        CoreConfig config = new CoreConfig(CLIENT_ID, Environment.SANDBOX);
        payPalWebCheckoutClient = new PayPalWebCheckoutClient(activity, config, RETURN_URL);

        payPalWebCheckoutClient.setListener(new PayPalWebCheckoutListener() {
            @Override
            public void onPayPalWebSuccess(PayPalWebCheckoutResult result) {
                Log.d(TAG, "Payment successful: " + result);
                handleOrderLiveData.postValue(true);
            }

            @Override
            public void onPayPalWebFailure(PayPalSDKError error) {
                Log.e(TAG, "Payment failed: " + error);
                handleOrderLiveData.postValue(false);
            }

            @Override
            public void onPayPalWebCanceled() {
                Log.d(TAG, "Payment canceled");
            }
        });

        PayPalWebCheckoutRequest request = new PayPalWebCheckoutRequest(orderID, PayPalWebCheckoutFundingSource.PAYPAL);
        payPalWebCheckoutClient.start(request);
    }

    public void captureOrder(String orderID, String accessToken) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderID + "/capture";
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create("{}", JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Capture Error: " + e.getMessage());
                handleCaptureLiveData.postValue(false);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Capture Response: " + response.body().string());
                    handleCaptureLiveData.postValue(true);
                } else {
                    Log.e(TAG, "Capture Error: " + response.code() + " - " + response.message());
                    handleCaptureLiveData.postValue(false);
                }
            }
        });
    }

    public void release() {
        if (payPalWebCheckoutClient != null) {
            payPalWebCheckoutClient.setListener(null);
            payPalWebCheckoutClient = null;
        }
    }

    private double convertVNDToUSD(double amountInVND, double exchangeRate) {
        return amountInVND / exchangeRate;
    }
} 