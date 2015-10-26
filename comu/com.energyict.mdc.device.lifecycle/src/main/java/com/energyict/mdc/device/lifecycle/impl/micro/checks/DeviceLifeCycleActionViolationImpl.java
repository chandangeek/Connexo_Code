package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import com.elster.jupiter.nls.Thesaurus;

/**
 * Provides an implementation for the {@link DeviceLifeCycleActionViolation} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-14 (15:35)
 */
public class DeviceLifeCycleActionViolationImpl implements DeviceLifeCycleActionViolation {

    private final Thesaurus thesaurus;
    private final MessageSeeds messageSeed;
    private final MicroCheck microCheck;

    public DeviceLifeCycleActionViolationImpl(Thesaurus thesaurus, MessageSeeds messageSeed, MicroCheck microCheck) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.microCheck = microCheck;
    }

    @Override
    public MicroCheck getCheck() {
        return this.microCheck;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format();
    }

}