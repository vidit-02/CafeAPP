package com.example.CafeAPP.wrapper;

import java.util.List;
import java.util.Map;

public class AnalyticsSummaryWrapper {

    private Integer totalRevenue;
    private Integer totalBills;
    private List<ProductSalesWrapper> topProducts;
    private Map<String,Integer> categoryRevenue;
    private List<AiInsightWrapper> aiInsights;

    public AnalyticsSummaryWrapper(Integer totalRevenue, Integer totalBills, List<ProductSalesWrapper> topProducts, Map<String, Integer> categoryRevenue, List<AiInsightWrapper> aiInsights) {
        this.totalRevenue = totalRevenue;
        this.totalBills = totalBills;
        this.topProducts = topProducts;
        this.categoryRevenue = categoryRevenue;
        this.aiInsights = aiInsights;
    }
}
