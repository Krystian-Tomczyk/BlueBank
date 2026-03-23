package com.example.bluebank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private final String BANK_URL = "http://192.168.0.138:8081/api/bank";
    private String accountNumber;
    private TextView tvBalance;
    private SwipeRefreshLayout swipeRefresh;
    private OkHttpClient client = new OkHttpClient();

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<BankTransaction> transactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Pobranie numeru konta przekazanego przy logowaniu
        accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");

        // 2. Inicjalizacja widoków podstawowych
        tvBalance = findViewById(R.id.tvBalance);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // 3. Konfiguracja RecyclerView dla historii transakcji
        rvTransactions = findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Zapobiega "skakaniu" ScrollView przy ładowaniu listy
        rvTransactions.setNestedScrollingEnabled(false);

        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(adapter);

        // 4. Ustawienie koloru animacji odświeżania
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_dark);

        // 5. Obsługa gestu "Swipe to Refresh" - odświeżamy obie rzeczy na raz
        swipeRefresh.setOnRefreshListener(() -> {
            fetchBalance();
            fetchHistory();
        });

        // 6. Inicjalizacja przycisków menu
        initMenu();

        // 7. Pierwsze pobranie danych przy wejściu na ekran
        fetchBalance();
        fetchHistory();
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
            intent.putExtra("ACCOUNT_NUMBER", accountNumber);
            startActivity(intent);
        });

        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            intent.putExtra("ACCOUNT_NUMBER", accountNumber);
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

    private void fetchHistory() {
        Request request = new Request.Builder()
                .url(BANK_URL + "/transactions/" + accountNumber) // Twój endpoint
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { /* obsługa błędu */ }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONArray array = new JSONArray(json);
                        transactionList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            // ZMIANA TUTAJ: Zamiast "title" dajemy "description"
                            String titleFromApi = obj.optString("description", "Brak tytułu");
                            String amountFromApi = obj.optString("amount", "0.00");
                            String typeFromApi = obj.optString("type", "OUTCOME");

                            transactionList.add(new BankTransaction(
                                    titleFromApi,
                                    amountFromApi,
                                    typeFromApi
                            ));
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}