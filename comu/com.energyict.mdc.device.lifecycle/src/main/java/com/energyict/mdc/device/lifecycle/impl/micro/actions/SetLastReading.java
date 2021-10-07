/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable validation on the Device.
 * @see {@link MicroAction#SET_LAST_READING}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (09:09)
 */
public class SetLastReading extends TranslatableServerMicroAction {

    public SetLastReading(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getLogBooks().forEach(logBook -> this.setLastReading(device, logBook, effectiveTimestamp));
        device.getLoadProfiles().forEach(loadProfile -> this.setLastReading(device, loadProfile, effectiveTimestamp));
        device.getLoadProfiles().forEach(loadProfile -> this.setLastConsecutiveReading(device, loadProfile, effectiveTimestamp));
    }

    private void setLastReading(Device device, LogBook logBook, Instant commissioningTimestamp) {
        device.getLogBookUpdaterFor(logBook).setLastReadingIfLater(commissioningTimestamp).update();
    }

    private void setLastReading(Device device, LoadProfile loadProfile, Instant commissioningTimestamp) {
        device.getLoadProfileUpdaterFor(loadProfile).setLastReadingIfLater(commissioningTimestamp).update();
    }

    private void setLastConsecutiveReading(Device device, LoadProfile loadProfile, Instant commissioningTimestamp) {
        device.getLoadProfileUpdaterFor(loadProfile).setLastConsecutiveReadingIfLater(commissioningTimestamp).update();
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.SET_LAST_READING;
    }
}