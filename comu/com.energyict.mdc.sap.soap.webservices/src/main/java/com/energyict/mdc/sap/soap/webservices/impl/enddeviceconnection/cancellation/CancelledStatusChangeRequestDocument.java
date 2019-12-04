/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;


public class CancelledStatusChangeRequestDocument {
    private String id;
    private String categoryCode;
    private int totalRequests;
    private int cancelledRequests;
    private int notCancelledRequests;

    public CancelledStatusChangeRequestDocument(String id, String categoryCode, int totalRequests, int cancelledRequests, int notCancelledRequests) {
        this.id = id;
        this.categoryCode = categoryCode;
        this.totalRequests = totalRequests;
        this.cancelledRequests = cancelledRequests;
        this.notCancelledRequests = notCancelledRequests;
    }

    public String getId() {
        return id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getCancelledRequests() {
        return cancelledRequests;
    }

    public int getNotCancelledRequests() {
        return notCancelledRequests;
    }

}