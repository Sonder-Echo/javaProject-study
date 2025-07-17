package com.yupi.usercenterbackend.common;

/**
 * 返回工具类
 *
 * @author sonder
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    public static <T> BaseResponse<T> success(){
        return new BaseResponse<>(0, null, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码信息
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 错误码
     * @param message 错误信息
     * @param description 错误描述
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, message, description);
    }

    /**
     * 失败
     * @param errorCode 错误码信息
     * @param message 错误信息
     * @param description 错误描述
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), message, description);
    }

    /**
     * 失败
     * @param errorCode 错误码信息
     * @param description 错误描述
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String description) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), description);
    }
}
