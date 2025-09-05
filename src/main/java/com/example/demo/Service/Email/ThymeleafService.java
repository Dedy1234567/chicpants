package com.example.demo.Service.Email;


import java.util.Map;

public interface ThymeleafService {
    String createContext(String template, Map<String, Object> variables);
}
