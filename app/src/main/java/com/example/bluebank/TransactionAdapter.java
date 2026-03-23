package com.example.bluebank;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<BankTransaction> transactions;

    public TransactionAdapter(List<BankTransaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BankTransaction transaction = transactions.get(position);
        holder.text1.setText(transaction.getTitle());
        holder.text2.setText(transaction.getAmount() + " PLN");

        // Prosta logika kolorowania kwoty
        if (transaction.getAmount().startsWith("-")) {
            holder.text2.setTextColor(Color.RED);
        } else {
            holder.text2.setTextColor(Color.parseColor("#2E7D32")); // Zielony
        }
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
