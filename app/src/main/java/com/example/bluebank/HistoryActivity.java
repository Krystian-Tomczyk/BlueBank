package com.example.bluebank;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class HistoryActivity extends AppCompatActivity {

    private final String HISTORY_URL = "http://192.168.0.138:8081/api/bank/transactions/";
    private String accountNumber;
    private RecyclerView rvTransactionHistory;
    private TransactionAdapter adapter;
    private List<BankTransaction> fullTransactionList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private TextView tvMonthlyExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Pobieramy numer konta (zakładam, że przekazujesz go z Dashboardu)
        accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");

        // Inicjalizacja listy
        rvTransactionHistory = findViewById(R.id.rvTransactionHistory);
        rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));
        tvMonthlyExpenses = findViewById(R.id.tvMonthlyExpenses);

        adapter = new TransactionAdapter(fullTransactionList);
        rvTransactionHistory.setAdapter(adapter);

        fetchFullHistory();
    }

    private void fetchFullHistory() {
        if (accountNumber == null) return;

        Request request = new Request.Builder()
                .url(HISTORY_URL + accountNumber)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HistoryActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONArray array = new JSONArray(json);

                        final List<BankTransaction> tempList = new ArrayList<>();
                        double totalExpenses = 0;

                        // Pobranie bieżącego miesiąca i roku do filtrowania (np. "2026-03")
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        String currentMonthPrefix = String.format("%d-%02d",
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH) + 1);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String description = obj.optString("description", "Brak opisu");
                            String amountStr = obj.optString("amount", "0.00");
                            String type = obj.optString("type", "OUTCOME");
                            String timestamp = obj.optString("timestamp", "");

                            // 1. Dodanie do listy obiektów
                            tempList.add(new BankTransaction(description, amountStr, type));

                            // 2. Obliczanie wydatków z bieżącego miesiąca
                            if (timestamp.startsWith(currentMonthPrefix)) {
                                try {
                                    double amount = Double.parseDouble(amountStr);
                                    // Sumujemy tylko wartości ujemne
                                    if (amount < 0) {
                                        totalExpenses += amount;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("HISTORY_ERROR", "Błędny format kwoty: " + amountStr);
                                }
                            }
                        }

                        // Przygotowanie tekstu do wyświetlenia
                        final String finalExpensesDisplay = String.format("Wydatki w tym miesiącu: %.2f PLN", totalExpenses);

                        // Aktualizacja UI w wątku głównym
                        runOnUiThread(() -> {
                            fullTransactionList.clear();
                            fullTransactionList.addAll(tempList);
                            adapter.notifyDataSetChanged();

                            if (tvMonthlyExpenses != null) {
                                tvMonthlyExpenses.setText(finalExpensesDisplay);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("HISTORY_ERROR", "Błąd parsowania: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
