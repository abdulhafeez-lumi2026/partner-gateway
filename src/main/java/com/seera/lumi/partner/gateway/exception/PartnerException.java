package com.seera.lumi.partner.gateway.exception;

import lombok.Getter;

@Getter
public class PartnerException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    public PartnerException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public PartnerException(String code, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
