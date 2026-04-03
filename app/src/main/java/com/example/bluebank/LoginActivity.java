package com.example.bluebank;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    private String savedAccountNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Odczytujemy zapisany numer konta
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        savedAccountNumber = prefs.getString("ACCOUNT_NUMBER", "");

        EditText etPin = findViewById(R.id.etPin); // Tylko pole PIN
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String pin = etPin.getText().toString().trim();
            if (!pin.isEmpty()) loginToBank(pin);
        });
    }

    private void loginToBank(String pin) {
        // Wysyłamy nr konta (z pamięci) i wpisany PIN
        String json = "{\"accountNumber\":\"" + savedAccountNumber + "\", \"pin\":\"" + pin + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().url(AppConfig.BANK_URL + "/login").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Błędny PIN!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}