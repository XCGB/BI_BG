package com.hj.hjBi.exception;

import com.hj.hjBi.common.ErrorCode;

/**
 * 自定义异常类
 *
 * @author: WHJ
 * @createTime: 2023-09-13 09:48
 * @description:
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
