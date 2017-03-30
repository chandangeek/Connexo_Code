/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.whiteboard.ReferenceInfo;
import com.elster.jupiter.rest.whiteboard.SpecificReferenceResolver;
import com.energyict.mdc.device.data.Device;

import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(name = "com.elster.jupiter.metering.rest.DeviceReferenceResolver", immediate = true, service = SpecificReferenceResolver.class)
public class DeviceReferenceResolver implements SpecificReferenceResolver {
    @Override
    public Optional<ReferenceInfo> resolve(Object object) {
        if (object instanceof Device) {
            return Optional.of(new ReferenceInfo("com.energyict.mdc.device.Device", ((Device) object).getName()));
        }
        return Optional.empty();
    }
}
