package com.example.CafeAPP.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CafeException extends RuntimeException{

    private final HttpStatus status;

    public CafeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
