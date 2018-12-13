/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will remove the location on the Device.
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#REMOVE_LOCATION}
 */
public class RemoveLocation extends TranslatableServerMicroAction {
    public RemoveLocation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.REMOVE_LOCATION;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.setLocation(null);
        device.setSpatialCoordinates(null);
        device.save();
    }
}
