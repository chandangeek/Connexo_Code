package com.elster.jupiter.webservices.rest.impl;

import java.time.Instant;

public class WebServiceCallOccurrenceInfo {
    public long id;
    public Instant startTime;
    public Instant endTime;
    public String status;
    public String request;
    public String applicationName;
    public EndPointConfigurationInfo endPointConfigurationInfo;
    public String payload;

}
