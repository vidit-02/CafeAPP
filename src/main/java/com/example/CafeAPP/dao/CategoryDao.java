package com.example.CafeAPP.dao;

import com.example.CafeAPP.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category, Integer> {

    List<Category> getAllCategories();
}
