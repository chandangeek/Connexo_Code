package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.Priority;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriorityInfo {
    public long urgency;
    public long impact;

    public PriorityInfo(Priority priority) {
        this.urgency = priority.getUrgency();
        this.impact = priority.getImpact();
    }

    public PriorityInfo() {
    }
}
