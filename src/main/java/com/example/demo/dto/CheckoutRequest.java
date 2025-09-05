package com.example.demo.dto;


import java.util.List;
import jakarta.validation.constraints.NotEmpty;

public class CheckoutRequest {

    @NotEmpty(message = "Product ID tidak boleh kosong")
    private List<Integer> productIds;

    @NotEmpty(message = "Kuantitas tidak boleh kosong")
    private List<Integer> quantities;

    // Getters dan Setters ini sangat penting agar Spring bisa membaca JSON
    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }
}