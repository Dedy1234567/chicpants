package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Category;
import com.example.demo.Repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Ambil semua kategori
    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    // Simpan kategori baru (opsional)
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    // Ambil kategori berdasarkan ID
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }
}

