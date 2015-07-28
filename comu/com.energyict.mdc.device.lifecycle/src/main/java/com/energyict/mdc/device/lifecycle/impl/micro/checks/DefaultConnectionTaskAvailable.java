package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.i18n.MicroCheckTranslationKey;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that there is a default connection task on the device.
 *
 *
 * check bits: 1
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-14 (15:31)
 */
public class DefaultConnectionTaskAvailable extends TranslatableServerMicroCheck {

    public DefaultConnectionTaskAvailable(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.DEFAULT_CONNECTION_AVAILABLE;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!anyDefaultConnectionTask(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.DEFAULT_CONNECTION_AVAILABLE,
                            MicroCheck.DEFAULT_CONNECTION_AVAILABLE));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<ConnectionTask<?, ?>> anyDefaultConnectionTask(Device device) {
        return device
                .getConnectionTasks()
                .stream()
                .filter(each -> each.isDefault())
                .findAny();
    }

}