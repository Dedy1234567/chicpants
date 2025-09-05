package com.example.demo.Controller;

import com.example.demo.Model.History;
import com.example.demo.Service.HistoryService;
import com.example.demo.dto.CheckoutRequest; // <-- Import DTO yang baru
import com.example.demo.dto.MessageResponse; // <-- Import untuk respons
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // <-- Import untuk validasi

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private HistoryService historyService;

    // Endpoint untuk memproses checkout (menggantikan /success)
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequest request) {
        // Menambahkan validasi untuk memastikan jumlah produk dan kuantitas sama
        if (request.getProductIds().size() != request.getQuantities().size()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Jumlah produk dan kuantitas tidak cocok!"));
        }

        try {
            for (int i = 0; i < request.getProductIds().size(); i++) {
                Integer productId = request.getProductIds().get(i);
                Integer quantity = request.getQuantities().get(i);
                historyService.saveHistory(quantity, productId);
            }
            return ResponseEntity.ok(new MessageResponse("Checkout berhasil diproses!"));
        } catch (RuntimeException e) {
            // Mengembalikan pesan error yang lebih spesifik dari service (misal: stok tidak cukup)
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // Endpoint untuk melihat riwayat belanja pengguna (menggantikan /profile)
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<History>> getMyHistory() {
        List<History> myHistory = historyService.getHistoryForCurrentUser();
        return ResponseEntity.ok(myHistory);
    }
}