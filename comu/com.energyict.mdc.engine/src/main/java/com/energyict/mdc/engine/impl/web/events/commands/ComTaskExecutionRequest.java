/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.engine.impl.events.EventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
class ComTaskExecutionRequest extends IdBusinessObjectRequest {

    private final CommunicationTaskService communicationTaskService;

    private List<ComTaskExecution> comTaskExecutions;

    ComTaskExecutionRequest(CommunicationTaskService communicationTaskService, long comTaskExecutionId) {
        this(communicationTaskService, singleton(comTaskExecutionId));
    }

    ComTaskExecutionRequest(CommunicationTaskService communicationTaskService, Set<Long> comTaskExecutionIds) {
        super(comTaskExecutionIds);
        this.communicationTaskService = communicationTaskService;
        this.validateComTaskExecutionIds();
    }

    private void validateComTaskExecutionIds() {
        this.comTaskExecutions = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comTaskExecutionId : this.getBusinessObjectIds()) {
            this.comTaskExecutions.add(this.findComTaskExecution(comTaskExecutionId));
        }
    }

    private ComTaskExecution findComTaskExecution(long comTaskExecutionId) {
        return communicationTaskService
                .findComTaskExecution(comTaskExecutionId)
                .orElseThrow(() -> new NotFoundException("ComTaskExecution with id " + comTaskExecutionId + " not found"));
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComTaskExecutions(null, this.comTaskExecutions);
    }

}