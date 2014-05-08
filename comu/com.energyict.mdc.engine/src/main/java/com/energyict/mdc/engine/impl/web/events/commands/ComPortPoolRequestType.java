package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.model.ComPortPool}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
public class ComPortPoolRequestType extends IdBusinessObjectRequestType {

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
        return new ComPortPoolRequest(ids);
    }

}