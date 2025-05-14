package com.project2.banhangmypham.utils;

import android.util.Base64;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.paypal.android.corepayments.CoreConfig;
import com.paypal.android.corepayments.Environment;
import com.paypal.android.corepayments.PayPalSDKError;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest;
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult;
import com.project2.banhangmypham.model.Product;
import com.project2.banhangmypham.model.TokenModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
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

public class PaypalUtils {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final String TAG = "PaypalUtils";
    public static String clientID = "AQfUp9XfSj1VR4pRVs57bDiTSvoig4PTpqzdXTVtyojn8mORHZIUPvhf7P1KwZ980JJnR_Z4DaE2ScwY";
    public static String secretKey = "EFD4qIQpKcun4OsvvzgZp2ds9vSbTTR59nTO9ltFJIK7dm-H-z5U1Y3vIi0F5-M3M59Dg5C5TzeqTSBS";
    public static String returnUrl = "myapp://return_url";

    private static MutableLiveData<String> _tokenLiveData = new MutableLiveData<>("");
    private static LiveData<String> tokenLiveData = _tokenLiveData;
    private static MutableLiveData<String> _createOrderLiveData = new MutableLiveData<>("");
    private static LiveData<String> createOrderLiveData = _createOrderLiveData;

    private static MutableLiveData<Boolean> _handerOrderLiveData = new MutableLiveData<>(false);
    private static LiveData<Boolean> handerOrderLiveData = _handerOrderLiveData;
    public static LiveData<String> getTokenLiveData() {
        return tokenLiveData;
    }
    public static LiveData<String> getCreateOrderLiveData() {
        return createOrderLiveData;
    }

    public static LiveData<Boolean> getHanderOrderLiveData() {
        return handerOrderLiveData;
    }
    public static double convertVNDToUSD(double amountInVND, double exchangeRate) {
        return amountInVND / exchangeRate;
    }

    public static void fetchAccessToken(){
        String url = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();
        // Generate Basic Auth header
        String credentials = Credentials.basic(clientID, secretKey);
        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            // Execute the request
            Response response = client.newCall(request).execute();

            // Check if the response is successful
            if (response.isSuccessful()) {
                // Print the response body (access token)
                TokenModel tokenModel = new Gson().fromJson(response.body().string(), TokenModel.class);
                _tokenLiveData.postValue(tokenModel.getAccess_token());
            } else {
                // Print error details
                Log.d(TAG,"Error: " + response.code() + " - " + response.message());
               Log.d(TAG,"Response Body: " + response.body().string());
            }
        } catch (IOException e) {
            // Handle exceptions
            Log.d(TAG,"Exception occurred: " + e.getMessage());
        }
    }
    public static void createOrder(FragmentActivity activity ,String token, long totalCart) {
        JSONObject orderRequestJson = new JSONObject();
        try {
            orderRequestJson.put("intent", "CAPTURE");
            JSONArray purchaseUnits = new JSONArray();
            JSONObject purchaseUnit = new JSONObject();
            JSONObject amount = new JSONObject();
            amount.put("currency_code", "USD");
            amount.put("value", String.valueOf(convertVNDToUSD(totalCart,25000)));
            purchaseUnit.put("amount", amount);
            purchaseUnits.put(purchaseUnit);
            orderRequestJson.put("purchase_units", purchaseUnits);
            JSONObject experienceContext = new JSONObject();
            experienceContext.put("return_url", returnUrl );
            experienceContext.put("cancel_url", "https://example.com/cancelUrl");

            orderRequestJson.put("application_context", experienceContext);

            Log.d(TAG, "createOrder: ==== > request = " + orderRequestJson.toString());
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();
            Request.Builder request = new Request.Builder()
                    .url("https://api-m.sandbox.paypal.com/v2/checkout/orders/")
                    .header("Authorization", "Bearer "+token);
            RequestBody requestBody = RequestBody.create(orderRequestJson.toString(),JSON);
            request.post(requestBody);
            Call call = client.newCall(request.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: create ====> "+e.getMessage() );
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "onFailure: create ====> "+response.code() + " - " + response.message() );
                    if (response.isSuccessful() && response.body() != null) {
                        String dataResult = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(dataResult);
                            Log.d(TAG, "onResponse: ===> jsonObject = " + jsonObject);
                            _createOrderLiveData.postValue(jsonObject.getString("id"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void handleOrderID(FragmentActivity activity, String orderID) {
        CoreConfig config = new CoreConfig(clientID, Environment.SANDBOX);
        PayPalWebCheckoutClient payPalWebCheckoutClient = new PayPalWebCheckoutClient(activity, config, returnUrl);

        payPalWebCheckoutClient.setListener(new PayPalWebCheckoutListener() {
            @Override
            public void onPayPalWebSuccess(PayPalWebCheckoutResult result) {
                Log.d(TAG, "onPayPalWebSuccess: " + result);
                _handerOrderLiveData.postValue(true);
            }

            @Override
            public void onPayPalWebFailure(PayPalSDKError error) {
                Log.d(TAG, "onPayPalWebFailure: " + error);
                _handerOrderLiveData.postValue(false);

            }

            @Override
            public void onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled");
            }
        });

//        orderid = orderID;

        PayPalWebCheckoutRequest payPalWebCheckoutRequest =
                new PayPalWebCheckoutRequest(orderID, PayPalWebCheckoutFundingSource.PAYPAL);
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest);
    }
    public static void captureOrder(String orderID, String accessToken) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderID + "/capture";
        JSONObject jsonObject =  new JSONObject();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonObject.toString(),JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Capture Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Capture Response: " + responseData);
                } else {
                    Log.e(TAG, "Capture Error: " + response.code() + " - " + response.message());
                }
            }
        });
    }
}
