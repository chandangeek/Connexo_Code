/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;

public class EndPointLogEntryInfo {
    public Instant timestamp;
    public String message;
    public IdWithNameInfo logLevel;
}
