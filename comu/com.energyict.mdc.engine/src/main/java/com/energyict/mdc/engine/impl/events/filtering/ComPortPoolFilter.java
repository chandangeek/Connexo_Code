/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.events.ComPortPoolRelatedEvent;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.List;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they do not relate
 * to a number of ComPortPools.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (09:47)
 */
public class ComPortPoolFilter implements EventFilterCriterion {

    private List<ComPortPool> comPortPools;

    public ComPortPoolFilter (List<ComPortPool> comPortPools) {
        super();
        this.comPortPools = comPortPools;
    }

    public List<ComPortPool> getComPortPools () {
        return comPortPools;
    }

    public void setComPortPools (List<ComPortPool> comPortPools) {
        this.comPortPools = comPortPools;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event.isComPortPoolRelated()) {
            ComPortPoolRelatedEvent comPortPoolEvent = (ComPortPoolRelatedEvent) event;
            return !this.comPortPools.stream().anyMatch(comPortPool -> comPortPool.getId() == comPortPoolEvent.getComPortPool().getId());
        }
        else {
            return false;
        }
    }

}