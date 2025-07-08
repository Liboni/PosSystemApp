package com.DST.scanlable.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DST.scanlable.R;
import com.DST.scanlable.model.Product;
import com.DST.scanlable.model.TagInfo;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set product details
        holder.productCode.setText(product.getPCode());
        holder.productName.setText(product.getPDesc());
        holder.productBrand.setText(product.getBrand());
        holder.productCategory.setText(product.getCategory());
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
        holder.tagCount.setText(String.valueOf(product.getTagCount()));

        // Set toggle click listener
        holder.productItem.setOnClickListener(v -> {
            product.toggleExpanded();
            notifyItemChanged(position);
        });

        // Set visibility of tag list based on expanded state
        if (product.isExpanded()) {
            holder.tagsContainer.setVisibility(View.VISIBLE);
            holder.expandIcon.setImageResource(R.drawable.ic_collapse);

            // Clear existing tags
            holder.tagsContainer.removeAllViews();

            // Add tag views
            for (TagInfo tag : product.getTags()) {
                View tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, holder.tagsContainer, false);
                TextView tagEpc = tagView.findViewById(R.id.tag_epc);
                tagEpc.setText(tag.getEpc());
                holder.tagsContainer.addView(tagView);
            }
        } else {
            holder.tagsContainer.setVisibility(View.GONE);
            holder.expandIcon.setImageResource(R.drawable.ic_expand);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView productCode;
        public TextView productName;
        public TextView productBrand;
        public TextView productCategory;
        public TextView productPrice;
        public TextView tagCount;
        public ImageView expandIcon;
        public LinearLayout tagsContainer;
        public View productItem;

        public ViewHolder(View view) {
            super(view);
            productItem = view;
            productCode = view.findViewById(R.id.product_code);
            productName = view.findViewById(R.id.product_name);
            productBrand = view.findViewById(R.id.product_brand);
            productCategory = view.findViewById(R.id.product_category);
            productPrice = view.findViewById(R.id.product_price);
            tagCount = view.findViewById(R.id.tag_count);
            expandIcon = view.findViewById(R.id.expand_icon);
            tagsContainer = view.findViewById(R.id.tags_container);
        }
    }
}