package com.example.financial_management.model;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class AbstractResponse<T> {
    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Indicates if the request was successful")
    private boolean success = true;

    @Schema(description = "Response code")
    private int code = 200;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Stack trace", example = "null")
    private String stackTrace;

    @Schema(description = "Execution time in seconds", example = "null")
    private double executionTimeInSeconds;

    @Schema(description = "Errors", example = "null")
    private List<ResponseError> errors;

    public ResponseEntity<AbstractResponse<T>> withData(Supplier<T> function) {
        try {
            long start = System.currentTimeMillis();
            T data = function.get();
            long processTime = System.currentTimeMillis() - start;

            if (processTime > 300) {
                log.warn("{} ms to get {} from {}", processTime, getName(data), getName(function));
            }

            this.setExecutionTimeInSeconds(processTime / 1000.0);

            if (data != null) {
                this.setSuccess(true)
                    .setData(data);
            } else {
                this.setSuccess(false)
                    .setMessage("No data available");
            }
        } catch (Exception e) {
            log.error("Get data failed", e);
            this.setSuccess(false)
                .setMessage(e.getMessage())
                .setStackTrace(e.getStackTrace() != null ? e.toString() : "No stack trace available")
                .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this);
    }

    private static String getName(Object o) {
        if (o == null) return "null";
        Class<?> clazz = o.getClass();
        String pkg = clazz.getPackageName();
        return clazz.getName().substring(pkg.length() + 1);
    }
}
