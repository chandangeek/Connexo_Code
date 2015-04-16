package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all the {@link ConnectionTask}s of a Device are complete.
 * @see ConnectionTask#getStatus()
 * @see ConnectionTask.ConnectionTaskLifecycleStatus#ACTIVE
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class ConnectionPropertiesAreValid implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public ConnectionPropertiesAreValid(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device) {
        if (anyInCompleteConnectionTask(device).isPresent()) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ConnectionTask<?, ?>> anyInCompleteConnectionTask(Device device) {
        return device
                .getConnectionTasks()
                .stream()
                .filter(each -> each.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE))
                .findAny();
    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
    }

}