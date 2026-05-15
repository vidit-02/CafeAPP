package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.dao.BillDao;
import com.example.CafeAPP.model.Bill;
import com.example.CafeAPP.service.AnalyticsBoardService;
import com.example.CafeAPP.wrapper.AnalyticsSummaryWrapper;
import com.example.CafeAPP.wrapper.ProductDetailsWrapper;
import com.example.CafeAPP.wrapper.ProductSalesWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class AnalyticsBoardServiceImpl implements AnalyticsBoardService {

    @Autowired
    BillDao billDao;

    @Autowired
    BillServiceImpl billService;


    @Override
    public ResponseEntity<AnalyticsSummaryWrapper> getAnalyticsSummary() {
        List<Bill> bills = billService.getBills().getBody();

        Map<String, Integer> productQuantityMap = new HashMap<>();
        Map<String, Integer> productRevenueMap = new HashMap<>();
        Map<String, Integer> categoryRevenueMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        int totalRevenue = 0;

        for( Bill bill : bills){
            totalRevenue += bill.getTotalAmount();

            List<ProductDetailsWrapper> products =
                    null;
            try {
                products = mapper.readValue(
                        bill.getProductDetails(),
                        new TypeReference<>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            for( ProductDetailsWrapper product : products){

                //productQuantityMap.put(product.getName(),productQuantityMap.get(product.getName()) + Integer.parseInt(product.getQuantity()));

                productQuantityMap.merge(
                        product.getName(),
                        Integer.parseInt(product.getQuantity()),
                        Integer::sum
                );
                categoryRevenueMap.merge(product.getCategory(),1,Integer::sum);

                productRevenueMap.merge(product.getName(),product.getTotal(),Integer::sum);

            }
        }

        System.out.println(productRevenueMap);
        System.out.println(productQuantityMap);
        System.out.println(categoryRevenueMap);
        System.out.println(totalRevenue);

        List<ProductSalesWrapper> topProducts = new ArrayList<>();

        for (String productName : productQuantityMap.keySet()) {

            ProductSalesWrapper dto = new ProductSalesWrapper();

            dto.setProductName(productName);

            dto.setQuantitySold(
                    productQuantityMap.get(productName)
            );

            dto.setRevenue(
                    productRevenueMap.get(productName)
            );

            topProducts.add(dto);
        }

        //sorting top products based on revenue
        topProducts.sort(
                (a, b) -> b.getRevenue() - a.getRevenue()
        );


        AnalyticsSummaryWrapper analyticsSummaryWrapper = new AnalyticsSummaryWrapper(totalRevenue,bills.size(),topProducts,categoryRevenueMap,null);

        return new ResponseEntity<>(analyticsSummaryWrapper, HttpStatus.OK);
    }
}