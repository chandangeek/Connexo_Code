package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.comserver.collections.Collections;
import com.energyict.comserver.eventsimpl.EventPublisher;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.engine.model.ComPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a number of {@link ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class ComPortRequest extends IdBusinessObjectRequest {

    private List<ComPort> comPorts;

    public ComPortRequest (int comPortId) {
        this(Collections.toSet(comPortId));
    }

    public ComPortRequest (Set<Integer> comPortIds) {
        super(comPortIds);
        this.validateComPortIds();
    }

    private void validateComPortIds () {
        this.comPorts = new ArrayList<ComPort>(this.getBusinessObjectIds().size());
        for (Integer comPortId : this.getBusinessObjectIds()) {
            this.comPorts.add(this.findComPort(comPortId));
        }
    }

    private ComPort findComPort (int comPortId) {
        ComPort comPort = ManagerFactory.getCurrent().getComPortFactory().find(comPortId);
        if (comPort == null) {
            throw new NotFoundException("ComPort with id " + comPortId + " not found");
        }
        else {
            return comPort;
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComPorts(null, this.comPorts);
    }

}