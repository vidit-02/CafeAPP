package com.example.CafeAPP.utils;

import com.example.CafeAPP.wrapper.BillWrapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BillEventProducer {

    private final KafkaTemplate<String, BillWrapper> kafkaTemplate;
    private static final String TOPIC = "bill-created";

    public BillEventProducer(KafkaTemplate<String, BillWrapper> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(BillWrapper event) {
        // key = uuid ensures ordering per bill if needed
        System.out.println("Publishing event: " + event.getUuid());
        kafkaTemplate.send(TOPIC, event.getUuid(), event);
    }
}