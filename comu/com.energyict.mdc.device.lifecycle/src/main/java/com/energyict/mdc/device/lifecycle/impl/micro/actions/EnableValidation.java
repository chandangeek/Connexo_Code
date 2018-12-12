/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getLastCheckedTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.lastCheckedTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will enable validation on the Device.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#ENABLE_VALIDATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-20 (09:29)
 */


public class EnableValidation extends TranslatableServerMicroAction {

    public EnableValidation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.singletonList(lastCheckedTimestamp(propertySpecService, this.getThesaurus()));
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        Instant lastCheckedTimestamp = getLastCheckedTimestamp(properties);
        lastCheckedTimestamp = lastCheckedTimestamp == null ? effectiveTimestamp : lastCheckedTimestamp;
        if(!device.getDeviceConfiguration().getValidateOnStore()) {
            device.forValidation().activateValidation(lastCheckedTimestamp);
        } else {
            device.forValidation().activateValidationOnStorage(lastCheckedTimestamp);
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.ENABLE_VALIDATION;
    }
}