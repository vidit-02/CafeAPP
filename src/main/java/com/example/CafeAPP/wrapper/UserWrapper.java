package com.example.CafeAPP.wrapper;

import lombok.Data;

import javax.persistence.Column;
@Data //handles getters and setters
public class UserWrapper {  //create this to structure what all info to get when we do get all user of a specific role in query.

    private Integer id;

    private String name;

    private String contactNumber;

    private String email;

    private String status;

    public UserWrapper(Integer id, String name, String contactNumber, String email, String status) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.status = status;
    }
}
