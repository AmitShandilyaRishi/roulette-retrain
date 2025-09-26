package com.amitthakur.roulette.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amitthakur.roulette.R;
import com.amitthakur.roulette.model.SpinRecord;
import java.util.ArrayList;
import java.util.List;

public class SpinAdapter extends RecyclerView.Adapter<SpinAdapter.VH> {
    private final List<SpinRecord> spins = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SpinRecord r = spins.get(pos);
        h.dealer.setText("Dealer: " + r.getDealerId());

        // Last 5 spins
        List<Integer> ln = r.getLastNumbers();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ln.size(); i++) {
            sb.append(ln.get(i));
            if (i < ln.size() - 1) sb.append(", ");
        }
        sb.append("]");
        h.last5.setText("Last5: " + sb);

        h.dropSector.setText("Sector: " + r.getDropSector());
    }

    @Override public int getItemCount() { return spins.size(); }

    public void submitList(List<SpinRecord> list) {
        spins.clear();
        spins.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView dealer, last5, dropSector;
        VH(@NonNull View v) {
            super(v);
            dealer     = v.findViewById(R.id.tvDealer);
            last5      = v.findViewById(R.id.tvLast5);
            dropSector = v.findViewById(R.id.tvDropSector);
        }
    }
}