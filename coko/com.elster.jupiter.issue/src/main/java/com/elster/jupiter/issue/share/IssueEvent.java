/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;

import java.util.Optional;

@ConsumerType
public interface IssueEvent {

    String getEventType();

    Optional<EndDevice> getEndDevice();

    //TODO - split IssueEvent in DeviceEvent and UsagePointEvent
    default Optional<UsagePoint> getUsagePoint() {
        return Optional.empty();
    }

    Optional<? extends OpenIssue> findExistingIssue();

    void apply(Issue issue);

    default boolean checkOccurrenceConditions(final String relativePeriodWithCount, final String triggeringEndDeviceEventTypes) {
        return true;
    }

    default boolean checkOccurrenceConditions(final String relativePeriodWithCount) {
        return true;
    }

}