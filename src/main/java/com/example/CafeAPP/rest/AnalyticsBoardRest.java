package com.example.CafeAPP.rest;

import com.example.CafeAPP.wrapper.AiInsightWrapper;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/analytics")
public interface AnalyticsBoardRest {

    @GetMapping(path = "/summary")
    ResponseEntity<AnalyticsSummaryWrapper> getAnalytics();

    @GetMapping(path = "/ai-summary")
    ResponseEntity<AiInsightWrapper> getAiSummary();

}
