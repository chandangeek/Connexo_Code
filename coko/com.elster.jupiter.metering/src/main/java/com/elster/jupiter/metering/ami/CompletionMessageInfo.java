package com.elster.jupiter.metering.ami;

public class CompletionMessageInfo {

    long correlationId;
    boolean status;
    String failureReason;

    public CompletionMessageInfo() {
    }

    public CompletionMessageInfo(String failureReason, long correlationId, boolean status) {
        this.failureReason = failureReason;
        this.correlationId = correlationId;
        this.status = status;
    }

    public long getCorrelationId() {
        return correlationId;
    }

    public boolean isStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return "CompletionMessageInfo{" +
                "correlationId=" + correlationId +
                ", status=" + status +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
