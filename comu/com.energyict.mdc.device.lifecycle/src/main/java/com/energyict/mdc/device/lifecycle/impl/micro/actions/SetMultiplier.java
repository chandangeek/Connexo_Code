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

/**
 * Provides an implementation of a {@link com.energyict.mdc.device.lifecycle.impl.ServerMicroAction}
 * which will set a multiplier value on the device
 *
 * Copyrights EnergyICT
 * Date: 09.12.15
 * Time: 13:36
 */
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
        if(multiplierValue != null && multiplierValue.compareTo(BigDecimal.ONE) == 1){
            device.setMultiplier(multiplierValue, effectiveTimestamp);
            device.save();
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.SET_MULTIPLIER;
    }
}
