package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:58)
 */
public class ComTaskExecutionRequestType extends IdBusinessObjectRequestType {

    @Override
    protected String getBusinessObjectTypeName () {
        return "comtaskexecution";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllComTaskExecutionsRequest();
    }

    @Override
    protected Request newRequestFor (Set<Long> ids) {
        return new ComTaskExecutionRequest(ids);
    }

}