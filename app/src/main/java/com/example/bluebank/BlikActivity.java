package com.example.bluebank;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BlikActivity extends AppCompatActivity {

    private final String BANK_URL = "http://192.168.0.102:8081/api/bank";
    private String accountNumber;
    private OkHttpClient client = new OkHttpClient();

    private TextView tvBlikCode, tvTimer;
    private Button btnGenerateBlik;

    private CountDownTimer countDownTimer;
    private final long TOTAL_TIME = 120000; // 2 minuty

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isDialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blik);

        accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");
        tvBlikCode = findViewById(R.id.tvBlikCode);
        tvTimer = findViewById(R.id.tvTimer);
        btnGenerateBlik = findViewById(R.id.btnGenerateBlik);

        btnGenerateBlik.setOnClickListener(v -> generateBlikCode());

        // Sprawdź czy istnieje aktywny kod w pamięci
        checkExistingCode();

        // Uruchom nasłuchiwanie transakcji
        startPollingBlik();
    }

    private void checkExistingCode() {
        SharedPreferences prefs = getSharedPreferences("BlikPrefs", MODE_PRIVATE);
        String savedCode = prefs.getString("currentCode", null);
        long timestamp = prefs.getLong("timestamp", 0);
        long currentTime = System.currentTimeMillis();

        if (savedCode != null && (currentTime - timestamp) < TOTAL_TIME) {
            long timeLeft = TOTAL_TIME - (currentTime - timestamp);
            updateBlikUI(savedCode);
            startTimer(timeLeft);
        }
    }

    private void generateBlikCode() {
        String url = BANK_URL + "/blik/generate?accountNumber=" + accountNumber;
        Request request = new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(BlikActivity.this, "Błąd serwera BLIK", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String code = response.body().string();

                    // Zapisz do pamięci urządzenia
                    SharedPreferences.Editor editor = getSharedPreferences("BlikPrefs", MODE_PRIVATE).edit();
                    editor.putString("currentCode", code);
                    editor.putLong("timestamp", System.currentTimeMillis());
                    editor.apply();

                    runOnUiThread(() -> {
                        updateBlikUI(code);
                        startTimer(TOTAL_TIME);
                    });
                }
            }
        });
    }

    private void updateBlikUI(String code) {
        if (code.length() == 6) {
            String formatted = code.substring(0, 3) + " " + code.substring(3);
            tvBlikCode.setText(formatted);
            btnGenerateBlik.setEnabled(false);
            btnGenerateBlik.setAlpha(0.5f);
        }
    }

    private void startTimer(long duration) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tvTimer.setText(String.format("Kod wygaśnie za %d:%02d", minutes, seconds));
            }

            public void onFinish() {
                resetBlik();
            }
        }.start();
    }

    private void resetBlik() {
        tvBlikCode.setText("--- ---");
        tvTimer.setText("Kod wygasł");
        btnGenerateBlik.setEnabled(true);
        btnGenerateBlik.setAlpha(1.0f);

        SharedPreferences.Editor editor = getSharedPreferences("BlikPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    // --- LOGIKA POLLINGU (ZATWIERDZANIE PŁATNOŚCI) ---

    private void startPollingBlik() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkPendingTransaction();
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(pollingRunnable);
    }

    private void checkPendingTransaction() {
        if (isDialogShowing) return;
        Request request = new Request.Builder().url(BANK_URL + "/blik/pending/" + accountNumber).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200 && response.body() != null) {
                    try {
                        JSONObject tx = new JSONObject(response.body().string());
                        runOnUiThread(() -> {
                            try {
                                showBlikDialog(tx.getString("amount"), tx.getString("storeName"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void showBlikDialog(String amount, String store) {
        isDialogShowing = true;
        new AlertDialog.Builder(this)
                .setTitle("Autoryzacja BLIK")
                .setMessage("Czy chcesz zapłacić " + amount + " PLN w " + store + "?")
                .setPositiveButton("Tak", (d, w) -> authorize(true))
                .setNegativeButton("Nie", (d, w) -> authorize(false))
                .setCancelable(false).show();
    }

    private void authorize(boolean status) {
        String url = BANK_URL + "/blik/authorize?accountNumber=" + accountNumber + "&isApproved=" + status;
        Request request = new Request.Builder().url(url).post(RequestBody.create(new byte[0])).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { isDialogShowing = false; }
            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    isDialogShowing = false;
                    resetBlik();
                    Toast.makeText(BlikActivity.this, status ? "Zapłacono!" : "Odrzucono", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        handler.removeCallbacks(pollingRunnable);
    }
}