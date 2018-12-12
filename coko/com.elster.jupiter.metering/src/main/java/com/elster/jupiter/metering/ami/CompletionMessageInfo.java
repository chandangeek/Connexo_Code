/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

public class CompletionMessageInfo {

    public enum CompletionMessageStatus {SUCCESS, FAILURE, CANCELLED}

    public enum FailureReason {
        ONE_OR_MORE_DEVICE_COMMANDS_FAILED,
        NO_COMTASK_TO_VERIFY_BREAKER_STATUS,
        INCORRECT_DEVICE_BREAKER_STATUS,
        SERVICE_CALL_HAS_BEEN_CANCELLED,
        ONE_OR_MORE_DEVICE_COMMANDS_HAVE_BEEN_REVOKED,
        UNEXPECTED_EXCEPTION
    }

    private String correlationId;
    private CompletionMessageStatus completionMessageStatus;
    private FailureReason failureReason;

    // Constructor only to be used by JSON deserialization
    public CompletionMessageInfo() {
    }

    public CompletionMessageInfo(String correlationId) {
        this.correlationId = correlationId;
        this.completionMessageStatus = CompletionMessageStatus.SUCCESS;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public CompletionMessageStatus getCompletionMessageStatus() {
        return completionMessageStatus;
    }

    public CompletionMessageInfo setCompletionMessageStatus(CompletionMessageStatus completionMessageStatus) {
        this.completionMessageStatus = completionMessageStatus;
        return this;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public CompletionMessageInfo setFailureReason(FailureReason failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompletionMessageInfo that = (CompletionMessageInfo) o;

        if (!correlationId.equals(that.correlationId)) {
            return false;
        }
        if (completionMessageStatus != that.completionMessageStatus) {
            return false;
        }
        return failureReason != null ? failureReason.equals(that.failureReason) : that.failureReason == null;

    }

    @Override
    public int hashCode() {
        int result = correlationId.hashCode();
        result = 31 * result + completionMessageStatus.hashCode();
        result = 31 * result + (failureReason != null ? failureReason.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletionMessageInfo{" +
                "correlationId='" + correlationId + '\'' +
                ", completionMessageStatus=" + completionMessageStatus +
                ", failureReason=" + failureReason +
                '}';
    }
}