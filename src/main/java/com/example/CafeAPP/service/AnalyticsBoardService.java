package com.example.CafeAPP.service;

import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import org.springframework.http.ResponseEntity;

public interface AnalyticsBoardService {
    ResponseEntity<AnalyticsSummaryWrapper> getAnalyticsSummary();
}
