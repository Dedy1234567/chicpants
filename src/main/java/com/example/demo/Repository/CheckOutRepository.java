package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.CheckOut;
import com.example.demo.Model.Product;

import jakarta.transaction.Transactional;

@Repository
public interface CheckOutRepository extends JpaRepository<CheckOut, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM CheckOut c WHERE c.product = :product")
    void deleteAllCheckOutByProduct(@Param("product") Product product);

}
