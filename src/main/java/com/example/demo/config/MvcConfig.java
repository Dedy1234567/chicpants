package com.example.demo.config; // <-- Sesuaikan dengan package Anda

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL Path: /uploads/**
        // Ini adalah path "virtual" yang akan Anda gunakan di HTML.
        registry.addResourceHandler("/uploads/**")
                // Physical Path: file:./upload-dir/image/
                // Ini adalah lokasi folder asli di server.
                // Pastikan ada garis miring di akhir.
                .addResourceLocations("file:./upload-dir/image/");
    }
}