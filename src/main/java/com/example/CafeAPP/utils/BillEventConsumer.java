package com.example.CafeAPP.utils;

import com.example.CafeAPP.wrapper.BillWrapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BillEventConsumer {

    private final PdfService pdfService;

    public BillEventConsumer(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @KafkaListener(topics = "bill-created", groupId = "pdf-group")
    public void consume(BillWrapper event) {
        System.out.println("Received event: " + event.getUuid());
        pdfService.generatePdf(event);
    }
}
