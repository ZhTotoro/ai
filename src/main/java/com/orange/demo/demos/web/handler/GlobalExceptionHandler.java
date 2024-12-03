package com.orange.demo.demos.web.handler;

import com.orange.demo.demos.web.base.CommonResult;
import com.orange.demo.demos.web.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author TOTORO
 * @since 2024/12/3 10:49
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleException(Exception ex) {

        if (ex instanceof ServiceException) {
            return ServiceExceptionHandler((ServiceException) ex);
        }

        return defaultExceptionHandler(ex);
    }

    private CommonResult<?> ServiceExceptionHandler(ServiceException ex) {
        log.warn("[ServiceExceptionHandler]", ex);
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }

    private CommonResult<?> defaultExceptionHandler(Exception ex) {
        log.warn("[defaultExceptionHandler]", ex);
        return CommonResult.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
}
