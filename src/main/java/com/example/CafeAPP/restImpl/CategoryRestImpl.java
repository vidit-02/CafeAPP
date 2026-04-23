package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.model.Category;
import com.example.CafeAPP.rest.CategoryRest;
import com.example.CafeAPP.service.CategoryService;
import com.example.CafeAPP.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryRestImpl implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
       return categoryService.addNewCategory(requestMap);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        return categoryService.getAllCategory(filterValue);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
       return categoryService.updateCategory(requestMap);
    }
}
