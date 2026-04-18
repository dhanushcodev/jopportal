package com.eazybytes.jobportal.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    HashMap<String, String> error = new HashMap<>();

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HashMap<String, String>> getExceptions(Exception exception) {
        error.put("error",exception.getMessage());
        error.put("status","505");
        return  new ResponseEntity<>(error,  HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
