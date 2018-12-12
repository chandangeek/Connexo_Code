/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

class ScheduledUsagePointStateChangeHandler implements TaskExecutor {

    private final ServerUsagePointLifeCycleService lifeCycleService;

    @Inject
    public ScheduledUsagePointStateChangeHandler(ServerUsagePointLifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        List<UsagePointStateChangeRequest> changeRequests = this.lifeCycleService.getDataModel().query(UsagePointStateChangeRequest.class, UsagePointStateChangePropertyImpl.class)
                .select(where(UsagePointStateChangeRequestImpl.Fields.STATUS.fieldName()).isEqualTo(UsagePointStateChangeRequest.Status.SCHEDULED)
                                .and(where(UsagePointStateChangeRequestImpl.Fields.TRANSITION_TIME.fieldName()).isLessThanOrEqual(taskOccurrence.getStartDate().get())),
                        new Order[]{Order.ascending(UsagePointStateChangeRequestImpl.Fields.TRANSITION_TIME.fieldName())}, true, new String[0]);
        for (UsagePointStateChangeRequest changeRequest : changeRequests) {
            try {
                ((UsagePointStateChangeRequestImpl) changeRequest).execute();
            } catch (Exception ex) {
                // TODO log
            }
        }
        this.lifeCycleService.rescheduleExecutor();
    }
}
