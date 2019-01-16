/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import java.time.Instant;
import java.util.Optional;

/**
 * Check if at least one connection is available on the device with the status: "Active".
 */
public class ActiveConnectionAvailable extends TranslatableServerMicroCheck {

    public ActiveConnectionAvailable(Thesaurus thesaurus){
       super(thesaurus);
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!anyActiveConnectionTask(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,
                            MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ConnectionTask<?, ?>> anyActiveConnectionTask(Device device) {
        return device
                .getConnectionTasks()
                .stream()
                .filter(each -> each.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE))
                .findAny();
    }
}