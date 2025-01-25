package org.example.hvvs.util;

public class ServiceResult<T> {
    private boolean success;
    private String errorCode;   // optional
    private String message;
    private T data;            // the result/payload

    // Constructors
    public ServiceResult() {
    }

    public ServiceResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ServiceResult(boolean success, String errorCode, String message, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    // Static helper methods for quick success/error creation
    public static <T> ServiceResult<T> success(T data, String message) {
        return new ServiceResult<>(true, null, message, data);
    }

    public static <T> ServiceResult<T> success(T data) {
        return new ServiceResult<>(true, null, null, data);
    }

    public static <T> ServiceResult<T> failure(String errorCode, String message) {
        return new ServiceResult<>(false, errorCode, message, null);
    }

    public static <T> ServiceResult<T> failure(String message) {
        return new ServiceResult<>(false, null, message, null);
    }

    // Getters & Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
