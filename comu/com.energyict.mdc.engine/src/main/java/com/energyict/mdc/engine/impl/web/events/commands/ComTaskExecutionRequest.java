package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

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
public class ComTaskExecutionRequest extends IdBusinessObjectRequest {

    private final DeviceDataService deviceDataService;

    private List<ComTaskExecution> comTaskExecutions;

    public ComTaskExecutionRequest(DeviceDataService deviceDataService, long comTaskExecutionId) {
        this(deviceDataService, singleton(comTaskExecutionId));
    }

    public ComTaskExecutionRequest(DeviceDataService deviceDataService, Set<Long> comTaskExecutionIds) {
        super(comTaskExecutionIds);
        this.deviceDataService = deviceDataService;
        this.validateComTaskExecutionIds();
    }

    private void validateComTaskExecutionIds () {
        this.comTaskExecutions = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comTaskExecutionId : this.getBusinessObjectIds()) {
            this.comTaskExecutions.add(this.findComTaskExecution(comTaskExecutionId));
        }
    }

    private ComTaskExecution findComTaskExecution (long comTaskExecutionId) {
        ComTaskExecution comTaskExecution = deviceDataService.findComTaskExecution(comTaskExecutionId);
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