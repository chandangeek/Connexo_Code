package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;

public class WebServiceCallOccurrenceInfo {
    public long id;
    public Instant startTime;
    public Instant endTime;
    public IdWithNameInfo status;
    public String request;
    public String applicationName;
    public EndPointConfigurationInfo endPointConfigurationInfo;
    public String payload;
    public String appServerName;

}
