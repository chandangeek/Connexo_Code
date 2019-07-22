/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class ServiceCallInfo {
    public IdWithNameInfo serviceCall;
    public IdWithNameInfo parentServiceCall;
    public IdWithNameInfo onState;
    public Instant receivedTime;
    public Instant lastModifyTime;
    public IdWithNameInfo serviceCallType;
    public List<ServiceCallLogEntryInfo> logs;
}
