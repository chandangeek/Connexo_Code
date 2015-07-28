package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that there is a {@link ScheduledComTaskExecution} on the device.
 *
 * check bits: 4
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (09:00)
 */
public class SharedScheduledCommunicationTaskAvailable extends TranslatableServerMicroCheck {

    public SharedScheduledCommunicationTaskAvailable(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!anyScheduledCommunicationTask(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,
                            MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ComTaskExecution> anyScheduledCommunicationTask(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .filter(ComTaskExecution::usesSharedSchedule)
                .findAny();
    }

}