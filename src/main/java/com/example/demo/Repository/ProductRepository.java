package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Product;
import com.example.demo.Model.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // cari produk berdasarkan nama
    List<Product> findByNameProductContainingIgnoreCase(String nameProduct);

    // urutkan produk berdasarkan harga (ascending)
    @Query("SELECT p FROM Product p ORDER BY p.harga ASC")
    List<Product> findAllByHargaAsc();

    // ambil produk milik user tertentu
    List<Product> findByUser(User user);

    Product getReferenceById(Long id);
}
