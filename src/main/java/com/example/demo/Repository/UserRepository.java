package com.example.demo.Repository;

import com.example.demo.Model.Role;
import com.example.demo.Model.User; // Pastikan package Model Anda benar

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Method untuk mencari user berdasarkan username saat login
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);


    // Method untuk memeriksa apakah username sudah ada saat registrasi
    Boolean existsByUsername(String username);

    // Method untuk memeriksa apakah email sudah ada saat registrasi
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
    List<User> findAllByRoleName(@Param("role") Role.ERole role);
    
    User findByUsernameAndPassword(String username, String password);

    List<User> findByUsernameContainingIgnoreCase(String username);
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

}