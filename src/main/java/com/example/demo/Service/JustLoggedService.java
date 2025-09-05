package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.JustLogged;
import com.example.demo.Repository.JustLoggedRepository;

@Service
public class JustLoggedService{

    @Autowired
    private JustLoggedRepository justLoggedRepository;
    
    public void saveJustLogged(JustLogged justLogged){
        justLoggedRepository.save(justLogged);
    }
    
    public JustLogged getJustLoggedByfirst(){
        return  justLoggedRepository.findFirstByOrderByIdAsc();
    }
}
