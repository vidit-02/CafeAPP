package com.example.CafeAPP.model;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name="User.findByEmailId",query = "Select u from User u where u.email=:email")

@NamedQuery(name="User.getAllUsers", query ="select new com.example.CafeAPP.wrapper.UserWrapper(u.id, u.name, u.contactNumber, u.email, u.status) from User u where u.role='user'") //new com.example.CafeApp.wrapper.UserWrapper() this is to get the data in the form of object of the wrapper

@NamedQuery(name="User.updateStatus",query = "update User u set u.status=:status where u.id=:id")

@NamedQuery(name="User.getAllAdminsEmail", query ="select u.email from User u where u.role='admin'")

@Data //by using this annotation we don't have to define constructor and getter setter for our variables
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "contactNumber")
    private String contactNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
