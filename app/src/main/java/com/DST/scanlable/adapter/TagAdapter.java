package com.DST.scanlable.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DST.scanlable.R;
import com.rfid.InventoryTagMap;

import java.util.List;
import java.util.Map;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private Context mContext;
    private List<InventoryTagMap> mList;
    private LayoutInflater layoutInflater;
    private Map<String, Integer> dtIndexMap;
    private final int bgColor = Color.rgb(135, 206, 235);
    private Integer thisPosition = null;

    public TagAdapter(Context context, List<InventoryTagMap> list, Map<String, Integer> map) {
        mContext = context;
        mList = list;
        dtIndexMap = map;
        layoutInflater = LayoutInflater.from(mContext);
    }

    public List<InventoryTagMap> getData(){
        return mList;
    }

    public Integer getThisPosition() {
        return thisPosition;
    }

    public void setThisPosition(Integer thisPosition) {
        this.thisPosition = thisPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tag, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            setThisPosition(holder.getAdapterPosition());
            notifyDataSetChanged();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String epc = mList.get(position).strEPC;
        int findIndex = 0;
        if (dtIndexMap != null) {
            synchronized (dtIndexMap) {
                if (dtIndexMap != null && epc != null)
                    try {
                        findIndex = dtIndexMap.get(epc);
                    }catch (NullPointerException nex){
                        nex.printStackTrace();
                    }
            }
        }
        holder.tvId.setText(String.valueOf(findIndex + 1));
        holder.tvEpc.setText(epc);
        if (getThisPosition() != null && position == getThisPosition()) {
            holder.itemView.setBackgroundColor(bgColor);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvEpc;

        public ViewHolder(final View view) {
            super(view);
            tvId = view.findViewById(R.id.id_text);
            tvEpc = view.findViewById(R.id.epc_text);
        }
    }

    public void notifyData(List<InventoryTagMap> poiItemList, Map<String, Integer> map) {
        if (poiItemList != null) {
            mList = poiItemList;
            dtIndexMap = map;
            notifyDataSetChanged();
        }
    }

}