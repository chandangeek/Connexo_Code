/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class WebServiceCallOccurrenceInfo {
    public long id;
    public IdWithNameInfo endpoint;
    public String webServiceName;
    public Instant startTime;
    public Instant endTime;
    public IdWithNameInfo status;
    public List<EndPointLogEntryInfo> logs;
}
