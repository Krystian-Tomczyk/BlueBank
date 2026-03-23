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

public class MainActivity extends AppCompatActivity {

    // Upewnij się, że to IP jest zgodne z IPv4 Twojego komputera
    private final String BANK_URL = "http://192.168.0.138:8081/api/bank";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ładujemy widok logowania

        client = new OkHttpClient();

        EditText etAccountNumber = findViewById(R.id.etAccountNumber);
        EditText etPin = findViewById(R.id.etPin);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String accNum = etAccountNumber.getText().toString().trim();
            String pin = etPin.getText().toString().trim();

            if (accNum.isEmpty() || pin.isEmpty()) {
                Toast.makeText(MainActivity.this, "Uzupełnij numer konta i PIN!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Zmieniamy tekst przycisku na czas ładowania
            btnLogin.setText("LOGOWANIE...");
            btnLogin.setEnabled(false);

            loginToBank(accNum, pin, btnLogin);
        });
    }

    private void loginToBank(String accountNumber, String pin, Button btnLogin) {
        String json = "{\"accountNumber\":\"" + accountNumber + "\", \"pin\":\"" + pin + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BANK_URL + "/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Błąd połączenia z bankiem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnLogin.setText("ZALOGUJ SIĘ");
                    btnLogin.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    btnLogin.setText("ZALOGUJ SIĘ");
                    btnLogin.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show();

                        // Przechodzimy do pulpitu
                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                        intent.putExtra("ACCOUNT_NUMBER", accountNumber);
                        startActivity(intent);
                        finish(); // Zamykamy ekran logowania
                    } else {
                        Toast.makeText(MainActivity.this, "Błędny nr konta lub PIN! (Kod: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}