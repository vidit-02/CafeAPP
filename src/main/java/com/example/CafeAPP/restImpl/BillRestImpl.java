package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.model.Bill;
import com.example.CafeAPP.rest.BillRest;
import com.example.CafeAPP.service.BillService;
import com.example.CafeAPP.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BillRestImpl implements BillRest {

    @Autowired
    BillService billService;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
       return billService.generateReport(requestMap);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        return billService.getBills();
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        return billService.getPdf(requestMap);
    }

    @Override
    public ResponseEntity<String> deletebill(Integer id) {
        return billService.deleteBill(id);
    }
}
