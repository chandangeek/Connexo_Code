/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Clock;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DefaultUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {

    public DefaultUsagePointDetailsInfo() {
    }

    public DefaultUsagePointDetailsInfo(UsagePointDetail detail) {
        super(detail);
    }

    @Override
    public ServiceKind getKind() {
        return ServiceKind.OTHER;
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newDefaultDetailBuilder(clock.instant());
    }
}
