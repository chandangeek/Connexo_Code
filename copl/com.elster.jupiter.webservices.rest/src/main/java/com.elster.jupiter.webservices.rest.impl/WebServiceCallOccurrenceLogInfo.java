/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import java.time.Instant;

public class WebServiceCallOccurrenceLogInfo {
    public long id;
    public Instant timestamp;
    public String message;
    public String logLevel;
    public EndPointConfigurationInfo endPointConfigurationInfo;
    public String stackTrace;
    public WebServiceCallOccurrenceInfo occurrenceInfo;
}