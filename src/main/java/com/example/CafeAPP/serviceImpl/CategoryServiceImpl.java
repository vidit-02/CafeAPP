package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.CafeAppApplication;
import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.CategoryDao;
import com.example.CafeAPP.exception.CafeException;
import com.example.CafeAPP.model.Category;
import com.example.CafeAPP.service.CategoryService;
import com.example.CafeAPP.utils.CafeUtils;
import com.google.common.base.Strings;
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
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        log.info("inside addNewCategory");
        try{
            if(jwtFilter.isAdmin()){  //only admin can add a category
                log.info("validating the map");
                if(ValidateCategoryMap(requestMap,false)){
                   log.info("map is validated");
                    categoryDao.save(getCategoryFromMap(requestMap,false));  //this will directly save it to the database using jpa repository lib no need to write sql
                    return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_ADDED_SUCCESSFULLY, HttpStatus.OK);
                }
            } else {
                throw new CafeException(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (DataAccessException ex) {
            throw new CafeException(
                    "Unable to add new category in database",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (CafeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            log.error("failed to add new category");
            throw new CafeException("Failed to add new Category",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    private boolean ValidateCategoryMap(Map<String, String> requestMap, boolean validateId) { //we use validateId as when adding a category we only need name but when updating a category we will have to validate the category using Id as well hence an option
        if(requestMap.containsKey("name")){
            if(requestMap.containsKey("id") && validateId){
                return true;
            } else if (!validateId){
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String,String> requestMap, Boolean isAdded){ //isAdded will be true for updating an existing category it is already added and will be false if adding a new category
        Category category = new Category();
        if(isAdded){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        log.info("inside getAllCategory :: {}", filterValue);

        try {

            List<Category> categories;

            if (!Strings.isNullOrEmpty(filterValue)
                    && filterValue.equalsIgnoreCase("true")) {

                categories = categoryDao.getAllCategories();

            } else {
                categories = categoryDao.findAll();
            }

            return ResponseEntity.ok(categories);

        } catch (DataAccessException ex) {

            log.error("Database error while fetching categories", ex);

            throw new CafeException(
                    "Unable to fetch categories",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            log.error("Unexpected error while fetching categories", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        log.info("inside updateCategory :: {}", requestMap);

        try {
            // Admin check
            if (!jwtFilter.isAdmin()) {
                throw new CafeException(
                        CafeConstants.UNAUTHORIZED_ACCESS,
                        HttpStatus.UNAUTHORIZED
                );
            }

            // Request validation
            if (!ValidateCategoryMap(requestMap, true)) {
                throw new CafeException(
                        CafeConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST
                );
            }
            Integer id = Integer.parseInt(requestMap.get("id"));

            Optional<Category> optional = categoryDao.findById(id);

            if (optional.isEmpty()) {
                throw new CafeException(
                        CafeConstants.CATEGORY_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }
            categoryDao.save(
                    getCategoryFromMap(requestMap, true)
            );
            return CafeUtils.getResponseEntity(
                    CafeConstants.CATEGORY_UPDATED_SUCCESSFULLY,
                    HttpStatus.OK
            );

        } catch (NumberFormatException ex) {
            throw new CafeException(
                    "Invalid category id",
                    HttpStatus.BAD_REQUEST
            );

        } catch (DataAccessException ex) {
            log.error("Database error while updating category", ex);
            throw new CafeException(
                    "Unable to update category",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while updating category", ex);
            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
