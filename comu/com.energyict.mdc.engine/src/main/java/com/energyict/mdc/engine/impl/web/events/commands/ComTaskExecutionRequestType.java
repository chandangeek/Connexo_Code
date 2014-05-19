package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.DeviceDataService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:58)
 */
public class ComTaskExecutionRequestType extends IdBusinessObjectRequestType {

    private final DeviceDataService deviceDataService;

    public ComTaskExecutionRequestType(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

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
        return new ComTaskExecutionRequest(deviceDataService, ids);
    }

}