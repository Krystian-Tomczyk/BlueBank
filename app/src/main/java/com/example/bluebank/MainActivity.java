package com.example.bluebank;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedAccount = prefs.getString("ACCOUNT_NUMBER", null);

        if (savedAccount == null) {
            // BRAK KONTA -> Idziemy do Rejestracji (Parowania)
            startActivity(new Intent(this, RegistrationActivity.class));
        } else {
            // KONTO ZAPISANE -> Idziemy do Logowania (Sam PIN)
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish(); // Zamykamy Router, by użytkownik nie mógł do niego wrócić przyciskiem "Wstecz"
    }
}