package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.model.EngineModelService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.model.ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
public class ComPortRequestType extends IdBusinessObjectRequestType {

    private final EngineModelService engineModelService;

    public ComPortRequestType(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Override
    protected String getBusinessObjectTypeName () {
        return "comport";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllComPortsRequest();
    }

    @Override
    protected Request newRequestFor (Set<Long> ids) {
        return new ComPortRequest(engineModelService, ids);
    }

}