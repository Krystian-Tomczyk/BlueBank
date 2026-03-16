package com.example.bluebank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private final String BANK_URL = "http://192.168.0.102:8081/api/bank";
    private String accountNumber;
    private TextView tvBalance;
    private SwipeRefreshLayout swipeRefresh;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Pobranie numeru konta przekazanego przy logowaniu
        accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");

        // Inicjalizacja widoków
        tvBalance = findViewById(R.id.tvBalance);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // Ustawienie koloru animacji odświeżania
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_dark);

        // Obsługa gestu "Swipe to Refresh"
        swipeRefresh.setOnRefreshListener(this::fetchBalance);

        // Inicjalizacja przycisków menu
        initMenu();

        // Pierwsze pobranie salda przy wejściu na ekran
        fetchBalance();
    }

    private void initMenu() {
        CardView cardBlik = findViewById(R.id.cardBlik);
        CardView cardTransfer = findViewById(R.id.cardTransfer);
        CardView cardHistory = findViewById(R.id.cardHistory);
        CardView cardSettings = findViewById(R.id.cardSettings);

        cardBlik.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BlikActivity.class);
            intent.putExtra("ACCOUNT_NUMBER", accountNumber);
            startActivity(intent);
        });

        cardTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, TransferActivity.class);
            startActivity(intent);
        });

        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void fetchBalance() {
        // Jeśli nie jest to odświeżanie gestem, a chcemy pokazać kółko:
        // swipeRefresh.setRefreshing(true);

        Request request = new Request.Builder()
                .url(BANK_URL + "/account/" + accountNumber)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(DashboardActivity.this, "Błąd połączenia z serwerem", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        String balance = obj.getString("balance");

                        runOnUiThread(() -> {
                            tvBalance.setText(balance + " PLN");
                            swipeRefresh.setRefreshing(false);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                } else {
                    runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                }
            }
        });
    }
}