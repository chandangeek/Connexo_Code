/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

public class FailedUsagePointOperation {

    private String errorCode;
    private String errorMessage;
    private String usagePointMrid;
    private String usagePointName;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUsagePointMrid() {
        return usagePointMrid;
    }

    public void setUsagePointMrid(String usagePointMrid) {
        this.usagePointMrid = usagePointMrid;
    }

    public String getUsagePointName() {
        return usagePointName;
    }

    public void setUsagePointName(String usagePointName) {
        this.usagePointName = usagePointName;
    }

}
