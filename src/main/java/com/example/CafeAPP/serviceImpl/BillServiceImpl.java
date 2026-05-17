package com.example.CafeAPP.serviceImpl;

import com.example.CafeAPP.JWT.JwtFilter;
import com.example.CafeAPP.constants.CafeConstants;
import com.example.CafeAPP.dao.BillDao;
import com.example.CafeAPP.exception.CafeException;
import com.example.CafeAPP.model.Bill;
import com.example.CafeAPP.service.AnalyticsCacheService;
import com.example.CafeAPP.service.BillService;
import com.example.CafeAPP.utils.BillEventProducer;
import com.example.CafeAPP.utils.CafeUtils;
import com.example.CafeAPP.wrapper.BillWrapper;
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
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private BillEventProducer billEventProducer;

    @Autowired
    AnalyticsCacheService cacheService;

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("inside generateReport");
        log.info("generate report map::::{}", requestMap);
        try{
            String fileName;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey("isGenerated") && (Boolean) requestMap.get("isGenerated")) {
                    fileName = (String) requestMap.get("uuid");
                    if (fileName == null || fileName.trim().isEmpty()) {
                        throw new CafeException(
                                "UUID is missing for generated bill",
                                HttpStatus.BAD_REQUEST
                        );
                    }
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid",fileName);
                    insertBill(requestMap);
                }

                cacheService.invalidateCache();

                BillWrapper event = new BillWrapper();
                event.setUuid(fileName);
                event.setName((String) requestMap.get("name"));
                event.setContactNumber((String) requestMap.get("contactNumber"));
                event.setEmail((String) requestMap.get("email"));
                event.setPaymentMethod((String) requestMap.get("paymentMethod"));
                event.setProductDetails((String) requestMap.get("productDetails"));
                event.setTotalAmount(String.valueOf(requestMap.get("totalAmount")));

                // Publish AFTER successful DB save
                billEventProducer.publish(event);

                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);

            } else {
                throw new CafeException(
                        CafeConstants.INVALID_DATA,
                        HttpStatus.BAD_REQUEST
                );
            }
        } catch (CafeException ex) {
            throw ex; // already custom handled

        }catch (Exception ex) {
            log.error("Error while generating report", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Transactional
    private void insertBill(Map<String, Object> requestMap) {
        try{
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String)requestMap.get("name"));
            bill.setEmail((String)requestMap.get("email"));
            bill.setContactNumber((String)requestMap.get("contactNumber"));
            bill.setPaymentMethod((String)requestMap.get("paymentMethod"));
            bill.setTotalAmount(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setPdfStatus("PENDING");
            billDao.save(bill);
        } catch (NumberFormatException ex) {

            throw new CafeException(
                    "Invalid total amount",
                    HttpStatus.BAD_REQUEST
            );

        } catch (DataAccessException ex) {

            throw new CafeException(
                    "Unable to save bill in database",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (CafeException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Error while inserting bill", ex);
            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
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
        try{
            if(jwtFilter.isAdmin()){
                list = billDao.getAllBills();
            } else {
                list=billDao.getBillByUsername(jwtFilter.getCurrentUser());
            }
            return new ResponseEntity<>(list,HttpStatus.OK);
        } catch (DataAccessException ex) {
            log.error("unable to fetch bill from DB");
            throw new CafeException("Unable to fetch bill from DB",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CafeException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error while fetching  bill",e);
            throw new CafeException(CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("inside getpdf",requestMap);
        try{
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)){
                throw new CafeException("UUID missing",HttpStatus.BAD_REQUEST);
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
        } catch (CafeException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error("Error reading PDF file", ex);

            throw new CafeException(
                    "Unable to read PDF file",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            log.error("Unexpected error while fetching PDF", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            Optional<Bill> optional = billDao.findById(id);
            if(!optional.isEmpty()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity(CafeConstants.BILL_DELETED_SUCCESSFULLY,HttpStatus.OK);
            } else {
                throw new CafeException(
                        CafeConstants.BILL_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                );
            }
        } catch (CafeException ex) {

            throw ex;

        } catch (DataAccessException ex) {

            log.error("Database error while deleting bill", ex);

            throw new CafeException(
                    "Unable to delete bill",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (Exception ex) {

            log.error("Unexpected error while deleting bill", ex);

            throw new CafeException(
                    CafeConstants.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private byte[] getByteArray(String filePath) throws IOException {
        try (InputStream targetStream =
                     new FileInputStream(new File(filePath))) {

            return IOUtils.toByteArray(targetStream);

        } catch (FileNotFoundException ex) {

            throw new CafeException(
                    "PDF file not found",
                    HttpStatus.NOT_FOUND
            );

        } catch (IOException ex) {

            throw new CafeException(
                    "Unable to read PDF file",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    public ResponseEntity<Map<String,String>> getBillStatus(String uuid){
        Bill bill = billDao.findByUuid(uuid);
        return ResponseEntity.ok(Map.of("status", bill.getPdfStatus()));
    }
}
