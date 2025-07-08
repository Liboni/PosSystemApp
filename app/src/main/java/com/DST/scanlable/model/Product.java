package com.DST.scanlable.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String pCode;
    private String hsCode;
    private String pDesc;
    private String brand;
    private String category;
    private double price;
    private List<TagInfo> tags;
    private boolean expanded = false;

    public Product(String pCode, String hsCode, String pDesc, String brand, String category, double price) {
        this.pCode = pCode;
        this.hsCode = hsCode;
        this.pDesc = pDesc;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.tags = new ArrayList<>();
    }

    public String getPCode() {
        return pCode;
    }

    public String getHsCode() {
        return hsCode;
    }

    public String getPDesc() {
        return pDesc;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public List<TagInfo> getTags() {
        return tags;
    }

    public void addTag(TagInfo tag) {
        // Check if tag already exists
        for (TagInfo existingTag : tags) {
            if (existingTag.getEpc().equals(tag.getEpc())) {
                return; // Tag already exists, don't add it again
            }
        }
        tags.add(tag);
    }

    public int getTagCount() {
        return tags.size();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}