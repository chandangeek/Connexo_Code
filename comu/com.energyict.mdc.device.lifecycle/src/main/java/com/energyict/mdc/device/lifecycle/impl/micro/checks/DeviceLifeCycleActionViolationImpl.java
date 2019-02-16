/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

public class DeviceLifeCycleActionViolationImpl implements DeviceLifeCycleActionViolation {

    private final Thesaurus thesaurus;
    private final MessageSeeds messageSeed;
    private final ServerMicroCheck microCheck;

    public DeviceLifeCycleActionViolationImpl(Thesaurus thesaurus, MessageSeeds messageSeed,
                                              ServerMicroCheck microCheck) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.microCheck = microCheck;
    }

    @Override
    public ServerMicroCheck getCheck() {
        return this.microCheck;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format();
    }
}