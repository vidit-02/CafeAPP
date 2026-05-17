package com.example.CafeAPP.restImpl;

import com.example.CafeAPP.rest.AnalyticsBoardRest;
import com.example.CafeAPP.service.AiInsightService;
import com.example.CafeAPP.service.AnalyticsBoardService;
import com.example.CafeAPP.service.AnalyticsCacheService;
import com.example.CafeAPP.wrapper.AiInsightWrapper;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsBoardRestImpl implements AnalyticsBoardRest {

    @Autowired
    AnalyticsBoardService analyticsBoardService;

    @Autowired
    AiInsightService aiInsightService;

    @Autowired
    AnalyticsCacheService cacheService;

    @Override
    public ResponseEntity<AnalyticsSummaryWrapper> getAnalytics() {
        return analyticsBoardService.getAnalyticsSummary();
    }

    @Override
    public ResponseEntity<AnalyticsSummaryWrapper> getAiSummary() {


        if(cacheService.isCacheValid()) {

            System.out.println("Returning cached AI insights");

            return ResponseEntity.ok(cacheService.getCachedInsights());
        }

        //return aiInsightService.generateInsights();
        AnalyticsSummaryWrapper analytics =
                analyticsBoardService.getAnalyticsSummary().getBody();

        AiInsightWrapper insights =
                aiInsightService.generateInsights(analytics);
        if (analytics != null) {
            analytics.setAiInsights(insights);
        }

        cacheService.updateCache(analytics);

        return ResponseEntity.ok(analytics);
    }
}
