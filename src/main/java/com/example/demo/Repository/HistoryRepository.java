package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.History;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;

import jakarta.transaction.Transactional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
        @Transactional
        @Modifying
        @Query("DELETE FROM History k WHERE k.product = :product")
        void deleteAllByProduct(Product product);

        List<History> findAllByUser(User user);

}
