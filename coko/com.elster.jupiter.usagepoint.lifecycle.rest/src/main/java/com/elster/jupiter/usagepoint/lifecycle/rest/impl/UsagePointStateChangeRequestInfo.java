package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class UsagePointStateChangeRequestInfo {
    public long id;
    public IdWithDisplayValueInfo<String> type;
    public String fromStateName;
    public String toStateName;
    public Instant transitionTime;
    public Instant scheduleTime;
    public IdWithDisplayValueInfo<Long> user;
    public IdWithDisplayValueInfo<UsagePointStateChangeRequest.Status> status;
    public String message;
    public List<IdWithNameInfo> microChecks;
    public List<IdWithNameInfo> microActions;
    public UsagePointInfo usagePoint;
    public Set<String> privileges;

    public UsagePointStateChangeRequestInfo() {
    }

    public static class UsagePointInfo {
        public long id;
        public String name;
        public long version;

        public UsagePointInfo() {
        }
    }
}
