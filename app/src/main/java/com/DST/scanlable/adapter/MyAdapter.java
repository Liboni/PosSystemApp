package com.DST.scanlable.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DST.scanlable.R;
import com.DST.scanlable.model.Product;

import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Context mContext;
    private List<Product> products;
    private LayoutInflater layoutInflater;
    private Map<String, Integer> dtIndexMap;
    private final int bgColor = Color.rgb(135, 206, 235);
    private Integer thisPosition = null;

    public MyAdapter(Context context, List<Product> list, Map<String, Integer> map) {
        mContext = context;
        products = list;
        dtIndexMap = map;
        layoutInflater = LayoutInflater.from(mContext);
    }

    public List<Product> getData(){
        return products;
    }

    public Integer getThisPosition() {
        return thisPosition;
    }

    public void setThisPosition(Integer thisPosition) {
        this.thisPosition = thisPosition;
    }

    @SuppressLint("NotifyDataSetChanged")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            setThisPosition(holder.getAdapterPosition());
            notifyDataSetChanged();
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = products.get(position);
        int findIndex = 0;
//        if (dtIndexMap != null) {
//            synchronized (dtIndexMap) {
//                if (dtIndexMap != null && epc != null)
//                    try {
//                        findIndex = dtIndexMap.get(epc);
//                    } catch (NullPointerException nex) {
//                        nex.printStackTrace();
//                    }
//            }
//        }
        holder.tvId.setText(String.valueOf(findIndex + 1));
        holder.tvCode.setText(product.getPCode());
        holder.tvName.setText(product.getPDesc());
        holder.tvCount.setText(String.valueOf(product.getTagCount()));
        if (getThisPosition() != null && position == getThisPosition()) {
            holder.itemView.setBackgroundColor(bgColor);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvCode;
        TextView tvName;
        TextView tvCount;

        public ViewHolder(final View view) {
            super(view);
            tvId = view.findViewById(R.id.idTV);
            tvCode = view.findViewById(R.id.codeTV);
            tvName = view.findViewById(R.id.productTV);
            tvCount = view.findViewById(R.id.countTV);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyData(List<Product> poiItemList, Map<String, Integer> map) {
        if (poiItemList != null) {
            products = poiItemList;
            dtIndexMap = map;
            notifyDataSetChanged();
        }
    }

}