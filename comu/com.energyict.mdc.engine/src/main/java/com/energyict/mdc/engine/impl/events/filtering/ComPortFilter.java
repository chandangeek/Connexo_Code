/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComPortRelatedEvent;
import com.energyict.mdc.engine.events.ComServerEvent;

import java.util.List;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they do not relate
 * to a number of {@link ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (08:33)
 */
public class ComPortFilter implements EventFilterCriterion {

    private List<ComPort> comPorts;

    public ComPortFilter (List<ComPort> comPorts) {
        super();
        this.comPorts = comPorts;
    }

    public List<ComPort> getComPorts () {
        return comPorts;
    }

    public void setComPorts (List<ComPort> comPorts) {
        this.comPorts = comPorts;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event.isComPortRelated()) {
            ComPortRelatedEvent comPortEvent = (ComPortRelatedEvent) event;
            return !this.comPorts.stream().anyMatch(comPort -> comPort.getId() == comPortEvent.getComPort().getId());
        }
        else {
            return false;
        }
    }

}