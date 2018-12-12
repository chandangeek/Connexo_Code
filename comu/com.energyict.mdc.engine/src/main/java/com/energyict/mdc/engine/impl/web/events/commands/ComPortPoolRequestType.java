/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.config.EngineConfigurationService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.config.ComPortPool}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
class ComPortPoolRequestType extends IdBusinessObjectRequestType {

    private final EngineConfigurationService engineConfigurationService;

    ComPortPoolRequestType(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    protected String getBusinessObjectTypeName () {
        return "comportpool";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllComPortPoolsRequest();
    }

    @Override
    protected Request newRequestFor (Set<Long> ids) {
        return new ComPortPoolRequest(engineConfigurationService, ids);
    }

}