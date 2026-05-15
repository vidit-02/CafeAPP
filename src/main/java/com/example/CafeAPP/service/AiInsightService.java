package com.example.CafeAPP.service;

import com.example.CafeAPP.wrapper.AiInsightWrapper;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;

public interface AiInsightService {
    AiInsightWrapper generateInsights(
            AnalyticsSummaryWrapper analytics
    );
}