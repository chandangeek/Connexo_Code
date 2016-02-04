package com.elster.jupiter.issue.rest.request;

public class IssueDueDateInfo {
    public Long startTime;
    public Long endTime;

    public IssueDueDateInfo(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
