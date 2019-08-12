/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.servicecall.LogLevel;

import java.time.Instant;

public class ServiceCallLogEntryInfo {
    public Instant timestamp;
    public String details;
    public LogLevel logLevel;
}
