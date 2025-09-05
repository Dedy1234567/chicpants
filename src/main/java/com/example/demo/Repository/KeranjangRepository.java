package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Keranjang;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;

import jakarta.transaction.Transactional;

@Repository
public interface KeranjangRepository extends JpaRepository<Keranjang, Integer>{
    List<Keranjang> findAllByUser(User user);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Keranjang k WHERE k.product = :product")
    void deleteAllByProduct(Product product);

    Keranjang findByProductAndUser(Product productById, User user);
    List<Keranjang> findAllByUserId(Long userId);


    
}
