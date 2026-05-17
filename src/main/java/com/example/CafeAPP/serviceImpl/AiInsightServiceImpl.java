package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.exception.CafeException;
import com.example.CafeAPP.service.AiInsightService;
import com.example.CafeAPP.service.AnalyticsCacheService;
import com.example.CafeAPP.wrapper.AiInsightWrapper;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import com.example.CafeAPP.wrapper.ProductSalesWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiInsightServiceImpl implements AiInsightService {
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public AiInsightServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }



    @Override
    public AiInsightWrapper generateInsights(AnalyticsSummaryWrapper analytics) {

        try {

            String prompt = buildPrompt(analytics);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of(
                                                    "text", prompt
                                            )
                                    )
                            )
                    )
            );

            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractAIResponse(response);

        } catch (Exception ex) {

            AiInsightWrapper fallback = new AiInsightWrapper();

            fallback.setTopInsights(
                    List.of("AI service unavailable")
            );

            fallback.setRecommendations(new ArrayList<>());
            fallback.setRisks(new ArrayList<>());

            return fallback;
        }
    }

    private AiInsightWrapper extractAIResponse(String response) {
        ObjectMapper mapper = new ObjectMapper();
        AiInsightWrapper insights = new AiInsightWrapper();
        try{

            JsonNode root = mapper.readTree(response);

            String generatedText =
                    root.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

            insights = mapper.readValue(
                    generatedText,
                    AiInsightWrapper.class
            );

        }catch (JsonProcessingException e) {
            throw new CafeException(
                    "Unable to extract AI Response",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CafeException("Failed to extract AI Response ",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return insights;
    }


    private String buildPrompt(AnalyticsSummaryWrapper analytics){

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            You are an AI business analyst for a cafe.
            Analyze the following analytics data.
            Do not include markdown.
            Do not include explanations.
            Only use the provided analytics data.
            Do not assume missing values.

            Return ONLY valid JSON in this format:

            {
              "topInsights": [],
              "recommendations": [],
              "risks": []
            }

            Analytics Data:
            """);

        prompt.append("\nTotal Revenue: ")
                .append(analytics.getTotalRevenue());

        prompt.append("\nTotal Bills: ")
                .append(analytics.getTotalBills());

        prompt.append("\nTop Products:\n");

        for(ProductSalesWrapper product : analytics.getTopProducts()) {

            prompt.append("- ")
                    .append(product.getProductName())
                    .append(" | Quantity: ")
                    .append(product.getQuantitySold())
                    .append(" | Revenue: ")
                    .append(product.getRevenue())
                    .append("\n");
        }

        prompt.append("\nCategory Revenue:\n");

        analytics.getCategoryRevenue().forEach((category, revenue) -> {
            prompt.append("- ")
                    .append(category)
                    .append(": ")
                    .append(revenue)
                    .append("\n");
        });

        return prompt.toString();
    }
}
