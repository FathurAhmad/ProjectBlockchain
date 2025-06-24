package com.example.projectblockchain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KatalogDetailActivity extends AppCompatActivity implements TransactionFinishedCallback {
    private TextView tvNama, tvDeskripsi, tvHarga, tvStok;
    private Button btnBeli;
    private String namaProduk;
    private int hargaProduk;
    private int stokProduk;

    // Ganti dengan Server Key Anda dari Midtrans Dashboard
    private static final String SERVER_KEY = "Mid-server-CX3U7IMdPs8_h2_gfjw_NOAM"; // Ganti dengan Server Key Anda
    private static final String MIDTRANS_BASE_URL = "https://app.sandbox.midtrans.com/snap/v1/transactions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.item_detail);

        // Initialize Midtrans SDK
        initMidtransSDK();

        tvNama = findViewById(R.id.tvNama);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvHarga = findViewById(R.id.tvHarga);
        tvStok = findViewById(R.id.tvStok);

        namaProduk = getIntent().getStringExtra("nama");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        hargaProduk = getIntent().getIntExtra("harga", 0);
        stokProduk = getIntent().getIntExtra("stok", 0);

        tvNama.setText(namaProduk);
        tvDeskripsi.setText(deskripsi);
        tvHarga.setText("Rp " + String.format("%,d", hargaProduk));
        tvStok.setText("Stok: " + stokProduk);

        btnBeli = findViewById(R.id.btnBeli);
        btnBeli.setOnClickListener(v -> {
            if (stokProduk > 0) {
                btnBeli.setEnabled(false);
                btnBeli.setText("Processing...");
                createSnapToken();
            } else {
                Toast.makeText(this, "Stok produk habis!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMidtransSDK() {
        // Ganti dengan Client Key Anda dari Midtrans Dashboard
        String CLIENT_KEY = "Mid-client-Sr_Gp81EhDCr-4EP"; // Ganti dengan Client Key Anda

        SdkUIFlowBuilder.init()
                .setClientKey(CLIENT_KEY) // Client key dari Midtrans Dashboard
                .setContext(this)
                .setTransactionFinishedCallback(this)
                .enableLog(true) // Set false di production
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .buildSDK();
    }

    private void createSnapToken() {
        // Generate unique order ID
        String orderId = "ORDER-" + UUID.randomUUID().toString();

        try {
            // Create JSON payload for Snap Token
            JSONObject transactionDetails = new JSONObject();
            transactionDetails.put("order_id", orderId);
            transactionDetails.put("gross_amount", hargaProduk);

            // Fix: Create item details as JSONArray instead of JSONObject
            JSONObject itemDetail = new JSONObject();
            itemDetail.put("id", "ITEM-" + namaProduk.replaceAll(" ", "-"));
            itemDetail.put("price", hargaProduk);
            itemDetail.put("quantity", 1);
            itemDetail.put("name", namaProduk);

            JSONArray itemDetailsArray = new JSONArray();
            itemDetailsArray.put(itemDetail);

            // Fix: Create proper customer details structure
            JSONObject billingAddress = new JSONObject();
            billingAddress.put("first_name", "Customer");
            billingAddress.put("last_name", "Name");
            billingAddress.put("address", "Jl. Contoh No. 123");
            billingAddress.put("city", "Malang");
            billingAddress.put("postal_code", "65141");
            billingAddress.put("phone", "08123456789");
            billingAddress.put("country_code", "IDN");

            JSONObject shippingAddress = new JSONObject();
            shippingAddress.put("first_name", "Customer");
            shippingAddress.put("last_name", "Name");
            shippingAddress.put("address", "Jl. Contoh No. 123");
            shippingAddress.put("city", "Malang");
            shippingAddress.put("postal_code", "65141");
            shippingAddress.put("phone", "08123456789");
            shippingAddress.put("country_code", "IDN");

            JSONObject customerDetails = new JSONObject();
            customerDetails.put("first_name", "Customer");
            customerDetails.put("last_name", "Name");
            customerDetails.put("email", "customer@example.com");
            customerDetails.put("phone", "08123456789");
            customerDetails.put("billing_address", billingAddress);
            customerDetails.put("shipping_address", shippingAddress);

            // Create final payload
            JSONObject payload = new JSONObject();
            payload.put("transaction_details", transactionDetails);
            payload.put("item_details", itemDetailsArray);
            payload.put("customer_details", customerDetails);

            // Add callbacks (optional but recommended)
            JSONObject callbacks = new JSONObject();
            callbacks.put("finish", "your_finish_url_here");
            callbacks.put("error", "your_error_url_here");
            callbacks.put("pending", "your_pending_url_here");
            // payload.put("callbacks", callbacks); // Uncomment if you have callback URLs

            Log.d("Midtrans", "Payload: " + payload.toString());

            // Send request to Midtrans
            sendSnapTokenRequest(payload.toString(), orderId);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Midtrans", "JSON creation error", e);
            resetButton();
            Toast.makeText(this, "Error creating payment request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSnapTokenRequest(String jsonPayload, String orderId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonPayload, JSON);

        // Create authorization header with Server Key
        String auth = "Basic " + android.util.Base64.encodeToString(
                (SERVER_KEY + ":").getBytes(), android.util.Base64.NO_WRAP);

        Request request = new Request.Builder()
                .url(MIDTRANS_BASE_URL)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", auth)
                .build();

        Log.d("Midtrans", "Request URL: " + MIDTRANS_BASE_URL);
        Log.d("Midtrans", "Request payload: " + jsonPayload);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Midtrans", "Network error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    resetButton();
                    Toast.makeText(KatalogDetailActivity.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("Midtrans", "Response code: " + response.code());
                Log.d("Midtrans", "Response body: " + responseBody);

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.has("token")) {
                                String snapToken = jsonResponse.getString("token");
                                Log.d("Midtrans", "Snap token received: " + snapToken);

                                // Start payment with snap token
                                startPaymentWithToken(snapToken, orderId);
                            } else {
                                resetButton();
                                Log.e("Midtrans", "No token in response: " + responseBody);
                                Toast.makeText(KatalogDetailActivity.this,
                                        "No payment token received", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            resetButton();
                            Log.e("Midtrans", "Error response: " + response.code() + " - " + responseBody);

                            // Try to parse error message
                            String errorMessage = "Payment setup failed";
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                if (errorJson.has("error_messages")) {
                                    JSONArray errors = errorJson.getJSONArray("error_messages");
                                    if (errors.length() > 0) {
                                        errorMessage = errors.getString(0);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("Midtrans", "Error parsing error response", e);
                            }

                            Toast.makeText(KatalogDetailActivity.this,
                                    errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        resetButton();
                        Log.e("Midtrans", "JSON parsing error", e);
                        Toast.makeText(KatalogDetailActivity.this,
                                "Error processing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startPaymentWithToken(String snapToken, String orderId) {
        try {
            // Create transaction request with the received token
            TransactionRequest transactionRequest = new TransactionRequest(orderId, hargaProduk);
            transactionRequest.setCustomerDetails(initCustomerDetails());

            // Add item details
            ItemDetails itemDetails = new ItemDetails(
                    "ITEM-" + namaProduk.replaceAll(" ", "-"),
                    hargaProduk,
                    1,
                    namaProduk
            );

            ArrayList<ItemDetails> itemDetailsList = new ArrayList<>();
            itemDetailsList.add(itemDetails);
            transactionRequest.setItemDetails(itemDetailsList);

            // Set the snap token
            MidtransSDK.getInstance().setTransactionRequest(transactionRequest);
            MidtransSDK.getInstance().startPaymentUiFlow(this, snapToken);

            Log.d("Midtrans", "Payment UI started with token: " + snapToken);
        } catch (Exception e) {
            Log.e("Midtrans", "Error starting payment UI", e);
            Toast.makeText(this, "Error starting payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            resetButton();
        }
    }

    private CustomerDetails initCustomerDetails() {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setPhone("08123456789");
        customerDetails.setFirstName("Customer");
        customerDetails.setLastName("Name");
        customerDetails.setEmail("customer@example.com");
        customerDetails.setCustomerIdentifier("customer-001");

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFirstName("Customer");
        billingAddress.setLastName("Name");
        billingAddress.setAddress("Jl. Contoh No. 123");
        billingAddress.setCity("Malang");
        billingAddress.setPostalCode("65141");
        billingAddress.setPhone("08123456789");
        billingAddress.setCountryCode("IDN");
        customerDetails.setBillingAddress(billingAddress);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Customer");
        shippingAddress.setLastName("Name");
        shippingAddress.setAddress("Jl. Contoh No. 123");
        shippingAddress.setCity("Malang");
        shippingAddress.setPostalCode("65141");
        shippingAddress.setPhone("08123456789");
        customerDetails.setShippingAddress(shippingAddress);

        return customerDetails;
    }

    private void resetButton() {
        btnBeli.setEnabled(true);
        btnBeli.setText("Beli");
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(this, "Pembayaran berhasil! Transaction ID: " +
                            result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    handleSuccessPayment(result);
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Pembayaran pending. Silakan selesaikan pembayaran Anda.",
                            Toast.LENGTH_LONG).show();
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(this, "Pembayaran gagal: " +
                            result.getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(this, "Pembayaran dibatalkan", Toast.LENGTH_LONG).show();
        } else {
            if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
                Toast.makeText(this, "Transaksi tidak valid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Transaksi selesai", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleSuccessPayment(TransactionResult result) {
        // Log transaction details
        Log.d("Payment", "Transaction ID: " + result.getResponse().getTransactionId());
        Log.d("Payment", "Order ID: " + result.getResponse().getOrderId());
        Log.d("Payment", "Payment Type: " + result.getResponse().getPaymentType());
        Log.d("Payment", "Transaction Status: " + result.getResponse().getTransactionStatus());

        Toast.makeText(this, "Terima kasih telah berbelanja!\nPesanan Anda sedang diproses.",
                Toast.LENGTH_LONG).show();

        // Kembali ke activity sebelumnya
        finish();
    }
}