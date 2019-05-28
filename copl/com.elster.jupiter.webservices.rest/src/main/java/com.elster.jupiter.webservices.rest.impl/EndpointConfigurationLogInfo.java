/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;

import java.time.Instant;

public class EndpointConfigurationLogInfo {
    public long id;
    public Instant timestamp;
    public String message;
    public String logLevel;
    public EndPointConfiguration endPointConfiguration;// = Reference.empty();
    public String stackTrace;
    public EndPointOccurrence occurrence;//= Reference.empty();
}