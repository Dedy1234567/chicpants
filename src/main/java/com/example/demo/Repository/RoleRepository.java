package com.example.demo.Repository;

import com.example.demo.Model.Role; // Pastikan package Model Anda benar
import com.example.demo.Model.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Method untuk mencari peran berdasarkan namanya
    Optional<Role> findByName(ERole name);
}
