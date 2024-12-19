package com.example.CafeAPP.wrapper;

import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;

@Data
public class ProductWrapper {
    private Integer id;

    private String name;

    private String description;

    private Integer price;

    private String status;

    private Integer categoryId;

    private String categoryName;

    public ProductWrapper(){

    }

    public ProductWrapper(Integer id, String name, String description, Integer price, String status, Integer categoryId, String categoryName){
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
        this.status=status;
        this.categoryId=categoryId;
        this.categoryName=categoryName;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price){
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
    }

}
