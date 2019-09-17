package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

public class CancelledMeterReadingDocument {
    private String id;
    private boolean success;
    private MessageSeeds cancelError;
    private Object[] cancelArgs;

    public CancelledMeterReadingDocument(String id, boolean success) {
        this.id = id;
        this.success = success;
    }

    public CancelledMeterReadingDocument(String id, boolean success, MessageSeeds cancelError, Object... cancelArgs) {
        this.id = id;
        this.success = success;
        this.cancelError = cancelError;
        this.cancelArgs = cancelArgs;
    }

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public MessageSeeds getCancelError() {
        return cancelError;
    }

    public Object[] getCancelArgs() {
        return cancelArgs;
    }
}
