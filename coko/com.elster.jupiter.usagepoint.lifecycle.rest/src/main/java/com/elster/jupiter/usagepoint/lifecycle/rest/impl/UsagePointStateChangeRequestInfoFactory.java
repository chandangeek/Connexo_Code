/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.users.User;

import java.util.stream.Collectors;

public class UsagePointStateChangeRequestInfoFactory {
    public UsagePointStateChangeRequestInfo from(UsagePointStateChangeRequest changeRequest, String application) {
        UsagePointStateChangeRequestInfo info = new UsagePointStateChangeRequestInfo();
        info.id = changeRequest.getId();
        info.fromStateName = changeRequest.getFromStateName();
        info.toStateName = changeRequest.getToStateName();
        info.transitionTime = changeRequest.getTransitionTime();
        info.scheduleTime = changeRequest.getScheduleTime();
        User originator = changeRequest.getOriginator();
        if (originator != null) {
            info.user = new IdWithDisplayValueInfo<>(originator.getId(), originator.getName());
        }
        info.status = new IdWithDisplayValueInfo<>(changeRequest.getStatus(), changeRequest.getStatusName());
        info.type = new IdWithDisplayValueInfo<>(changeRequest.getType().name(), changeRequest.getTypeName());
        info.message = changeRequest.getGeneralFailReason();
        info.microChecks = changeRequest.getFailReasons()
                .stream()
                .filter(fail -> UsagePointStateChangeFail.FailSource.CHECK.equals(fail.getFailSource()))
                .map(fail -> new IdWithNameInfo(fail.getName(), fail.getMessage()))
                .collect(Collectors.toList());
        info.microActions = changeRequest.getFailReasons()
                .stream()
                .filter(fail -> UsagePointStateChangeFail.FailSource.ACTION.equals(fail.getFailSource()))
                .map(fail -> new IdWithNameInfo(fail.getName(), fail.getMessage()))
                .collect(Collectors.toList());
        info.userCanManageRequest = changeRequest.userCanManageRequest(application);
        UsagePoint usagePoint = changeRequest.getUsagePoint();
        if (usagePoint != null) {
            info.usagePoint = new UsagePointStateChangeRequestInfo.UsagePointInfo();
            info.usagePoint.name = usagePoint.getName();
            info.usagePoint.version = usagePoint.getVersion();
        }
        return info;
    }
}
