package com.example.bluebank;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private String accountNumber;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_settings);
        accountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnCards = findViewById(R.id.btnCards);

        btnLogout.setOnClickListener(v -> {
            // 1. Czyścimy pamięć
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // 2. Wyrzucamy klienta do ekranu Rejestracji i czyścimy historię aktywności
            Intent intent = new Intent(SettingsActivity.this, RegistrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCards.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CardsActivity.class);
            intent.putExtra("ACCOUNT_NUMBER", accountNumber);
            startActivity(intent);
        });
    }
}
