package com.gestion.intervention.adapter.wrapper;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseWrapper {
    public static Map<String, Object> success(Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> error(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> warning(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "warning");
        response.put("message", message);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> info(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "info");
        response.put("message", message);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> status(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "status");
        response.put("message", message);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> custom(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("code", status);
        return response;
    }

    public static Map<String, Object> validationError(List<FieldError> fieldErrors, HttpStatus status, String triggeredBy) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("code", status);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            Map<String, String> errorDetail = new HashMap<>();
            errorDetail.put("field", fieldError.getField());
            errorDetail.put("message", fieldError.getDefaultMessage());
            errors.add(errorDetail);
        }

        response.put("errors", errors);
        response.put("timestamp", timestamp);
        response.put("triggeredBy", triggeredBy);

        return response;
        /* EXAMPLE RETURN ERROR
        {
          "status": "error",
          "message": "Validation failed",
          "code": 400,
          "errors": [
            {
              "field": "username",
              "message": "Username cannot be blank"
            },
            {
              "field": "password",
              "message": "Password must be between 6 and 100 characters"
            }
          ],
          "timestamp": "2025-04-01T15:30:00.123",
          "triggeredBy": "test"
        }
        */
    }
}