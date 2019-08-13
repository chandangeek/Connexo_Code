/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable validation on the Device.
 * @see {@link MicroAction#DISABLE_VALIDATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (08:43)
 */
public class DisableValidation extends TranslatableServerMicroAction {

    public DisableValidation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.forValidation().deactivateValidation();
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.DISABLE_VALIDATION;
    }
}