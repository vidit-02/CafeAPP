package com.example.CafeAPP.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CafeException.class)
    public ResponseEntity<Map<String,Object>> handleCafe(CafeException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of(
                        "message", ex.getMessage(),
                        "status", ex.getStatus().value()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "message", "Internal Server Error",
                        "status", 500
                ));
    }
}