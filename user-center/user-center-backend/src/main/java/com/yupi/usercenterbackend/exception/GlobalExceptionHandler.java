package com.yupi.usercenterbackend.exception;

import com.yupi.usercenterbackend.common.BaseResponse;
import com.yupi.usercenterbackend.common.ErrorCode;
import com.yupi.usercenterbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author sonder
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessException(BusinessException e){
        log.error("businessException: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeException(RuntimeException e){
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
