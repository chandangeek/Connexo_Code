package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.comserver.collections.Collections;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.engine.model.ComPortPool;

import java.util.ArrayList;
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

    private List<ComPortPool> comPortPools;

    public ComPortPoolRequest (long comPortPoolId) {
        this(Collections.toSet(comPortPoolId));
    }

    public ComPortPoolRequest (Set<Long> comPortPoolIds) {
        super(comPortPoolIds);
        this.validateComPortPoolIds();
    }

    private void validateComPortPoolIds () {
        this.comPortPools = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comPortPoolId : this.getBusinessObjectIds()) {
            this.comPortPools.add(this.findComPortPool(comPortPoolId));
        }
    }

    private ComPortPool findComPortPool (long comPortPoolId) {
        ComPortPool comPortPool = ManagerFactory.getCurrent().getComPortPoolFactory().find(comPortPoolId);
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