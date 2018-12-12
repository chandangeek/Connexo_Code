/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will cancel all running service calls on a device.
 *
 * @see {@link MicroAction#CANCEL_ALL_SERVICE_CALLS}
 */
public class CancelAllServiceCalls extends TranslatableServerMicroAction {

    private final ServiceCallService serviceCallService;

    public CancelAllServiceCalls(Thesaurus thesaurus, ServiceCallService serviceCallService) {
        super(thesaurus);
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        serviceCallService.cancelServiceCallsFor(device);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CANCEL_ALL_SERVICE_CALLS;
    }
}