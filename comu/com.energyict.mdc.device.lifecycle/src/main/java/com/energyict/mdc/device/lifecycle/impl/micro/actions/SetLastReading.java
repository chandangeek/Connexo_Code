package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.effectiveTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getEffectiveTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable validation on the Device.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#SET_LAST_READING}
 *
 * action bits: 1
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (09:09)
 */
public class SetLastReading implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(effectiveTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        Instant commissioningTimestamp = getEffectiveTimestamp(properties);
        device.getLogBooks().forEach(logBook -> this.setLastReading(device, logBook, commissioningTimestamp));
        device.getLoadProfiles().forEach(loadProfile -> this.setLastReading(device, loadProfile, commissioningTimestamp));
    }

    private void setLastReading(Device device, LogBook logBook, Instant commissioningTimestamp) {
        device.getLogBookUpdaterFor(logBook).setLastLogBookIfLater(commissioningTimestamp).update();
    }

    private void setLastReading(Device device, LoadProfile loadProfile, Instant commissioningTimestamp) {
        device.getLoadProfileUpdaterFor(loadProfile).setLastReadingIfLater(commissioningTimestamp).update();
    }

}