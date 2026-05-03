package com.example.CafeAPP.utils;

import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.BillDao;
import com.example.CafeAPP.model.Bill;
import com.example.CafeAPP.wrapper.BillWrapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class PdfService {

    private final BillDao billRepository;

    public PdfService(BillDao billRepository) {
        this.billRepository = billRepository;
    }

    public void generatePdf(BillWrapper event) {

        String fileName = event.getUuid();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));

            document.open();

            setRectangleInPdf(document);

            Paragraph heading = new Paragraph("Cafe Management System", getFont("Header"));
            heading.setAlignment(Element.ALIGN_CENTER);
            document.add(heading);

            String data = "Name: " + event.getName() + "\n"
                    + "Contact Number: " + event.getContactNumber() + "\n"
                    + "Email: " + event.getEmail() + "\n"
                    + "Payment Method: " + event.getPaymentMethod();

            document.add(new Paragraph(data + "\n\n", getFont("Data")));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table);

            JSONArray jsonArray = CafeUtils.getJsonArrayFromString(event.getProductDetails());

            for (int i = 0; i < jsonArray.length(); i++) {
                addRow(table, CafeUtils.getMapFromJson(jsonArray.getString(i)));
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Total :" + event.getTotalAmount() + "\nThank you for visiting",
                    getFont("Data"));

            document.add(footer);
            document.close();

            // ✅ update DB status
            updateStatus(fileName, "GENERATED");

        } catch (Exception ex) {
            updateStatus(fileName, "FAILED");
        }
    }

    private void updateStatus(String uuid, String status) {
        Bill bill = billRepository.findByUuid(uuid);
        if (bill != null) {
            bill.setPdfStatus(status);
            billRepository.save(bill);
        }
    }

    private void addRow(PdfPTable table, Map<String, Object> data) {

        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        //table.addCell((String) data.get("quantity"));
        table.addCell(data.get("quantity").toString());
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {

        Stream.of("Name", "Category", "Quantity", "Price", "SubTotal")
                .forEach(columnTitle ->{
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void setRectangleInPdf(Document document) throws DocumentException {

        Rectangle rectangle = new Rectangle(577,825,18,15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type){

        switch (type){
            case "Header":
                Font headerFont=FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18,BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont=FontFactory.getFont(FontFactory.TIMES_ROMAN,11,BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

}
