package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.rest.ProductRest;
import com.example.CafeAPP.service.ProductService;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ProductRestImpl implements ProductRest {

    @Autowired
    ProductService productService;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        return productService.addNewProduct(requestMap);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        return productService.getAllProducts();
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        return productService.updateProduct(requestMap);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        return productService.deleteProduct(id);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        return productService.updateProductStatus(requestMap);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getProductByCategory(Integer id) {
        return productService.getProductByCategory(id);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        return productService.getProductById(id);
    }
}
