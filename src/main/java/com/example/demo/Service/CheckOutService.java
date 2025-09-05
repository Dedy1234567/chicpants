package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.CheckOut;
import com.example.demo.Model.Product;
import com.example.demo.Repository.CheckOutRepository;
import com.example.demo.Repository.ProductRepository;

@Service
public class CheckOutService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CheckOutRepository checkOutRepository;


    public Product addCheckOutProduct(Integer id){
        Product product = productRepository.getReferenceById(id);
        return product;
    }

    public void saveCheckOut(CheckOut checkOut){
        checkOutRepository.save(checkOut);
    }

    public List<CheckOut> getAllCheckOut() {
        return checkOutRepository.findAll();
    }

    public void hapusCheckOutDenganProduct(Product product) {
        checkOutRepository.deleteAllCheckOutByProduct(product);
    }
    public void deleteAllCheckOut(){
        checkOutRepository.deleteAll();
    }
}
