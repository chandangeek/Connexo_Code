/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UsagePointStateChangeRequestInfo {
    public long id;
    public IdWithDisplayValueInfo<String> type = new IdWithDisplayValueInfo<>();
    public String fromStateName;
    public String toStateName;
    public Instant transitionTime;
    public Instant scheduleTime;
    public IdWithDisplayValueInfo<Long> user = new IdWithDisplayValueInfo<>();
    public IdWithDisplayValueInfo<UsagePointStateChangeRequest.Status> status = new IdWithDisplayValueInfo<>();
    public String message;
    public List<IdWithNameInfo> microChecks = new ArrayList<>();
    public List<IdWithNameInfo> microActions = new ArrayList<>();
    public UsagePointInfo usagePoint = new UsagePointInfo();
    public boolean userCanManageRequest;

    public UsagePointStateChangeRequestInfo() {
    }

    public static class UsagePointInfo {
        public String name;
        public long version;

        public UsagePointInfo() {
        }
    }
}
