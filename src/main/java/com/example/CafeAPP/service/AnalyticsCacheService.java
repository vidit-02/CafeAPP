package com.example.CafeAPP.service;

import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;

public interface AnalyticsCacheService {
    AnalyticsSummaryWrapper getCachedInsights();

    void updateCache(AnalyticsSummaryWrapper insights);

    boolean isCacheValid();

    void invalidateCache();
}
