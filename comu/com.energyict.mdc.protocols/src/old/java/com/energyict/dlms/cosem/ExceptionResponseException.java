/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import java.io.IOException;

public class ExceptionResponseException extends IOException {

    private final int stateError;
    private final int serviceError;

    public ExceptionResponseException(int stateError, int serviceError) {
        this(null, stateError, serviceError);
    }

    public ExceptionResponseException(String message, int stateError, int serviceError) {
        super((message != null ? message + " " : "") + getDescription(stateError, serviceError));
        this.stateError = stateError;
        this.serviceError = serviceError;
    }
    
    private static String getDescription(int stateError, int serviceError) {
        StringBuilder sb = new StringBuilder();
        sb.append("ExceptionResponse=");

        sb.append("\"");
        switch (stateError) {
            case 0x01:
                sb.append("service-not-allowed");
                break;
            case 0x02:
                sb.append("service-unknown");
                break;
            default:
                sb.append("invalid-state-error!");
        }

        sb.append(", ");
        switch (serviceError) {
            case 0x01:
                sb.append("operation-not-possible");
                break;
            case 0x02:
                sb.append("service-not-supported");
                break;
            case 0x03:
                sb.append("other-reason");
                break;
            default:
                sb.append("invalid-service-error!");
        }
        sb.append("\" [").append(stateError).append(",").append(serviceError).append("]");
        return sb.toString();
    }

}
