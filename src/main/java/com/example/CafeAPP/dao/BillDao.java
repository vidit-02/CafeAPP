package com.example.CafeAPP.dao;

import com.example.CafeAPP.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillDao extends JpaRepository<Bill, Integer>{


    List<Bill> getAllBills();

    List<Bill> getBillByUsername(@Param("username") String currentUser);
}
