package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.comserver.collections.Collections;
import com.energyict.comserver.eventsimpl.EventPublisher;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link ComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class ComTaskExecutionRequest extends IdBusinessObjectRequest {

    private List<ComTaskExecution> comTaskExecutions;

    public ComTaskExecutionRequest (int comTaskExecutionId) {
        this(Collections.toSet(comTaskExecutionId));
    }

    public ComTaskExecutionRequest (Set<Integer> comTaskExecutionIds) {
        super(comTaskExecutionIds);
        this.validateComTaskExecutionIds();
    }

    private void validateComTaskExecutionIds () {
        this.comTaskExecutions = new ArrayList<ComTaskExecution>(this.getBusinessObjectIds().size());
        for (Integer comTaskExecutionId : this.getBusinessObjectIds()) {
            this.comTaskExecutions.add(this.findComTaskExecution(comTaskExecutionId));
        }
    }

    private ComTaskExecution findComTaskExecution (int comTaskExecutionId) {
        ComTaskExecution comTaskExecution = ManagerFactory.getCurrent().getComTaskExecutionFactory().find(comTaskExecutionId);
        if (comTaskExecution == null) {
            throw new NotFoundException("ComTaskExecution with id " + comTaskExecutionId + " not found");
        }
        else {
            return comTaskExecution;
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComTaskExecutions(null, this.comTaskExecutions);
    }

}