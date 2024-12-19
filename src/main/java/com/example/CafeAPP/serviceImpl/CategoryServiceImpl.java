package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.CafeAppApplication;
import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.CategoryDao;
import com.example.CafeAPP.model.Category;
import com.example.CafeAPP.service.CategoryService;
import com.example.CafeAPP.utils.CafeUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.info("something wrong");
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
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
        try{
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")){
                //In case some filter value is provided
                return new ResponseEntity<List<Category>>(categoryDao.getAllCategories(),HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(),HttpStatus.OK); //pre=provided method by jpa to return all categories
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(ValidateCategoryMap(requestMap,true)){
                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optional.isEmpty()){
                        categoryDao.save(getCategoryFromMap(requestMap,true));
                        return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_UPDATED_SUCCESSFULLY,HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity(CafeConstants.CATEGORY_NOT_FOUND,HttpStatus.OK);
                    }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return  CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.OK);
    }
}
