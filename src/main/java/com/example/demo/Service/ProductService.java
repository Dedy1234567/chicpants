package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Category;
import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        Product product = productRepository.getReferenceById(id);
        return product;
    }

    public List<Product> getCartByUser(User user) {
        return productRepository.findByUser(user);
    }

    public Product findById(Long id) {
        Product product = productRepository.getReferenceById(id);
        return product;
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);;
    }

    public List<Category> getAllCatagories(){
        return categoryRepository.findAll();
    }

}
