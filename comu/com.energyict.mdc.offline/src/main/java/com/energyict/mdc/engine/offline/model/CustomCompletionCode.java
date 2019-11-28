package com.energyict.mdc.engine.offline.model;

public class CustomCompletionCode {

    private String completionCode;
    private String reasonCode;

    public CustomCompletionCode(String completionCode, String reasonCode) {
        this.completionCode = completionCode;
        this.reasonCode = reasonCode;
    }

    public String getCompletionCode() {
        return completionCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
