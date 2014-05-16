package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a number of ComPortPools.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class ComPortPoolRequest extends IdBusinessObjectRequest {

    private final EngineModelService engineModelService;

    private List<ComPortPool> comPortPools;

    public ComPortPoolRequest(EngineModelService engineModelService, long comPortPoolId) {
        this(engineModelService, Collections.singleton(comPortPoolId));
    }

    public ComPortPoolRequest (EngineModelService engineModelService, Set<Long> comPortPoolIds) {
        super(comPortPoolIds);
        this.validateComPortPoolIds();
        this.engineModelService = engineModelService;
    }

    private void validateComPortPoolIds () {
        this.comPortPools = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comPortPoolId : this.getBusinessObjectIds()) {
            this.comPortPools.add(this.findComPortPool(comPortPoolId));
        }
    }

    private ComPortPool findComPortPool (long comPortPoolId) {
        ComPortPool comPortPool = engineModelService.findComPortPool(comPortPoolId);
        if (comPortPool == null) {
            throw new NotFoundException("ComPortPool with id " + comPortPoolId + " not found");
        }
        else {
            return comPortPool;
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComPortPools(null, this.comPortPools);
    }

}