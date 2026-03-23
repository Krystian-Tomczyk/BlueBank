package com.example.bluebank;

public class BankTransaction {
    private String title;
    private String amount;
    private String type; // np. "INCOME" lub "OUTCOME"

    public BankTransaction(String title, String amount, String type) {
        this.title = title;
        this.amount = amount;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getAmount() { return amount; }
    public String getType() { return type; }
}
