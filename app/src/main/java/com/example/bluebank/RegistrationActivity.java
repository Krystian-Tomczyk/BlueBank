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

public class RegistrationActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration); // Twój XML z Nr konta i PINem

        EditText etAccountNumber = findViewById(R.id.etAccountNumber);
        EditText etPin = findViewById(R.id.etPin);
        Button btnRegister = findViewById(R.id.btnLogin); // Przycisk "POWIĄŻ"

        btnRegister.setOnClickListener(v -> {
            String accNum = etAccountNumber.getText().toString().trim();
            String pin = etPin.getText().toString().trim();

            if (accNum.isEmpty() || pin.isEmpty()) return;
            registerDevice(accNum, pin);
        });
    }

    private void registerDevice(String accountNumber, String pin) {
        String json = "{\"accountNumber\":\"" + accountNumber + "\", \"pin\":\"" + pin + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().url(AppConfig.BANK_URL + "/login").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(RegistrationActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // SUKCES! Zapisujemy nr konta "na stałe" w telefonie
                        SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                        editor.putString("ACCOUNT_NUMBER", accountNumber);
                        editor.apply();

                        Toast.makeText(RegistrationActivity.this, "Urządzenie powiązane!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Błędne dane!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}