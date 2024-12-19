package com.example.CafeAPP.model;

import lombok.Data;
import org.hibernate.Interceptor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Bill.getAllBills",query = "select b from Bill b order by b.id desc")

@NamedQuery(name = "Bill.getBillByUsername",query = "select b from Bill b where b.createdBy=:username order by b.id desc")

@Data
@Entity
@Table(name = "bill")
@DynamicUpdate
@DynamicInsert
public class Bill implements Serializable {

    private static final long serialVersion = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contactNumber")
    private String contactNumber;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "totalAmount")
    private Integer totalAmount;

    @Column(name = "productDetails",columnDefinition = "TEXT") //to expand the cart in the json format
    private String productDetails;

    @Column(name = "createdBy")
    private String createdBy;

}
