package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public String action;
    public String filter;
    public List<String> usagePointMRIDs;
    public List<Long> calendarIds;
    public boolean immediately;
    public Long startTime;
}
