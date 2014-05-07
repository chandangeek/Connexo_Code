package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.model.ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
public class ComPortRequestType extends IdBusinessObjectRequestType {

    @Override
    protected String getBusinessObjectTypeName () {
        return "comport";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllComPortsRequest();
    }

    @Override
    protected Request newRequestFor (Set<Integer> ids) {
        return new ComPortRequest(ids);
    }

}