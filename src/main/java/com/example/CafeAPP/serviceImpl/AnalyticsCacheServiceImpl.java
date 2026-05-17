package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.service.AnalyticsCacheService;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AnalyticsCacheServiceImpl implements AnalyticsCacheService {

    private AnalyticsSummaryWrapper cachedInsights;

    private LocalDateTime lastUpdated;

    private static final long CACHE_DURATION_MINUTES = 15;

    @Override
    public AnalyticsSummaryWrapper getCachedInsights() {
        return cachedInsights;
    }

    @Override
    public void updateCache(AnalyticsSummaryWrapper insights) {
        this.cachedInsights = insights;

        this.lastUpdated = LocalDateTime.now();

    }

    @Override
    public boolean isCacheValid() {
        if (cachedInsights == null || lastUpdated == null) {
            return false;
        }

        return Duration.between(
                lastUpdated,
                LocalDateTime.now()
        ).toMinutes() < CACHE_DURATION_MINUTES;
    }

    @Override
    public void invalidateCache() {
        System.out.println("Invalidating Cache");

        this.cachedInsights = null;

        this.lastUpdated = null;
    }
}
