package com.example.CafeAPP.wrapper;

import lombok.Data;

@Data
public class ProductSalesWrapper {
    private String productName;
    private Integer quantitySold;
    private Integer revenue;
}
