package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.JustLogged;
import com.example.demo.Model.Keranjang;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Repository.KeranjangRepository;

@Service
public class KeranjangService {
    
    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private ProductService productService;
 
    public void keranjang(Keranjang keranjang){
        keranjangRepository.save(keranjang);
    }
    public List<Keranjang> getKeranjangByUserId(Long userId){
        return keranjangRepository.findAllByUserId(userId);
    }
    

    public List<Keranjang> getKeranjangByJustLoggedUser(JustLogged justLogged){
        
        return keranjangRepository.findAllByUser(justLogged.getUser());
    }

    public void hapusKeranjangDenganProduct(Product product) {
        keranjangRepository.deleteAllByProduct(product);
    }

    public Boolean getKeranjangByProduct(Integer product,Long userId){
        List<Keranjang> keranjang = keranjangRepository.findAll();
        boolean cek=true;
        for (Keranjang x : keranjang) {
            if (x.getProduct().getId().equals(product) && x.getUser().getId().equals(userId)) {
                cek = false; // Mengembalikan keranjang yang ditemukan
            }
                   
        }
        return cek;
    }

    public void deleteKeranjang(Integer productId,User user){
        
        Keranjang keranjang = keranjangRepository.findByProductAndUser(productService.getProductById(productId),user);
        if(keranjang != null){
            keranjangRepository.delete(keranjang);

        }

    }

}
