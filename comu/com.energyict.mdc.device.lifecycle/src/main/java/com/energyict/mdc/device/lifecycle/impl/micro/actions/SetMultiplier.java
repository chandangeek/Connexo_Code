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
import com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class SetMultiplier extends TranslatableServerMicroAction {

    public SetMultiplier(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.singletonList(DeviceLifeCyclePropertySupport.multiplierPropertySpec(propertySpecService, this.getThesaurus()));
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        BigDecimal multiplierValue = DeviceLifeCyclePropertySupport.getMultiplierValue(properties);
        device.setMultiplier(multiplierValue, effectiveTimestamp);
        device.save();
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.SET_MULTIPLIER;
    }
}
