package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.config.EngineConfigurationService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.config.ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
public class ComPortRequestType extends IdBusinessObjectRequestType {

    private final EngineConfigurationService engineConfigurationService;

    public ComPortRequestType(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
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
        return new ComPortRequest(engineConfigurationService, ids);
    }

}