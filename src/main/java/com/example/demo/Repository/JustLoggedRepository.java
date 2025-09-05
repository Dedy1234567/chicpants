package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.JustLogged;

@Repository
public interface JustLoggedRepository extends JpaRepository<JustLogged,Integer>{
    JustLogged findFirstByOrderByIdAsc();

}
