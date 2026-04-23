package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.CategoryDao;
import com.example.CafeAPP.dao.ProductDao;
import com.example.CafeAPP.exception.CafeException;
import com.example.CafeAPP.model.Category;
import com.example.CafeAPP.model.Product;
import com.example.CafeAPP.service.ProductService;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.wrapper.ProductWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    ProductDao productDao;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        log.info("inside addNewProduct :: {}", requestMap);

        try {

            // Admin authorization check
            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            // Validate request payload
            if (!validateProductMap(requestMap, false)) {
                throw new CafeException(
                        CafeConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST
                );
            }

            // Save product
            productDao.save(
                    getProductFromMap(requestMap, false)
            );
            return CafeUtils.getResponseEntity(
                    CafeConstants.PRODUCT_ADDED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (DataAccessException ex) {
            log.error("Database error while adding product", ex);
            throw new CafeException(
                    "Unable to add product",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while adding product", ex);
            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
            return new ResponseEntity<>(productDao.getAllProducts(),HttpStatus.OK);
        }  catch (DataAccessException ex) {
            log.error("Database error while getting all products", ex);
            throw new CafeException(
                    "Unable to get products",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {
            log.error("Unexpected error while getting all products", ex);
            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

       private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        Product product = new Product();
        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setName(requestMap.get("name"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        product.setDescription(requestMap.get("description"));
        product.setCategory(category);
        return product;

    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")){
            if(requestMap.containsKey("id") && validateId){
                return true;
            } else if(!validateId) {
                return true;
            }
        }
        return false;
    }


    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        log.info("inside updateProduct :: {}", requestMap);

        try {

            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            if (!validateProductMap(requestMap, true)) {
                throw new CafeException(
                        CafeConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST
                );
            }

            Integer id = Integer.parseInt(requestMap.get("id"));

            Optional<Product> optional = productDao.findById(id);

            if (optional.isEmpty()) {
                throw new CafeException(
                        CafeConstants.PRODUCT_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            Product product = getProductFromMap(requestMap, true);
            product.setStatus(optional.get().getStatus());

            productDao.save(product);

            return CafeUtils.getResponseEntity(
                    CafeConstants.PRODUCT_UPDATED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (NumberFormatException ex) {

            throw new CafeException(
                    "Invalid product id",
                    HttpStatus.BAD_REQUEST
            );

        } catch (DataAccessException ex) {

            log.error("Database error while updating product", ex);

            throw new CafeException(
                    "Unable to update product",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while updating product", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        log.info("inside deleteProduct :: {}", id);

        try {

            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            Optional<Product> optional = productDao.findById(id);

            if (optional.isEmpty()) {
                throw new CafeException(
                        CafeConstants.PRODUCT_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            productDao.deleteById(id);

            return CafeUtils.getResponseEntity(
                    CafeConstants.PRODUCT_DELETED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (DataAccessException ex) {

            log.error("Database error while deleting product", ex);

            throw new CafeException(
                    "Unable to delete product",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while deleting product", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
        log.info("inside updateProductStatus :: {}", requestMap);

        try {

            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            Integer id = Integer.parseInt(requestMap.get("id"));

            Optional<Product> optional = productDao.findById(id);

            if (optional.isEmpty()) {
                throw new CafeException(
                        CafeConstants.PRODUCT_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            productDao.updateProductStatus(
                    requestMap.get("status"),
                    id
            );

            return CafeUtils.getResponseEntity(
                    CafeConstants.PRODUCT_UPDATED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (NumberFormatException ex) {

            throw new CafeException(
                    "Invalid product id",
                    HttpStatus.BAD_REQUEST
            );

        } catch (DataAccessException ex) {

            log.error("Database error while updating product status", ex);

            throw new CafeException(
                    "Unable to update product status",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while updating product status", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getProductByCategory(Integer id) {
        log.info("inside getProductByCategory :: {}", id);

        try {

            return ResponseEntity.ok(
                    productDao.getProductByCategory(id)
            );

        } catch (DataAccessException ex) {

            log.error("Database error while fetching products by category", ex);

            throw new CafeException(
                    "Unable to fetch products",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            log.error("Unexpected error while fetching products by category", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        log.info("inside getProductById :: {}", id);

        try {

            ProductWrapper product = productDao.getProductById(id);

            if (product == null) {
                throw new CafeException(
                        CafeConstants.PRODUCT_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }

            return ResponseEntity.ok(product);

        } catch (DataAccessException ex) {

            log.error("Database error while fetching product by id", ex);

            throw new CafeException(
                    "Unable to fetch product",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while fetching product by id", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


}
