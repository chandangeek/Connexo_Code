/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.events.EventPublisher;

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
class ComPortPoolRequest extends IdBusinessObjectRequest {

    private final EngineConfigurationService engineConfigurationService;

    private List<ComPortPool> comPortPools;

    ComPortPoolRequest(EngineConfigurationService engineConfigurationService, long comPortPoolId) {
        this(engineConfigurationService, Collections.singleton(comPortPoolId));
    }

    ComPortPoolRequest(EngineConfigurationService engineConfigurationService, Set<Long> comPortPoolIds) {
        super(comPortPoolIds);
        this.engineConfigurationService = engineConfigurationService;
        this.validateComPortPoolIds();
    }

    private void validateComPortPoolIds () {
        this.comPortPools = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comPortPoolId : this.getBusinessObjectIds()) {
            this.comPortPools.add(this.findComPortPool(comPortPoolId));
        }
    }

    private ComPortPool findComPortPool (long comPortPoolId) {
        return engineConfigurationService
                .findComPortPool(comPortPoolId)
                .orElseThrow(() -> new NotFoundException("ComPortPool with id " + comPortPoolId + " not found"));
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComPortPools(null, this.comPortPools);
    }

}