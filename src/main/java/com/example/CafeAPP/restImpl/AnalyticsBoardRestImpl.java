package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.rest.AnalyticsBoardRest;
import com.example.CafeAPP.service.AnalyticsBoardService;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsBoardRestImpl implements AnalyticsBoardRest {

    @Autowired
    AnalyticsBoardService analyticsBoardService;

    @Override
    public ResponseEntity<AnalyticsSummaryWrapper> getAnalytics() {
        return analyticsBoardService.getAnalyticsSummary();
    }
}
