package com.example.CafeAPP.dao;

import com.example.CafeAPP.model.User;
import com.example.CafeAPP.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.util.List;


public interface UserDao extends JpaRepository<User, Integer> {
    // Custom query methods can be defined here, JPA provides ready-to-use CRUD operations, support for custom queries, and seamless integration with JPA providers like Hibernate. By extending JpaRepository, you can focus on the business logic.
    User findByEmailId(@Param("email") String email);

    List<UserWrapper> getAllUsers();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status,@Param("id") Integer id);

    List<String> getAllAdminsEmail();
}
