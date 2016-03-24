package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.Comparator;

public class GoingOnInfo {
    public String type;
    public long id;
    public String description;
    public Instant dueDate;
    public Severity severity;
    public String assignee;
    public Boolean assigneeIsCurrentUser;
    public String status;

    Comparator<GoingOnInfo> order() {
        return Comparator
                .comparing(GoingOnInfo::getSeverity)
                .thenComparing(GoingOnInfo::getDueDate, Comparator.<Instant>naturalOrder().reversed());
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public Severity getSeverity() {
        return severity;
    }
}
