package com.dosmartie.helper;

import com.dosmartie.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseMessage<T> {
    public synchronized BaseResponse<T> setSuccessResponse(String result, T data) {
        return new BaseResponse<>(result, null, true, HttpStatus.OK.value(), data);
    }

    public synchronized BaseResponse<T> setFailureResponse(String result, Exception exception) {
        return new BaseResponse<>(result, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
    }

    public synchronized BaseResponse<T> setFailureResponse(String errorDesc) {
        return new BaseResponse<>(null, errorDesc, false, HttpStatus.BAD_REQUEST.value(), null);
    }
}
