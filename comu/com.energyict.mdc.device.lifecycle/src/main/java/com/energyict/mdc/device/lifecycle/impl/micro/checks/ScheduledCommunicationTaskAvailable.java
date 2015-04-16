package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that there is a scheduled communication task on the device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:28)
 */
public class ScheduledCommunicationTaskAvailable implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ScheduledCommunicationTaskAvailable(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device) {
        if (!anyScheduledCommunicationTask(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED,
                            MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ComTaskExecution> anyScheduledCommunicationTask(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .filter(each -> !each.isAdHoc())
                .findAny();
    }

}