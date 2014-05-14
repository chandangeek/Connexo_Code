package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.model.ComPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

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

    public ComPortRequest (long comPortId) {
        this(singleton(comPortId));
    }

    public ComPortRequest (Set<Long> comPortIds) {
        super(comPortIds);
        this.validateComPortIds();
    }

    private void validateComPortIds () {
        this.comPorts = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long comPortId : this.getBusinessObjectIds()) {
            this.comPorts.add(this.findComPort(comPortId));
        }
    }

    private ComPort findComPort (long comPortId) {
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