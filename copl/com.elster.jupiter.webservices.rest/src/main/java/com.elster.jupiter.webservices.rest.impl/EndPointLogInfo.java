/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import java.time.Instant;

public class EndPointLogInfo {
    public long id;
    public Instant timestamp;
    public String message;
    public String logLevel;
    public EndPointConfigurationInfo endPointConfigurationInfo;
    public String stackTrace;
    public WebServiceCallOccurrenceInfo occurrenceInfo;
}