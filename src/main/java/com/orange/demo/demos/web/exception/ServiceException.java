package com.orange.demo.demos.web.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author TOTORO
 * @since 2024/12/3 11:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -7116387536521742784L;

    private final Integer code;

    private final String message;

}
