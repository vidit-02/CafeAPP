package com.example.CafeAPP.rest;

import com.example.CafeAPP.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path="/category")
public interface CategoryRest {

    @PostMapping(path="/add")
    ResponseEntity<String> addNewCategory(@RequestBody Map<String,String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<Category>> getAllCategory(@RequestParam(required = false) String filterValue);
    //filter value is passed in case we want to get all categories with at least one product otherwise if we need all categories we can simply not send any body as required is false
    //we use requestParam as we only need a parameter


    @PostMapping(path= "/update")
    ResponseEntity<String> updateCategory(@RequestBody Map<String,String> requestMap);
}
