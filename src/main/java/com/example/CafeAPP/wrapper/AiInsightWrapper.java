package com.example.CafeAPP.wrapper;

import java.util.List;

public class AiInsightWrapper {
    private List<String> topInsights;

    private List<String> recommendations;

    private List<String> risks;

    public List<String> getTopInsights() {
        return topInsights;
    }

    public void setTopInsights(List<String> topInsights) {
        this.topInsights = topInsights;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }
}
