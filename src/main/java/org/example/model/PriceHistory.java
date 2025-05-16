package org.example.model;

import java.time.LocalDate;

public class PriceHistory {

    private Float price;
    private String store;
    private String category;
    private String brand;
    private LocalDate date;

    public PriceHistory() {
    }

    public PriceHistory(Float price, String store, String category, String brand, LocalDate timestamp) {
        this.price = price;
        this.store = store;
        this.category = category;
        this.brand = brand;
        this.date = timestamp;

    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Float getPrice() {
        return price;
    }

    public String getStore() {
        return store;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public LocalDate getDate() {
        return date;
    }
};
