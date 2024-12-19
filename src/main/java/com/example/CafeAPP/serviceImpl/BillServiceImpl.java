package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.BillDao;
import com.example.CafeAPP.model.Bill;
import com.example.CafeAPP.service.BillService;
import com.example.CafeAPP.utils.CafeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BillServiceImpl implements BillService {


    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("inside generateReport");
        try{
            String fileName;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey("isGenerated") && (Boolean) requestMap.get("isGenerated")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid",fileName);
                    insertBill(requestMap);
                }
                String data = "Name: "+requestMap.get("name")+"\n"+"Contact Number: "+requestMap.get("contactNumber")+
                        "\n"+"Email: "+requestMap.get("email")+"\n"+"Payment Method: "+requestMap.get("paymentMethod");
                Document document = new Document();
                PdfWriter.getInstance(document,new FileOutputStream(CafeConstants.STORE_LOCATION+"\\"+fileName+".pdf"));

                document.open();
                setRectangleInPdf(document);

                Paragraph heading = new Paragraph("Cafe Management System",getFont("Header"));
                heading.setAlignment(Element.ALIGN_CENTER); //bydefault left side
                document.add(heading);

                Paragraph paragraph = new Paragraph(data+"\n \n",getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));

                for(int i=0;i<jsonArray.length();i++){
                    addRow(table,CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total :"+requestMap.get("totalAmount")+"\n"+"Thankyou for visiting", getFont("Data"));
                document.add(footer);
                document.close();
                return new ResponseEntity<>("{\"uuid\":\""+fileName+"\"}",HttpStatus.OK);

            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

     private void addRow(PdfPTable table, Map<String, Object> data) {
        log.info("inside addRow");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("inside addTableHeader");
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
        log.info("inside setRectangleInPdf");
        Rectangle rectangle = new Rectangle(577,825,18,15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try{
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String)requestMap.get("name"));
            bill.setEmail((String)requestMap.get("email"));
            bill.setContactNumber((String)requestMap.get("contactNumber"));
            bill.setPaymentMethod((String)requestMap.get("paymentmethod"));
            bill.setTotalAmount(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            // Convert productDetails to JSON string
//            ObjectMapper objectMapper = new ObjectMapper();
//            String productDetailsJson = objectMapper.writeValueAsString(requestMap.get("productDetails"));
//            bill.setProductDetails(productDetailsJson);
            bill.setProductDetails((String) requestMap.get("productDetails"));
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Font getFont(String type){
        log.info("inside getFont");
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
    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("totalAmount") &&
                requestMap.containsKey("productDetails");
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list;
        if(jwtFilter.isAdmin()){
            list = billDao.getAllBills();
        } else {
            list=billDao.getBillByUsername(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("inside getpdf",requestMap);
        try{
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)){
                return new ResponseEntity<>(byteArray,HttpStatus.BAD_REQUEST);
            }
            String filepath=CafeConstants.STORE_LOCATION+"\\"+(String) requestMap.get("uuid")+".pdf";
            if(CafeUtils.isFileExist(filepath)){
                byteArray=getByteArray(filepath);
                return new ResponseEntity<>(byteArray,HttpStatus.OK);
            } else {
                requestMap.put("isGenerated", true);
                generateReport(requestMap);
                byteArray=getByteArray(filepath);
                return new ResponseEntity<>(byteArray,HttpStatus.OK);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            Optional<Bill> optional = billDao.findById(id);
            if(!optional.isEmpty()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity(CafeConstants.BILL_DELETED_SUCCESSFULLY,HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.BILL_NOT_FOUND,HttpStatus.OK);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filepath) throws IOException {
        File file = new File(filepath);
        InputStream targetStream = new FileInputStream(file);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;


    }
}
