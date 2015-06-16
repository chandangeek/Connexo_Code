package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Predicates;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that there is a {@link ManuallyScheduledComTaskExecution} on the device.
 *
 * check bits: 2
 *
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:28)
 */
public class ManuallyScheduledCommunicationTaskAvailable implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ManuallyScheduledCommunicationTaskAvailable(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!anyManuallyScheduledCommunicationTask(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,
                            MicroCheck.AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ComTaskExecution> anyManuallyScheduledCommunicationTask(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .filter(ComTaskExecution::isScheduledManually)
                .filter(Predicates.not(ComTaskExecution::isAdHoc))
                .findAny();
    }

}