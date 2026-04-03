package com.example.bluebank;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class CardsActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    private String myAccountNumber;
    private LinearLayout layoutCardsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        layoutCardsContainer = findViewById(R.id.layoutCardsContainer);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        myAccountNumber = prefs.getString("ACCOUNT_NUMBER", null);

        fetchMyCards();
    }

    private void fetchMyCards() {
        Request request = new Request.Builder()
                .url(AppConfig.BANK_URL + "/account/" + myAccountNumber)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        JSONArray cardsArray = obj.getJSONArray("cards");

                        runOnUiThread(() -> {
                            layoutCardsContainer.removeAllViews();

                            if (cardsArray.length() == 0) {
                                TextView tvEmpty = new TextView(CardsActivity.this);
                                tvEmpty.setText("Nie masz jeszcze żadnych kart.");
                                layoutCardsContainer.addView(tvEmpty);
                                return;
                            }

                            for (int i = 0; i < cardsArray.length(); i++) {
                                try {
                                    JSONObject cardObj = cardsArray.getJSONObject(i);
                                    addCardView(cardObj.getString("cardUid"), cardObj.getBoolean("active"));
                                } catch (Exception e) { e.printStackTrace(); }
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void addCardView(String cardUid, boolean isActive) {
        // Tworzymy "Kafel" karty
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLayout.setPadding(30, 40, 30, 40);
        cardLayout.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        cardLayout.setLayoutParams(params);
        cardLayout.setElevation(4f);

        // Tekst z numerem UID
        TextView tvCardUid = new TextView(this);
        tvCardUid.setText("Karta NFC\n" + cardUid);
        tvCardUid.setTextSize(16f);
        tvCardUid.setTextColor(Color.parseColor("#1A237E"));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvCardUid.setLayoutParams(textParams);

        // Przełącznik Aktywna/Zablokowana
        Switch switchActive = new Switch(this);
        switchActive.setChecked(isActive);
        switchActive.setText(isActive ? "Aktywna" : "Zablokowana");
        switchActive.setTextColor(isActive ? Color.parseColor("#4CAF50") : Color.RED);

        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchActive.setText(isChecked ? "Aktywna" : "Zablokowana");
            switchActive.setTextColor(isChecked ? Color.parseColor("#4CAF50") : Color.RED);
            changeCardStatus(cardUid, isChecked);
        });

        cardLayout.addView(tvCardUid);
        cardLayout.addView(switchActive);
        layoutCardsContainer.addView(cardLayout);
    }

    private void changeCardStatus(String cardUid, boolean isActive) {
        Request request = new Request.Builder()
                .url(AppConfig.BANK_URL + "/card/" + cardUid + "/status?isActive=" + isActive)
                .patch(RequestBody.create(null, new byte[0]))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) {
                runOnUiThread(() -> Toast.makeText(CardsActivity.this, "Status zmieniony", Toast.LENGTH_SHORT).show());
            }
        });
    }
}