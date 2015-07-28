package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.*;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will enable estimation on the Device.
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#ENABLE_ESTIMATION}
 */
public class EnableEstimation extends TranslatableServerMicroAction {

    public EnableEstimation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.forEstimation().activateEstimation();
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.ENABLE_ESTIMATION;
    }
}