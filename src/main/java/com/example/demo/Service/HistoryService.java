package com.example.demo.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.History;
import com.example.demo.Model.JustLogged;
import com.example.demo.Model.Keranjang; // <-- Pastikan import ini ada
import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Repository.HistoryRepository;
import com.example.demo.Repository.KeranjangRepository; // <-- Tambahkan import ini
import com.example.demo.Repository.UserRepository;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ProductService productService;

    // Hapus @Autowired untuk KeranjangService yang lama
    // @Autowired
    // private KeranjangService keranjangService; 

    @Autowired
    private KeranjangRepository keranjangRepository; // <-- Tambahkan ini

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User tidak ditemukan dari konteks keamanan."));
    }

    @Transactional
    public void saveHistory(Integer quantity, Integer productId) {
        // 1. Dapatkan pengguna yang sedang login
        User currentUser = getCurrentUser();

        // 2. Dapatkan produk
        Product product = productService.getProductById(productId);

        // 3. Kurangi stok produk
        if (product.getStok() < quantity) {
            throw new RuntimeException("Error: Stok produk tidak mencukupi.");
        }
        product.setStok(product.getStok() - quantity);
        productService.saveProduct(product);

        // 4. Buat dan simpan riwayat
        History history = new History();
        history.setJumlah(quantity);
        history.setProduct(product);
        history.setUser(currentUser);
        historyRepository.save(history);

        // 5. Hapus item dari keranjang (jika ada) - LOGIKA BARU YANG SUDAH DIPERBAIKI
        Keranjang itemInCart = keranjangRepository.findByProductAndUser(product, currentUser);
        if (itemInCart != null) {
            keranjangRepository.delete(itemInCart);
        }
    }

    // ... (method-method lain di bawah ini tidak perlu diubah) ...

    public List<History> getHistoryForCurrentUser() {
        User currentUser = getCurrentUser();
        return historyRepository.findAllByUser(currentUser);
    }
    
    public List<History> getHistoryByUser(User user) {
        return historyRepository.findAllByUser(user);
    }

    public void hapusHistoryDenganProduct(Product product) {
        historyRepository.deleteAllByProduct(product);
    }

    public List<History> getHistoryByJustLoggedUser(JustLogged justLogged){
        
        return historyRepository.findAllByUser(justLogged.getUser());
    }

    public List<History> getAllHistory(){
        return historyRepository.findAll();
    }
    
    public List<History> getAllHistoryById(Long id){
        return historyRepository.findAllByUser(userRepository.getReferenceById(id));
    }


    public List<History> getAllHistoryByUserId(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            return historyRepository.findAllByUser(userOpt.get());
        }
        return List.of();
    }
}