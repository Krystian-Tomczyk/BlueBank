package com.example.bluebank;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import java.io.IOException;

public class TransferActivity extends AppCompatActivity {

    private String myAccountNumber;
    private EditText etPhone, etAmount;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        myAccountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");
        etPhone = findViewById(R.id.etTargetPhone);
        etAmount = findViewById(R.id.etTransferAmount);
        Button btnSend = findViewById(R.id.btnSendTransfer);

        btnSend.setOnClickListener(v -> performTransfer());
    }

    private void performTransfer() {
        String targetPhone = etPhone.getText().toString();
        String amount = etAmount.getText().toString();

        if (targetPhone.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        // Budujemy żądanie do BlikServer (port 8082)
        RequestBody body = new FormBody.Builder()
                .add("fromAccount", myAccountNumber)
                .add("toPhone", targetPhone)
                .add("amount", amount)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.0.138:8082/api/blik/transfer")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TransferActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                runOnUiThread(() -> {
                    if (result.equals("SUCCESS")) {
                        Toast.makeText(TransferActivity.this, "Przelew wysłany pomyślnie!", Toast.LENGTH_LONG).show();
                        finish(); // Zamknij ekran po sukcesie
                    } else {
                        Toast.makeText(TransferActivity.this, "Błąd: " + result, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}