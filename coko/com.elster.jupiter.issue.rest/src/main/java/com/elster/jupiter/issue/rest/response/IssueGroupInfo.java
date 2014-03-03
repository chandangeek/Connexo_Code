package com.elster.jupiter.issue.rest.response;

public class IssueGroupInfo {
    private String reason;
    private long number;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }
}
