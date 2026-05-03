package com.example.CafeAPP.wrapper;

import lombok.Data;

@Data
public class BillWrapper {
    private String uuid;
    private String name;
    private String contactNumber;
    private String email;
    private String paymentMethod;
    private String productDetails; // JSON string
    private String totalAmount;
}
