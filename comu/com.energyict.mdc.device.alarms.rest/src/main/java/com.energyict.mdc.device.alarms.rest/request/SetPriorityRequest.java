package com.energyict.mdc.device.alarms.rest.request;


import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.energyict.mdc.device.alarms.rest.response.PriorityInfo;

public class SetPriorityRequest {

    public long id;
    public IssueShortInfo alarm;
    public PriorityInfo priority;
}
