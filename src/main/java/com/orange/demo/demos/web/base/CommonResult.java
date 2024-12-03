package com.orange.demo.demos.web.base;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author TOTORO
 * @since 2024/12/3 10:54
 */
@Data

public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = -2487505517576843428L;

    private Integer code;

    private String message;

    private T data;

    public CommonResult() {
    }

    public CommonResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(HttpStatus.OK.value(), "success", data);
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        return new CommonResult<>(code, message);
    }
}
