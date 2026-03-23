package com.example.bluebank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    // ADRES IP TWOJEGO KOMPUTERA Z URUCHOMIONYM SPRING BOOT:
    private final String BANK_URL = "http://192.168.0.138:8081/api/bank";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        EditText etAccountNumber = findViewById(R.id.etAccountNumber);
        EditText etPin = findViewById(R.id.etPin);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String accNum = etAccountNumber.getText().toString();
            String pin = etPin.getText().toString();
            loginToBank(accNum, pin);
        });
    }

    private void loginToBank(String accountNumber, String pin) {
        // Tworzymy JSON z danymi logowania
        String json = "{\"accountNumber\":\"" + accountNumber + "\", \"pin\":\"" + pin + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BANK_URL + "/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Błąd połączenia z bankiem!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Zalogowano! Przechodzimy do pulpitu
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("ACCOUNT_NUMBER", accountNumber); // Przekazujemy nr konta
                        startActivity(intent);
                        finish(); // Zamykamy ekran logowania
                    } else {
                        Toast.makeText(LoginActivity.this, "Błędny nr konta lub PIN!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}